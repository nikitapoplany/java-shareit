package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты для {@link ItemController}
 */
@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private User owner;
    private Item item;
    private ItemDto itemDto;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Иван Иванов", "ivan@example.com");
        item = new Item(1L, "Дрель", "Электрическая дрель", true, owner, null);
        itemDto = new ItemDto(1L, "Дрель", "Электрическая дрель", true, owner.getId(), null);
    }

    /**
     * Тест на создание вещи с корректными данными.
     * Проверяет, что эндпоинт возвращает статус 200 и созданную вещь.
     */
    @Test
    void createItem_WithValidData_ShouldReturnCreatedItem() throws Exception {
        // Подготовка
        when(itemService.createItem(anyLong(), any(Item.class))).thenReturn(item);

        // Действие и проверка
        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Дрель")))
                .andExpect(jsonPath("$.description", is("Электрическая дрель")))
                .andExpect(jsonPath("$.available", is(true)))
                .andExpect(jsonPath("$.ownerId", is(1)));
    }

    /**
     * Тест на создание вещи с некорректными данными.
     * Проверяет, что эндпоинт возвращает статус 400 и сообщение об ошибке.
     */
    @Test
    void createItem_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Подготовка
        when(itemService.createItem(anyLong(), any(Item.class)))
                .thenThrow(new ValidationException("Название вещи не может быть пустым"));

        // Действие и проверка
        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Название вещи не может быть пустым")));
    }

    /**
     * Тест на создание вещи без указания пользователя.
     * Проверяет, что эндпоинт возвращает статус 400 и сообщение об ошибке.
     */
    @Test
    void createItem_WithoutUserId_ShouldReturnBadRequest() throws Exception {
        // Действие и проверка
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Тест на обновление вещи с корректными данными.
     * Проверяет, что эндпоинт возвращает статус 200 и обновленную вещь.
     */
    @Test
    void updateItem_WithValidData_ShouldReturnUpdatedItem() throws Exception {
        // Подготовка
        Item updatedItem = new Item(1L, "Перфоратор", "Мощный перфоратор", false, owner, null);
        ItemDto updatedItemDto = new ItemDto(1L, "Перфоратор", "Мощный перфоратор", false, owner.getId(), null);

        when(itemService.updateItem(anyLong(), anyLong(), any(Item.class))).thenReturn(updatedItem);

        // Действие и проверка
        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedItemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Перфоратор")))
                .andExpect(jsonPath("$.description", is("Мощный перфоратор")))
                .andExpect(jsonPath("$.available", is(false)))
                .andExpect(jsonPath("$.ownerId", is(1)));
    }

    /**
     * Тест на обновление несуществующей вещи.
     * Проверяет, что эндпоинт возвращает статус 404 и сообщение об ошибке.
     */
    @Test
    void updateItem_WithNonExistentItem_ShouldReturnNotFound() throws Exception {
        // Подготовка
        when(itemService.updateItem(anyLong(), anyLong(), any(Item.class)))
                .thenThrow(new NotFoundException("Вещь с ID 999 не найдена"));

        // Действие и проверка
        mockMvc.perform(patch("/items/999")
                        .header(USER_ID_HEADER, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Вещь с ID 999 не найдена")));
    }

    /**
     * Тест на обновление вещи не владельцем.
     * Проверяет, что эндпоинт возвращает статус 404 и сообщение об ошибке.
     */
    @Test
    void updateItem_ByNonOwner_ShouldReturnNotFound() throws Exception {
        // Подготовка
        when(itemService.updateItem(anyLong(), anyLong(), any(Item.class)))
                .thenThrow(new NotFoundException("Пользователь с ID 2 не является владельцем вещи с ID 1"));

        // Действие и проверка
        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Пользователь с ID 2 не является владельцем вещи с ID 1")));
    }

    /**
     * Тест на получение вещи по ID.
     * Проверяет, что эндпоинт возвращает статус 200 и вещь.
     */
    @Test
    void getItemById_WithExistingId_ShouldReturnItem() throws Exception {
        // Подготовка
        ItemDto itemDto = new ItemDto(1L, "Дрель", "Электрическая дрель", true, 1L, null);
        when(itemService.getItemWithBookingsAndComments(anyLong(), anyLong())).thenReturn(itemDto);

        // Действие и проверка
        mockMvc.perform(get("/items/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Дрель")))
                .andExpect(jsonPath("$.description", is("Электрическая дрель")))
                .andExpect(jsonPath("$.available", is(true)))
                .andExpect(jsonPath("$.ownerId", is(1)));
    }

    /**
     * Тест на получение несуществующей вещи.
     * Проверяет, что эндпоинт возвращает статус 404 и сообщение об ошибке.
     */
    @Test
    void getItemById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Подготовка
        when(itemService.getItemWithBookingsAndComments(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Вещь с ID 999 не найдена"));

        // Действие и проверка
        mockMvc.perform(get("/items/999")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Вещь с ID 999 не найдена")));
    }

    /**
     * Тест на получение всех вещей пользователя.
     * Проверяет, что эндпоинт возвращает статус 200 и список вещей.
     */
    @Test
    void getUserItems_ShouldReturnUserItems() throws Exception {
        // Подготовка
        Item item1 = new Item(1L, "Дрель", "Электрическая дрель", true, owner, null);
        Item item2 = new Item(2L, "Отвертка", "Крестовая отвертка", true, owner, null);
        List<Item> items = Arrays.asList(item1, item2);

        when(itemService.getUserItems(anyLong())).thenReturn(items);

        // Действие и проверка
        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Дрель")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Отвертка")));
    }

    /**
     * Тест на получение вещей несуществующего пользователя.
     * Проверяет, что эндпоинт возвращает статус 404 и сообщение об ошибке.
     */
    @Test
    void getUserItems_WithNonExistentUser_ShouldReturnNotFound() throws Exception {
        // Подготовка
        when(itemService.getUserItems(anyLong()))
                .thenThrow(new NotFoundException("Пользователь с ID 999 не найден"));

        // Действие и проверка
        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Пользователь с ID 999 не найден")));
    }

    /**
     * Тест на поиск вещей по тексту.
     * Проверяет, что эндпоинт возвращает статус 200 и список найденных вещей.
     */
    @Test
    void searchItems_ShouldReturnMatchingItems() throws Exception {
        // Подготовка
        Item item = new Item(1L, "Дрель", "Электрическая дрель", true, owner, null);
        List<Item> items = Collections.singletonList(item);

        when(itemService.searchItems(anyString())).thenReturn(items);

        // Действие и проверка
        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Дрель")))
                .andExpect(jsonPath("$[0].description", is("Электрическая дрель")))
                .andExpect(jsonPath("$[0].available", is(true)));
    }

    /**
     * Тест на поиск вещей с пустым текстом.
     * Проверяет, что эндпоинт возвращает статус 200 и пустой список.
     */
    @Test
    void searchItems_WithEmptyText_ShouldReturnEmptyList() throws Exception {
        // Подготовка
        when(itemService.searchItems("")).thenReturn(Collections.emptyList());

        // Действие и проверка
        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}