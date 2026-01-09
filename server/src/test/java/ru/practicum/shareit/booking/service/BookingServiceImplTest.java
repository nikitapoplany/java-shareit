package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Тесты для {@link BookingServiceImpl}
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserService userService;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User user;
    private User owner;
    private Item item;
    private Booking booking;
    private LocalDateTime now;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        start = now.plusDays(1);
        end = now.plusDays(2);

        user = new User(1L, "User", "user@example.com");
        owner = new User(2L, "Owner", "owner@example.com");
        item = new Item(1L, "Item", "Description", true, owner, null);
        booking = new Booking(1L, start, end, item, user, BookingStatus.WAITING);
    }

    @Test
    void createBooking_WithValidData_ShouldCreateBooking() {
        // Подготовка
        Booking inputBooking = new Booking(null, start, end, item, null, null);

        when(userService.getUserById(user.getId())).thenReturn(user);
        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(item);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        // Действие
        Booking result = bookingService.createBooking(user.getId(), inputBooking);

        // Проверка
        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
        assertEquals(booking.getItem(), result.getItem());
        assertEquals(booking.getBooker(), result.getBooker());
        assertEquals(BookingStatus.WAITING, result.getStatus());
    }

    @Test
    void createBooking_WithUnavailableItem_ShouldThrowValidationException() {
        // Подготовка
        Item unavailableItem = new Item(1L, "Item", "Description", false, owner, null);
        Booking inputBooking = new Booking(null, start, end, unavailableItem, null, null);

        when(userService.getUserById(user.getId())).thenReturn(user);
        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(unavailableItem);

        // Действие и проверка
        ValidationException exception = assertThrows(ValidationException.class, () ->
            bookingService.createBooking(user.getId(), inputBooking)
        );

        assertTrue(exception.getMessage().contains("недоступна для бронирования"));
    }

    @Test
    void createBooking_ByOwner_ShouldThrowNotFoundException() {
        // Подготовка
        Booking inputBooking = new Booking(null, start, end, item, null, null);

        when(userService.getUserById(owner.getId())).thenReturn(owner);
        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(item);

        // Действие и проверка
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            bookingService.createBooking(owner.getId(), inputBooking)
        );

        assertTrue(exception.getMessage().contains("Владелец вещи не может бронировать свою вещь"));
    }

    @Test
    void createBooking_WithStartInPast_ShouldThrowValidationException() {
        // Подготовка
        LocalDateTime pastStart = now.minusDays(1);
        Booking inputBooking = new Booking(null, pastStart, end, item, null, null);

        when(userService.getUserById(user.getId())).thenReturn(user);
        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(item);

        // Действие и проверка
        ValidationException exception = assertThrows(ValidationException.class, () ->
            bookingService.createBooking(user.getId(), inputBooking)
        );

        assertTrue(exception.getMessage().contains("Дата начала бронирования не может быть в прошлом"));
    }

    @Test
    void createBooking_WithEndBeforeStart_ShouldThrowValidationException() {
        // Подготовка
        LocalDateTime earlierEnd = start.minusDays(1);
        Booking inputBooking = new Booking(null, start, earlierEnd, item, null, null);

        when(userService.getUserById(user.getId())).thenReturn(user);
        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(item);

        // Действие и проверка
        ValidationException exception = assertThrows(ValidationException.class, () ->
            bookingService.createBooking(user.getId(), inputBooking)
        );

        assertTrue(exception.getMessage().contains("Дата окончания бронирования не может быть раньше даты начала"));
    }

    @Test
    void approveBooking_WithValidData_ShouldApproveBooking() {
        // Подготовка
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        Booking approvedBooking = new Booking(
            booking.getId(),
            booking.getStart(),
            booking.getEnd(),
            booking.getItem(),
            booking.getBooker(),
            BookingStatus.APPROVED
        );

        when(bookingRepository.save(any(Booking.class))).thenReturn(approvedBooking);

        // Действие
        Booking result = bookingService.approveBooking(owner.getId(), booking.getId(), true);

        // Проверка
        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    void approveBooking_WithNonExistentBooking_ShouldThrowNotFoundException() {
        // Подготовка
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Действие и проверка
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            bookingService.approveBooking(owner.getId(), 999L, true)
        );

        assertTrue(exception.getMessage().contains("Бронирование с ID 999 не найдено"));
    }

    @Test
    void approveBooking_ByNonOwner_ShouldThrowValidationException() {
        // Подготовка
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        // Действие и проверка
        ValidationException exception = assertThrows(ValidationException.class, () ->
            bookingService.approveBooking(user.getId(), booking.getId(), true)
        );

        assertTrue(exception.getMessage().contains("не является владельцем вещи"));
    }

    @Test
    void approveBooking_AlreadyApproved_ShouldThrowValidationException() {
        // Подготовка
        Booking approvedBooking = new Booking(
            booking.getId(),
            booking.getStart(),
            booking.getEnd(),
            booking.getItem(),
            booking.getBooker(),
            BookingStatus.APPROVED
        );

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(approvedBooking));

        // Действие и проверка
        ValidationException exception = assertThrows(ValidationException.class, () ->
            bookingService.approveBooking(owner.getId(), booking.getId(), true)
        );

        assertTrue(exception.getMessage().contains("Бронирование уже подтверждено или отклонено"));
    }

    @Test
    void getBookingById_WithValidData_ShouldReturnBooking() {
        // Подготовка
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        // Действие
        Booking result = bookingService.getBookingById(user.getId(), booking.getId());

        // Проверка
        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
    }

    @Test
    void getBookingById_WithNonExistentBooking_ShouldThrowNotFoundException() {
        // Подготовка
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Действие и проверка
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            bookingService.getBookingById(user.getId(), 999L)
        );

        assertTrue(exception.getMessage().contains("Бронирование с ID 999 не найдено"));
    }

    @Test
    void getBookingById_ByUnauthorizedUser_ShouldThrowNotFoundException() {
        // Подготовка
        User anotherUser = new User(3L, "Another", "another@example.com");
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        // Действие и проверка
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            bookingService.getBookingById(anotherUser.getId(), booking.getId())
        );

        assertTrue(exception.getMessage().contains("не имеет доступа к бронированию"));
    }

    @Test
    void getUserBookings_WithStateAll_ShouldReturnAllBookings() {
        // Подготовка
        List<Booking> bookings = Arrays.asList(booking);
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(bookingRepository.findByBookerOrderByStartDesc(eq(user))).thenReturn(bookings);

        // Действие
        List<Booking> result = bookingService.getUserBookings(user.getId(), "ALL");

        // Проверка
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void getUserBookings_WithStateCurrent_ShouldReturnCurrentBookings() {
        // Подготовка
        List<Booking> bookings = Arrays.asList(booking);
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(bookingRepository.findCurrentBookingsByBooker(eq(user), any(LocalDateTime.class)))
            .thenReturn(bookings);

        // Действие
        List<Booking> result = bookingService.getUserBookings(user.getId(), "CURRENT");

        // Проверка
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void getUserBookings_WithStatePast_ShouldReturnPastBookings() {
        // Подготовка
        List<Booking> bookings = Arrays.asList(booking);
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(bookingRepository.findByBookerAndEndBeforeOrderByStartDesc(eq(user), any(LocalDateTime.class)))
            .thenReturn(bookings);

        // Действие
        List<Booking> result = bookingService.getUserBookings(user.getId(), "PAST");

        // Проверка
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void getUserBookings_WithStateFuture_ShouldReturnFutureBookings() {
        // Подготовка
        List<Booking> bookings = Arrays.asList(booking);
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(bookingRepository.findByBookerAndStartAfterOrderByStartDesc(eq(user), any(LocalDateTime.class)))
            .thenReturn(bookings);

        // Действие
        List<Booking> result = bookingService.getUserBookings(user.getId(), "FUTURE");

        // Проверка
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void getUserBookings_WithStateWaiting_ShouldReturnWaitingBookings() {
        // Подготовка
        List<Booking> bookings = Arrays.asList(booking);
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(bookingRepository.findByBookerAndStatusOrderByStartDesc(eq(user), eq(BookingStatus.WAITING)))
            .thenReturn(bookings);

        // Действие
        List<Booking> result = bookingService.getUserBookings(user.getId(), "WAITING");

        // Проверка
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void getUserBookings_WithStateRejected_ShouldReturnRejectedBookings() {
        // Подготовка
        Booking rejectedBooking = new Booking(
            booking.getId(),
            booking.getStart(),
            booking.getEnd(),
            booking.getItem(),
            booking.getBooker(),
            BookingStatus.REJECTED
        );

        List<Booking> bookings = Arrays.asList(rejectedBooking);
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(bookingRepository.findByBookerAndStatusOrderByStartDesc(eq(user), eq(BookingStatus.REJECTED)))
            .thenReturn(bookings);

        // Действие
        List<Booking> result = bookingService.getUserBookings(user.getId(), "REJECTED");

        // Проверка
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(rejectedBooking.getId(), result.get(0).getId());
        assertEquals(BookingStatus.REJECTED, result.get(0).getStatus());
    }

    @Test
    void getUserBookings_WithInvalidState_ShouldThrowValidationException() {
        // Подготовка
        when(userService.getUserById(user.getId())).thenReturn(user);

        // Действие и проверка
        ValidationException exception = assertThrows(ValidationException.class, () ->
            bookingService.getUserBookings(user.getId(), "INVALID")
        );

        assertTrue(exception.getMessage().contains("Неизвестное состояние: INVALID"));
    }

    @Test
    void getOwnerBookings_WithStateAll_ShouldReturnAllBookings() {
        // Подготовка
        List<Booking> bookings = Arrays.asList(booking);
        when(userService.getUserById(owner.getId())).thenReturn(owner);
        when(bookingRepository.findBookingsByOwner(eq(owner))).thenReturn(bookings);

        // Действие
        List<Booking> result = bookingService.getOwnerBookings(owner.getId(), "ALL");

        // Проверка
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void getOwnerBookings_WithStateCurrent_ShouldReturnCurrentBookings() {
        // Подготовка
        List<Booking> bookings = Arrays.asList(booking);
        when(userService.getUserById(owner.getId())).thenReturn(owner);
        when(bookingRepository.findCurrentBookingsByOwner(eq(owner), any(LocalDateTime.class)))
            .thenReturn(bookings);

        // Действие
        List<Booking> result = bookingService.getOwnerBookings(owner.getId(), "CURRENT");

        // Проверка
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void getOwnerBookings_WithStatePast_ShouldReturnPastBookings() {
        // Подготовка
        List<Booking> bookings = Arrays.asList(booking);
        when(userService.getUserById(owner.getId())).thenReturn(owner);
        when(bookingRepository.findPastBookingsByOwner(eq(owner), any(LocalDateTime.class)))
            .thenReturn(bookings);

        // Действие
        List<Booking> result = bookingService.getOwnerBookings(owner.getId(), "PAST");

        // Проверка
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void getOwnerBookings_WithStateFuture_ShouldReturnFutureBookings() {
        // Подготовка
        List<Booking> bookings = Arrays.asList(booking);
        when(userService.getUserById(owner.getId())).thenReturn(owner);
        when(bookingRepository.findFutureBookingsByOwner(eq(owner), any(LocalDateTime.class)))
            .thenReturn(bookings);

        // Действие
        List<Booking> result = bookingService.getOwnerBookings(owner.getId(), "FUTURE");

        // Проверка
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void getOwnerBookings_WithStateWaiting_ShouldReturnWaitingBookings() {
        // Подготовка
        List<Booking> bookings = Arrays.asList(booking);
        when(userService.getUserById(owner.getId())).thenReturn(owner);
        when(bookingRepository.findBookingsByOwnerAndStatus(eq(owner), eq(BookingStatus.WAITING)))
            .thenReturn(bookings);

        // Действие
        List<Booking> result = bookingService.getOwnerBookings(owner.getId(), "WAITING");

        // Проверка
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void getOwnerBookings_WithStateRejected_ShouldReturnRejectedBookings() {
        // Подготовка
        Booking rejectedBooking = new Booking(
            booking.getId(),
            booking.getStart(),
            booking.getEnd(),
            booking.getItem(),
            booking.getBooker(),
            BookingStatus.REJECTED
        );

        List<Booking> bookings = Arrays.asList(rejectedBooking);
        when(userService.getUserById(owner.getId())).thenReturn(owner);
        when(bookingRepository.findBookingsByOwnerAndStatus(eq(owner), eq(BookingStatus.REJECTED)))
            .thenReturn(bookings);

        // Действие
        List<Booking> result = bookingService.getOwnerBookings(owner.getId(), "REJECTED");

        // Проверка
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(rejectedBooking.getId(), result.get(0).getId());
        assertEquals(BookingStatus.REJECTED, result.get(0).getStatus());
    }

    @Test
    void getOwnerBookings_WithInvalidState_ShouldThrowValidationException() {
        // Подготовка
        when(userService.getUserById(owner.getId())).thenReturn(owner);

        // Действие и проверка
        ValidationException exception = assertThrows(ValidationException.class, () ->
            bookingService.getOwnerBookings(owner.getId(), "INVALID")
        );

        assertTrue(exception.getMessage().contains("Неизвестное состояние: INVALID"));
    }
}