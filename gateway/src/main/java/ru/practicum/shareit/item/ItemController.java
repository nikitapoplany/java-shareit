package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import jakarta.validation.Valid;

/**
 * Контроллер для работы с вещами.
 */
@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    /**
     * Создает новую вещь.
     *
     * @param userId  идентификатор пользователя-владельца
     * @param itemDto данные вещи
     * @return созданная вещь
     */
    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(USER_ID_HEADER) Long userId, @RequestBody @Valid ItemDto itemDto) {
        log.info("Creating item {}, userId={}", itemDto, userId);
        return itemClient.createItem(userId, itemDto);
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
    public ResponseEntity<Object> updateItem(@RequestHeader(USER_ID_HEADER) Long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("Updating item {}, userId={}", itemId, userId);
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    /**
     * Получает вещь по идентификатору.
     *
     * @param userId идентификатор пользователя, запрашивающего информацию
     * @param itemId идентификатор вещи
     * @return вещь с информацией о бронированиях и комментариях
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader(USER_ID_HEADER) Long userId,
                                          @PathVariable Long itemId) {
        log.info("Get item {}, userId={}", itemId, userId);
        return itemClient.getItemById(userId, itemId);
    }

    /**
     * Получает список всех вещей пользователя.
     *
     * @param userId идентификатор пользователя-владельца
     * @return список вещей
     */
    @GetMapping
    public ResponseEntity<Object> getUserItems(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Get items, userId={}", userId);
        return itemClient.getUserItems(userId);
    }

    /**
     * Ищет вещи по тексту в названии или описании.
     *
     * @param text текст для поиска
     * @return список найденных вещей
     */
    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text) {
        log.info("Search items with text={}", text);
        return itemClient.searchItems(text);
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
    public ResponseEntity<Object> createComment(@RequestHeader(USER_ID_HEADER) Long userId,
                                              @PathVariable Long itemId,
                                              @RequestBody @Valid CommentDto commentDto) {
        log.info("Creating comment for item {}, userId={}", itemId, userId);
        return itemClient.createComment(userId, itemId, commentDto);
    }
}