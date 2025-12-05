package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Реализация сервиса для работы с пользователями.
 */
@Service
public class UserServiceImpl implements UserService {
    private final Map<Long, User> users = new HashMap<>();
    private Long nextId = 1L;

    // Регулярное выражение для проверки формата email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    @Override
    public User createUser(User user) {
        // Проверка наличия email
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }

        // Проверка формата email
        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new ValidationException("Некорректный формат email: " + user.getEmail());
        }

        // Проверка на уникальность email
        validateEmailUniqueness(user.getEmail(), null);

        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(Long userId, User user) {
        // Проверка существования пользователя
        User existingUser = getUserById(userId);

        // Проверка формата email, если он передан
        if (user.getEmail() != null) {
            if (user.getEmail().isBlank()) {
                throw new ValidationException("Email не может быть пустым");
            }

            if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
                throw new ValidationException("Некорректный формат email: " + user.getEmail());
            }

            // Проверка на уникальность email, если он изменился
            if (!user.getEmail().equals(existingUser.getEmail())) {
                validateEmailUniqueness(user.getEmail(), userId);
            }
        }

        // Обновляем только переданные поля
        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }

        return existingUser;
    }

    @Override
    public User getUserById(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteUser(Long userId) {
        // Проверка существования пользователя
        getUserById(userId);
        users.remove(userId);
    }

    /**
     * Проверяет уникальность email среди пользователей.
     *
     * @param email проверяемый email
     * @param excludeUserId ID пользователя, которого нужно исключить из проверки (при обновлении)
     */
    private void validateEmailUniqueness(String email, Long excludeUserId) {
        for (User user : users.values()) {
            if (user.getEmail().equals(email) && (excludeUserId == null || !user.getId().equals(excludeUserId))) {
                throw new ConflictException("Пользователь с email " + email + " уже существует");
            }
        }
    }
}