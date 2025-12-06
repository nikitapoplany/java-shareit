package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Тесты для {@link ItemServiceImpl}
 */
@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Иван Иванов", "ivan@example.com");
        item = new Item(null, "Дрель", "Электрическая дрель", true, owner, null);
    }

    /**
     * Тест на создание вещи с корректными данными.
     * Проверяет, что вещь успешно создается и ей присваивается ID.
     */
    @Test
    void createItem_WithValidData_ShouldCreateItem() {
        // Подготовка
        when(userService.getUserById(anyLong())).thenReturn(owner);

        // Действие
        Item createdItem = itemService.createItem(owner.getId(), item);

        // Проверка
        assertNotNull(createdItem);
        assertNotNull(createdItem.getId());
        assertEquals("Дрель", createdItem.getName());
        assertEquals("Электрическая дрель", createdItem.getDescription());
        assertTrue(createdItem.getAvailable());
        assertEquals(owner, createdItem.getOwner());
    }

    /**
     * Тест на создание вещи с пустым названием.
     * Проверяет, что выбрасывается исключение ValidationException.
     */
    @Test
    void createItem_WithEmptyName_ShouldThrowValidationException() {
        // Подготовка
        when(userService.getUserById(anyLong())).thenReturn(owner);
        item.setName("");

        // Действие и проверка
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> itemService.createItem(owner.getId(), item)
        );

        // Проверка сообщения об ошибке
        assertTrue(exception.getMessage().contains("не может быть пустым"));
    }

    /**
     * Тест на создание вещи с пустым описанием.
     * Проверяет, что выбрасывается исключение ValidationException.
     */
    @Test
    void createItem_WithEmptyDescription_ShouldThrowValidationException() {
        // Подготовка
        when(userService.getUserById(anyLong())).thenReturn(owner);
        item.setDescription("");

        // Действие и проверка
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> itemService.createItem(owner.getId(), item)
        );

        // Проверка сообщения об ошибке
        assertTrue(exception.getMessage().contains("не может быть пустым"));
    }

    /**
     * Тест на создание вещи без указания статуса доступности.
     * Проверяет, что выбрасывается исключение ValidationException.
     */
    @Test
    void createItem_WithNullAvailable_ShouldThrowValidationException() {
        // Подготовка
        when(userService.getUserById(anyLong())).thenReturn(owner);
        item.setAvailable(null);

        // Действие и проверка
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> itemService.createItem(owner.getId(), item)
        );

        // Проверка сообщения об ошибке
        assertTrue(exception.getMessage().contains("должен быть указан"));
    }

    /**
     * Тест на создание вещи несуществующим пользователем.
     * Проверяет, что выбрасывается исключение NotFoundException.
     */
    @Test
    void createItem_WithNonExistentUser_ShouldThrowNotFoundException() {
        // Подготовка
        when(userService.getUserById(anyLong()))
                .thenThrow(new NotFoundException("Пользователь с ID 999 не найден"));

        // Действие и проверка
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.createItem(999L, item)
        );

        // Проверка сообщения об ошибке
        assertTrue(exception.getMessage().contains("не найден"));
    }

    /**
     * Тест на обновление вещи с корректными данными.
     * Проверяет, что данные вещи успешно обновляются.
     */
    @Test
    void updateItem_WithValidData_ShouldUpdateItem() {
        // Подготовка
        Item existingItem = new Item(1L, "Дрель", "Электрическая дрель", true, owner, null);
        Item updatedItemData = new Item(null, "Перфоратор", "Мощный перфоратор", false, null, null);

        // Добавляем существующую вещь в сервис
        when(userService.getUserById(anyLong())).thenReturn(owner);
        Item createdItem = itemService.createItem(owner.getId(), existingItem);

        // Действие
        Item updatedItem = itemService.updateItem(owner.getId(), createdItem.getId(), updatedItemData);

        // Проверка
        assertEquals(createdItem.getId(), updatedItem.getId());
        assertEquals("Перфоратор", updatedItem.getName());
        assertEquals("Мощный перфоратор", updatedItem.getDescription());
        assertFalse(updatedItem.getAvailable());
        assertEquals(owner, updatedItem.getOwner());
    }

    /**
     * Тест на обновление вещи с частичными данными.
     * Проверяет, что обновляются только переданные поля.
     */
    @Test
    void updateItem_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        // Подготовка
        Item existingItem = new Item(1L, "Дрель", "Электрическая дрель", true, owner, null);

        // Добавляем существующую вещь в сервис
        when(userService.getUserById(anyLong())).thenReturn(owner);
        Item createdItem = itemService.createItem(owner.getId(), existingItem);

        // Обновляем только название
        Item nameUpdate = new Item(null, "Перфоратор", null, null, null, null);
        Item updatedItem1 = itemService.updateItem(owner.getId(), createdItem.getId(), nameUpdate);

        // Проверка
        assertEquals(createdItem.getId(), updatedItem1.getId());
        assertEquals("Перфоратор", updatedItem1.getName());
        assertEquals("Электрическая дрель", updatedItem1.getDescription());
        assertTrue(updatedItem1.getAvailable());

        // Обновляем только описание
        Item descriptionUpdate = new Item(null, null, "Мощный перфоратор", null, null, null);
        Item updatedItem2 = itemService.updateItem(owner.getId(), createdItem.getId(), descriptionUpdate);

        // Проверка
        assertEquals(createdItem.getId(), updatedItem2.getId());
        assertEquals("Перфоратор", updatedItem2.getName());
        assertEquals("Мощный перфоратор", updatedItem2.getDescription());
        assertTrue(updatedItem2.getAvailable());

        // Обновляем только статус доступности
        Item availableUpdate = new Item(null, null, null, false, null, null);
        Item updatedItem3 = itemService.updateItem(owner.getId(), createdItem.getId(), availableUpdate);

        // Проверка
        assertEquals(createdItem.getId(), updatedItem3.getId());
        assertEquals("Перфоратор", updatedItem3.getName());
        assertEquals("Мощный перфоратор", updatedItem3.getDescription());
        assertFalse(updatedItem3.getAvailable());
    }

    /**
     * Тест на обновление несуществующей вещи.
     * Проверяет, что выбрасывается исключение NotFoundException.
     */
    @Test
    void updateItem_WithNonExistentItem_ShouldThrowNotFoundException() {
        // Подготовка
        Item updatedItemData = new Item(null, "Перфоратор", "Мощный перфоратор", false, null, null);

        // Действие и проверка
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.updateItem(owner.getId(), 999L, updatedItemData)
        );

        // Проверка сообщения об ошибке
        assertTrue(exception.getMessage().contains("не найдена"));
    }

    /**
     * Тест на обновление вещи не владельцем.
     * Проверяет, что выбрасывается исключение NotFoundException.
     */
    @Test
    void updateItem_ByNonOwner_ShouldThrowNotFoundException() {
        // Подготовка
        Item existingItem = new Item(1L, "Дрель", "Электрическая дрель", true, owner, null);
        User anotherUser = new User(2L, "Петр Петров", "petr@example.com");

        // Добавляем существующую вещь в сервис
        when(userService.getUserById(1L)).thenReturn(owner);
        Item createdItem = itemService.createItem(owner.getId(), existingItem);

        Item updatedItemData = new Item(null, "Перфоратор", null, null, null, null);

        // Действие и проверка
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.updateItem(anotherUser.getId(), createdItem.getId(), updatedItemData)
        );

        // Проверка сообщения об ошибке
        assertTrue(exception.getMessage().contains("не является владельцем"));
    }

    /**
     * Тест на получение вещи по ID.
     * Проверяет, что возвращается правильная вещь.
     */
    @Test
    void getItemById_WithExistingId_ShouldReturnItem() {
        // Подготовка
        when(userService.getUserById(anyLong())).thenReturn(owner);
        Item createdItem = itemService.createItem(owner.getId(), item);
        Long itemId = createdItem.getId();

        // Действие
        Item retrievedItem = itemService.getItemById(itemId);

        // Проверка
        assertEquals(itemId, retrievedItem.getId());
        assertEquals("Дрель", retrievedItem.getName());
        assertEquals("Электрическая дрель", retrievedItem.getDescription());
        assertTrue(retrievedItem.getAvailable());
        assertEquals(owner, retrievedItem.getOwner());
    }

    /**
     * Тест на получение несуществующей вещи.
     * Проверяет, что выбрасывается исключение NotFoundException.
     */
    @Test
    void getItemById_WithNonExistentId_ShouldThrowNotFoundException() {
        // Действие и проверка
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.getItemById(999L)
        );

        // Проверка сообщения об ошибке
        assertTrue(exception.getMessage().contains("не найдена"));
    }

    /**
     * Тест на получение всех вещей пользователя.
     * Проверяет, что возвращается список всех вещей пользователя.
     */
    @Test
    void getUserItems_ShouldReturnUserItems() {
        // Подготовка
        when(userService.getUserById(anyLong())).thenReturn(owner);

        Item item1 = new Item(null, "Дрель", "Электрическая дрель", true, owner, null);
        Item item2 = new Item(null, "Отвертка", "Крестовая отвертка", true, owner, null);

        itemService.createItem(owner.getId(), item1);
        itemService.createItem(owner.getId(), item2);

        // Действие
        List<Item> userItems = itemService.getUserItems(owner.getId());

        // Проверка
        assertEquals(2, userItems.size());
        assertTrue(userItems.stream().anyMatch(i -> "Дрель".equals(i.getName())));
        assertTrue(userItems.stream().anyMatch(i -> "Отвертка".equals(i.getName())));
    }

    /**
     * Тест на получение вещей несуществующего пользователя.
     * Проверяет, что выбрасывается исключение NotFoundException.
     */
    @Test
    void getUserItems_WithNonExistentUser_ShouldThrowNotFoundException() {
        // Подготовка
        when(userService.getUserById(999L))
                .thenThrow(new NotFoundException("Пользователь с ID 999 не найден"));

        // Действие и проверка
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.getUserItems(999L)
        );

        // Проверка сообщения об ошибке
        assertTrue(exception.getMessage().contains("не найден"));
    }

    /**
     * Тест на поиск вещей по тексту.
     * Проверяет, что возвращаются только доступные вещи, содержащие текст в названии или описании.
     */
    @Test
    void searchItems_ShouldReturnMatchingItems() {
        // Подготовка
        when(userService.getUserById(anyLong())).thenReturn(owner);

        Item item1 = new Item(null, "Дрель", "Электрическая дрель", true, owner, null);
        Item item2 = new Item(null, "Перфоратор", "Мощный перфоратор", true, owner, null);
        Item item3 = new Item(null, "Отвертка", "Крестовая отвертка", false, owner, null); // Недоступна

        itemService.createItem(owner.getId(), item1);
        itemService.createItem(owner.getId(), item2);
        itemService.createItem(owner.getId(), item3);

        // Действие - поиск по названию
        List<Item> searchResults1 = itemService.searchItems("дрель");

        // Проверка
        assertEquals(1, searchResults1.size());
        assertEquals("Дрель", searchResults1.get(0).getName());

        // Действие - поиск по описанию
        List<Item> searchResults2 = itemService.searchItems("мощный");

        // Проверка
        assertEquals(1, searchResults2.size());
        assertEquals("Перфоратор", searchResults2.get(0).getName());

        // Действие - поиск недоступной вещи
        List<Item> searchResults3 = itemService.searchItems("отвертка");

        // Проверка - недоступная вещь не должна быть найдена
        assertEquals(0, searchResults3.size());
    }

    /**
     * Тест на поиск вещей с пустым текстом.
     * Проверяет, что возвращается пустой список.
     */
    @Test
    void searchItems_WithEmptyText_ShouldReturnEmptyList() {
        // Подготовка
        when(userService.getUserById(anyLong())).thenReturn(owner);

        Item item = new Item(null, "Дрель", "Электрическая дрель", true, owner, null);
        itemService.createItem(owner.getId(), item);

        // Действие
        List<Item> searchResults = itemService.searchItems("");

        // Проверка
        assertTrue(searchResults.isEmpty());
    }
}