package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты для {@link BookingController}
 */
@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private User user;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingDto bookingDto;
    private BookingResponseDto bookingResponseDto;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @BeforeEach
    void setUp() {
        user = new User(1L, "User", "user@example.com");
        owner = new User(2L, "Owner", "owner@example.com");
        item = new Item(1L, "Item", "Description", true, owner, null);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        booking = new Booking(1L, start, end, item, user, Booking.BookingStatus.WAITING);

        bookingDto = new BookingDto(null, start, end, item.getId());

        UserDto userDto = new UserDto(user.getId(), user.getName(), user.getEmail());
        ItemDto itemDto = new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getAvailable(), owner.getId(), null);

        bookingResponseDto = new BookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                userDto,
                itemDto
        );
    }

    @Test
    void createBooking_WithValidData_ShouldReturnCreatedBooking() throws Exception {
        when(bookingService.createBooking(anyLong(), any(Booking.class)))
                .thenReturn(booking);

        mockMvc.perform(post("/bookings")
                .header(USER_ID_HEADER, user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId().intValue())))
                .andExpect(jsonPath("$.status", is(booking.getStatus().toString())));
    }

    @Test
    void createBooking_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        when(bookingService.createBooking(anyLong(), any(Booking.class)))
                .thenThrow(new ValidationException("Некорректные данные бронирования"));

        mockMvc.perform(post("/bookings")
                .header(USER_ID_HEADER, user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_WithoutUserId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking_WithValidData_ShouldReturnUpdatedBooking() throws Exception {
        Booking approvedBooking = new Booking(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem(),
                booking.getBooker(),
                Booking.BookingStatus.APPROVED
        );

        when(bookingService.approveBooking(eq(owner.getId()), eq(booking.getId()), eq(true)))
                .thenReturn(approvedBooking);

        mockMvc.perform(patch("/bookings/{bookingId}", booking.getId())
                .header(USER_ID_HEADER, owner.getId())
                .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId().intValue())))
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void approveBooking_ByNonOwner_ShouldReturnBadRequest() throws Exception {
        when(bookingService.approveBooking(eq(user.getId()), eq(booking.getId()), eq(true)))
                .thenThrow(new ValidationException("Пользователь не является владельцем вещи"));

        mockMvc.perform(patch("/bookings/{bookingId}", booking.getId())
                .header(USER_ID_HEADER, user.getId())
                .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingById_WithValidData_ShouldReturnBooking() throws Exception {
        when(bookingService.getBookingById(eq(user.getId()), eq(booking.getId())))
                .thenReturn(booking);

        mockMvc.perform(get("/bookings/{bookingId}", booking.getId())
                .header(USER_ID_HEADER, user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId().intValue())))
                .andExpect(jsonPath("$.status", is(booking.getStatus().toString())));
    }

    @Test
    void getBookingById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Бронирование не найдено"));

        mockMvc.perform(get("/bookings/{bookingId}", 999)
                .header(USER_ID_HEADER, user.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserBookings_ShouldReturnBookings() throws Exception {
        List<Booking> bookings = Arrays.asList(booking);
        when(bookingService.getUserBookings(eq(user.getId()), anyString()))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                .header(USER_ID_HEADER, user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(booking.getId().intValue())));
    }

    @Test
    void getUserBookings_WithInvalidState_ShouldReturnBadRequest() throws Exception {
        when(bookingService.getUserBookings(anyLong(), eq("INVALID")))
                .thenThrow(new ValidationException("Неизвестное состояние: INVALID"));

        mockMvc.perform(get("/bookings")
                .header(USER_ID_HEADER, user.getId())
                .param("state", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOwnerBookings_ShouldReturnBookings() throws Exception {
        List<Booking> bookings = Arrays.asList(booking);
        when(bookingService.getOwnerBookings(eq(owner.getId()), anyString()))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner")
                .header(USER_ID_HEADER, owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(booking.getId().intValue())));
    }

    @Test
    void getOwnerBookings_WithInvalidState_ShouldReturnBadRequest() throws Exception {
        when(bookingService.getOwnerBookings(anyLong(), eq("INVALID")))
                .thenThrow(new ValidationException("Неизвестное состояние: INVALID"));

        mockMvc.perform(get("/bookings/owner")
                .header(USER_ID_HEADER, owner.getId())
                .param("state", "INVALID"))
                .andExpect(status().isBadRequest());
    }
}