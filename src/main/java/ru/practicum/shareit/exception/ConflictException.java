package ru.practicum.shareit.exception;

/**
 * Исключение, выбрасываемое при конфликте данных (например, дублирование email).
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}