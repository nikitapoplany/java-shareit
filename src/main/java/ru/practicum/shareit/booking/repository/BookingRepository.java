package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
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
     * Поиск бронирований по пользователю-букеру.
     *
     * @param booker пользователь, который бронирует вещь
     * @param sort   параметры сортировки
     * @return список бронирований пользователя
     */
    List<Booking> findByBooker(User booker, Sort sort);

    /**
     * Поиск текущих бронирований пользователя-букера.
     *
     * @param booker пользователь, который бронирует вещь
     * @param now    текущее время
     * @param sort   параметры сортировки
     * @return список текущих бронирований пользователя
     */
    List<Booking> findByBookerAndStartBeforeAndEndAfter(User booker, LocalDateTime now, LocalDateTime now1, Sort sort);

    /**
     * Поиск прошедших бронирований пользователя-букера.
     *
     * @param booker пользователь, который бронирует вещь
     * @param now    текущее время
     * @param sort   параметры сортировки
     * @return список прошедших бронирований пользователя
     */
    List<Booking> findByBookerAndEndBefore(User booker, LocalDateTime now, Sort sort);

    /**
     * Поиск будущих бронирований пользователя-букера.
     *
     * @param booker пользователь, который бронирует вещь
     * @param now    текущее время
     * @param sort   параметры сортировки
     * @return список будущих бронирований пользователя
     */
    List<Booking> findByBookerAndStartAfter(User booker, LocalDateTime now, Sort sort);

    /**
     * Поиск бронирований пользователя-букера по статусу.
     *
     * @param booker пользователь, который бронирует вещь
     * @param status статус бронирования
     * @param sort   параметры сортировки
     * @return список бронирований пользователя с указанным статусом
     */
    List<Booking> findByBookerAndStatus(User booker, BookingStatus status, Sort sort);

    /**
     * Поиск бронирований по владельцу вещи.
     *
     * @param owner владелец вещи
     * @param sort  параметры сортировки
     * @return список бронирований вещей владельца
     */
    @Query("select b from Booking b where b.item.owner = ?1")
    List<Booking> findByItemOwner(User owner, Sort sort);

    /**
     * Поиск текущих бронирований по владельцу вещи.
     *
     * @param owner владелец вещи
     * @param now   текущее время
     * @param sort  параметры сортировки
     * @return список текущих бронирований вещей владельца
     */
    @Query("select b from Booking b where b.item.owner = ?1 and b.start < ?2 and b.end > ?2")
    List<Booking> findCurrentByItemOwner(User owner, LocalDateTime now, Sort sort);

    /**
     * Поиск прошедших бронирований по владельцу вещи.
     *
     * @param owner владелец вещи
     * @param now   текущее время
     * @param sort  параметры сортировки
     * @return список прошедших бронирований вещей владельца
     */
    @Query("select b from Booking b where b.item.owner = ?1 and b.end < ?2")
    List<Booking> findPastByItemOwner(User owner, LocalDateTime now, Sort sort);

    /**
     * Поиск будущих бронирований по владельцу вещи.
     *
     * @param owner владелец вещи
     * @param now   текущее время
     * @param sort  параметры сортировки
     * @return список будущих бронирований вещей владельца
     */
    @Query("select b from Booking b where b.item.owner = ?1 and b.start > ?2")
    List<Booking> findFutureByItemOwner(User owner, LocalDateTime now, Sort sort);

    /**
     * Поиск бронирований по владельцу вещи и статусу.
     *
     * @param owner  владелец вещи
     * @param status статус бронирования
     * @param sort   параметры сортировки
     * @return список бронирований вещей владельца с указанным статусом
     */
    @Query("select b from Booking b where b.item.owner = ?1 and b.status = ?2")
    List<Booking> findByItemOwnerAndStatus(User owner, BookingStatus status, Sort sort);

    /**
     * Поиск последнего завершенного бронирования для вещи.
     *
     * @param item вещь
     * @param now  текущее время
     * @return последнее завершенное бронирование для вещи
     */
    Booking findFirstByItemAndEndBeforeOrderByEndDesc(Item item, LocalDateTime now);

    /**
     * Поиск ближайшего будущего бронирования для вещи.
     *
     * @param item вещь
     * @param now  текущее время
     * @return ближайшее будущее бронирование для вещи
     */
    Booking findFirstByItemAndStartAfterOrderByStartAsc(Item item, LocalDateTime now);

    /**
     * Проверка, бронировал ли пользователь вещь и завершилось ли бронирование.
     *
     * @param item   вещь
     * @param booker пользователь
     * @param now    текущее время
     * @return true, если пользователь бронировал вещь и бронирование завершилось
     */
    boolean existsByItemAndBookerAndEndBeforeAndStatus(Item item, User booker, LocalDateTime now, BookingStatus status);
}