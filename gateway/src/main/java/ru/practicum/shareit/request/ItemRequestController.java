package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Контроллер для работы с запросами вещей.
 */
@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    /**
     * Создает новый запрос вещи.
     *
     * @param userId      идентификатор пользователя, создающего запрос
     * @param requestDto  данные запроса
     * @return созданный запрос
     */
    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(USER_ID_HEADER) Long userId,
                                               @RequestBody @Valid ItemRequestDto requestDto) {
        log.info("Creating request {}, userId={}", requestDto, userId);
        return itemRequestClient.createRequest(userId, requestDto);
    }

    /**
     * Получает список запросов пользователя.
     *
     * @param userId  идентификатор пользователя
     * @return список запросов
     */
    @GetMapping
    public ResponseEntity<Object> getUserRequests(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Get requests, userId={}", userId);
        return itemRequestClient.getUserRequests(userId);
    }

    /**
     * Получает список всех запросов других пользователей.
     *
     * @param userId  идентификатор пользователя
     * @param from    индекс первого элемента
     * @param size    размер страницы
     * @return список запросов
     */
    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader(USER_ID_HEADER) Long userId,
                                                @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Get all requests, userId={}, from={}, size={}", userId, from, size);
        return itemRequestClient.getAllRequests(userId, from, size);
    }

    /**
     * Получает запрос по идентификатору.
     *
     * @param userId     идентификатор пользователя
     * @param requestId  идентификатор запроса
     * @return запрос
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader(USER_ID_HEADER) Long userId,
                                               @PathVariable Long requestId) {
        log.info("Get request {}, userId={}", requestId, userId);
        return itemRequestClient.getRequestById(userId, requestId);
    }
}