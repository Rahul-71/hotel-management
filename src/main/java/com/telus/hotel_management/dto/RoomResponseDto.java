package com.telus.hotel_management.dto;

import com.telus.hotel_management.entity.RoomStatus;

public class RoomResponseDto {
    private Long id;

//    private String roomNumber;

    private Double pricePerNight;

    private String roomType;

    private RoomStatus status;

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

//    public String getRoomNumber() {
//        return roomNumber;
//    }
//
//    public void setRoomNumber(String roomNumber) {
//        this.roomNumber = roomNumber;
//    }

    public Double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(Double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "RoomResponseDto{" +
                "id=" + id +
                ", pricePerNight=" + pricePerNight +
                ", roomType='" + roomType + '\'' +
                ", status=" + status +
                '}';
    }
}
