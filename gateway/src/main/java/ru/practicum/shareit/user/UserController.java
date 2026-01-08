package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import jakarta.validation.Valid;

/**
 * Контроллер для работы с пользователями.
 */
@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;

    /**
     * Создает нового пользователя.
     *
     * @param userDto данные пользователя
     * @return созданный пользователь
     */
    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody @Valid UserDto userDto) {
        log.info("Creating user {}", userDto);
        return userClient.createUser(userDto);
    }

    /**
     * Обновляет данные пользователя.
     *
     * @param userId  идентификатор пользователя
     * @param userDto данные для обновления
     * @return обновленный пользователь
     */
    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable long userId, @RequestBody UserDto userDto) {
        log.info("Updating user {}, userId={}", userDto, userId);
        return userClient.updateUser(userId, userDto);
    }

    /**
     * Получает пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return пользователь
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(@PathVariable long userId) {
        log.info("Get user {}", userId);
        return userClient.getUserById(userId);
    }

    /**
     * Получает список всех пользователей.
     *
     * @return список пользователей
     */
    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.info("Get all users");
        return userClient.getAllUsers();
    }

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable long userId) {
        log.info("Delete user {}", userId);
        return userClient.deleteUser(userId);
    }
}