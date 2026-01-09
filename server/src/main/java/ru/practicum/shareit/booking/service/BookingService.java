package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.Booking;

import java.util.List;

/**
 * Интерфейс сервиса для работы с бронированиями.
 */
public interface BookingService {
    /**
     * Создает новое бронирование.
     *
     * @param userId  ID пользователя, создающего бронирование
     * @param booking данные бронирования
     * @return созданное бронирование
     */
    Booking createBooking(Long userId, Booking booking);

    /**
     * Подтверждает или отклоняет бронирование.
     *
     * @param userId    ID пользователя, владельца вещи
     * @param bookingId ID бронирования
     * @param approved  флаг подтверждения (true - подтвердить, false - отклонить)
     * @return обновленное бронирование
     */
    Booking approveBooking(Long userId, Long bookingId, Boolean approved);

    /**
     * Получает данные о бронировании по его ID.
     *
     * @param userId    ID пользователя, запрашивающего данные
     * @param bookingId ID бронирования
     * @return данные бронирования
     */
    Booking getBookingById(Long userId, Long bookingId);

    /**
     * Получает список бронирований пользователя.
     *
     * @param userId ID пользователя
     * @param state  состояние бронирований (ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED)
     * @return список бронирований
     */
    List<Booking> getUserBookings(Long userId, String state);

    /**
     * Получает список бронирований для вещей пользователя.
     *
     * @param userId ID пользователя, владельца вещей
     * @param state  состояние бронирований (ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED)
     * @return список бронирований
     */
    List<Booking> getOwnerBookings(Long userId, String state);
}