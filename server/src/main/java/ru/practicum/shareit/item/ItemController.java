package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Контроллер для работы с вещами.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    /**
     * Создает новую вещь.
     *
     * @param userId  идентификатор пользователя-владельца
     * @param itemDto данные вещи
     * @return созданная вещь
     */
    @PostMapping
    public ItemDto createItem(@RequestHeader(USER_ID_HEADER) Long userId, @RequestBody ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        Item createdItem = itemService.createItem(userId, item);
        return ItemMapper.toItemDto(createdItem);
    }

    /**
     * Обновляет данные вещи.
     *
     * @param userId  идентификатор пользователя-владельца
     * @param itemId  идентификатор вещи
     * @param itemDto данные для обновления
     * @return обновленная вещь
     */
    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(USER_ID_HEADER) Long userId,
                              @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        Item updatedItem = itemService.updateItem(userId, itemId, item);
        return ItemMapper.toItemDto(updatedItem);
    }

    /**
     * Получает вещь по идентификатору.
     *
     * @param userId идентификатор пользователя, запрашивающего информацию
     * @param itemId идентификатор вещи
     * @return вещь с информацией о бронированиях и комментариях
     */
    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader(USER_ID_HEADER) Long userId,
                               @PathVariable Long itemId) {
        return itemService.getItemWithBookingsAndComments(itemId, userId);
    }

    /**
     * Получает список всех вещей пользователя.
     *
     * @param userId идентификатор пользователя-владельца
     * @return список вещей
     */
    @GetMapping
    public List<ItemDto> getUserItems(@RequestHeader(USER_ID_HEADER) Long userId) {
        List<Item> items = itemService.getUserItems(userId);
        return items.stream()
                .map(item -> itemService.getItemWithBookingsAndComments(item.getId(), userId))
                .collect(Collectors.toList());
    }

    /**
     * Ищет вещи по тексту в названии или описании.
     *
     * @param text текст для поиска
     * @return список найденных вещей
     */
    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        List<Item> items = itemService.searchItems(text);
        return ItemMapper.toItemDtoList(items);
    }

    /**
     * Создает комментарий к вещи.
     *
     * @param userId     идентификатор пользователя, оставляющего комментарий
     * @param itemId     идентификатор вещи
     * @param commentDto данные комментария
     * @return созданный комментарий
     */
    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader(USER_ID_HEADER) Long userId,
                                    @PathVariable Long itemId,
                                    @RequestBody CommentDto commentDto) {
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        Comment createdComment = itemService.addComment(userId, itemId, comment);
        return CommentMapper.toCommentDto(createdComment);
    }
}