package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.Booking.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы с бронированиями.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    /**
     * Поиск бронирований по пользователю, отсортированных по дате начала (в порядке убывания).
     *
     * @param booker пользователь, создавший бронирование
     * @return список бронирований
     */
    List<Booking> findByBookerOrderByStartDesc(User booker);

    /**
     * Поиск текущих бронирований пользователя.
     *
     * @param booker пользователь, создавший бронирование
     * @param now    текущее время
     * @return список текущих бронирований
     */
    @Query("select b from Booking b " +
            "where b.booker = ?1 " +
            "and b.start <= ?2 " +
            "and b.end >= ?2 " +
            "order by b.start desc")
    List<Booking> findCurrentBookingsByBooker(User booker, LocalDateTime now);

    /**
     * Поиск прошедших бронирований пользователя.
     *
     * @param booker пользователь, создавший бронирование
     * @param now    текущее время
     * @return список прошедших бронирований
     */
    List<Booking> findByBookerAndEndBeforeOrderByStartDesc(User booker, LocalDateTime now);

    /**
     * Поиск будущих бронирований пользователя.
     *
     * @param booker пользователь, создавший бронирование
     * @param now    текущее время
     * @return список будущих бронирований
     */
    List<Booking> findByBookerAndStartAfterOrderByStartDesc(User booker, LocalDateTime now);

    /**
     * Поиск бронирований пользователя по статусу.
     *
     * @param booker пользователь, создавший бронирование
     * @param status статус бронирования
     * @return список бронирований с указанным статусом
     */
    List<Booking> findByBookerAndStatusOrderByStartDesc(User booker, BookingStatus status);

    /**
     * Поиск бронирований для вещей пользователя.
     *
     * @param owner владелец вещей
     * @return список бронирований
     */
    @Query("select b from Booking b " +
            "where b.item.owner = ?1 " +
            "order by b.start desc")
    List<Booking> findBookingsByOwner(User owner);

    /**
     * Поиск текущих бронирований для вещей пользователя.
     *
     * @param owner владелец вещей
     * @param now   текущее время
     * @return список текущих бронирований
     */
    @Query("select b from Booking b " +
            "where b.item.owner = ?1 " +
            "and b.start <= ?2 " +
            "and b.end >= ?2 " +
            "order by b.start desc")
    List<Booking> findCurrentBookingsByOwner(User owner, LocalDateTime now);

    /**
     * Поиск прошедших бронирований для вещей пользователя.
     *
     * @param owner владелец вещей
     * @param now   текущее время
     * @return список прошедших бронирований
     */
    @Query("select b from Booking b " +
            "where b.item.owner = ?1 " +
            "and b.end < ?2 " +
            "order by b.start desc")
    List<Booking> findPastBookingsByOwner(User owner, LocalDateTime now);

    /**
     * Поиск будущих бронирований для вещей пользователя.
     *
     * @param owner владелец вещей
     * @param now   текущее время
     * @return список будущих бронирований
     */
    @Query("select b from Booking b " +
            "where b.item.owner = ?1 " +
            "and b.start > ?2 " +
            "order by b.start desc")
    List<Booking> findFutureBookingsByOwner(User owner, LocalDateTime now);

    /**
     * Поиск бронирований для вещей пользователя по статусу.
     *
     * @param owner  владелец вещей
     * @param status статус бронирования
     * @return список бронирований с указанным статусом
     */
    @Query("select b from Booking b " +
            "where b.item.owner = ?1 " +
            "and b.status = ?2 " +
            "order by b.start desc")
    List<Booking> findBookingsByOwnerAndStatus(User owner, BookingStatus status);

    /**
     * Поиск последнего завершенного бронирования для вещи.
     *
     * @param item вещь
     * @param now  текущее время
     * @return последнее завершенное бронирование
     */
    @Query("select b from Booking b " +
            "where b.item = ?1 " +
            "and b.end < ?2 " +
            "and b.status = 'APPROVED' " +
            "order by b.end desc, b.id desc")
    List<Booking> findLastBookingForItem(Item item, LocalDateTime now);

    /**
     * Поиск ближайшего будущего бронирования для вещи.
     *
     * @param item вещь
     * @param now  текущее время
     * @return ближайшее будущее бронирование
     */
    @Query("select b from Booking b " +
            "where b.item = ?1 " +
            "and b.start > ?2 " +
            "and b.status = 'APPROVED' " +
            "order by b.start asc")
    List<Booking> findNextBookingForItem(Item item, LocalDateTime now);

    /**
     * Проверка, бронировал ли пользователь вещь.
     *
     * @param item   вещь
     * @param booker пользователь
     * @param now    текущее время
     * @return true, если пользователь бронировал вещь и бронирование завершено
     */
    @Query("select exists (select 1 from Booking b " +
            "where b.item = ?1 " +
            "and b.booker = ?2 " +
            "and b.end <= ?3 " +
            "and b.status = 'APPROVED')")
    boolean hasUserBookedItem(Item item, User booker, LocalDateTime now);

    /**
     * Проверка завершенного APPROVED-бронирования по идентификаторам.
     * Использует сравнение по ID, что исключает любые неоднозначности сравнения сущностей.
     */
    @Query("select (count(b) > 0) from Booking b " +
            "where b.item.id = ?1 " +
            "and b.booker.id = ?2 " +
            "and b.end <= ?3 " +
            "and b.status = 'APPROVED'")
    boolean hasUserCompletedApprovedBooking(Long itemId, Long bookerId, LocalDateTime now);

    /**
     * Последняя APPROVED-бронирование для пары (item, booker) по убыванию end.
     */
    @Query("select b from Booking b where b.item.id = ?1 and b.booker.id = ?2 and b.status = 'APPROVED' order by b.end desc")
    List<Booking> findLastApprovedBookingForPair(Long itemId, Long bookerId);
}