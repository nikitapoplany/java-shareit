package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
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
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    // Регулярное выражение для проверки формата email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    @Override
    @Transactional
    public User createUser(User user) {
        // Проверка наличия email
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }

        // Проверка формата email
        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new ValidationException("Некорректный формат email: " + user.getEmail());
        }

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Пользователь с email " + user.getEmail() + " уже существует");
        }
    }

    @Override
    @Transactional
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
        }

        // Обновляем только переданные поля
        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }

        try {
            return userRepository.save(existingUser);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Пользователь с email " + existingUser.getEmail() + " уже существует");
        }
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        // Проверка существования пользователя
        getUserById(userId);
        userRepository.deleteById(userId);
    }
}