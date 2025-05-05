package com.telus.hotel_management.service;

import com.telus.hotel_management.dto.ReservationRequestDto;
import com.telus.hotel_management.dto.ReservationUpdateDto;
import com.telus.hotel_management.entity.*;
import com.telus.hotel_management.exception.ResourceNotFoundException;
import com.telus.hotel_management.exception.UnauthorizedException;
import com.telus.hotel_management.repository.ReservationRepository;
import com.telus.hotel_management.repository.RoomRepository;
import com.telus.hotel_management.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;


@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, RoomRepository roomRepository, UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    private UserDetailsImpl getCurrentUserDetails() throws UnauthorizedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            log.error("User is not authenticated in service layer");
            throw new UnauthorizedException("User is not authenticated");
        }
        return (UserDetailsImpl) authentication.getPrincipal();
    }

    // Create a new reservation
    @Transactional
    public Reservation createReservation(ReservationRequestDto requestDto) throws UnauthorizedException {
        log.info("Service: Creating new reservation for room ID: {} from {} to {}",
                requestDto.getRoomId(), requestDto.getCheckInDate(), requestDto.getCheckOutDate());

        // 1. get authenticated user
        UserDetailsImpl currentUserDetails = getCurrentUserDetails();
        User user = userRepository.findById(currentUserDetails.getId())
                .orElseThrow(() -> {
                    log.error("Service: Authenticated user found in security context but not in DB with ID: {}", currentUserDetails.getId());
                    return new ResourceNotFoundException("Authenticated user not found!");
                });

        // 2. find the room
        Room room = roomRepository.findById(requestDto.getRoomId())
                .orElseThrow(() -> {
                    log.error("Service: Room not found with ID: {}", requestDto.getRoomId());
                    return new ResourceNotFoundException("Room not found with ID: " + requestDto.getRoomId());
                });

        log.info("Service: Checking status for Room ID: {}. Current status: {}", room.getId(), room.getStatus());

        if (room.getStatus() != RoomStatus.AVAILABLE) {
            log.error("Service: Room {} is not available for booking. Current status: {}", room.getId(), room.getStatus());
            throw new RuntimeException("Room is not available for booking. Current status: " + room.getStatus());
        }

        // 3.  date validation
        log.info("Service: Validating dates: {} to {}", requestDto.getCheckInDate(), requestDto.getCheckOutDate());
        // check-out is strictly after check-in
        if (!requestDto.getCheckOutDate().isAfter(requestDto.getCheckInDate())) {
            log.error("Service: Invalid dates for reservation: check-out date {} is not after check-in date {}", requestDto.getCheckOutDate(), requestDto.getCheckInDate());
            throw new RuntimeException("Check-out date must be after check-in date.");
        }
        //  check-in is not in the past
        if (requestDto.getCheckInDate().isBefore(LocalDate.now())) {
            log.error("Service: Invalid dates for reservation: check-in date {} cannot be in the past", requestDto.getCheckInDate());
            throw new RuntimeException("Check-in date cannot be in the past.");
        }


        // 4. check room availability for the given dates
        log.info("Service: Checking availability for Room ID: {} from {} to {}", room.getId(), requestDto.getCheckInDate(), requestDto.getCheckOutDate());
        if (reservationRepository.existsOverlappingReservation(room.getId(), requestDto.getCheckInDate(), requestDto.getCheckOutDate())) {
            log.error("Service: Room {} is not available for the selected dates: {} to {}", room.getId(), requestDto.getCheckInDate(), requestDto.getCheckOutDate());
            throw new RuntimeException("Room is not available for the selected dates.");
        }


        // 5. calculate total price
        long nights = Period.between(requestDto.getCheckInDate(), requestDto.getCheckOutDate()).getDays();
        if (nights < 1) {
            log.error("Service: Calculated nights {} is less than one for reservation dates {} to {}", nights, requestDto.getCheckInDate(), requestDto.getCheckOutDate());
            throw new RuntimeException("Reservation must be for at least one night.");
        }
        double totalPrice = room.getPricePerNight() * nights;
        log.info("Service: Calculated total price: {} for {} nights on room {}", totalPrice, nights, room.getId());


        // 6. create entity
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setRoom(room);
        reservation.setCheckInDate(requestDto.getCheckInDate());
        reservation.setCheckOutDate(requestDto.getCheckOutDate());
        reservation.setTotalPrice(totalPrice);
        reservation.setStatus(ReservationStatus.PENDING);

        // 7. save the reservation
        log.info("Service: Saving new reservation for user {} and room {}", user.getUsername(), room.getId());
        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("Service: Reservation created and saved with ID: {}", savedReservation.getId());

        return savedReservation;
    }

    // fetch reservation details by reservation id
    public Reservation getReservationById(Long reservationId) {
        log.info("Service: Retrieving reservation with ID: {}", reservationId);
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.error("Service: Reservation not found with ID: {}", reservationId);
                    return new ResourceNotFoundException("Reservation not found with ID: " + reservationId);
                });
    }

    // fetch all reservations for the authenticated user
    public List<Reservation> getMyReservations() throws UnauthorizedException {
        log.info("Service: Retrieving reservations for authenticated user");
        UserDetailsImpl currentUserDetails = getCurrentUserDetails();
        User user = userRepository.findById(currentUserDetails.getId())
                .orElseThrow(() -> {
                    log.error("Service: Authenticated user found in security context but not in DB with ID: {}", currentUserDetails.getId());
                    return new ResourceNotFoundException("Authenticated user not found!");
                });

        List<Reservation> userReservations = reservationRepository.findByUser(user);
        log.info("Service: Found {} reservations for user {}", userReservations.size(), user.getUsername());
        return userReservations;
    }

    // fetch reservations by guest ID (for hotel/admin)
    public List<Reservation> getReservationsByGuestId(Long guestId) {
        log.info("Service: Retrieving reservations for guest with ID: {}", guestId);
        // Optional: Check if guestId exists before querying reservations (Good practice)
        if (!userRepository.existsById(guestId)) {
            log.error("Service: Guest user not found with ID: {}", guestId);
            throw new ResourceNotFoundException("Guest user not found with ID: " + guestId);
        }
        // Assumes findByUserId method exists in ReservationRepository taking a Long ID
        List<Reservation> guestReservations = reservationRepository.findByUserId(guestId);
        log.info("Service: Found {} reservations for guest ID {}", guestReservations.size(), guestId);
        return guestReservations;
    }

    // fetch all reservations (for hotel/admin)
    public List<Reservation> getAllReservations() {
        log.info("Service: Retrieving all reservations");
        List<Reservation> allReservations = reservationRepository.findAll();
        log.info("Service: Found {} total reservations", allReservations.size());
        return allReservations;
    }

    // Cancel a reservation
    @Transactional
    public Reservation cancelReservation(Long reservationId) throws UnauthorizedException {
        log.info("Service: Cancelling reservation with ID: {}", reservationId);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.error("Service: Reservation not found with ID: {}", reservationId);
                    return new ResourceNotFoundException("Reservation not found with ID: " + reservationId);
                });


        if (reservation.getStatus() == ReservationStatus.CANCELLED || reservation.getStatus() == ReservationStatus.COMPLETED) {
            log.error("Service: Reservation {} cannot be cancelled as it is already {}", reservation.getId(), reservation.getStatus());
            throw new RuntimeException("Reservation cannot be cancelled as it is already " + reservation.getStatus());
        }

        UserDetailsImpl currentUserDetails = getCurrentUserDetails();
        boolean isAdminOrHotel = currentUserDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN") || auth.getAuthority().equals("HOTEL")); // <-- Use "ADMIN", "HOTEL" authorities

        if (!isAdminOrHotel && !reservation.getUser().getId().equals(currentUserDetails.getId())) {
            log.error("Service: User {} is not authorized to cancel reservation {}", currentUserDetails.getUsername(), reservation.getId());

            throw new UnauthorizedException("You are not authorized to cancel this reservation."); // Your custom UnauthorizedException
        }

        reservation.setStatus(ReservationStatus.CANCELLED); // <-- Set status to CANCELLED
        log.info("Service: Reservation {} cancelled successfully", reservation.getId());
        Reservation cancelledReservation = reservationRepository.save(reservation);


        return cancelledReservation;
    }

    @Transactional
    public Reservation updateReservation(Long reservationId, ReservationUpdateDto updateDto) {
        log.info("Service: Updating reservation with ID: {}", reservationId);
        Reservation existingReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.error("Service: Reservation not found with ID: {}", reservationId);
                    return new ResourceNotFoundException("Reservation not found with ID: " + reservationId);
                });


        if (updateDto.getRoomId() != null && !existingReservation.getRoom().getId().equals(updateDto.getRoomId())) {
            log.info("Service: Updating room for reservation {} to room ID {}", reservationId, updateDto.getRoomId());
            Room newRoom = roomRepository.findById(updateDto.getRoomId())
                    .orElseThrow(() -> {
                        log.error("Service: New Room not found with ID: {}", updateDto.getRoomId());
                        return new ResourceNotFoundException("New Room not found with ID: " + updateDto.getRoomId());
                    });
            existingReservation.setRoom(newRoom);
        }

        boolean datesChanged = false;
        if (updateDto.getCheckInDate() != null && !updateDto.getCheckInDate().isEqual(existingReservation.getCheckInDate())) {
            log.info("Service: Updating check-in date for reservation {} to {}", reservationId, updateDto.getCheckInDate());
            // TODO: Add validation: Cannot update check-in to the past?
            if (updateDto.getCheckInDate().isBefore(LocalDate.now())) {
                log.error("Service: Updated check-in date {} is in the past for reservation {}", updateDto.getCheckInDate(), reservationId);
                throw new RuntimeException("Updated check-in date cannot be in the past."); // Replace
            }
            existingReservation.setCheckInDate(updateDto.getCheckInDate());
            datesChanged = true;
        }

        if (updateDto.getCheckOutDate() != null && !updateDto.getCheckOutDate().isEqual(existingReservation.getCheckOutDate())) {
            log.info("Service: Updating check-out date for reservation {} to {}", reservationId, updateDto.getCheckOutDate());
            if (updateDto.getCheckOutDate().isBefore(LocalDate.now())) {
                log.error("Service: Updated check-out date {} is in the past for reservation {}", updateDto.getCheckOutDate(), reservationId);
                throw new RuntimeException("Updated check-out date cannot be in the past."); // Replace
            }
            existingReservation.setCheckOutDate(updateDto.getCheckOutDate());
            datesChanged = true;
        }


        // calculating price and rechecking availability if dates or room changed
        if (datesChanged || updateDto.getRoomId() != null) {
            log.info("Service: Dates or Room changed for reservation {}. Recalculating price and checking availability.", reservationId);

            // revalidating date range after updates
            if (!existingReservation.getCheckOutDate().isAfter(existingReservation.getCheckInDate())) {
                log.error("Service: Updated check-out date {} is not after check-in date {} for reservation {}",
                        existingReservation.getCheckOutDate(), existingReservation.getCheckInDate(), reservationId);
                throw new RuntimeException("Updated check-out date must be after check-in date."); // TODO: Replace RuntimeException
            }

            long nights = Period.between(existingReservation.getCheckInDate(), existingReservation.getCheckOutDate()).getDays();
            if (nights < 1) {
                log.error("Service: Updated dates for reservation {} result in less than one night ({})", reservationId, nights);
                throw new RuntimeException("Updated dates result in less than one night."); // TODO: Replace RuntimeException
            }

            log.info("Service: Checking availability for updated reservation {} dates {} to {} on room {}",
                    reservationId, existingReservation.getCheckInDate(), existingReservation.getCheckOutDate(), existingReservation.getRoom().getId());
            List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(
                    existingReservation.getRoom().getId(), existingReservation.getCheckInDate(), existingReservation.getCheckOutDate());

            boolean hasOverlap = overlappingReservations.stream()
                    .anyMatch(r -> !r.getId().equals(existingReservation.getId()));

            if (hasOverlap) {
                log.error("Service: Room is not available for the updated dates for reservation {}", reservationId);
                throw new RuntimeException("Room is not available for the updated dates.");
            }

            existingReservation.setTotalPrice(existingReservation.getRoom().getPricePerNight() * nights);
            log.info("Service: Recalculated total price for reservation {}: {}", reservationId, existingReservation.getTotalPrice());
        }

        if (updateDto.getStatus() != null && !updateDto.getStatus().equals(existingReservation.getStatus())) {
            log.info("Service: Updating status for reservation {} from {} to {}", reservationId, existingReservation.getStatus(), updateDto.getStatus());
            existingReservation.setStatus(updateDto.getStatus());
        }

        log.info("Service: Saving updated reservation with ID: {}", existingReservation.getId());
        Reservation updatedReservation = reservationRepository.save(existingReservation);


        return updatedReservation;
    }

}