package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

/**
 * Интерфейс сервиса для работы с запросами вещей.
 */
public interface ItemRequestService {
    /**
     * Создает новый запрос вещи.
     *
     * @param userId      ID пользователя, создающего запрос
     * @param itemRequest данные запроса
     * @return созданный запрос
     */
    ItemRequest createItemRequest(Long userId, ItemRequest itemRequest);

    /**
     * Получает список запросов пользователя.
     *
     * @param userId ID пользователя
     * @return список запросов с информацией о вещах
     */
    List<ItemRequestDto> getUserItemRequests(Long userId);

    /**
     * Получает список запросов других пользователей.
     *
     * @param userId ID пользователя, запросы которого нужно исключить
     * @param from   индекс первого элемента
     * @param size   размер страницы
     * @return список запросов других пользователей с информацией о вещах
     */
    List<ItemRequestDto> getAllItemRequests(Long userId, Integer from, Integer size);

    /**
     * Получает данные о запросе по его ID.
     *
     * @param userId    ID пользователя, запрашивающего данные
     * @param requestId ID запроса
     * @return данные запроса с информацией о вещах
     */
    ItemRequestDto getItemRequestById(Long userId, Long requestId);
}