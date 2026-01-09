package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.Booking.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для {@link BookingRepository}
 */
@DataJpaTest
@ActiveProfiles("test")
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private User booker;
    private Item item;
    private Booking pastBooking;
    private Booking currentBooking;
    private Booking futureBooking;
    private Booking waitingBooking;
    private Booking rejectedBooking;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        // Создаем пользователей
        owner = userRepository.save(new User(null, "Owner", "owner@example.com"));
        booker = userRepository.save(new User(null, "Booker", "booker@example.com"));

        // Создаем вещь
        item = itemRepository.save(new Item(null, "Item", "Description", true, owner, null));

        // Создаем бронирования с разными статусами и датами
        pastBooking = bookingRepository.save(new Booking(
                null,
                now.minusDays(2),
                now.minusDays(1),
                item,
                booker,
                BookingStatus.APPROVED
        ));

        currentBooking = bookingRepository.save(new Booking(
                null,
                now.minusDays(1),
                now.plusDays(1),
                item,
                booker,
                BookingStatus.APPROVED
        ));

        futureBooking = bookingRepository.save(new Booking(
                null,
                now.plusDays(1),
                now.plusDays(2),
                item,
                booker,
                BookingStatus.APPROVED
        ));

        waitingBooking = bookingRepository.save(new Booking(
                null,
                now.plusDays(3),
                now.plusDays(4),
                item,
                booker,
                BookingStatus.WAITING
        ));

        rejectedBooking = bookingRepository.save(new Booking(
                null,
                now.plusDays(5),
                now.plusDays(6),
                item,
                booker,
                BookingStatus.REJECTED
        ));
    }

    @Test
    void findByBookerOrderByStartDesc_ShouldReturnAllBookerBookings() {
        // Действие
        List<Booking> bookings = bookingRepository.findByBookerOrderByStartDesc(booker);

        // Проверка
        assertEquals(5, bookings.size());
        assertTrue(bookings.contains(pastBooking));
        assertTrue(bookings.contains(currentBooking));
        assertTrue(bookings.contains(futureBooking));
        assertTrue(bookings.contains(waitingBooking));
        assertTrue(bookings.contains(rejectedBooking));
    }

    @Test
    void findCurrentBookingsByBooker_ShouldReturnCurrentBookings() {
        // Действие
        List<Booking> bookings = bookingRepository.findCurrentBookingsByBooker(booker, now);

        // Проверка
        assertEquals(1, bookings.size());
        assertEquals(currentBooking.getId(), bookings.get(0).getId());
    }

    @Test
    void findByBookerAndEndBeforeOrderByStartDesc_ShouldReturnPastBookings() {
        // Действие
        List<Booking> bookings = bookingRepository.findByBookerAndEndBeforeOrderByStartDesc(booker, now);

        // Проверка
        assertEquals(1, bookings.size());
        assertEquals(pastBooking.getId(), bookings.get(0).getId());
    }

    @Test
    void findByBookerAndStartAfterOrderByStartDesc_ShouldReturnFutureBookings() {
        // Действие
        List<Booking> bookings = bookingRepository.findByBookerAndStartAfterOrderByStartDesc(booker, now);

        // Проверка
        assertEquals(3, bookings.size());
        assertTrue(bookings.contains(futureBooking));
        assertTrue(bookings.contains(waitingBooking));
        assertTrue(bookings.contains(rejectedBooking));
    }

    @Test
    void findByBookerAndStatusOrderByStartDesc_ShouldReturnBookingsWithSpecificStatus() {
        // Действие - WAITING
        List<Booking> waitingBookings = bookingRepository.findByBookerAndStatusOrderByStartDesc(booker, BookingStatus.WAITING);

        // Проверка
        assertEquals(1, waitingBookings.size());
        assertEquals(waitingBooking.getId(), waitingBookings.get(0).getId());

        // Действие - REJECTED
        List<Booking> rejectedBookings = bookingRepository.findByBookerAndStatusOrderByStartDesc(booker, BookingStatus.REJECTED);

        // Проверка
        assertEquals(1, rejectedBookings.size());
        assertEquals(rejectedBooking.getId(), rejectedBookings.get(0).getId());
    }

    @Test
    void findBookingsByOwner_ShouldReturnAllOwnerBookings() {
        // Действие
        List<Booking> bookings = bookingRepository.findBookingsByOwner(owner);

        // Проверка
        assertEquals(5, bookings.size());
        assertTrue(bookings.contains(pastBooking));
        assertTrue(bookings.contains(currentBooking));
        assertTrue(bookings.contains(futureBooking));
        assertTrue(bookings.contains(waitingBooking));
        assertTrue(bookings.contains(rejectedBooking));
    }

    @Test
    void findCurrentBookingsByOwner_ShouldReturnCurrentOwnerBookings() {
        // Действие
        List<Booking> bookings = bookingRepository.findCurrentBookingsByOwner(owner, now);

        // Проверка
        assertEquals(1, bookings.size());
        assertEquals(currentBooking.getId(), bookings.get(0).getId());
    }

    @Test
    void findPastBookingsByOwner_ShouldReturnPastOwnerBookings() {
        // Действие
        List<Booking> bookings = bookingRepository.findPastBookingsByOwner(owner, now);

        // Проверка
        assertEquals(1, bookings.size());
        assertEquals(pastBooking.getId(), bookings.get(0).getId());
    }

    @Test
    void findFutureBookingsByOwner_ShouldReturnFutureOwnerBookings() {
        // Действие
        List<Booking> bookings = bookingRepository.findFutureBookingsByOwner(owner, now);

        // Проверка
        assertEquals(3, bookings.size());
        assertTrue(bookings.contains(futureBooking));
        assertTrue(bookings.contains(waitingBooking));
        assertTrue(bookings.contains(rejectedBooking));
    }

    @Test
    void findBookingsByOwnerAndStatus_ShouldReturnOwnerBookingsWithSpecificStatus() {
        // Действие - WAITING
        List<Booking> waitingBookings = bookingRepository.findBookingsByOwnerAndStatus(owner, BookingStatus.WAITING);

        // Проверка
        assertEquals(1, waitingBookings.size());
        assertEquals(waitingBooking.getId(), waitingBookings.get(0).getId());

        // Действие - REJECTED
        List<Booking> rejectedBookings = bookingRepository.findBookingsByOwnerAndStatus(owner, BookingStatus.REJECTED);

        // Проверка
        assertEquals(1, rejectedBookings.size());
        assertEquals(rejectedBooking.getId(), rejectedBookings.get(0).getId());
    }

    @Test
    void findLastBookingForItem_ShouldReturnLastCompletedBooking() {
        // Действие
        List<Booking> lastBookings = bookingRepository.findLastBookingForItem(item, now);

        // Проверка
        assertFalse(lastBookings.isEmpty());
        assertEquals(pastBooking.getId(), lastBookings.get(0).getId());
    }

    @Test
    void findNextBookingForItem_ShouldReturnNextBooking() {
        // Действие
        List<Booking> nextBookings = bookingRepository.findNextBookingForItem(item, now);

        // Проверка
        assertFalse(nextBookings.isEmpty());
        assertEquals(futureBooking.getId(), nextBookings.get(0).getId());
    }

    @Test
    void hasUserBookedItem_ShouldCheckIfUserHasCompletedBooking() {
        // Действие - существующее завершенное бронирование
        boolean exists = bookingRepository.hasUserBookedItem(item, booker, now);

        // Проверка
        assertTrue(exists);

        // Действие - несуществующее бронирование (другой пользователь)
        User anotherUser = userRepository.save(new User(null, "Another", "another@example.com"));
        boolean notExists = bookingRepository.hasUserBookedItem(item, anotherUser, now);

        // Проверка
        assertFalse(notExists);
    }
}