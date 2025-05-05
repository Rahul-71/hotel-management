package com.telus.hotel_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class RoomRequestDto {
    //    @NotBlank(message = "Room number is required")
//    private String roomNumber;
    @NotBlank(message = "Room type is required")
    private String roomType;
    @NotNull(message = "Price per night is required")
    @Positive(message = "Price per night must be a positive number")
    private Double pricePerNight;

//    public String getRoomNumber() {
//        return roomNumber;
//    }
//
//    public void setRoomNumber(String roomNumber) {
//        this.roomNumber = roomNumber;
//    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(Double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    @Override
    public String toString() {
        return "RoomRequestDto{" +
                "roomType='" + roomType + '\'' +
                ", pricePerNight=" + pricePerNight +
                '}';
    }
}
