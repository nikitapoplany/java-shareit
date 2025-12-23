package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс для преобразования между Item и ItemDto.
 */
public class ItemMapper {

    /**
     * Преобразует Item в ItemDto.
     *
     * @param item объект вещи
     * @return объект DTO вещи
     */
    public static ItemDto toItemDto(Item item) {
        ItemDto itemDto = new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwner() != null ? item.getOwner().getId() : null,
                item.getRequest() != null ? item.getRequest().getId() : null
        );
        
        return itemDto;
    }

    /**
     * Преобразует ItemDto в Item.
     *
     * @param itemDto объект DTO вещи
     * @return объект вещи
     */
    public static Item toItem(ItemDto itemDto) {
        User owner = null;
        if (itemDto.getOwnerId() != null) {
            owner = new User();
            owner.setId(itemDto.getOwnerId());
        }

        ItemRequest request = null;
        if (itemDto.getRequestId() != null) {
            request = new ItemRequest();
            request.setId(itemDto.getRequestId());
        }

        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner,
                request
        );
    }

    /**
     * Преобразует ItemDto в Item с указанным владельцем.
     *
     * @param itemDto объект DTO вещи
     * @param owner   владелец вещи
     * @return объект вещи
     */
    public static Item toItem(ItemDto itemDto, User owner) {
        ItemRequest request = null;
        if (itemDto.getRequestId() != null) {
            request = new ItemRequest();
            request.setId(itemDto.getRequestId());
        }

        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner,
                request
        );
    }

    /**
     * Преобразует список Item в список ItemDto.
     *
     * @param items список объектов вещей
     * @return список объектов DTO вещей
     */
    public static List<ItemDto> toItemDtoList(List<Item> items) {
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}