package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.Booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с вещами.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public Item createItem(Long userId, Item item) {
        log.info("Создание вещи пользователем с ID {}: {}", userId, item);

        // Проверка существования пользователя
        User owner = userService.getUserById(userId);
        log.debug("Найден владелец: {}", owner);

        // Валидация обязательных полей
        validateItemFields(item);
        log.debug("Валидация полей вещи успешно пройдена");

        // Установка владельца
        item.setOwner(owner);

        // Сохранение
        Item savedItem = itemRepository.save(item);
        log.info("Вещь успешно создана: {}", savedItem);
        return savedItem;
    }

    /**
     * Валидирует обязательные поля вещи.
     *
     * @param item проверяемая вещь
     */
    private void validateItemFields(Item item) {
        log.debug("Валидация полей вещи: {}", item);

        if (item.getName() == null || item.getName().isBlank()) {
            log.warn("Попытка создания вещи с пустым названием");
            throw new ValidationException("Название вещи не может быть пустым");
        }

        if (item.getDescription() == null || item.getDescription().isBlank()) {
            log.warn("Попытка создания вещи с пустым описанием");
            throw new ValidationException("Описание вещи не может быть пустым");
        }

        if (item.getAvailable() == null) {
            log.warn("Попытка создания вещи без указания статуса доступности");
            throw new ValidationException("Статус доступности вещи должен быть указан");
        }

        log.debug("Валидация полей вещи успешно завершена");
    }

    @Override
    @Transactional
    public Item updateItem(Long userId, Long itemId, Item item) {
        log.info("Обновление вещи с ID {} пользователем с ID {}: {}", itemId, userId, item);

        // Проверка существования вещи
        Item existingItem = getItemById(itemId);
        log.debug("Найдена существующая вещь: {}", existingItem);

        // Проверка, что пользователь является владельцем вещи
        if (!existingItem.getOwner().getId().equals(userId)) {
            log.warn("Пользователь с ID {} не является владельцем вещи с ID {}", userId, itemId);
            throw new NotFoundException("Пользователь с ID " + userId + " не является владельцем вещи с ID " + itemId);
        }

        // Обновляем только переданные поля
        if (item.getName() != null) {
            log.debug("Обновление названия вещи с ID {}: {} -> {}", itemId, existingItem.getName(), item.getName());
            existingItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            log.debug("Обновление описания вещи с ID {}", itemId);
            existingItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            log.debug("Обновление статуса доступности вещи с ID {}: {} -> {}",
                    itemId, existingItem.getAvailable(), item.getAvailable());
            existingItem.setAvailable(item.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        log.info("Вещь с ID {} успешно обновлена", itemId);
        return updatedItem;
    }

    @Override
    public Item getItemById(Long itemId) {
        log.debug("Получение вещи по ID: {}", itemId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Вещь с ID {} не найдена", itemId);
                    return new NotFoundException("Вещь с ID " + itemId + " не найдена");
                });
        log.debug("Найдена вещь: {}", item);
        return item;
    }

    @Override
    public List<Item> getUserItems(Long userId) {
        log.info("Получение списка вещей пользователя с ID: {}", userId);

        // Проверка существования пользователя
        User owner = userService.getUserById(userId);
        log.debug("Найден владелец: {}", owner);

        List<Item> items = itemRepository.findByOwnerOrderById(owner);
        log.debug("Найдено {} вещей пользователя с ID {}", items.size(), userId);
        return items;
    }

    @Override
    public List<Item> searchItems(String text) {
        log.info("Поиск вещей по тексту: '{}'", text);

        if (text == null || text.isBlank()) {
            log.debug("Поисковый запрос пуст, возвращаем пустой список");
            return new ArrayList<>();
        }

        List<Item> items = itemRepository.search(text);
        log.debug("Найдено {} вещей по запросу '{}'", items.size(), text);
        return items;
    }

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        log.info("Создание комментария пользователем с ID {} для вещи с ID {}: {}", userId, itemId, commentDto);

        // Проверка существования пользователя
        User author = userService.getUserById(userId);
        log.debug("Найден автор комментария: {}", author);

        // Проверка существования вещи
        Item item = getItemById(itemId);
        log.debug("Найдена вещь для комментария: {}", item);

        // Проверка, что пользователь брал вещь в аренду и аренда завершена
        LocalDateTime now = LocalDateTime.now();
        boolean hasBooking = bookingRepository.existsByItemAndBookerAndEndBeforeAndStatus(
                item, author, now, BookingStatus.APPROVED);

        if (!hasBooking) {
            log.warn("Пользователь с ID {} не может оставить комментарий к вещи с ID {}, " +
                    "так как не брал её в аренду или аренда не завершена", userId, itemId);
            throw new ValidationException("Пользователь с ID " + userId +
                    " не может оставить комментарий к вещи с ID " + itemId +
                    ", так как не брал её в аренду или аренда не завершена");
        }
        log.debug("Проверка бронирования пройдена успешно");

        // Создание и сохранение комментария
        Comment comment = CommentMapper.toComment(commentDto, item, author);
        comment.setCreated(now);

        Comment savedComment = commentRepository.save(comment);
        log.info("Комментарий успешно создан: {}", savedComment);
        return CommentMapper.toCommentDto(savedComment);
    }

    @Override
    public List<CommentDto> getItemComments(Long itemId) {
        log.info("Получение комментариев для вещи с ID: {}", itemId);

        // Проверка существования вещи
        Item item = getItemById(itemId);
        log.debug("Найдена вещь: {}", item);

        List<CommentDto> comments = commentRepository.findByItemOrderByCreatedDesc(item).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        log.debug("Найдено {} комментариев для вещи с ID {}", comments.size(), itemId);
        return comments;
    }

    @Override
    public ItemDto getItemWithBookingsAndComments(Long itemId, Long userId) {
        log.info("Получение вещи с ID {} с бронированиями и комментариями для пользователя с ID {}", itemId, userId);

        // Проверка существования вещи
        Item item = getItemById(itemId);
        log.debug("Найдена вещь: {}", item);

        // Создаем DTO вещи
        ItemDto itemDto = ItemMapper.toItemDto(item);

        // Получаем комментарии к вещи
        List<CommentDto> comments = getItemComments(itemId);
        itemDto.setComments(comments);
        log.debug("Добавлены комментарии к вещи с ID {}: {}", itemId, comments.size());

        // Если пользователь не является владельцем вещи, не добавляем информацию о бронированиях
        if (!item.getOwner().getId().equals(userId)) {
            log.debug("Пользователь с ID {} не является владельцем вещи с ID {}, информация о бронированиях не добавлена",
                    userId, itemId);
            return itemDto;
        }

        // Получаем последнее завершенное бронирование
        LocalDateTime now = LocalDateTime.now();
        Booking lastBooking = bookingRepository.findFirstByItemAndEndBeforeOrderByEndDesc(item, now);
        if (lastBooking != null) {
            log.debug("Найдено последнее завершенное бронирование для вещи с ID {}: {}", itemId, lastBooking);
            itemDto.setLastBooking(BookingMapper.toBookingDtoShort(lastBooking));
        }

        // Получаем ближайшее будущее бронирование
        Booking nextBooking = bookingRepository.findFirstByItemAndStartAfterOrderByStartAsc(item, now);
        if (nextBooking != null) {
            log.debug("Найдено ближайшее будущее бронирование для вещи с ID {}: {}", itemId, nextBooking);
            itemDto.setNextBooking(BookingMapper.toBookingDtoShort(nextBooking));
        }

        log.debug("Вещь с ID {} успешно получена с бронированиями и комментариями", itemId);
        return itemDto;
    }
}