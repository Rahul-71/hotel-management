package com.telus.hotel_management.controller;

import com.telus.hotel_management.dto.ReservationRequestDto;
import com.telus.hotel_management.dto.ReservationResponseDto;
import com.telus.hotel_management.dto.ReservationUpdateDto;
import com.telus.hotel_management.entity.Reservation;
import com.telus.hotel_management.entity.ReservationStatus;
import com.telus.hotel_management.exception.UnauthorizedException;
import com.telus.hotel_management.service.ReservationService;
import com.telus.hotel_management.service.UserDetailsImpl;
import com.telus.hotel_management.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ReservationController reservationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private ReservationResponseDto createResponseDto(Long id) {
        ReservationResponseDto dto = new ReservationResponseDto();
        dto.setReservationId(id);
        dto.setStatus(ReservationStatus.PENDING);
        return dto;
    }

    private Reservation createReservation(Long id) {
        Reservation reservation = new Reservation();
        reservation.setId(id);
        return reservation;
    }

    @Test
    void createReservation_Success() throws Exception, UnauthorizedException {
        ReservationRequestDto request = new ReservationRequestDto();
        Reservation reservation = createReservation(1L);
        ReservationResponseDto responseDto = createResponseDto(1L);

        when(reservationService.createReservation(request)).thenReturn(reservation);
        when(modelMapper.map(reservation, ReservationResponseDto.class)).thenReturn(responseDto);

        ResponseEntity<ReservationResponseDto> response = reservationController.createReservation(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDto.getReservationId(), response.getBody().getReservationId());
        verify(reservationService).createReservation(request);
    }

    @Test
    void createReservation_Failure() throws UnauthorizedException {
        ReservationRequestDto request = new ReservationRequestDto();
        when(reservationService.createReservation(request)).thenThrow(new RuntimeException());

        assertThrows(UnauthorizedException.class, () ->
                reservationController.createReservation(request));
    }

    @Test
    void getReservationsByUsername_Success() {
        String username = "testuser";
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(1L);
        when(userService.loadUserByUsername(username)).thenReturn(userDetails);

        Reservation reservation = createReservation(1L);
        when(reservationService.getReservationsByGuestId(1L)).thenReturn(Collections.singletonList(reservation));
        when(modelMapper.map(any(), eq(ReservationResponseDto.class))).thenReturn(createResponseDto(1L));

        ResponseEntity<List<ReservationResponseDto>> response = reservationController.getReservationsByUsername(username);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getReservationsByUsername_Failure() {
        when(userService.loadUserByUsername(anyString())).thenThrow(new RuntimeException());

        ResponseEntity<List<ReservationResponseDto>> response = reservationController.getReservationsByUsername("invalid");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // Cancel Reservation Tests
    @Test
    void cancelReservation_Success() throws Exception, UnauthorizedException {
        Long reservationId = 1L;
        when(reservationService.cancelReservation(reservationId)).thenReturn(createReservation(1L));

        ResponseEntity<?> response = reservationController.cancelReservation(reservationId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reservationService).cancelReservation(reservationId);
    }

    @Test
    void cancelReservation_Failure() throws UnauthorizedException {
        Long reservationId = 1L;
        doThrow(new RuntimeException()).when(reservationService).cancelReservation(reservationId);

        assertThrows(RuntimeException.class, () ->
                reservationController.cancelReservation(reservationId));
    }

    // Update Reservation Tests
    @Test
    void updateReservation_Success() {
        Long reservationId = 1L;
        ReservationUpdateDto updateDto = new ReservationUpdateDto();
        updateDto.setReservationId(reservationId);
        Reservation updated = createReservation(reservationId);

        when(reservationService.updateReservation(reservationId, updateDto)).thenReturn(updated);
        when(modelMapper.map(updated, ReservationResponseDto.class)).thenReturn(createResponseDto(reservationId));

        ResponseEntity<ReservationResponseDto> response = reservationController.updateReservation(reservationId, updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reservationId, response.getBody().getReservationId());
    }

    @Test
    void updateReservation_IdMismatch() {
        ReservationUpdateDto updateDto = new ReservationUpdateDto();
        updateDto.setReservationId(2L);

        ResponseEntity<ReservationResponseDto> response = reservationController.updateReservation(1L, updateDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
