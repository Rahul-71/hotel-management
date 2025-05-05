package com.telus.hotel_management.service;

import com.telus.hotel_management.dto.RoomRequestDto;
import com.telus.hotel_management.entity.Room;
import com.telus.hotel_management.entity.RoomStatus;
import com.telus.hotel_management.exception.ResourceNotFoundException;
import com.telus.hotel_management.repository.RoomRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private ModelMapper modelMapper;

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    // create new room
    public Room createRoom(RoomRequestDto roomRequestDto) {
        log.info("Creating new room with request: {}", roomRequestDto);
        Room room = modelMapper.map(roomRequestDto, Room.class);

        Room savedRoom = roomRepository.save(room);
        log.info("Room created successfully with ID: {}", savedRoom.getId());
        return savedRoom;
    }

    // update existing room (only admin can update using this api)
    public Room updateRoom(Long roomId, RoomRequestDto roomRequestDto) {
        log.info("Updating existing room with ID: {} and request: {}", roomId, roomRequestDto);
        Room existingRoom = roomRepository.findById(roomId)
                .orElseThrow(() -> {
                    log.error("Room not found with ID: {}", roomId);
                    return new ResourceNotFoundException("Room not found with ID: " + roomId);
                });

//        existingRoom.setRoomNumber(roomRequestDto.getRoomNumber());
        existingRoom.setPricePerNight(roomRequestDto.getPricePerNight());
        existingRoom.setRoomType(roomRequestDto.getRoomType());
        log.info("updated existingRoom data: {}", existingRoom);

        Room updatedRoom = roomRepository.save(existingRoom);
        log.info("Room updated successfully with ID: {} with updatated data: {}", updatedRoom.getId(), updatedRoom);
        return updatedRoom;
    }

    // fetch room by roomid
    public Room getRoomById(Long roomId) {
        log.info("Fetching room by ID: {}", roomId);
        return roomRepository.findById(roomId)
                .orElseThrow(() -> {
                    log.error("Room not found with ID: {}", roomId);
                    return new ResourceNotFoundException("Room not found with ID: " + roomId);
                });
    }

    // fetch all rooms
    public List<Room> getAllRooms() {
        log.info("Fetching all rooms");
        List<Room> rooms = roomRepository.findAll();
        log.info("Fetched {} rooms", rooms.size());
        return rooms;
    }

    // delete room by roomid
    public void deleteRoom(Long roomId) {
        log.info("Deleting room with ID: {}", roomId);
        if (!roomRepository.existsById(roomId)) {
            log.error("Room not found with ID: {}", roomId);
            throw new ResourceNotFoundException("Room not found with ID: " + roomId);
        }
// if room is occupied, deletion is not possible
        if (roomRepository.findById(roomId).get().getStatus() != RoomStatus.AVAILABLE) {
            log.error("Room is not available for deletion with ID: {}", roomId);
            throw new ResourceNotFoundException("Room is not available");
        }

        roomRepository.deleteById(roomId);
        log.info("Room deleted successfully with ID: {}", roomId);
    }
}