package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST-контроллер для работы с бронированиями.
 */
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final BookingService bookingService;

    /**
     * Создание бронирования.
     */
    @PostMapping
    public BookingResponseDto createBooking(@RequestHeader(USER_ID_HEADER) Long userId,
                                            @RequestBody BookingDto bookingDto) {
        log.info("[POST /bookings] userId={}, payload={}", userId, bookingDto);
        Booking booking = BookingMapper.toBooking(bookingDto);
        Booking created = bookingService.createBooking(userId, booking);
        return BookingMapper.toBookingResponseDto(created);
    }

    /**
     * Подтверждение/отклонение бронирования владельцем вещи.
     */
    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveBooking(@RequestHeader(USER_ID_HEADER) Long userId,
                                             @PathVariable Long bookingId,
                                             @RequestParam("approved") Boolean approved) {
        log.info("[PATCH /bookings/{}] userId={}, approved={}", bookingId, userId, approved);
        Booking updated = bookingService.approveBooking(userId, bookingId, approved);
        return BookingMapper.toBookingResponseDto(updated);
    }

    /**
     * Получение бронирования по id (доступно арендатору и владельцу вещи).
     */
    @GetMapping("/{bookingId}")
    public BookingResponseDto getBooking(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @PathVariable Long bookingId) {
        log.info("[GET /bookings/{}] userId={}", bookingId, userId);
        Booking booking = bookingService.getBookingById(userId, bookingId);
        return BookingMapper.toBookingResponseDto(booking);
    }

    /**
     * Получение бронирований текущего пользователя (как арендатора).
     */
    @GetMapping
    public List<BookingResponseDto> getUserBookings(@RequestHeader(USER_ID_HEADER) Long userId,
                                                    @RequestParam(name = "state", defaultValue = "ALL") String state) {
        log.info("[GET /bookings] userId={}, state={}", userId, state);
        List<Booking> bookings = bookingService.getUserBookings(userId, state);
        return bookings.stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }

    /**
     * Получение бронирований для вещей пользователя (как владельца).
     */
    @GetMapping("/owner")
    public List<BookingResponseDto> getOwnerBookings(@RequestHeader(USER_ID_HEADER) Long userId,
                                                     @RequestParam(name = "state", defaultValue = "ALL") String state) {
        log.info("[GET /bookings/owner] userId={}, state={}", userId, state);
        List<Booking> bookings = bookingService.getOwnerBookings(userId, state);
        return bookings.stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }
}
