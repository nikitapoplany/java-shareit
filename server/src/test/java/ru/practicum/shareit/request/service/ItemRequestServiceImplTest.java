package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserService userService;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User user;
    private User otherUser;
    private ItemRequest itemRequest;
    private Item item;
    private final Long userId = 1L;
    private final Long otherUserId = 2L;
    private final Long requestId = 1L;
    private final Long itemId = 1L;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(userId);
        user.setName("Пользователь");
        user.setEmail("user@example.com");

        otherUser = new User();
        otherUser.setId(otherUserId);
        otherUser.setName("Другой пользователь");
        otherUser.setEmail("other@example.com");

        itemRequest = new ItemRequest();
        itemRequest.setId(requestId);
        itemRequest.setDescription("Нужна дрель");
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());

        item = new Item();
        item.setId(itemId);
        item.setName("Дрель");
        item.setDescription("Мощная дрель");
        item.setAvailable(true);
        item.setOwner(otherUser);
        item.setRequest(itemRequest);
    }

    @Test
    void createItemRequest_WithValidData_ShouldCreateRequest() {
        // Подготовка
        ItemRequest requestToCreate = new ItemRequest();
        requestToCreate.setDescription("Нужна дрель");

        when(userService.getUserById(userId)).thenReturn(user);
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        // Действие
        ItemRequest result = itemRequestService.createItemRequest(userId, requestToCreate);

        // Проверка
        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals("Нужна дрель", result.getDescription());
        assertEquals(user, result.getRequestor());
        assertNotNull(result.getCreated());
    }

    @Test
    void createItemRequest_WithEmptyDescription_ShouldThrowException() {
        // Подготовка
        ItemRequest requestToCreate = new ItemRequest();
        requestToCreate.setDescription("");

        when(userService.getUserById(userId)).thenReturn(user);

        // Действие и проверка
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> itemRequestService.createItemRequest(userId, requestToCreate)
        );

        assertEquals("Описание запроса не может быть пустым", exception.getMessage());
    }

    @Test
    void createItemRequest_WithNonExistentUser_ShouldThrowException() {
        // Подготовка
        ItemRequest requestToCreate = new ItemRequest();
        requestToCreate.setDescription("Нужна дрель");

        when(userService.getUserById(userId)).thenThrow(new NotFoundException("Пользователь не найден"));

        // Действие и проверка
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.createItemRequest(userId, requestToCreate)
        );

        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void getUserItemRequests_WithValidUser_ShouldReturnRequests() {
        // Подготовка
        List<ItemRequest> requests = Collections.singletonList(itemRequest);
        List<Item> items = Collections.singletonList(item);

        when(userService.getUserById(userId)).thenReturn(user);
        when(itemRequestRepository.findByRequestorOrderByCreatedDesc(user)).thenReturn(requests);
        when(itemRepository.findByRequestInOrderById(requests)).thenReturn(items);

        // Действие
        List<ItemRequestDto> result = itemRequestService.getUserItemRequests(userId);

        // Проверка
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(requestId, result.get(0).getId());
        assertEquals("Нужна дрель", result.get(0).getDescription());
        assertEquals(userId, result.get(0).getRequestorId());
        assertEquals(1, result.get(0).getItems().size());
        assertEquals(itemId, result.get(0).getItems().get(0).getId());
    }

    @Test
    void getUserItemRequests_WithNoRequests_ShouldReturnEmptyList() {
        // Подготовка
        when(userService.getUserById(userId)).thenReturn(user);
        when(itemRequestRepository.findByRequestorOrderByCreatedDesc(user)).thenReturn(Collections.emptyList());

        // Действие
        List<ItemRequestDto> result = itemRequestService.getUserItemRequests(userId);

        // Проверка
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllItemRequests_WithValidParams_ShouldReturnRequests() {
        // Подготовка
        int from = 0;
        int size = 10;
        List<ItemRequest> requests = Collections.singletonList(itemRequest);
        List<Item> items = Collections.singletonList(item);
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"));

        when(userService.getUserById(otherUserId)).thenReturn(otherUser);
        when(itemRequestRepository.findByRequestorNot(eq(otherUser), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(requests));
        when(itemRepository.findByRequestInOrderById(requests)).thenReturn(items);

        // Действие
        List<ItemRequestDto> result = itemRequestService.getAllItemRequests(otherUserId, from, size);

        // Проверка
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(requestId, result.get(0).getId());
        assertEquals("Нужна дрель", result.get(0).getDescription());
        assertEquals(userId, result.get(0).getRequestorId());
        assertEquals(1, result.get(0).getItems().size());
        assertEquals(itemId, result.get(0).getItems().get(0).getId());
    }

    @Test
    void getAllItemRequests_WithInvalidPagination_ShouldThrowException() {
        // Подготовка
        int from = -1;
        int size = 10;

        when(userService.getUserById(otherUserId)).thenReturn(otherUser);

        // Действие и проверка
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> itemRequestService.getAllItemRequests(otherUserId, from, size)
        );

        assertEquals("Параметры пагинации должны быть положительными числами", exception.getMessage());
    }

    @Test
    void getItemRequestById_WithValidId_ShouldReturnRequest() {
        // Подготовка
        List<Item> items = Collections.singletonList(item);

        when(userService.getUserById(userId)).thenReturn(user);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findByRequestOrderById(itemRequest)).thenReturn(items);

        // Действие
        ItemRequestDto result = itemRequestService.getItemRequestById(userId, requestId);

        // Проверка
        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals("Нужна дрель", result.getDescription());
        assertEquals(userId, result.getRequestorId());
        assertEquals(1, result.getItems().size());
        assertEquals(itemId, result.getItems().get(0).getId());
    }

    @Test
    void getItemRequestById_WithNonExistentId_ShouldThrowException() {
        // Подготовка
        when(userService.getUserById(userId)).thenReturn(user);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // Действие и проверка
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.getItemRequestById(userId, requestId)
        );

        assertEquals("Запрос с ID " + requestId + " не найден", exception.getMessage());
    }
}