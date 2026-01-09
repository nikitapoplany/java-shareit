package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.List;

/**
 * Репозиторий для работы с запросами вещей.
 */
@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    /**
     * Поиск запросов по пользователю, создавшему запрос.
     *
     * @param requestor пользователь, создавший запрос
     * @return список запросов
     */
    List<ItemRequest> findByRequestorOrderByCreatedDesc(User requestor);

    /**
     * Поиск запросов, созданных другими пользователями.
     *
     * @param requestor пользователь, запросы которого нужно исключить
     * @return список запросов других пользователей
     */
    List<ItemRequest> findByRequestorNotOrderByCreatedDesc(User requestor);

    /**
     * Поиск запросов, созданных другими пользователями, с пагинацией.
     *
     * @param requestor пользователь, запросы которого нужно исключить
     * @param pageable  параметры пагинации
     * @return страница запросов других пользователей
     */
    Page<ItemRequest> findByRequestorNot(User requestor, Pageable pageable);
}