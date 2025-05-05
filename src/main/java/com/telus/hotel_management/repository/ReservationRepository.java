package com.telus.hotel_management.repository;

import com.telus.hotel_management.entity.Reservation;
import com.telus.hotel_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUser(User user);

    List<Reservation> findByUserId(Long userId);

    // Custom query to find overlapping reservations for a specific room and date range
    @Query("SELECT r FROM Reservation r WHERE r.room.id = :roomId " +
            "AND r.status IN ('PENDING', 'CONFIRMED') " + // Only check active/pending bookings
            "AND ((r.checkInDate <= :checkOutDate) AND (r.checkOutDate >= :checkInDate))")
    List<Reservation> findOverlappingReservations(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.room.id = :roomId " +
            "AND r.status IN ('PENDING', 'CONFIRMED') " +
            "AND ((r.checkInDate <= :checkOutDate) AND (r.checkOutDate >= :checkInDate))")
    boolean existsOverlappingReservation(@Param("roomId") Long roomId, @Param("checkInDate") LocalDate checkInDate, @Param("checkOutDate") LocalDate checkOutDate);


}
