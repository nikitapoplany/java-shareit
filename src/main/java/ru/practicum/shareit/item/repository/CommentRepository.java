package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

/**
 * Репозиторий для работы с комментариями.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    /**
     * Поиск комментариев по вещи.
     *
     * @param item вещь
     * @return список комментариев к вещи
     */
    List<Comment> findByItemOrderByCreatedDesc(Item item);

    /**
     * Поиск комментариев по списку вещей.
     *
     * @param items список вещей
     * @return список комментариев к вещам
     */
    List<Comment> findByItemInOrderByCreatedDesc(List<Item> items);
}