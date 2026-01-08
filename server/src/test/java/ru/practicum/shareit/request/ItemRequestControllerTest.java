package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {

    @Mock
    private ItemRequestService itemRequestService;

    @InjectMocks
    private ItemRequestController itemRequestController;

    private ItemRequestDto requestDto;
    private ItemRequestDto responseDto;
    private List<ItemRequestDto> requestDtos;
    private final Long userId = 1L;
    private final Long requestId = 1L;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");

        responseDto = new ItemRequestDto();
        responseDto.setId(requestId);
        responseDto.setDescription("Нужна дрель");
        responseDto.setRequestorId(userId);
        responseDto.setCreated(now);
        responseDto.setItems(Collections.emptyList());

        ItemRequestDto requestDto2 = new ItemRequestDto();
        requestDto2.setId(2L);
        requestDto2.setDescription("Нужен перфоратор");
        requestDto2.setRequestorId(2L);
        requestDto2.setCreated(now.minusDays(1));

        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Мощная дрель");
        itemDto.setAvailable(true);
        itemDto.setOwnerId(2L);
        itemDto.setRequestId(requestId);

        responseDto.setItems(Collections.singletonList(itemDto));

        requestDtos = Arrays.asList(responseDto, requestDto2);
    }

    @Test
    void createRequest_ShouldReturnCreatedRequest() {
        // Подготовка
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(requestDto.getDescription());

        ItemRequest createdRequest = new ItemRequest();
        createdRequest.setId(requestId);
        createdRequest.setDescription(requestDto.getDescription());
        createdRequest.setRequestor(null); // В тесте не важно
        createdRequest.setCreated(responseDto.getCreated());

        when(itemRequestService.createItemRequest(eq(userId), any(ItemRequest.class)))
                .thenReturn(createdRequest);

        // Действие
        ItemRequestDto result = itemRequestController.createRequest(userId, requestDto);

        // Проверка
        assertEquals(responseDto.getId(), result.getId());
        assertEquals(responseDto.getDescription(), result.getDescription());
    }

    @Test
    void getUserRequests_ShouldReturnUserRequests() {
        // Подготовка
        when(itemRequestService.getUserItemRequests(userId))
                .thenReturn(Collections.singletonList(responseDto));

        // Действие
        List<ItemRequestDto> result = itemRequestController.getUserRequests(userId);

        // Проверка
        assertEquals(1, result.size());
        assertEquals(responseDto.getId(), result.get(0).getId());
        assertEquals(responseDto.getDescription(), result.get(0).getDescription());
    }

    @Test
    void getAllRequests_ShouldReturnAllRequests() {
        // Подготовка
        int from = 0;
        int size = 10;
        when(itemRequestService.getAllItemRequests(userId, from, size))
                .thenReturn(requestDtos);

        // Действие
        List<ItemRequestDto> result = itemRequestController.getAllRequests(userId, from, size);

        // Проверка
        assertEquals(2, result.size());
        assertEquals(responseDto.getId(), result.get(0).getId());
        assertEquals(responseDto.getDescription(), result.get(0).getDescription());
    }

    @Test
    void getRequestById_ShouldReturnRequest() {
        // Подготовка
        when(itemRequestService.getItemRequestById(userId, requestId))
                .thenReturn(responseDto);

        // Действие
        ItemRequestDto result = itemRequestController.getRequestById(userId, requestId);

        // Проверка
        assertEquals(responseDto.getId(), result.getId());
        assertEquals(responseDto.getDescription(), result.getDescription());
        assertEquals(1, result.getItems().size());
        assertEquals("Дрель", result.getItems().get(0).getName());
    }
}