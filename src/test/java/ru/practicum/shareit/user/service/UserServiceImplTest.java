package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Тесты для {@link UserServiceImpl}
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    /**
     * Тест на создание пользователя с корректными данными.
     * Проверяет, что пользователь успешно создается и ему присваивается ID.
     */
    @Test
    void createUser_WithValidData_ShouldCreateUser() {
        // Подготовка
        User user = new User(null, "Иван Иванов", "ivan@example.com");
        User savedUser = new User(1L, "Иван Иванов", "ivan@example.com");
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Действие
        User createdUser = userService.createUser(user);

        // Проверка
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("Иван Иванов", createdUser.getName());
        assertEquals("ivan@example.com", createdUser.getEmail());
    }

    /**
     * Тест на создание пользователя с пустым email.
     * Проверяет, что выбрасывается исключение ValidationException.
     */
    @Test
    void createUser_WithEmptyEmail_ShouldThrowValidationException() {
        // Подготовка
        User user = new User(null, "Иван Иванов", "");

        // Действие и проверка
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.createUser(user)
        );

        // Проверка сообщения об ошибке
        assertTrue(exception.getMessage().contains("не может быть пустым"));
    }

    /**
     * Тест на создание пользователя с некорректным форматом email.
     * Проверяет, что выбрасывается исключение ValidationException.
     */
    @Test
    void createUser_WithInvalidEmail_ShouldThrowValidationException() {
        // Подготовка
        User user = new User(null, "Иван Иванов", "invalid-email");

        // Действие и проверка
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.createUser(user)
        );

        // Проверка сообщения об ошибке
        assertTrue(exception.getMessage().contains("Некорректный формат email"));
    }

    /**
     * Тест на создание пользователя с уже существующим email.
     * Проверяет, что выбрасывается исключение ConflictException.
     */
    @Test
    void createUser_WithDuplicateEmail_ShouldThrowConflictException() {
        // Подготовка
        User user1 = new User(null, "Иван Иванов", "ivan@example.com");
        User user2 = new User(null, "Петр Петров", "ivan@example.com");

        // Создаем первого пользователя
        userService.createUser(user1);

        // Действие и проверка для второго пользователя
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.createUser(user2)
        );

        // Проверка сообщения об ошибке
        assertTrue(exception.getMessage().contains("уже существует"));
    }

    /**
     * Тест на обновление пользователя с корректными данными.
     * Проверяет, что данные пользователя успешно обновляются.
     */
    @Test
    void updateUser_WithValidData_ShouldUpdateUser() {
        // Подготовка
        User user = new User(null, "Иван Иванов", "ivan@example.com");
        User createdUser = userService.createUser(user);
        Long userId = createdUser.getId();

        User updatedUserData = new User(null, "Иван Сидоров", "ivan.sidorov@example.com");

        // Действие
        User updatedUser = userService.updateUser(userId, updatedUserData);

        // Проверка
        assertEquals(userId, updatedUser.getId());
        assertEquals("Иван Сидоров", updatedUser.getName());
        assertEquals("ivan.sidorov@example.com", updatedUser.getEmail());
    }

    /**
     * Тест на обновление пользователя с частичными данными.
     * Проверяет, что обновляются только переданные поля.
     */
    @Test
    void updateUser_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        // Подготовка
        User user = new User(null, "Иван Иванов", "ivan@example.com");
        User createdUser = userService.createUser(user);
        Long userId = createdUser.getId();

        // Обновляем только имя
        User nameUpdate = new User(null, "Иван Сидоров", null);
        User updatedUser1 = userService.updateUser(userId, nameUpdate);

        // Проверка
        assertEquals(userId, updatedUser1.getId());
        assertEquals("Иван Сидоров", updatedUser1.getName());
        assertEquals("ivan@example.com", updatedUser1.getEmail());

        // Обновляем только email
        User emailUpdate = new User(null, null, "ivan.sidorov@example.com");
        User updatedUser2 = userService.updateUser(userId, emailUpdate);

        // Проверка
        assertEquals(userId, updatedUser2.getId());
        assertEquals("Иван Сидоров", updatedUser2.getName());
        assertEquals("ivan.sidorov@example.com", updatedUser2.getEmail());
    }

    /**
     * Тест на обновление несуществующего пользователя.
     * Проверяет, что выбрасывается исключение NotFoundException.
     */
    @Test
    void updateUser_WithNonExistentId_ShouldThrowNotFoundException() {
        // Подготовка
        User updatedUserData = new User(null, "Иван Сидоров", "ivan.sidorov@example.com");

        // Действие и проверка
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.updateUser(999L, updatedUserData)
        );

        // Проверка сообщения об ошибке
        assertTrue(exception.getMessage().contains("не найден"));
    }

    /**
     * Тест на обновление пользователя с некорректным форматом email.
     * Проверяет, что выбрасывается исключение ValidationException.
     */
    @Test
    void updateUser_WithInvalidEmail_ShouldThrowValidationException() {
        // Подготовка
        User user = new User(null, "Иван Иванов", "ivan@example.com");
        User createdUser = userService.createUser(user);
        Long userId = createdUser.getId();

        User updatedUserData = new User(null, null, "invalid-email");

        // Действие и проверка
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.updateUser(userId, updatedUserData)
        );

        // Проверка сообщения об ошибке
        assertTrue(exception.getMessage().contains("Некорректный формат email"));
    }

    /**
     * Тест на обновление пользователя с уже существующим email.
     * Проверяет, что выбрасывается исключение ConflictException.
     */
    @Test
    void updateUser_WithDuplicateEmail_ShouldThrowConflictException() {
        // Подготовка
        User user1 = new User(null, "Иван Иванов", "ivan@example.com");
        User user2 = new User(null, "Петр Петров", "petr@example.com");

        // Создаем пользователей
        userService.createUser(user1);
        User createdUser2 = userService.createUser(user2);
        Long user2Id = createdUser2.getId();

        // Пытаемся обновить email второго пользователя на email первого
        User updatedUserData = new User(null, null, "ivan@example.com");

        // Действие и проверка
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.updateUser(user2Id, updatedUserData)
        );

        // Проверка сообщения об ошибке
        assertTrue(exception.getMessage().contains("уже существует"));
    }

    /**
     * Тест на получение пользователя по ID.
     * Проверяет, что возвращается правильный пользователь.
     */
    @Test
    void getUserById_WithExistingId_ShouldReturnUser() {
        // Подготовка
        User user = new User(null, "Иван Иванов", "ivan@example.com");
        User createdUser = userService.createUser(user);
        Long userId = createdUser.getId();

        // Действие
        User retrievedUser = userService.getUserById(userId);

        // Проверка
        assertEquals(userId, retrievedUser.getId());
        assertEquals("Иван Иванов", retrievedUser.getName());
        assertEquals("ivan@example.com", retrievedUser.getEmail());
    }

    /**
     * Тест на получение несуществующего пользователя.
     * Проверяет, что выбрасывается исключение NotFoundException.
     */
    @Test
    void getUserById_WithNonExistentId_ShouldThrowNotFoundException() {
        // Действие и проверка
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getUserById(999L)
        );

        // Проверка сообщения об ошибке
        assertTrue(exception.getMessage().contains("не найден"));
    }

    /**
     * Тест на получение всех пользователей.
     * Проверяет, что возвращается список всех созданных пользователей.
     */
    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Подготовка
        User user1 = new User(null, "Иван Иванов", "ivan@example.com");
        User user2 = new User(null, "Петр Петров", "petr@example.com");

        userService.createUser(user1);
        userService.createUser(user2);

        // Действие
        List<User> users = userService.getAllUsers();

        // Проверка
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> "ivan@example.com".equals(u.getEmail())));
        assertTrue(users.stream().anyMatch(u -> "petr@example.com".equals(u.getEmail())));
    }

    /**
     * Тест на удаление пользователя.
     * Проверяет, что пользователь успешно удаляется.
     */
    @Test
    void deleteUser_WithExistingId_ShouldDeleteUser() {
        // Подготовка
        User user = new User(null, "Иван Иванов", "ivan@example.com");
        User createdUser = userService.createUser(user);
        Long userId = createdUser.getId();

        // Действие
        userService.deleteUser(userId);

        // Проверка
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getUserById(userId)
        );

        assertTrue(exception.getMessage().contains("не найден"));
    }

    /**
     * Тест на удаление несуществующего пользователя.
     * Проверяет, что выбрасывается исключение NotFoundException.
     */
    @Test
    void deleteUser_WithNonExistentId_ShouldThrowNotFoundException() {
        // Действие и проверка
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.deleteUser(999L)
        );

        // Проверка сообщения об ошибке
        assertTrue(exception.getMessage().contains("не найден"));
    }
}