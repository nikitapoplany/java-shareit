package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
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
    void findByBooker_ShouldReturnAllBookerBookings() {
        // Действие
        List<Booking> bookings = bookingRepository.findByBooker(booker, Sort.by(Sort.Direction.DESC, "start"));
        
        // Проверка
        assertEquals(5, bookings.size());
        assertTrue(bookings.contains(pastBooking));
        assertTrue(bookings.contains(currentBooking));
        assertTrue(bookings.contains(futureBooking));
        assertTrue(bookings.contains(waitingBooking));
        assertTrue(bookings.contains(rejectedBooking));
    }

    @Test
    void findByBookerAndStartBeforeAndEndAfter_ShouldReturnCurrentBookings() {
        // Действие
        List<Booking> bookings = bookingRepository.findByBookerAndStartBeforeAndEndAfter(
                booker, now, now, Sort.by(Sort.Direction.DESC, "start"));
        
        // Проверка
        assertEquals(1, bookings.size());
        assertEquals(currentBooking.getId(), bookings.get(0).getId());
    }

    @Test
    void findByBookerAndEndBefore_ShouldReturnPastBookings() {
        // Действие
        List<Booking> bookings = bookingRepository.findByBookerAndEndBefore(
                booker, now, Sort.by(Sort.Direction.DESC, "start"));
        
        // Проверка
        assertEquals(1, bookings.size());
        assertEquals(pastBooking.getId(), bookings.get(0).getId());
    }

    @Test
    void findByBookerAndStartAfter_ShouldReturnFutureBookings() {
        // Действие
        List<Booking> bookings = bookingRepository.findByBookerAndStartAfter(
                booker, now, Sort.by(Sort.Direction.DESC, "start"));
        
        // Проверка
        assertEquals(3, bookings.size());
        assertTrue(bookings.contains(futureBooking));
        assertTrue(bookings.contains(waitingBooking));
        assertTrue(bookings.contains(rejectedBooking));
    }

    @Test
    void findByBookerAndStatus_ShouldReturnBookingsWithSpecificStatus() {
        // Действие - WAITING
        List<Booking> waitingBookings = bookingRepository.findByBookerAndStatus(
                booker, BookingStatus.WAITING, Sort.by(Sort.Direction.DESC, "start"));
        
        // Проверка
        assertEquals(1, waitingBookings.size());
        assertEquals(waitingBooking.getId(), waitingBookings.get(0).getId());
        
        // Действие - REJECTED
        List<Booking> rejectedBookings = bookingRepository.findByBookerAndStatus(
                booker, BookingStatus.REJECTED, Sort.by(Sort.Direction.DESC, "start"));
        
        // Проверка
        assertEquals(1, rejectedBookings.size());
        assertEquals(rejectedBooking.getId(), rejectedBookings.get(0).getId());
    }

    @Test
    void findByItemOwner_ShouldReturnAllOwnerBookings() {
        // Действие
        List<Booking> bookings = bookingRepository.findByItemOwner(
                owner, Sort.by(Sort.Direction.DESC, "start"));
        
        // Проверка
        assertEquals(5, bookings.size());
        assertTrue(bookings.contains(pastBooking));
        assertTrue(bookings.contains(currentBooking));
        assertTrue(bookings.contains(futureBooking));
        assertTrue(bookings.contains(waitingBooking));
        assertTrue(bookings.contains(rejectedBooking));
    }

    @Test
    void findCurrentByItemOwner_ShouldReturnCurrentOwnerBookings() {
        // Действие
        List<Booking> bookings = bookingRepository.findCurrentByItemOwner(
                owner, now, Sort.by(Sort.Direction.DESC, "start"));
        
        // Проверка
        assertEquals(1, bookings.size());
        assertEquals(currentBooking.getId(), bookings.get(0).getId());
    }

    @Test
    void findPastByItemOwner_ShouldReturnPastOwnerBookings() {
        // Действие
        List<Booking> bookings = bookingRepository.findPastByItemOwner(
                owner, now, Sort.by(Sort.Direction.DESC, "start"));
        
        // Проверка
        assertEquals(1, bookings.size());
        assertEquals(pastBooking.getId(), bookings.get(0).getId());
    }

    @Test
    void findFutureByItemOwner_ShouldReturnFutureOwnerBookings() {
        // Действие
        List<Booking> bookings = bookingRepository.findFutureByItemOwner(
                owner, now, Sort.by(Sort.Direction.DESC, "start"));
        
        // Проверка
        assertEquals(3, bookings.size());
        assertTrue(bookings.contains(futureBooking));
        assertTrue(bookings.contains(waitingBooking));
        assertTrue(bookings.contains(rejectedBooking));
    }

    @Test
    void findByItemOwnerAndStatus_ShouldReturnOwnerBookingsWithSpecificStatus() {
        // Действие - WAITING
        List<Booking> waitingBookings = bookingRepository.findByItemOwnerAndStatus(
                owner, BookingStatus.WAITING, Sort.by(Sort.Direction.DESC, "start"));
        
        // Проверка
        assertEquals(1, waitingBookings.size());
        assertEquals(waitingBooking.getId(), waitingBookings.get(0).getId());
        
        // Действие - REJECTED
        List<Booking> rejectedBookings = bookingRepository.findByItemOwnerAndStatus(
                owner, BookingStatus.REJECTED, Sort.by(Sort.Direction.DESC, "start"));
        
        // Проверка
        assertEquals(1, rejectedBookings.size());
        assertEquals(rejectedBooking.getId(), rejectedBookings.get(0).getId());
    }

    @Test
    void findFirstByItemAndEndBeforeOrderByEndDesc_ShouldReturnLastCompletedBooking() {
        // Действие
        Booking lastBooking = bookingRepository.findFirstByItemAndEndBeforeOrderByEndDesc(item, now);
        
        // Проверка
        assertNotNull(lastBooking);
        assertEquals(pastBooking.getId(), lastBooking.getId());
    }

    @Test
    void findFirstByItemAndStartAfterOrderByStartAsc_ShouldReturnNextBooking() {
        // Действие
        Booking nextBooking = bookingRepository.findFirstByItemAndStartAfterOrderByStartAsc(item, now);
        
        // Проверка
        assertNotNull(nextBooking);
        assertEquals(futureBooking.getId(), nextBooking.getId());
    }

    @Test
    void existsByItemAndBookerAndEndBeforeAndStatus_ShouldCheckIfUserHasCompletedBooking() {
        // Действие - существующее завершенное бронирование
        boolean exists = bookingRepository.existsByItemAndBookerAndEndBeforeAndStatus(
                item, booker, now, BookingStatus.APPROVED);
        
        // Проверка
        assertTrue(exists);
        
        // Действие - несуществующее бронирование (другой пользователь)
        User anotherUser = userRepository.save(new User(null, "Another", "another@example.com"));
        boolean notExists = bookingRepository.existsByItemAndBookerAndEndBeforeAndStatus(
                item, anotherUser, now, BookingStatus.APPROVED);
        
        // Проверка
        assertFalse(notExists);
    }
}