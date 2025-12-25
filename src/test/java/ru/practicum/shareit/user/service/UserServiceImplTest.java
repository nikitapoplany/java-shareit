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
        User user = new User(null, "Петр Петров", "ivan@example.com");
        
        // Мокируем выброс исключения при попытке сохранить пользователя с дублирующимся email
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("Duplicate email"));

        // Действие и проверка
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.createUser(user)
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
        Long userId = 1L;
        User existingUser = new User(userId, "Иван Иванов", "ivan@example.com");
        User updatedUserData = new User(null, "Иван Сидоров", "ivan.sidorov@example.com");
        User updatedUser = new User(userId, "Иван Сидоров", "ivan.sidorov@example.com");
        
        // Мокируем поиск пользователя по ID
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        
        // Мокируем сохранение обновленного пользователя
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Действие
        User result = userService.updateUser(userId, updatedUserData);

        // Проверка
        assertEquals(userId, result.getId());
        assertEquals("Иван Сидоров", result.getName());
        assertEquals("ivan.sidorov@example.com", result.getEmail());
    }

    /**
     * Тест на обновление пользователя с частичными данными.
     * Проверяет, что обновляются только переданные поля.
     */
    @Test
    void updateUser_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        // Подготовка
        Long userId = 1L;
        User existingUser = new User(userId, "Иван Иванов", "ivan@example.com");
        User afterNameUpdate = new User(userId, "Иван Сидоров", "ivan@example.com");
        User afterEmailUpdate = new User(userId, "Иван Сидоров", "ivan.sidorov@example.com");
        
        // Мокируем поиск пользователя по ID для первого обновления
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        
        // Мокируем сохранение после обновления имени
        when(userRepository.save(any(User.class))).thenReturn(afterNameUpdate, afterEmailUpdate);

        // Обновляем только имя
        User nameUpdate = new User(null, "Иван Сидоров", null);
        User updatedUser1 = userService.updateUser(userId, nameUpdate);

        // Проверка
        assertEquals(userId, updatedUser1.getId());
        assertEquals("Иван Сидоров", updatedUser1.getName());
        assertEquals("ivan@example.com", updatedUser1.getEmail());

        // Мокируем поиск пользователя по ID для второго обновления
        when(userRepository.findById(userId)).thenReturn(Optional.of(afterNameUpdate));

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
        Long userId = 1L;
        User existingUser = new User(userId, "Иван Иванов", "ivan@example.com");
        User updatedUserData = new User(null, null, "invalid-email");
        
        // Мокируем поиск пользователя по ID
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

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
        Long userId = 2L;
        User existingUser = new User(userId, "Петр Петров", "petr@example.com");
        User updatedUserData = new User(null, null, "ivan@example.com");
        
        // Мокируем поиск пользователя по ID
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        
        // Мокируем выброс исключения при попытке сохранить пользователя с дублирующимся email
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("Duplicate email"));

        // Действие и проверка
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.updateUser(userId, updatedUserData)
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
        Long userId = 1L;
        User user = new User(userId, "Иван Иванов", "ivan@example.com");
        
        // Мокируем поиск пользователя по ID
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

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
        User user1 = new User(1L, "Иван Иванов", "ivan@example.com");
        User user2 = new User(2L, "Петр Петров", "petr@example.com");
        List<User> userList = Arrays.asList(user1, user2);
        
        // Мокируем получение всех пользователей
        when(userRepository.findAll()).thenReturn(userList);

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
        Long userId = 1L;
        User user = new User(userId, "Иван Иванов", "ivan@example.com");
        
        // Мокируем поиск пользователя по ID перед удалением
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // Действие
        userService.deleteUser(userId);
        
        // Мокируем поиск пользователя по ID после удаления
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

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