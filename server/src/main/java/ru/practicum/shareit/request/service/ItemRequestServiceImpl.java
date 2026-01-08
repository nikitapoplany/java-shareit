package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с запросами вещей.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequest createItemRequest(Long userId, ItemRequest itemRequest) {
        log.info("Создание запроса вещи пользователем с ID {}: {}", userId, itemRequest);

        // Проверка существования пользователя
        User requestor = userService.getUserById(userId);
        log.debug("Найден пользователь-запрашивающий: {}", requestor);

        // Валидация описания запроса
        if (itemRequest.getDescription() == null || itemRequest.getDescription().isBlank()) {
            log.warn("Попытка создания запроса с пустым описанием");
            throw new ValidationException("Описание запроса не может быть пустым");
        }

        // Установка пользователя и времени создания
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        log.info("Запрос вещи успешно создан: {}", savedRequest);
        return savedRequest;
    }

    @Override
    public List<ItemRequestDto> getUserItemRequests(Long userId) {
        log.info("Получение списка запросов пользователя с ID: {}", userId);

        // Проверка существования пользователя
        User requestor = userService.getUserById(userId);
        log.debug("Найден пользователь: {}", requestor);

        // Получение запросов пользователя
        List<ItemRequest> requests = itemRequestRepository.findByRequestorOrderByCreatedDesc(requestor);
        log.debug("Найдено {} запросов пользователя с ID {}", requests.size(), userId);

        // Если запросов нет, возвращаем пустой список
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        // Получение вещей для запросов
        List<Item> items = itemRepository.findByRequestInOrderById(requests);
        Map<Long, List<ItemDto>> itemsByRequestId = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(ItemMapper::toItemDto, Collectors.toList())
                ));

        // Формирование DTO с информацией о вещах
        List<ItemRequestDto> result = requests.stream()
                .map(request -> {
                    List<ItemDto> requestItems = itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList());
                    return ItemRequestMapper.toItemRequestDto(request, requestItems);
                })
                .collect(Collectors.toList());

        log.debug("Сформирован список запросов с информацией о вещах");
        return result;
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId, Integer from, Integer size) {
        log.info("Получение списка запросов других пользователей для пользователя с ID {}, from={}, size={}", userId, from, size);

        // Проверка существования пользователя
        User user = userService.getUserById(userId);
        log.debug("Найден пользователь: {}", user);

        // Валидация параметров пагинации
        if (from < 0 || size <= 0) {
            log.warn("Некорректные параметры пагинации: from={}, size={}", from, size);
            throw new ValidationException("Параметры пагинации должны быть положительными числами");
        }

        // Получение запросов других пользователей с пагинацией
        // Вычисляем номер страницы как целочисленное деление from на size
        int page = from / size;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "created"));
        log.debug("Запрос страницы {} размером {} для запросов других пользователей", page, size);
        List<ItemRequest> requests = itemRequestRepository.findByRequestorNot(user, pageRequest).getContent();
        log.debug("Найдено {} запросов других пользователей", requests.size());

        // Если запросов нет, возвращаем пустой список
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        // Получение вещей для запросов
        List<Item> items = itemRepository.findByRequestInOrderById(requests);
        Map<Long, List<ItemDto>> itemsByRequestId = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(ItemMapper::toItemDto, Collectors.toList())
                ));

        // Формирование DTO с информацией о вещах
        List<ItemRequestDto> result = requests.stream()
                .map(request -> {
                    List<ItemDto> requestItems = itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList());
                    return ItemRequestMapper.toItemRequestDto(request, requestItems);
                })
                .collect(Collectors.toList());

        log.debug("Сформирован список запросов других пользователей с информацией о вещах");
        return result;
    }

    @Override
    public ItemRequestDto getItemRequestById(Long userId, Long requestId) {
        log.info("Получение запроса с ID {} пользователем с ID {}", requestId, userId);

        // Проверка существования пользователя
        userService.getUserById(userId);

        // Получение запроса по ID
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Запрос с ID {} не найден", requestId);
                    return new NotFoundException("Запрос с ID " + requestId + " не найден");
                });
        log.debug("Найден запрос: {}", request);

        // Получение вещей для запроса
        List<Item> items = itemRepository.findByRequestOrderById(request);
        List<ItemDto> itemDtos = items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        // Формирование DTO с информацией о вещах
        ItemRequestDto result = ItemRequestMapper.toItemRequestDto(request, itemDtos);
        log.debug("Сформирован DTO запроса с информацией о вещах");
        return result;
    }
}