package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.Booking.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Реализация сервиса для работы с бронированиями.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    @Override
    @Transactional
    public Booking createBooking(Long userId, Booking booking) {
        log.info("Создание бронирования пользователем с ID {}: {}", userId, booking);

        // Проверка существования пользователя
        User booker = userService.getUserById(userId);
        log.debug("Найден пользователь-арендатор: {}", booker);

        // Проверка существования вещи
        Long itemId = booking.getItem().getId();
        Item item = itemService.getItemById(itemId);
        log.debug("Найдена вещь для бронирования: {}", item);

        // Проверка доступности вещи
        if (!item.getAvailable()) {
            log.warn("Попытка бронирования недоступной вещи с ID {}", itemId);
            throw new ValidationException("Вещь с ID " + item.getId() + " недоступна для бронирования");
        }

        // Проверка, что пользователь не является владельцем вещи
        if (item.getOwner().getId().equals(userId)) {
            log.warn("Попытка бронирования вещи с ID {} её владельцем с ID {}", itemId, userId);
            throw new NotFoundException("Владелец вещи не может бронировать свою вещь");
        }

        // Установка полной информации о вещи
        booking.setItem(item);

        // Проверка корректности дат бронирования
        validateBookingDates(booking);
        log.debug("Валидация дат бронирования успешно пройдена");

        // Установка пользователя и статуса
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Бронирование успешно создано: {}", savedBooking);
        return savedBooking;
    }

    @Override
    @Transactional
    public Booking approveBooking(Long userId, Long bookingId, Boolean approved) {
        log.info("Подтверждение/отклонение бронирования с ID {} пользователем с ID {}, approved={}",
                bookingId, userId, approved);

        // Проверка существования бронирования
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Бронирование с ID {} не найдено", bookingId);
                    return new NotFoundException("Бронирование с ID " + bookingId + " не найдено");
                });
        log.debug("Найдено бронирование: {}", booking);

        // Проверка, что пользователь является владельцем вещи
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            log.warn("Пользователь с ID {} не является владельцем вещи для бронирования с ID {}", userId, bookingId);
            throw new ValidationException("Пользователь с ID " + userId + " не является владельцем вещи");
        }

        // Проверка, что бронирование в статусе WAITING
        if (booking.getStatus() != BookingStatus.WAITING) {
            log.warn("Попытка изменения статуса бронирования с ID {}, которое уже подтверждено или отклонено", bookingId);
            throw new ValidationException("Бронирование уже подтверждено или отклонено");
        }

        // Установка статуса
        BookingStatus newStatus = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        log.debug("Изменение статуса бронирования с ID {} с {} на {}", bookingId, booking.getStatus(), newStatus);
        booking.setStatus(newStatus);

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Бронирование с ID {} успешно {} пользователем с ID {}",
                bookingId, approved ? "подтверждено" : "отклонено", userId);
        return updatedBooking;
    }

    @Override
    public Booking getBookingById(Long userId, Long bookingId) {
        log.info("Получение бронирования с ID {} пользователем с ID {}", bookingId, userId);

        // Проверка существования бронирования
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Бронирование с ID {} не найдено", bookingId);
                    return new NotFoundException("Бронирование с ID " + bookingId + " не найдено");
                });
        log.debug("Найдено бронирование: {}", booking);

        // Проверка, что пользователь является автором бронирования или владельцем вещи
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            log.warn("Пользователь с ID {} не имеет доступа к бронированию с ID {}", userId, bookingId);
            throw new NotFoundException("Пользователь с ID " + userId + " не имеет доступа к бронированию с ID " + bookingId);
        }

        log.debug("Доступ к бронированию с ID {} для пользователя с ID {} подтвержден", bookingId, userId);
        return booking;
    }

    @Override
    public List<Booking> getUserBookings(Long userId, String state) {
        log.info("Получение списка бронирований пользователя с ID {}, состояние: {}", userId, state);

        // Проверка существования пользователя
        User user = userService.getUserById(userId);
        log.debug("Найден пользователь: {}", user);

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        // Фильтрация по состоянию
        try {
            switch (state.toUpperCase()) {
                case "ALL":
                    log.debug("Поиск всех бронирований пользователя с ID {}", userId);
                    bookings = bookingRepository.findByBooker(user, SORT_BY_START_DESC);
                    break;
                case "CURRENT":
                    log.debug("Поиск текущих бронирований пользователя с ID {}", userId);
                    bookings = bookingRepository.findByBookerAndStartBeforeAndEndAfter(user, now, now, SORT_BY_START_DESC);
                    break;
                case "PAST":
                    log.debug("Поиск прошедших бронирований пользователя с ID {}", userId);
                    bookings = bookingRepository.findByBookerAndEndBefore(user, now, SORT_BY_START_DESC);
                    break;
                case "FUTURE":
                    log.debug("Поиск будущих бронирований пользователя с ID {}", userId);
                    bookings = bookingRepository.findByBookerAndStartAfter(user, now, SORT_BY_START_DESC);
                    break;
                case "WAITING":
                    log.debug("Поиск ожидающих бронирований пользователя с ID {}", userId);
                    bookings = bookingRepository.findByBookerAndStatus(user, BookingStatus.WAITING, SORT_BY_START_DESC);
                    break;
                case "REJECTED":
                    log.debug("Поиск отклоненных бронирований пользователя с ID {}", userId);
                    bookings = bookingRepository.findByBookerAndStatus(user, BookingStatus.REJECTED, SORT_BY_START_DESC);
                    break;
                default:
                    log.warn("Указано неизвестное состояние бронирования: {}", state);
                    throw new ValidationException("Неизвестное состояние: " + state);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Ошибка при обработке состояния бронирования: {}", e.getMessage());
            throw new ValidationException("Неизвестное состояние: " + state);
        }

        log.debug("Найдено {} бронирований пользователя с ID {} в состоянии {}", bookings.size(), userId, state);
        return bookings;
    }

    @Override
    public List<Booking> getOwnerBookings(Long userId, String state) {
        log.info("Получение списка бронирований для вещей владельца с ID {}, состояние: {}", userId, state);

        // Проверка существования пользователя
        User owner = userService.getUserById(userId);
        log.debug("Найден владелец: {}", owner);

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        // Фильтрация по состоянию
        try {
            switch (state.toUpperCase()) {
                case "ALL":
                    log.debug("Поиск всех бронирований для вещей владельца с ID {}", userId);
                    bookings = bookingRepository.findByItemOwner(owner, SORT_BY_START_DESC);
                    break;
                case "CURRENT":
                    log.debug("Поиск текущих бронирований для вещей владельца с ID {}", userId);
                    bookings = bookingRepository.findCurrentByItemOwner(owner, now, SORT_BY_START_DESC);
                    break;
                case "PAST":
                    log.debug("Поиск прошедших бронирований для вещей владельца с ID {}", userId);
                    bookings = bookingRepository.findPastByItemOwner(owner, now, SORT_BY_START_DESC);
                    break;
                case "FUTURE":
                    log.debug("Поиск будущих бронирований для вещей владельца с ID {}", userId);
                    bookings = bookingRepository.findFutureByItemOwner(owner, now, SORT_BY_START_DESC);
                    break;
                case "WAITING":
                    log.debug("Поиск ожидающих бронирований для вещей владельца с ID {}", userId);
                    bookings = bookingRepository.findByItemOwnerAndStatus(owner, BookingStatus.WAITING, SORT_BY_START_DESC);
                    break;
                case "REJECTED":
                    log.debug("Поиск отклоненных бронирований для вещей владельца с ID {}", userId);
                    bookings = bookingRepository.findByItemOwnerAndStatus(owner, BookingStatus.REJECTED, SORT_BY_START_DESC);
                    break;
                default:
                    log.warn("Указано неизвестное состояние бронирования: {}", state);
                    throw new ValidationException("Неизвестное состояние: " + state);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Ошибка при обработке состояния бронирования: {}", e.getMessage());
            throw new ValidationException("Неизвестное состояние: " + state);
        }

        log.debug("Найдено {} бронирований для вещей владельца с ID {} в состоянии {}", bookings.size(), userId, state);
        return bookings;
    }

    /**
     * Валидирует даты бронирования.
     *
     * @param booking бронирование для проверки
     */
    private void validateBookingDates(Booking booking) {
        log.debug("Валидация дат бронирования: start={}, end={}", booking.getStart(), booking.getEnd());
        LocalDateTime now = LocalDateTime.now();

        if (booking.getStart() == null) {
            log.warn("Попытка создания бронирования с пустой датой начала");
            throw new ValidationException("Дата начала бронирования не может быть пустой");
        }

        if (booking.getEnd() == null) {
            log.warn("Попытка создания бронирования с пустой датой окончания");
            throw new ValidationException("Дата окончания бронирования не может быть пустой");
        }

        if (booking.getStart().isBefore(now)) {
            log.warn("Попытка создания бронирования с датой начала в прошлом: {}", booking.getStart());
            throw new ValidationException("Дата начала бронирования не может быть в прошлом");
        }

        if (booking.getEnd().isBefore(booking.getStart())) {
            log.warn("Попытка создания бронирования с датой окончания раньше даты начала: start={}, end={}",
                    booking.getStart(), booking.getEnd());
            throw new ValidationException("Дата окончания бронирования не может быть раньше даты начала");
        }

        if (booking.getStart().equals(booking.getEnd())) {
            log.warn("Попытка создания бронирования с одинаковыми датами начала и окончания: {}", booking.getStart());
            throw new ValidationException("Дата начала и окончания бронирования не могут совпадать");
        }
    }
}