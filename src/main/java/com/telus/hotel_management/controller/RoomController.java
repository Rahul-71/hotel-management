package com.telus.hotel_management.controller;

import com.telus.hotel_management.dto.RoomRequestDto;
import com.telus.hotel_management.dto.RoomResponseDto;
import com.telus.hotel_management.entity.Room;
import com.telus.hotel_management.service.RoomService;
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

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;
    private final ModelMapper modelMapper;
    private final static Logger log = LoggerFactory.getLogger(RoomController.class);


    @Autowired
    public RoomController(ModelMapper modelMapper, RoomService roomService) {
        this.modelMapper = modelMapper;
        this.roomService = roomService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RoomResponseDto> createRoom(@Valid @RequestBody RoomRequestDto roomRequestDto) {
        log.info("Creating new room with request: {}", roomRequestDto);
        Room createRoom = roomService.createRoom(roomRequestDto);
        log.info("Room created successfully with ID: {}", createRoom);
        RoomResponseDto mappedRoom = modelMapper.map(createRoom, RoomResponseDto.class);
        log.info("created RoomDTO: {}", mappedRoom);
        return new ResponseEntity<>(mappedRoom, HttpStatus.CREATED);
    }


    @PutMapping("/update/{roomId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RoomResponseDto> updateRoom(@PathVariable Long roomId, @Valid @RequestBody RoomRequestDto roomRequestDto) {
        log.info("Updating existing room with ID: {} and request: {}", roomId, roomRequestDto);
        Room updateRoom = roomService.updateRoom(roomId, roomRequestDto);
        log.info("Room updated successfully with ID: {}", updateRoom.getId());
        RoomResponseDto mappedRoom = modelMapper.map(updateRoom, RoomResponseDto.class);
        log.info("updated RoomDTO: {}", mappedRoom);
        return new ResponseEntity<>(mappedRoom, HttpStatus.OK);
    }

    // fetch room by roomid
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponseDto> getRoomById(@PathVariable Long roomId) {
        log.info("Fetching room by ID: {}", roomId);
        Room room = roomService.getRoomById(roomId);
        log.info("Room fetched successfully with ID: {}", room.getId());
        RoomResponseDto roomDto = modelMapper.map(room, RoomResponseDto.class);
        log.info("fetched RoomDTO: {}", roomDto);
        return new ResponseEntity<>(roomDto, HttpStatus.OK);
    }

    // fetch all rooms
    @GetMapping("/all")
    public ResponseEntity<List<RoomResponseDto>> getAllRooms() {
        log.info("Fetching all rooms");
        List<Room> rooms = roomService.getAllRooms();
        log.info("Fetched {} rooms", rooms.size());
        return new ResponseEntity<>(
                rooms.isEmpty() ? null :
                        rooms.stream()
                                .map(room -> modelMapper.map(room, RoomResponseDto.class))
                                .toList(),
                HttpStatus.OK);
    }
    
    @DeleteMapping("/delete/{roomId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> deleteRoom(@PathVariable Long roomId) {
        log.info("Deleting room with ID: {}", roomId);
        roomService.deleteRoom(roomId);
        log.info("Room deleted successfully with ID: {}", roomId);
        return new ResponseEntity<>("Room deleted successfully", HttpStatus.OK);
    }
}
