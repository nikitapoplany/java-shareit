package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
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
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    @Override
    @Transactional
    public Booking createBooking(Long userId, Booking booking) {
        // Проверка существования пользователя
        User booker = userService.getUserById(userId);

        // Проверка существования вещи
        Item item = itemService.getItemById(booking.getItem().getId());

        // Проверка доступности вещи
        if (!item.getAvailable()) {
            throw new ValidationException("Вещь с ID " + item.getId() + " недоступна для бронирования");
        }

        // Проверка, что пользователь не является владельцем вещи
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец вещи не может бронировать свою вещь");
        }
        
        // Установка полной информации о вещи
        booking.setItem(item);

        // Проверка корректности дат бронирования
        validateBookingDates(booking);

        // Установка пользователя и статуса
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking approveBooking(Long userId, Long bookingId, Boolean approved) {
        // Проверка существования бронирования
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с ID " + bookingId + " не найдено"));

        // Проверка, что пользователь является владельцем вещи
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Пользователь с ID " + userId + " не является владельцем вещи");
        }

        // Проверка, что бронирование в статусе WAITING
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование уже подтверждено или отклонено");
        }

        // Установка статуса
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return bookingRepository.save(booking);
    }

    @Override
    public Booking getBookingById(Long userId, Long bookingId) {
        // Проверка существования бронирования
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с ID " + bookingId + " не найдено"));

        // Проверка, что пользователь является автором бронирования или владельцем вещи
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не имеет доступа к бронированию с ID " + bookingId);
        }

        return booking;
    }

    @Override
    public List<Booking> getUserBookings(Long userId, String state) {
        // Проверка существования пользователя
        User user = userService.getUserById(userId);

        LocalDateTime now = LocalDateTime.now();

        // Фильтрация по состоянию
        switch (state.toUpperCase()) {
            case "ALL":
                return bookingRepository.findByBooker(user, SORT_BY_START_DESC);
            case "CURRENT":
                return bookingRepository.findByBookerAndStartBeforeAndEndAfter(user, now, now, SORT_BY_START_DESC);
            case "PAST":
                return bookingRepository.findByBookerAndEndBefore(user, now, SORT_BY_START_DESC);
            case "FUTURE":
                return bookingRepository.findByBookerAndStartAfter(user, now, SORT_BY_START_DESC);
            case "WAITING":
                return bookingRepository.findByBookerAndStatus(user, BookingStatus.WAITING, SORT_BY_START_DESC);
            case "REJECTED":
                return bookingRepository.findByBookerAndStatus(user, BookingStatus.REJECTED, SORT_BY_START_DESC);
            default:
                throw new ValidationException("Неизвестное состояние: " + state);
        }
    }

    @Override
    public List<Booking> getOwnerBookings(Long userId, String state) {
        // Проверка существования пользователя
        User owner = userService.getUserById(userId);

        LocalDateTime now = LocalDateTime.now();

        // Фильтрация по состоянию
        switch (state.toUpperCase()) {
            case "ALL":
                return bookingRepository.findByItemOwner(owner, SORT_BY_START_DESC);
            case "CURRENT":
                return bookingRepository.findCurrentByItemOwner(owner, now, SORT_BY_START_DESC);
            case "PAST":
                return bookingRepository.findPastByItemOwner(owner, now, SORT_BY_START_DESC);
            case "FUTURE":
                return bookingRepository.findFutureByItemOwner(owner, now, SORT_BY_START_DESC);
            case "WAITING":
                return bookingRepository.findByItemOwnerAndStatus(owner, BookingStatus.WAITING, SORT_BY_START_DESC);
            case "REJECTED":
                return bookingRepository.findByItemOwnerAndStatus(owner, BookingStatus.REJECTED, SORT_BY_START_DESC);
            default:
                throw new ValidationException("Неизвестное состояние: " + state);
        }
    }

    /**
     * Валидирует даты бронирования.
     *
     * @param booking бронирование для проверки
     */
    private void validateBookingDates(Booking booking) {
        LocalDateTime now = LocalDateTime.now();

        if (booking.getStart() == null) {
            throw new ValidationException("Дата начала бронирования не может быть пустой");
        }

        if (booking.getEnd() == null) {
            throw new ValidationException("Дата окончания бронирования не может быть пустой");
        }

        if (booking.getStart().isBefore(now)) {
            throw new ValidationException("Дата начала бронирования не может быть в прошлом");
        }

        if (booking.getEnd().isBefore(booking.getStart())) {
            throw new ValidationException("Дата окончания бронирования не может быть раньше даты начала");
        }

        if (booking.getStart().equals(booking.getEnd())) {
            throw new ValidationException("Дата начала и окончания бронирования не могут совпадать");
        }
    }
}