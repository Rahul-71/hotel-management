package com.telus.hotel_management.service;

import com.telus.hotel_management.dto.ReservationRequestDto;
import com.telus.hotel_management.dto.ReservationUpdateDto;
import com.telus.hotel_management.entity.*;
import com.telus.hotel_management.exception.ResourceNotFoundException;
import com.telus.hotel_management.exception.UnauthorizedException;
import com.telus.hotel_management.repository.ReservationRepository;
import com.telus.hotel_management.repository.RoomRepository;
import com.telus.hotel_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {
    @InjectMocks
    private ReservationService reservationService;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    private User createMockUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private Room createMockRoom(Long id, Double pricePerNight, RoomStatus status) {
        Room room = new Room();
        room.setId(id);
        room.setPricePerNight(pricePerNight);
        room.setStatus(status);
        return room;
    }

    private Reservation createMockReservation(Long id, User user, Room room, LocalDate checkIn, LocalDate checkOut, double totalPrice, ReservationStatus status) {
        Reservation res = new Reservation();
        res.setId(id);
        res.setUser(user);
        res.setRoom(room);
        res.setCheckInDate(checkIn);
        res.setCheckOutDate(checkOut);
        res.setTotalPrice(totalPrice);
        res.setStatus(status);
        return res;
    }

    private UserDetailsImpl createMockUserDetails(Long id, String username, List<String> authorities) {
        User mockUser = createMockUser(id, username);
        mockUser.setRoles(authorities.stream().map(role -> new Role(RoleType.valueOf(role))).collect(Collectors.toSet()));
        return new UserDetailsImpl(mockUser);
    }

    @BeforeEach
    void setup() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createReservation_success() throws UnauthorizedException {
        ReservationRequestDto dto = new ReservationRequestDto();
        dto.setRoomId(1L);
        dto.setCheckInDate(LocalDate.now().plusDays(1));
        dto.setCheckOutDate(LocalDate.now().plusDays(3));

        UserDetailsImpl userDetails = createMockUserDetails(10L, "testuser", List.of("USER"));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        User user = createMockUser(10L, "testuser");
        Room room = createMockRoom(1L, 100.0, RoomStatus.AVAILABLE);
        Reservation saved = createMockReservation(50L, user, room, dto.getCheckInDate(), dto.getCheckOutDate(), 200.0, ReservationStatus.PENDING);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(reservationRepository.existsOverlappingReservation(any(), any(), any())).thenReturn(false);
        when(reservationRepository.save(any())).thenReturn(saved);

        Reservation result = reservationService.createReservation(dto);

        assertEquals(50L, result.getId());
        assertEquals(ReservationStatus.PENDING, result.getStatus());
        assertEquals(user, result.getUser());
        assertEquals(room, result.getRoom());

        verify(userRepository).findById(10L);
        verify(roomRepository).findById(1L);
        verify(reservationRepository).existsOverlappingReservation(any(), any(), any());
        verify(reservationRepository).save(any());
    }


    @Test
    void createReservation_roomNotAvailable() {
        var dto = new ReservationRequestDto();
        dto.setRoomId(1L);
        dto.setCheckInDate(LocalDate.now().plusDays(1));
        dto.setCheckOutDate(LocalDate.now().plusDays(3));

        var user = createMockUser(10L, "testuser");
        var room = createMockRoom(1L, 100.0, RoomStatus.OCCUPIED);

        when(authentication.getPrincipal()).thenReturn(createMockUserDetails(10L, "testuser", List.of("USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        var ex = assertThrows(RuntimeException.class, () -> reservationService.createReservation(dto));
        assertTrue(ex.getMessage().contains("Room is not available"));

        verify(reservationRepository, never()).save(any());
    }


    @Test
    void getReservationById_success() {
        Long reservationId = 1L;
        Reservation mockReservation = createMockReservation(reservationId, createMockUser(1L, "user"), createMockRoom(1L, 100.0, RoomStatus.AVAILABLE), LocalDate.now(), LocalDate.now().plusDays(1), 100.0, ReservationStatus.CONFIRMED);
        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.of(mockReservation));

        Reservation foundReservation = reservationService.getReservationById(reservationId);
        assertNotNull(foundReservation);
        assertEquals(reservationId, foundReservation.getId());
        assertEquals(mockReservation.getStatus(), foundReservation.getStatus());

        verify(reservationRepository).findById(eq(reservationId));
    }


    @Test
    void getReservationById_notFound() {
        Long reservationId = 99L;

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reservationService.getReservationById(reservationId);
        });

        assertFalse(exception.getMessage().contains("Reservation not found with ID : " + reservationId));
        verify(reservationRepository).findById(eq(reservationId));
    }

    @Test
    void getMyReservations_success() throws UnauthorizedException {
        var user = createMockUser(10L, "testuser");
        var reservations = List.of(
                createMockReservation(1L, user, createMockRoom(101L, 100.0, RoomStatus.AVAILABLE), LocalDate.now(), LocalDate.now().plusDays(1), 100.0, ReservationStatus.CONFIRMED),
                createMockReservation(2L, user, createMockRoom(102L, 150.0, RoomStatus.AVAILABLE), LocalDate.now().plusDays(2), LocalDate.now().plusDays(3), 150.0, ReservationStatus.PENDING)
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(createMockUserDetails(10L, "testuser", List.of("USER")));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(reservationRepository.findByUser(user)).thenReturn(reservations);

        var result = reservationService.getMyReservations();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());

        verify(reservationRepository).findByUser(user);
    }


    @Test
    void getReservationsByGuestId_success() {
        Long guestId = 5L;
        var guest = createMockUser(guestId, "guestuser");
        var reservations = List.of(
                createMockReservation(3L, guest, createMockRoom(201L, 200.0, RoomStatus.AVAILABLE), LocalDate.now().plusDays(5), LocalDate.now().plusDays(7), 400.0, ReservationStatus.CONFIRMED)
        );

        when(userRepository.existsById(guestId)).thenReturn(true);
        when(reservationRepository.findByUserId(guestId)).thenReturn(reservations);

        var result = reservationService.getReservationsByGuestId(guestId);

        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getId());

        verify(userRepository).existsById(guestId);
        verify(reservationRepository).findByUserId(guestId);
    }


    @Test
    void getReservationsByGuestId_guestNotFound() {
        Long guestId = 99L;
        when(userRepository.existsById(eq(guestId))).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reservationService.getReservationsByGuestId(guestId);
        });

        assertTrue(exception.getMessage().contains("Guest user not found with ID: " + guestId));

        verify(userRepository).existsById(eq(guestId));
        verify(reservationRepository, never()).findByUserId(anyLong());
    }


    @Test
    void getAllReservations_success() {
        var reservations = List.of(
                createMockReservation(1L, createMockUser(1L, "user1"), createMockRoom(101L, 100.0, RoomStatus.AVAILABLE), LocalDate.now(), LocalDate.now().plusDays(1), 100.0, ReservationStatus.CONFIRMED),
                createMockReservation(2L, createMockUser(2L, "user2"), createMockRoom(102L, 150.0, RoomStatus.OCCUPIED), LocalDate.now().minusDays(1), LocalDate.now(), 150.0, ReservationStatus.COMPLETED)
        );
        when(reservationRepository.findAll()).thenReturn(reservations);

        var result = reservationService.getAllReservations();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());

        verify(reservationRepository).findAll();
    }


    @Test
    void cancelReservation_success_userOwns() throws UnauthorizedException {
        Long resId = 1L, userId = 10L;

        var userDetails = createMockUserDetails(userId, "owneruser", List.of("USER"));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        var mockUser = createMockUser(userId, "owneruser");
        var pendingRes = createMockReservation(resId, mockUser, createMockRoom(1L, 100.0, RoomStatus.AVAILABLE),
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(7), 200.0, ReservationStatus.PENDING);
        when(reservationRepository.findById(resId)).thenReturn(Optional.of(pendingRes));
        when(reservationRepository.save(any())).thenReturn(pendingRes);

        var result = reservationService.cancelReservation(resId);

        assertEquals(ReservationStatus.CANCELLED, result.getStatus());
        verify(reservationRepository).findById(resId);
        verify(reservationRepository).save(pendingRes);
    }


    @Test
    void cancelReservation_notAuthorized_userDoesNotOwn() {
        Long reservationId = 1L;
        Long currentUserId = 10L;
        Long reservationOwnerId = 20L;

        UserDetailsImpl mockCurrentUserDetails = createMockUserDetails(currentUserId, "currentuser", Collections.singletonList("USER"));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(mockCurrentUserDetails);

        User mockOwnerUser = createMockUser(reservationOwnerId, "owneruser");
        Reservation mockReservation = createMockReservation(reservationId, mockOwnerUser, createMockRoom(1L, 100.0, RoomStatus.AVAILABLE), LocalDate.now().plusDays(5), LocalDate.now().plusDays(7), 200.0, ReservationStatus.PENDING);
        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.of(mockReservation));

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            reservationService.cancelReservation(reservationId);
        });

        assertTrue(exception.getMessage().contains("You are not authorized to cancel this reservation."));
        verify(reservationRepository).findById(eq(reservationId));
        verify(reservationRepository, never()).save(any());
    }


    @Test
    void updateReservation_notFound() {
        Long reservationId = 99L;
        ReservationUpdateDto updateDto = new ReservationUpdateDto();
        updateDto.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(eq(reservationId))).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reservationService.updateReservation(reservationId, updateDto);
        });

        assertTrue(exception.getMessage().contains("Reservation not found with ID: " + reservationId));
        verify(reservationRepository).findById(eq(reservationId));
    }

}