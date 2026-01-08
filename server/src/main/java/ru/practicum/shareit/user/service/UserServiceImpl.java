package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Реализация сервиса для работы с пользователями.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    // Регулярное выражение для проверки формата email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    @Override
    @Transactional
    public User createUser(User user) {
        log.info("Создание пользователя: {}", user);

        // Проверка наличия email
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Попытка создания пользователя с пустым email");
            throw new ValidationException("Email не может быть пустым");
        }

        // Проверка формата email
        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            log.warn("Попытка создания пользователя с некорректным форматом email: {}", user.getEmail());
            throw new ValidationException("Некорректный формат email: " + user.getEmail());
        }

        try {
            User savedUser = userRepository.save(user);
            log.info("Пользователь успешно создан: {}", savedUser);
            return savedUser;
        } catch (DataIntegrityViolationException e) {
            log.warn("Конфликт при создании пользователя с email {}: {}", user.getEmail(), e.getMessage());
            throw new ConflictException("Пользователь с email " + user.getEmail() + " уже существует");
        }
    }

    @Override
    @Transactional
    public User updateUser(Long userId, User user) {
        log.info("Обновление пользователя с ID {}: {}", userId, user);

        // Проверка существования пользователя
        User existingUser = getUserById(userId);
        log.debug("Найден существующий пользователь: {}", existingUser);

        // Проверка формата email, если он передан
        if (user.getEmail() != null) {
            if (user.getEmail().isBlank()) {
                log.warn("Попытка обновления пользователя с пустым email, userId: {}", userId);
                throw new ValidationException("Email не может быть пустым");
            }

            if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
                log.warn("Попытка обновления пользователя с некорректным форматом email: {}, userId: {}",
                        user.getEmail(), userId);
                throw new ValidationException("Некорректный формат email: " + user.getEmail());
            }
        }

        // Обновляем только переданные поля
        if (user.getName() != null) {
            log.debug("Обновление имени пользователя с ID {}: {} -> {}", userId, existingUser.getName(), user.getName());
            existingUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            log.debug("Обновление email пользователя с ID {}: {} -> {}", userId, existingUser.getEmail(), user.getEmail());
            existingUser.setEmail(user.getEmail());
        }

        try {
            User updatedUser = userRepository.save(existingUser);
            log.info("Пользователь с ID {} успешно обновлен", userId);
            return updatedUser;
        } catch (DataIntegrityViolationException e) {
            log.warn("Конфликт при обновлении пользователя с ID {}, email {}: {}",
                    userId, existingUser.getEmail(), e.getMessage());
            throw new ConflictException("Пользователь с email " + existingUser.getEmail() + " уже существует");
        }
    }

    @Override
    public User getUserById(Long userId) {
        log.debug("Получение пользователя по ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", userId);
                    return new NotFoundException("Пользователь с ID " + userId + " не найден");
                });
        log.debug("Найден пользователь: {}", user);
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        log.info("Получение списка всех пользователей");
        List<User> users = userRepository.findAll();
        log.debug("Найдено {} пользователей", users.size());
        return users;
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с ID: {}", userId);
        // Проверка существования пользователя
        getUserById(userId);
        userRepository.deleteById(userId);
        log.info("Пользователь с ID {} успешно удален", userId);
    }
}