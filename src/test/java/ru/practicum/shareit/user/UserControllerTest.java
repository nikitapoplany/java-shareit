package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты для {@link UserController}
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    /**
     * Тест на создание пользователя с корректными данными.
     * Проверяет, что эндпоинт возвращает статус 200 и созданного пользователя.
     */
    @Test
    void createUser_WithValidData_ShouldReturnCreatedUser() throws Exception {
        // Подготовка
        UserDto userDto = new UserDto(null, "Иван Иванов", "ivan@example.com");
        User user = new User(null, "Иван Иванов", "ivan@example.com");
        User createdUser = new User(1L, "Иван Иванов", "ivan@example.com");

        when(userService.createUser(any(User.class))).thenReturn(createdUser);

        // Действие и проверка
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Иван Иванов")))
                .andExpect(jsonPath("$.email", is("ivan@example.com")));
    }

    /**
     * Тест на создание пользователя с некорректными данными.
     * Проверяет, что эндпоинт возвращает статус 400 и сообщение об ошибке.
     */
    @Test
    void createUser_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Подготовка
        UserDto userDto = new UserDto(null, "Иван Иванов", "invalid-email");

        when(userService.createUser(any(User.class)))
                .thenThrow(new ValidationException("Некорректный формат email: invalid-email"));

        // Действие и проверка
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Некорректный формат email: invalid-email")));
    }

    /**
     * Тест на создание пользователя с уже существующим email.
     * Проверяет, что эндпоинт возвращает статус 409 и сообщение об ошибке.
     */
    @Test
    void createUser_WithDuplicateEmail_ShouldReturnConflict() throws Exception {
        // Подготовка
        UserDto userDto = new UserDto(null, "Иван Иванов", "ivan@example.com");

        when(userService.createUser(any(User.class)))
                .thenThrow(new ConflictException("Пользователь с email ivan@example.com уже существует"));

        // Действие и проверка
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Пользователь с email ivan@example.com уже существует")));
    }

    /**
     * Тест на обновление пользователя с корректными данными.
     * Проверяет, что эндпоинт возвращает статус 200 и обновленного пользователя.
     */
    @Test
    void updateUser_WithValidData_ShouldReturnUpdatedUser() throws Exception {
        // Подготовка
        UserDto userDto = new UserDto(null, "Иван Сидоров", "ivan.sidorov@example.com");
        User updatedUser = new User(1L, "Иван Сидоров", "ivan.sidorov@example.com");

        when(userService.updateUser(anyLong(), any(User.class))).thenReturn(updatedUser);

        // Действие и проверка
        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Иван Сидоров")))
                .andExpect(jsonPath("$.email", is("ivan.sidorov@example.com")));
    }

    /**
     * Тест на обновление несуществующего пользователя.
     * Проверяет, что эндпоинт возвращает статус 404 и сообщение об ошибке.
     */
    @Test
    void updateUser_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Подготовка
        UserDto userDto = new UserDto(null, "Иван Сидоров", "ivan.sidorov@example.com");

        when(userService.updateUser(anyLong(), any(User.class)))
                .thenThrow(new NotFoundException("Пользователь с ID 999 не найден"));

        // Действие и проверка
        mockMvc.perform(patch("/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Пользователь с ID 999 не найден")));
    }

    /**
     * Тест на получение пользователя по ID.
     * Проверяет, что эндпоинт возвращает статус 200 и пользователя.
     */
    @Test
    void getUserById_WithExistingId_ShouldReturnUser() throws Exception {
        // Подготовка
        User user = new User(1L, "Иван Иванов", "ivan@example.com");

        when(userService.getUserById(1L)).thenReturn(user);

        // Действие и проверка
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Иван Иванов")))
                .andExpect(jsonPath("$.email", is("ivan@example.com")));
    }

    /**
     * Тест на получение несуществующего пользователя.
     * Проверяет, что эндпоинт возвращает статус 404 и сообщение об ошибке.
     */
    @Test
    void getUserById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Подготовка
        when(userService.getUserById(999L))
                .thenThrow(new NotFoundException("Пользователь с ID 999 не найден"));

        // Действие и проверка
        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Пользователь с ID 999 не найден")));
    }

    /**
     * Тест на получение всех пользователей.
     * Проверяет, что эндпоинт возвращает статус 200 и список пользователей.
     */
    @Test
    void getAllUsers_ShouldReturnAllUsers() throws Exception {
        // Подготовка
        User user1 = new User(1L, "Иван Иванов", "ivan@example.com");
        User user2 = new User(2L, "Петр Петров", "petr@example.com");
        List<User> users = Arrays.asList(user1, user2);

        when(userService.getAllUsers()).thenReturn(users);

        // Действие и проверка
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Иван Иванов")))
                .andExpect(jsonPath("$[0].email", is("ivan@example.com")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Петр Петров")))
                .andExpect(jsonPath("$[1].email", is("petr@example.com")));
    }

    /**
     * Тест на удаление пользователя.
     * Проверяет, что эндпоинт возвращает статус 200.
     */
    @Test
    void deleteUser_WithExistingId_ShouldReturnOk() throws Exception {
        // Действие и проверка
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        // Проверка, что метод сервиса был вызван
        Mockito.verify(userService).deleteUser(1L);
    }

    /**
     * Тест на удаление несуществующего пользователя.
     * Проверяет, что эндпоинт возвращает статус 404 и сообщение об ошибке.
     */
    @Test
    void deleteUser_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Подготовка
        doThrow(new NotFoundException("Пользователь с ID 999 не найден"))
                .when(userService).deleteUser(999L);

        // Действие и проверка
        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Пользователь с ID 999 не найден")));
    }
}