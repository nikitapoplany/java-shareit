package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.Booking.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Тесты для {@link ItemServiceImpl}
 */
@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        owner = new User(1L, "Иван Иванов", "ivan@example.com");
        booker = new User(2L, "Петр Петров", "petr@example.com");
        item = new Item(1L, "Дрель", "Электрическая дрель", true, owner, null);
    }

    /**
     * Тест на создание вещи с корректными данными.
     * Проверяет, что вещь успешно создается и ей присваивается ID.
     */
    @Test
    void createItem_WithValidData_ShouldCreateItem() {
        // Подготовка
        when(userService.getUserById(anyLong())).thenReturn(owner);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

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

        // Создаем обновленную версию вещи для возврата из mock
        Item updatedItem = new Item(1L, "Перфоратор", "Мощный перфоратор", false, owner, null);

        // Мокируем поиск вещи по ID
        when(itemRepository.findById(existingItem.getId())).thenReturn(java.util.Optional.of(existingItem));

        // Мокируем сохранение обновленной вещи
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        // Действие
        Item result = itemService.updateItem(owner.getId(), existingItem.getId(), updatedItemData);

        // Проверка
        assertEquals(existingItem.getId(), result.getId());
        assertEquals("Перфоратор", result.getName());
        assertEquals("Мощный перфоратор", result.getDescription());
        assertFalse(result.getAvailable());
        assertEquals(owner, result.getOwner());
    }

    /**
     * Тест на обновление вещи с частичными данными.
     * Проверяет, что обновляются только переданные поля.
     */
    @Test
    void updateItem_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        // Подготовка
        Item existingItem = new Item(1L, "Дрель", "Электрическая дрель", true, owner, null);

        // Создаем обновленные версии вещи для каждого этапа обновления
        Item afterNameUpdate = new Item(1L, "Перфоратор", "Электрическая дрель", true, owner, null);
        Item afterDescriptionUpdate = new Item(1L, "Перфоратор", "Мощный перфоратор", true, owner, null);
        Item afterAvailableUpdate = new Item(1L, "Перфоратор", "Мощный перфоратор", false, owner, null);

        // Мокируем поиск вещи по ID
        when(itemRepository.findById(existingItem.getId())).thenReturn(java.util.Optional.of(existingItem));

        // Мокируем сохранение для всех случаев
        when(itemRepository.save(any(Item.class))).thenReturn(afterNameUpdate, afterDescriptionUpdate, afterAvailableUpdate);

        // Обновляем только название
        Item nameUpdate = new Item(null, "Перфоратор", null, null, null, null);
        Item updatedItem1 = itemService.updateItem(owner.getId(), existingItem.getId(), nameUpdate);

        // Проверка
        assertEquals(existingItem.getId(), updatedItem1.getId());
        assertEquals("Перфоратор", updatedItem1.getName());
        assertEquals("Электрическая дрель", updatedItem1.getDescription());
        assertTrue(updatedItem1.getAvailable());

        // Обновляем только описание
        Item descriptionUpdate = new Item(null, null, "Мощный перфоратор", null, null, null);
        Item updatedItem2 = itemService.updateItem(owner.getId(), existingItem.getId(), descriptionUpdate);

        // Проверка
        assertEquals(existingItem.getId(), updatedItem2.getId());
        assertEquals("Перфоратор", updatedItem2.getName());
        assertEquals("Мощный перфоратор", updatedItem2.getDescription());
        assertTrue(updatedItem2.getAvailable());

        // Обновляем только статус доступности
        Item availableUpdate = new Item(null, null, null, false, null, null);
        Item updatedItem3 = itemService.updateItem(owner.getId(), existingItem.getId(), availableUpdate);

        // Проверка
        assertEquals(existingItem.getId(), updatedItem3.getId());
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

        // Мокируем поиск вещи по ID
        when(itemRepository.findById(existingItem.getId())).thenReturn(java.util.Optional.of(existingItem));

        Item updatedItemData = new Item(null, "Перфоратор", null, null, null, null);

        // Действие и проверка
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.updateItem(anotherUser.getId(), existingItem.getId(), updatedItemData)
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
        Long itemId = 1L;
        when(itemRepository.findById(itemId)).thenReturn(java.util.Optional.of(item));

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

        Item item1 = new Item(1L, "Дрель", "Электрическая дрель", true, owner, null);
        Item item2 = new Item(2L, "Отвертка", "Крестовая отвертка", true, owner, null);

        List<Item> items = Arrays.asList(item1, item2);
        when(itemRepository.findByOwnerOrderById(owner)).thenReturn(items);

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
        Item item1 = new Item(1L, "Дрель", "Электрическая дрель", true, owner, null);
        Item item2 = new Item(2L, "Перфоратор", "Мощный перфоратор", true, owner, null);

        // Мокируем поиск по названию
        when(itemRepository.search("дрель")).thenReturn(List.of(item1));

        // Действие - поиск по названию
        List<Item> searchResults1 = itemService.searchItems("дрель");

        // Проверка
        assertEquals(1, searchResults1.size());
        assertEquals("Дрель", searchResults1.get(0).getName());

        // Мокируем поиск по описанию
        when(itemRepository.search("мощный")).thenReturn(List.of(item2));

        // Действие - поиск по описанию
        List<Item> searchResults2 = itemService.searchItems("мощный");

        // Проверка
        assertEquals(1, searchResults2.size());
        assertEquals("Перфоратор", searchResults2.get(0).getName());

        // Мокируем поиск недоступной вещи
        when(itemRepository.search("отвертка")).thenReturn(List.of());

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

    /**
     * Тест на создание комментария с корректными данными.
     * Проверяет, что комментарий успешно создается и возвращается DTO.
     */
    @Test
    void createComment_WithValidData_ShouldCreateComment() {
        // Подготовка
        CommentDto commentDto = new CommentDto(null, "Отличная дрель, спасибо!", null, null);
        Comment comment = new Comment(null, "Отличная дрель, спасибо!", item, booker, now);
        Comment savedComment = new Comment(1L, "Отличная дрель, спасибо!", item, booker, now);

        when(userService.getUserById(booker.getId())).thenReturn(booker);
        when(itemRepository.findById(item.getId())).thenReturn(java.util.Optional.of(item));
        when(bookingRepository.existsByItemAndBookerAndEndBeforeAndStatus(
                eq(item), eq(booker), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        // Действие
        CommentDto result = itemService.createComment(booker.getId(), item.getId(), commentDto);

        // Проверка
        assertNotNull(result);
        assertEquals(savedComment.getId(), result.getId());
        assertEquals(savedComment.getText(), result.getText());
        assertEquals(booker.getName(), result.getAuthorName());
        assertNotNull(result.getCreated());
    }

    /**
     * Тест на создание комментария пользователем, который не бронировал вещь.
     * Проверяет, что выбрасывается исключение ValidationException.
     */
    @Test
    void createComment_ByUserWhoHasNotBookedItem_ShouldThrowValidationException() {
        // Подготовка
        CommentDto commentDto = new CommentDto(null, "Отличная дрель, спасибо!", null, null);

        when(userService.getUserById(booker.getId())).thenReturn(booker);
        when(itemRepository.findById(item.getId())).thenReturn(java.util.Optional.of(item));
        when(bookingRepository.existsByItemAndBookerAndEndBeforeAndStatus(
                eq(item), eq(booker), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(false);

        // Действие и проверка
        ValidationException exception = assertThrows(ValidationException.class, () ->
            itemService.createComment(booker.getId(), item.getId(), commentDto)
        );

        // Проверка сообщения об ошибке
        assertTrue(exception.getMessage().contains("не может оставить комментарий"));
    }

    /**
     * Тест на получение комментариев к вещи.
     * Проверяет, что возвращается список комментариев.
     */
    @Test
    void getItemComments_ShouldReturnComments() {
        // Подготовка
        Comment comment1 = new Comment(1L, "Отличная дрель!", item, booker, now.minusDays(2));
        Comment comment2 = new Comment(2L, "Работает хорошо", item, booker, now.minusDays(1));
        List<Comment> comments = Arrays.asList(comment1, comment2);

        when(itemRepository.findById(item.getId())).thenReturn(java.util.Optional.of(item));
        when(commentRepository.findByItemOrderByCreatedDesc(item)).thenReturn(comments);

        // Действие
        List<CommentDto> result = itemService.getItemComments(item.getId());

        // Проверка
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(comment1.getId(), result.get(0).getId());
        assertEquals(comment1.getText(), result.get(0).getText());
        assertEquals(booker.getName(), result.get(0).getAuthorName());
        assertEquals(comment2.getId(), result.get(1).getId());
        assertEquals(comment2.getText(), result.get(1).getText());
        assertEquals(booker.getName(), result.get(1).getAuthorName());
    }

    /**
     * Тест на получение вещи с информацией о бронированиях и комментариях для владельца.
     * Проверяет, что возвращается DTO с информацией о бронированиях и комментариях.
     */
    @Test
    void getItemWithBookingsAndComments_ForOwner_ShouldReturnItemWithBookingsAndComments() {
        // Подготовка
        Comment comment = new Comment(1L, "Отличная дрель!", item, booker, now.minusDays(1));
        List<Comment> comments = Arrays.asList(comment);

        Booking lastBooking = new Booking(1L, now.minusDays(2), now.minusDays(1), item, booker, BookingStatus.APPROVED);
        Booking nextBooking = new Booking(2L, now.plusDays(1), now.plusDays(2), item, booker, BookingStatus.APPROVED);

        when(itemRepository.findById(item.getId())).thenReturn(java.util.Optional.of(item));
        when(commentRepository.findByItemOrderByCreatedDesc(item)).thenReturn(comments);
        when(bookingRepository.findFirstByItemAndEndBeforeOrderByEndDesc(eq(item), any(LocalDateTime.class)))
                .thenReturn(lastBooking);
        when(bookingRepository.findFirstByItemAndStartAfterOrderByStartAsc(eq(item), any(LocalDateTime.class)))
                .thenReturn(nextBooking);

        // Действие
        ItemDto result = itemService.getItemWithBookingsAndComments(item.getId(), owner.getId());

        // Проверка
        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());

        assertNotNull(result.getComments());
        assertEquals(1, result.getComments().size());
        assertEquals(comment.getId(), result.getComments().get(0).getId());
        assertEquals(comment.getText(), result.getComments().get(0).getText());

        assertNotNull(result.getLastBooking());
        assertEquals(lastBooking.getId(), result.getLastBooking().getId());

        assertNotNull(result.getNextBooking());
        assertEquals(nextBooking.getId(), result.getNextBooking().getId());
    }

    /**
     * Тест на получение вещи с информацией о бронированиях и комментариях для не владельца.
     * Проверяет, что возвращается DTO с комментариями, но без информации о бронированиях.
     */
    @Test
    void getItemWithBookingsAndComments_ForNonOwner_ShouldReturnItemWithCommentsOnly() {
        // Подготовка
        Comment comment = new Comment(1L, "Отличная дрель!", item, booker, now.minusDays(1));
        List<Comment> comments = Arrays.asList(comment);

        when(itemRepository.findById(item.getId())).thenReturn(java.util.Optional.of(item));
        when(commentRepository.findByItemOrderByCreatedDesc(item)).thenReturn(comments);

        // Действие
        ItemDto result = itemService.getItemWithBookingsAndComments(item.getId(), booker.getId());

        // Проверка
        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());

        assertNotNull(result.getComments());
        assertEquals(1, result.getComments().size());
        assertEquals(comment.getId(), result.getComments().get(0).getId());
        assertEquals(comment.getText(), result.getComments().get(0).getText());

        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }
}