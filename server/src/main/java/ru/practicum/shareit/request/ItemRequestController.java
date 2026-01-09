package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

/**
 * Контроллер для работы с запросами вещей.
 */
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    /**
     * Создает новый запрос вещи.
     *
     * @param userId      идентификатор пользователя, создающего запрос
     * @param requestDto  данные запроса
     * @return созданный запрос
     */
    @PostMapping
    public ItemRequestDto createRequest(@RequestHeader(USER_ID_HEADER) Long userId,
                                       @RequestBody ItemRequestDto requestDto) {
        // Преобразуем DTO в сущность без указания пользователя
        // Пользователь будет установлен в сервисе на основе userId
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(requestDto.getDescription());

        // Создаем запрос
        ItemRequest createdRequest = itemRequestService.createItemRequest(userId, itemRequest);
        return ItemRequestMapper.toItemRequestDto(createdRequest);
    }

    /**
     * Получает список запросов пользователя.
     *
     * @param userId  идентификатор пользователя
     * @return список запросов с информацией о вещах
     */
    @GetMapping
    public List<ItemRequestDto> getUserRequests(@RequestHeader(USER_ID_HEADER) Long userId) {
        return itemRequestService.getUserItemRequests(userId);
    }

    /**
     * Получает список запросов других пользователей.
     *
     * @param userId  идентификатор пользователя
     * @param from    индекс первого элемента
     * @param size    размер страницы
     * @return список запросов других пользователей с информацией о вещах
     */
    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader(USER_ID_HEADER) Long userId,
                                              @RequestParam(defaultValue = "0") Integer from,
                                              @RequestParam(defaultValue = "10") Integer size) {
        return itemRequestService.getAllItemRequests(userId, from, size);
    }

    /**
     * Получает данные о запросе по его ID.
     *
     * @param userId     идентификатор пользователя
     * @param requestId  идентификатор запроса
     * @return данные запроса с информацией о вещах
     */
    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader(USER_ID_HEADER) Long userId,
                                        @PathVariable Long requestId) {
        return itemRequestService.getItemRequestById(userId, requestId);
    }
}