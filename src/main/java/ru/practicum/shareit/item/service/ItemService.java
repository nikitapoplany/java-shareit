package ru.practicum.shareit.item.service;

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
}