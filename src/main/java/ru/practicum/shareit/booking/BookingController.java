package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Контроллер для работы с бронированиями.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    /**
     * Создает новое бронирование.
     *
     * @param userId     ID пользователя, создающего бронирование
     * @param bookingDto данные бронирования
     * @return созданное бронирование
     */
    @PostMapping
    public BookingResponseDto createBooking(@RequestHeader(USER_ID_HEADER) Long userId,
                                           @RequestBody BookingDto bookingDto) {
        Booking booking = BookingMapper.toBooking(bookingDto);
        return BookingMapper.toBookingResponseDto(bookingService.createBooking(userId, booking));
    }

    /**
     * Подтверждает или отклоняет бронирование.
     *
     * @param userId    ID пользователя, владельца вещи
     * @param bookingId ID бронирования
     * @param approved  флаг подтверждения (true - подтвердить, false - отклонить)
     * @return обновленное бронирование
     */
    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveBooking(@RequestHeader(USER_ID_HEADER) Long userId,
                                            @PathVariable Long bookingId,
                                            @RequestParam Boolean approved) {
        return BookingMapper.toBookingResponseDto(bookingService.approveBooking(userId, bookingId, approved));
    }

    /**
     * Получает данные о бронировании по его ID.
     *
     * @param userId    ID пользователя, запрашивающего данные
     * @param bookingId ID бронирования
     * @return данные бронирования
     */
    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(@RequestHeader(USER_ID_HEADER) Long userId,
                                            @PathVariable Long bookingId) {
        return BookingMapper.toBookingResponseDto(bookingService.getBookingById(userId, bookingId));
    }

    /**
     * Получает список бронирований пользователя.
     *
     * @param userId ID пользователя
     * @param state  состояние бронирований (ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED)
     * @return список бронирований
     */
    @GetMapping
    public List<BookingResponseDto> getUserBookings(@RequestHeader(USER_ID_HEADER) Long userId,
                                                   @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getUserBookings(userId, state).stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Получает список бронирований для вещей пользователя.
     *
     * @param userId ID пользователя, владельца вещей
     * @param state  состояние бронирований (ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED)
     * @return список бронирований
     */
    @GetMapping("/owner")
    public List<BookingResponseDto> getOwnerBookings(@RequestHeader(USER_ID_HEADER) Long userId,
                                                    @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getOwnerBookings(userId, state).stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }
}
