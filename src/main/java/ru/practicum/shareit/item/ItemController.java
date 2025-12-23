package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * Контроллер для работы с вещами.
 */
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /**
     * Создает новую вещь.
     *
     * @param userId  идентификатор пользователя-владельца
     * @param itemDto данные вещи
     * @return созданная вещь
     */
    @PostMapping
    public ResponseEntity<ItemDto> createItem(@RequestHeader(USER_ID_HEADER) Long userId, @RequestBody ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        Item createdItem = itemService.createItem(userId, item);
        return ResponseEntity.ok(ItemMapper.toItemDto(createdItem));
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
    public ResponseEntity<ItemDto> updateItem(@RequestHeader(USER_ID_HEADER) Long userId,
                              @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        Item updatedItem = itemService.updateItem(userId, itemId, item);
        return ResponseEntity.ok(ItemMapper.toItemDto(updatedItem));
    }

    /**
     * Получает вещь по идентификатору.
     *
     * @param userId идентификатор пользователя, запрашивающего информацию
     * @param itemId идентификатор вещи
     * @return вещь с информацией о бронированиях и комментариях
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItemById(@RequestHeader(USER_ID_HEADER) Long userId, 
                                              @PathVariable Long itemId) {
        return ResponseEntity.ok(itemService.getItemWithBookingsAndComments(itemId, userId));
    }

    /**
     * Получает список всех вещей пользователя.
     *
     * @param userId идентификатор пользователя-владельца
     * @return список вещей
     */
    @GetMapping
    public ResponseEntity<List<ItemDto>> getUserItems(@RequestHeader(USER_ID_HEADER) Long userId) {
        List<Item> items = itemService.getUserItems(userId);
        return ResponseEntity.ok(ItemMapper.toItemDtoList(items));
    }

    /**
     * Ищет вещи по тексту в названии или описании.
     *
     * @param text текст для поиска
     * @return список найденных вещей
     */
    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(@RequestParam String text) {
        List<Item> items = itemService.searchItems(text);
        return ResponseEntity.ok(ItemMapper.toItemDtoList(items));
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
    public ResponseEntity<CommentDto> createComment(@RequestHeader(USER_ID_HEADER) Long userId,
                                                  @PathVariable Long itemId,
                                                  @RequestBody CommentDto commentDto) {
        return ResponseEntity.ok(itemService.createComment(userId, itemId, commentDto));
    }
}
