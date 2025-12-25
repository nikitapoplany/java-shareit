package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.Booking.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для {@link BookingMapper}
 */
class BookingMapperTest {

    @Test
    void toBooking_ShouldMapDtoToEntity() {
        // Подготовка
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        Long itemId = 1L;
        
        BookingDto bookingDto = new BookingDto(2L, start, end, itemId);
        
        // Действие
        Booking booking = BookingMapper.toBooking(bookingDto);
        
        // Проверка
        assertNotNull(booking);
        assertEquals(bookingDto.getId(), booking.getId());
        assertEquals(bookingDto.getStart(), booking.getStart());
        assertEquals(bookingDto.getEnd(), booking.getEnd());
        assertEquals(bookingDto.getItemId(), booking.getItem().getId());
    }
    
    @Test
    void toBookingResponseDto_ShouldMapEntityToResponseDto() {
        // Подготовка
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        
        User user = new User(1L, "User", "user@example.com");
        User owner = new User(2L, "Owner", "owner@example.com");
        Item item = new Item(3L, "Item", "Description", true, owner, null);
        
        Booking booking = new Booking(4L, start, end, item, user, BookingStatus.WAITING);
        
        // Действие
        BookingResponseDto responseDto = BookingMapper.toBookingResponseDto(booking);
        
        // Проверка
        assertNotNull(responseDto);
        assertEquals(booking.getId(), responseDto.getId());
        assertEquals(booking.getStart(), responseDto.getStart());
        assertEquals(booking.getEnd(), responseDto.getEnd());
        assertEquals(booking.getStatus(), responseDto.getStatus());
        
        assertNotNull(responseDto.getBooker());
        assertEquals(user.getId(), responseDto.getBooker().getId());
        assertEquals(user.getName(), responseDto.getBooker().getName());
        assertEquals(user.getEmail(), responseDto.getBooker().getEmail());
        
        assertNotNull(responseDto.getItem());
        assertEquals(item.getId(), responseDto.getItem().getId());
        assertEquals(item.getName(), responseDto.getItem().getName());
        assertEquals(item.getDescription(), responseDto.getItem().getDescription());
        assertEquals(item.getAvailable(), responseDto.getItem().getAvailable());
    }
    
    @Test
    void toBookingDtoShort_ShouldCreateSimplifiedDto() {
        // Подготовка
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        
        User user = new User(1L, "User", "user@example.com");
        User owner = new User(2L, "Owner", "owner@example.com");
        Item item = new Item(3L, "Item", "Description", true, owner, null);
        
        Booking booking = new Booking(4L, start, end, item, user, BookingStatus.WAITING);
        
        // Действие
        BookingDto bookingDto = BookingMapper.toBookingDtoShort(booking);
        
        // Проверка
        assertNotNull(bookingDto);
        assertEquals(booking.getId(), bookingDto.getId());
        assertEquals(booking.getStart(), bookingDto.getStart());
        assertEquals(booking.getEnd(), bookingDto.getEnd());
        assertEquals(booking.getItem().getId(), bookingDto.getItemId());
    }
}