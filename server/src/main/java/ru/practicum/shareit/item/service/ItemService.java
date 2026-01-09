package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

/**
 * Интерфейс сервиса для работы с вещами.
 */
public interface ItemService {
    /**
     * Создает новую вещь.
     *
     * @param userId ID пользователя, владельца вещи
     * @param item   данные вещи
     * @return созданная вещь
     */
    Item createItem(Long userId, Item item);

    /**
     * Обновляет данные вещи.
     *
     * @param userId ID пользователя, владельца вещи
     * @param itemId ID вещи
     * @param item   данные для обновления
     * @return обновленная вещь
     */
    Item updateItem(Long userId, Long itemId, Item item);

    /**
     * Получает вещь по ID.
     *
     * @param userId ID пользователя, запрашивающего данные
     * @param itemId ID вещи
     * @return вещь
     */
    Item getItemById(Long userId, Long itemId);

    /**
     * Получает список вещей пользователя.
     *
     * @param userId ID пользователя
     * @return список вещей
     */
    List<Item> getUserItems(Long userId);

    /**
     * Ищет вещи по тексту в названии или описании.
     *
     * @param text текст для поиска
     * @return список вещей, содержащих текст в названии или описании
     */
    List<Item> searchItems(String text);

    /**
     * Добавляет комментарий к вещи.
     *
     * @param userId    ID пользователя, оставляющего комментарий
     * @param itemId    ID вещи
     * @param comment   комментарий
     * @return добавленный комментарий
     */
    Comment addComment(Long userId, Long itemId, Comment comment);

    /**
     * Получает комментарии к вещи.
     *
     * @param itemId ID вещи
     * @return список комментариев
     */
    List<CommentDto> getItemComments(Long itemId);

    /**
     * Получает вещь по ID с информацией о бронированиях и комментариях.
     *
     * @param itemId ID вещи
     * @param userId ID пользователя, запрашивающего данные
     * @return DTO вещи с информацией о бронированиях и комментариях
     */
    ItemDto getItemWithBookingsAndComments(Long itemId, Long userId);
}