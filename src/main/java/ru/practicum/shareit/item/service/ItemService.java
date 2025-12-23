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
     * @param userId идентификатор пользователя-владельца
     * @param item   данные вещи
     * @return созданная вещь
     */
    Item createItem(Long userId, Item item);

    /**
     * Обновляет данные вещи.
     *
     * @param userId идентификатор пользователя-владельца
     * @param itemId идентификатор вещи
     * @param item   данные для обновления
     * @return обновленная вещь
     */
    Item updateItem(Long userId, Long itemId, Item item);

    /**
     * Получает вещь по идентификатору.
     *
     * @param itemId идентификатор вещи
     * @return вещь
     */
    Item getItemById(Long itemId);

    /**
     * Получает список всех вещей пользователя.
     *
     * @param userId идентификатор пользователя-владельца
     * @return список вещей
     */
    List<Item> getUserItems(Long userId);

    /**
     * Ищет вещи по тексту в названии или описании.
     *
     * @param text текст для поиска
     * @return список найденных вещей
     */
    List<Item> searchItems(String text);
    
    /**
     * Создает комментарий к вещи.
     *
     * @param userId    идентификатор пользователя, оставляющего комментарий
     * @param itemId    идентификатор вещи
     * @param commentDto данные комментария
     * @return созданный комментарий
     */
    CommentDto createComment(Long userId, Long itemId, CommentDto commentDto);
    
    /**
     * Получает комментарии к вещи.
     *
     * @param itemId идентификатор вещи
     * @return список комментариев
     */
    List<CommentDto> getItemComments(Long itemId);
    
    /**
     * Получает вещь по идентификатору с информацией о бронированиях и комментариях.
     *
     * @param itemId идентификатор вещи
     * @param userId идентификатор пользователя, запрашивающего информацию
     * @return вещь с информацией о бронированиях и комментариях
     */
    ItemDto getItemWithBookingsAndComments(Long itemId, Long userId);
}