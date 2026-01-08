package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Глобальный обработчик исключений.
 */
@RestControllerAdvice
public class ErrorHandler {

    /**
     * Обрабатывает исключения валидации.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(MethodArgumentNotValidException e) {
        return Map.of("error", "Ошибка валидации: " + e.getMessage());
    }

    /**
     * Обрабатывает исключения отсутствия обязательного заголовка.
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMissingHeader(MissingRequestHeaderException e) {
        return Map.of("error", "Отсутствует обязательный заголовок: " + e.getHeaderName());
    }

    /**
     * Обрабатывает исключения, связанные с отсутствием ресурса.
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundException(NotFoundException e) {
        return Map.of("error", e.getMessage());
    }

    /**
     * Обрабатывает исключения, связанные с конфликтами данных.
     */
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleConflictException(ConflictException e) {
        return Map.of("error", e.getMessage());
    }

    /**
     * Обрабатывает исключения, связанные с некорректными данными.
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(ValidationException e) {
        return Map.of("error", e.getMessage());
    }

    /**
     * Обрабатывает все остальные исключения.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleException(Exception e) {
        return Map.of("error", "Произошла непредвиденная ошибка: " + e.getMessage());
    }
}