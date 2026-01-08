package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для запроса вещи.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestDto {
    private Long id;

    @NotBlank(message = "Описание запроса не может быть пустым")
    private String description;

    private Long requestorId;
    private LocalDateTime created;
    private List<ItemDto> items;
}