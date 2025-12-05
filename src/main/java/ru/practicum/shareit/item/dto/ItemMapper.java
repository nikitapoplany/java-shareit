package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

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
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwner() != null ? item.getOwner().getId() : null,
                item.getRequest() != null ? item.getRequest().getId() : null
        );
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
}