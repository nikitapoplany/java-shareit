package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserMapper;

/**
 * Маппер для преобразования между Booking и DTO.
 */
public class BookingMapper {
    /**
     * Преобразует BookingDto в Booking.
     *
     * @param bookingDto DTO бронирования
     * @return объект бронирования
     */
    public static Booking toBooking(BookingDto bookingDto) {
        Booking booking = new Booking();
        booking.setId(bookingDto.getId());
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        
        Item item = new Item();
        item.setId(bookingDto.getItemId());
        booking.setItem(item);
        
        return booking;
    }

    /**
     * Преобразует Booking в BookingResponseDto.
     *
     * @param booking объект бронирования
     * @return DTO бронирования для ответа
     */
    public static BookingResponseDto toBookingResponseDto(Booking booking) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                UserMapper.toUserDto(booking.getBooker()),
                ItemMapper.toItemDto(booking.getItem())
        );
    }

    /**
     * Создает упрощенное представление бронирования для включения в другие DTO.
     *
     * @param booking объект бронирования
     * @return упрощенное DTO бронирования
     */
    public static BookingDto toBookingDtoShort(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem().getId()
        );
    }
}