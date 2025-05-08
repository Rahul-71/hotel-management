package com.telus.hotel_management.controller;

import com.telus.hotel_management.dto.RoomRequestDto;
import com.telus.hotel_management.dto.RoomResponseDto;
import com.telus.hotel_management.entity.Room;
import com.telus.hotel_management.service.RoomService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RoomControllerTest {

    @Mock
    private RoomService roomService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RoomController roomController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Room createRoom(Long id) {
        Room room = new Room();
        room.setId(id);
        return room;
    }

    private RoomResponseDto createResponseDto(Long id) {
        RoomResponseDto dto = new RoomResponseDto();
        dto.setId(id);
        return dto;
    }

    @Test
    void createRoom_Success() {
        RoomRequestDto requestDto = new RoomRequestDto();
        Room room = createRoom(1L);
        RoomResponseDto responseDto = createResponseDto(1L);

        when(roomService.createRoom(requestDto)).thenReturn(room);
        when(modelMapper.map(room, RoomResponseDto.class)).thenReturn(responseDto);

        ResponseEntity<RoomResponseDto> response = roomController.createRoom(requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
        verify(roomService).createRoom(requestDto);
    }

    @Test
    void updateRoom_Success() {
        Long roomId = 1L;
        RoomRequestDto requestDto = new RoomRequestDto();
        Room updatedRoom = createRoom(roomId);
        RoomResponseDto responseDto = createResponseDto(roomId);

        when(roomService.updateRoom(roomId, requestDto)).thenReturn(updatedRoom);
        when(modelMapper.map(updatedRoom, RoomResponseDto.class)).thenReturn(responseDto);

        ResponseEntity<RoomResponseDto> response = roomController.updateRoom(roomId, requestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
        verify(roomService).updateRoom(roomId, requestDto);
    }

    @Test
    void getRoomById_Success() {
        Long roomId = 1L;
        Room room = createRoom(roomId);
        RoomResponseDto responseDto = createResponseDto(roomId);

        when(roomService.getRoomById(roomId)).thenReturn(room);
        when(modelMapper.map(room, RoomResponseDto.class)).thenReturn(responseDto);

        ResponseEntity<RoomResponseDto> response = roomController.getRoomById(roomId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
        verify(roomService).getRoomById(roomId);
    }

    @Test
    void getAllRooms_Success() {
        Room room = createRoom(1L);
        RoomResponseDto responseDto = createResponseDto(1L);

        when(roomService.getAllRooms()).thenReturn(Collections.singletonList(room));
        when(modelMapper.map(room, RoomResponseDto.class)).thenReturn(responseDto);

        ResponseEntity<List<RoomResponseDto>> response = roomController.getAllRooms();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(roomService).getAllRooms();
    }

    @Test
    void getAllRooms_EmptyList() {
        when(roomService.getAllRooms()).thenReturn(Collections.emptyList());

        ResponseEntity<List<RoomResponseDto>> response = roomController.getAllRooms();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(roomService).getAllRooms();
    }

    @Test
    void deleteRoom_Success() {
        Long roomId = 1L;
        doNothing().when(roomService).deleteRoom(roomId);

        ResponseEntity<String> response = roomController.deleteRoom(roomId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Room deleted successfully", response.getBody());
        verify(roomService).deleteRoom(roomId);
    }
}
