package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * Преобразует список User в список UserDto.
     *
     * @param users список объектов пользователей
     * @return список объектов DTO пользователей
     */
    public static List<UserDto> toUserDtoList(List<User> users) {
        return users.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }
}