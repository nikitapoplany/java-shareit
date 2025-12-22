package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.User;

import java.util.List;

/**
 * Интерфейс сервиса для работы с пользователями.
 */
public interface UserService {

    /**
     * Создает нового пользователя.
     *
     * @param user данные пользователя
     * @return созданный пользователь
     */
    User createUser(User user);

    /**
     * Обновляет данные пользователя.
     *
     * @param userId идентификатор пользователя
     * @param user   данные для обновления
     * @return обновленный пользователь
     */
    User updateUser(Long userId, User user);

    /**
     * Получает пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return пользователь
     */
    User getUserById(Long userId);

    /**
     * Получает список всех пользователей.
     *
     * @return список пользователей
     */
    List<User> getAllUsers();

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     */
    void deleteUser(Long userId);
}