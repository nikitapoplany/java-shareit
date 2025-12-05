package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.User;

/**
 * Класс для преобразования между User и UserDto.
 */
public class UserMapper {

    /**
     * Преобразует User в UserDto.
     *
     * @param user объект пользователя
     * @return объект DTO пользователя
     */
    public static UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    /**
     * Преобразует UserDto в User.
     *
     * @param userDto объект DTO пользователя
     * @return объект пользователя
     */
    public static User toUser(UserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getName(),
                userDto.getEmail()
        );
    }
}