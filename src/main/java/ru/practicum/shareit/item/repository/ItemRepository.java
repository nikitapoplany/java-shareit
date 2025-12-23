package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.List;

/**
 * Репозиторий для работы с вещами.
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    /**
     * Поиск вещей по тексту в названии или описании.
     *
     * @param text текст для поиска
     * @return список вещей, содержащих текст в названии или описании
     */
    @Query("select i from Item i " +
            "where upper(i.name) like upper(concat('%', ?1, '%')) " +
            "or upper(i.description) like upper(concat('%', ?1, '%')) " +
            "and i.available = true")
    List<Item> search(String text);

    /**
     * Поиск вещей по владельцу.
     *
     * @param owner владелец вещей
     * @return список вещей, принадлежащих указанному владельцу
     */
    List<Item> findByOwnerOrderById(User owner);
}