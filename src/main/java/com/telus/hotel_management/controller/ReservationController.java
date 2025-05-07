package com.telus.hotel_management.controller;

import com.telus.hotel_management.dto.ReservationRequestDto;
import com.telus.hotel_management.dto.ReservationResponseDto;
import com.telus.hotel_management.dto.ReservationUpdateDto;
import com.telus.hotel_management.entity.Reservation;
import com.telus.hotel_management.exception.UnauthorizedException;
import com.telus.hotel_management.service.ReservationService;
import com.telus.hotel_management.service.UserDetailsImpl;
import com.telus.hotel_management.service.UserService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {
    private final ReservationService reservationService;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    @Autowired
    public ReservationController(ModelMapper modelMapper, ReservationService reservationService, UserService userService) {
        this.modelMapper = modelMapper;
        this.reservationService = reservationService;
        this.userService = userService;
    }

//    private ReservationResponseDto mapReservationToDto(Reservation reservation) {
//        log.debug("Mapping Reservation to DTO");
//        return new ModelMapper().map(reservation, ReservationResponseDto.class);
//    }

    @PostMapping("/createReservation")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ReservationResponseDto> createReservation(@Valid @RequestBody ReservationRequestDto reservationDto) throws UnauthorizedException {
        log.info("Creating new reservation");
        try {
            Reservation createdReservation = reservationService.createReservation(reservationDto);
            ReservationResponseDto reservationResponseDto = modelMapper.map(createdReservation, ReservationResponseDto.class);
            log.info("Reservation created successfully {}", reservationResponseDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(reservationResponseDto);
        } catch (Exception e) {
            log.error("Error creating reservation", e);
            throw new UnauthorizedException("Error creating reservation");
        }
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('HOTEL') or hasAuthority('ADMIN')")
    public ResponseEntity<List<ReservationResponseDto>> getReservationsByUsername(@PathVariable String username) {
        log.info("Getting reservations by username");
        try {
            List<Reservation> reservations = reservationService.getReservationsByGuestId(((UserDetailsImpl) (userService.loadUserByUsername(username))).getId());
            log.info("Reservations retrieved successfully");
            List<ReservationResponseDto> reservationDtos = reservations.stream()
                    .map(reservation -> modelMapper.map(reservation, ReservationResponseDto.class))
                    .collect(Collectors.toList());
            log.info("List of reservations returned to user with username: {} are {}", username, reservationDtos);
            return ResponseEntity.ok(reservationDtos);
        } catch (Exception e) {
            log.error("Error getting reservations by username", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/guest/{guestId}")
    @PreAuthorize("hasAuthority('HOTEL') or hasAuthority('ADMIN')")
    public ResponseEntity<List<ReservationResponseDto>> getReservationsByGuestId(@PathVariable Long guestId) {
        log.info("Getting reservations by guest ID");
        try {
            List<Reservation> reservations = reservationService.getReservationsByGuestId(guestId);
            log.debug("Reservations retrieved successfully");
            List<ReservationResponseDto> reservationDtos = reservations.stream()
                    .map(reservation -> modelMapper.map(reservation, ReservationResponseDto.class))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(reservationDtos);
        } catch (Exception e) {
            log.error("Error getting reservations by guest ID", e);
            return ResponseEntity.badRequest().build();
        }
    }


    @DeleteMapping("/{reservationId}")
    @PreAuthorize("hasAuthority('HOTEL') or hasAuthority('ADMIN')")
    public ResponseEntity<?> cancelReservation(@PathVariable Long reservationId) throws UnauthorizedException {
        log.info("Cancelling reservation");
        try {
            reservationService.cancelReservation(reservationId);
            log.debug("Reservation cancelled successfully");
            return ResponseEntity.ok().body("Reservation with reservationId: " + reservationId + " cancelled successfully");
        } catch (Exception e) {
            log.info("Error cancelling reservation {}", e);
            throw new RuntimeException("Error cancelling reservation " + e.getLocalizedMessage());
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('HOTEL') or hasAuthority('ADMIN')")
    public ResponseEntity<List<ReservationResponseDto>> getAllReservations() {
        log.info("Getting all reservations");
        try {
            List<Reservation> reservations = reservationService.getAllReservations();
            log.debug("Reservations retrieved successfully");
            List<ReservationResponseDto> reservationDtos = reservations.stream()
                    .map(reservation -> modelMapper.map(reservation, ReservationResponseDto.class))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(reservationDtos);
        } catch (Exception e) {
            log.error("Error getting all reservations", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<ReservationResponseDto>> getMyReservations() throws UnauthorizedException {
        log.info("Getting my reservations");
        try {
            List<Reservation> reservations = reservationService.getMyReservations();
            log.debug("Reservations retrieved successfully");
            List<ReservationResponseDto> reservationDtos = reservations.stream()
                    .map(reservation -> modelMapper.map(reservation, ReservationResponseDto.class))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(reservationDtos);
        } catch (Exception e) {
            log.error("Error getting my reservations", e);
            throw new UnauthorizedException("Error getting my reservations");
        }
    }

    @PutMapping("/{reservationId}")
    @PreAuthorize("hasAuthority('HOTEL') or hasAuthority('ADMIN')")
    public ResponseEntity<ReservationResponseDto> updateReservation(@PathVariable Long reservationId, @Valid @RequestBody ReservationUpdateDto updateDto) {
        log.info("Updating reservation");
        try {
            if (updateDto.getReservationId() != null && !updateDto.getReservationId().equals(reservationId)) {
                log.error("Reservation ID mismatch");
                return ResponseEntity.badRequest().body(null);
            }
            updateDto.setReservationId(reservationId);

            Reservation updatedReservation = reservationService.updateReservation(reservationId, updateDto);
            log.debug("Reservation updated successfully");
            return ResponseEntity.ok(modelMapper.map(updatedReservation, ReservationResponseDto.class));
        } catch (Exception e) {
            log.error("Error updating reservation", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
