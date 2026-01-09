package ru.practicum.shareit.exception;

/**
 * Исключение, выбрасываемое при попытке доступа к несуществующему ресурсу.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}