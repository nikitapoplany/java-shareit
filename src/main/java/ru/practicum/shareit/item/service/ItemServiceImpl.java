package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.Booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с вещами.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public Item createItem(Long userId, Item item) {
        // Проверка существования пользователя
        User owner = userService.getUserById(userId);

        // Валидация обязательных полей
        validateItemFields(item);

        // Установка владельца
        item.setOwner(owner);

        // Сохранение
        return itemRepository.save(item);
    }

    /**
     * Валидирует обязательные поля вещи.
     *
     * @param item проверяемая вещь
     */
    private void validateItemFields(Item item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("Название вещи не может быть пустым");
        }

        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("Описание вещи не может быть пустым");
        }

        if (item.getAvailable() == null) {
            throw new ValidationException("Статус доступности вещи должен быть указан");
        }
    }

    @Override
    @Transactional
    public Item updateItem(Long userId, Long itemId, Item item) {
        // Проверка существования вещи
        Item existingItem = getItemById(itemId);

        // Проверка, что пользователь является владельцем вещи
        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не является владельцем вещи с ID " + itemId);
        }

        // Обновляем только переданные поля
        if (item.getName() != null) {
            existingItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            existingItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            existingItem.setAvailable(item.getAvailable());
        }

        return itemRepository.save(existingItem);
    }

    @Override
    public Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));
    }

    @Override
    public List<Item> getUserItems(Long userId) {
        // Проверка существования пользователя
        User owner = userService.getUserById(userId);

        return itemRepository.findByOwnerOrderById(owner);
    }

    @Override
    public List<Item> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        return itemRepository.search(text);
    }
    
    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        // Проверка существования пользователя
        User author = userService.getUserById(userId);
        
        // Проверка существования вещи
        Item item = getItemById(itemId);
        
        // Проверка, что пользователь брал вещь в аренду и аренда завершена
        LocalDateTime now = LocalDateTime.now();
        boolean hasBooking = bookingRepository.existsByItemAndBookerAndEndBeforeAndStatus(
                item, author, now, BookingStatus.APPROVED);
        
        if (!hasBooking) {
            throw new ValidationException("Пользователь с ID " + userId + 
                    " не может оставить комментарий к вещи с ID " + itemId + 
                    ", так как не брал её в аренду или аренда не завершена");
        }
        
        // Создание и сохранение комментария
        Comment comment = CommentMapper.toComment(commentDto, item, author);
        comment.setCreated(now);
        
        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(savedComment);
    }
    
    @Override
    public List<CommentDto> getItemComments(Long itemId) {
        // Проверка существования вещи
        Item item = getItemById(itemId);
        
        return commentRepository.findByItemOrderByCreatedDesc(item).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public ItemDto getItemWithBookingsAndComments(Long itemId, Long userId) {
        // Проверка существования вещи
        Item item = getItemById(itemId);
        
        // Создаем DTO вещи
        ItemDto itemDto = ItemMapper.toItemDto(item);
        
        // Получаем комментарии к вещи
        List<CommentDto> comments = getItemComments(itemId);
        itemDto.setComments(comments);
        
        // Если пользователь не является владельцем вещи, не добавляем информацию о бронированиях
        if (!item.getOwner().getId().equals(userId)) {
            return itemDto;
        }
        
        // Получаем последнее завершенное бронирование
        LocalDateTime now = LocalDateTime.now();
        Booking lastBooking = bookingRepository.findFirstByItemAndEndBeforeOrderByEndDesc(item, now);
        if (lastBooking != null) {
            itemDto.setLastBooking(BookingMapper.toBookingDtoShort(lastBooking));
        }
        
        // Получаем ближайшее будущее бронирование
        Booking nextBooking = bookingRepository.findFirstByItemAndStartAfterOrderByStartAsc(item, now);
        if (nextBooking != null) {
            itemDto.setNextBooking(BookingMapper.toBookingDtoShort(nextBooking));
        }
        
        return itemDto;
    }
}