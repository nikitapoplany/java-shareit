package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MissingRequestHeaderException;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.service.UserService;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты для {@link ErrorHandler}
 */
@WebMvcTest(UserController.class)
class ErrorHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    /**
     * Тест на обработку исключения NotFoundException.
     * Проверяет, что возвращается статус 404 и сообщение об ошибке.
     */
    @Test
    void handleNotFoundException_ShouldReturnNotFound() throws Exception {
        // Подготовка
        when(userService.getUserById(anyLong()))
                .thenThrow(new NotFoundException("Пользователь с ID 999 не найден"));

        // Действие и проверка
        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Пользователь с ID 999 не найден")));
    }

    /**
     * Тест на обработку исключения ValidationException.
     * Проверяет, что возвращается статус 400 и сообщение об ошибке.
     */
    @Test
    void handleValidationException_ShouldReturnBadRequest() throws Exception {
        // Подготовка
        when(userService.getUserById(anyLong()))
                .thenThrow(new ValidationException("Некорректный формат данных"));

        // Действие и проверка
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Некорректный формат данных")));
    }

    /**
     * Тест на обработку исключения ConflictException.
     * Проверяет, что возвращается статус 409 и сообщение об ошибке.
     */
    @Test
    void handleConflictException_ShouldReturnConflict() throws Exception {
        // Подготовка
        when(userService.getUserById(anyLong()))
                .thenThrow(new ConflictException("Пользователь с таким email уже существует"));

        // Действие и проверка
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Пользователь с таким email уже существует")));
    }

    /**
     * Тест на обработку исключения отсутствия обязательного заголовка.
     * Проверяет, что при запросе без обязательного заголовка возвращается статус 400.
     */
    @Test
    void handleMissingHeader_ShouldReturnBadRequest() throws Exception {
        // Подготовка - используем другой подход для тестирования обработки исключения
        String errorMessage = "Отсутствует обязательный заголовок: X-Sharer-User-Id";
        when(userService.getUserById(anyLong()))
                .thenAnswer(invocation -> {
                    throw new MissingRequestHeaderException("X-Sharer-User-Id", null) {
                        @Override
                        public String getMessage() {
                            return errorMessage;
                        }
                    };
                });

        // Действие и проверка
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is(errorMessage)));
    }

    /**
     * Тест на обработку непредвиденного исключения.
     * Проверяет, что возвращается статус 500 и сообщение об ошибке.
     */
    @Test
    void handleException_ShouldReturnInternalServerError() throws Exception {
        // Подготовка
        when(userService.getUserById(anyLong()))
                .thenThrow(new RuntimeException("Непредвиденная ошибка"));

        // Действие и проверка
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }
}