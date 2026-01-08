package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования между ItemRequest и DTO.
 */
public class ItemRequestMapper {
    /**
     * Преобразует ItemRequest в ItemRequestDto.
     *
     * @param itemRequest объект запроса вещи
     * @return объект DTO запроса вещи
     */
    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        Long requestorId = itemRequest.getRequestor() != null ? itemRequest.getRequestor().getId() : null;
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                requestorId,
                itemRequest.getCreated(),
                new ArrayList<>()
        );
    }

    /**
     * Преобразует ItemRequest в ItemRequestDto с указанным списком вещей.
     *
     * @param itemRequest объект запроса вещи
     * @param items       список DTO вещей
     * @return объект DTO запроса вещи
     */
    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest, List<ItemDto> items) {
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getRequestor().getId(),
                itemRequest.getCreated(),
                items
        );
    }

    /**
     * Преобразует ItemRequestDto в ItemRequest.
     *
     * @param itemRequestDto объект DTO запроса вещи
     * @param requestor      пользователь, создавший запрос
     * @return объект запроса вещи
     */
    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User requestor) {
        return new ItemRequest(
                itemRequestDto.getId(),
                itemRequestDto.getDescription(),
                requestor,
                itemRequestDto.getCreated()
        );
    }

    /**
     * Преобразует список ItemRequest в список ItemRequestDto.
     *
     * @param itemRequests список объектов запросов вещей
     * @return список объектов DTO запросов вещей
     */
    public static List<ItemRequestDto> toItemRequestDtoList(List<ItemRequest> itemRequests) {
        return itemRequests.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }
}