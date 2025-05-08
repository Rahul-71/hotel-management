package com.telus.hotel_management.service;


import com.telus.hotel_management.dto.RoomRequestDto;
import com.telus.hotel_management.entity.Room;
import com.telus.hotel_management.entity.RoomStatus;
import com.telus.hotel_management.exception.ResourceNotFoundException;
import com.telus.hotel_management.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {


    @InjectMocks
    private RoomService roomService;


    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ModelMapper modelMapper;

    @Test
    void createRoom_success() {

        RoomRequestDto roomRequestDto = createMockRoomRequestDto("Deluxe", 200.0);

        Room mockMappedRoom = createMockRoom(null, "Deluxe", 200.0, null);

        when(modelMapper.map(eq(roomRequestDto), eq(Room.class))).thenReturn(mockMappedRoom);

        Room mockSavedRoom = createMockRoom(1L, "Deluxe", 200.0, RoomStatus.AVAILABLE);

        when(roomRepository.save(any(Room.class))).thenReturn(mockSavedRoom);

        Room createdRoom = roomService.createRoom(roomRequestDto);

        assertNotNull(createdRoom);
        assertEquals(1L, createdRoom.getId());
        assertEquals("Deluxe", createdRoom.getRoomType());
        assertEquals(200.0, createdRoom.getPricePerNight());
        assertEquals(RoomStatus.AVAILABLE, createdRoom.getStatus());

        verify(modelMapper).map(eq(roomRequestDto), eq(Room.class));
        verify(roomRepository).save(any(Room.class));
    }

    public RoomRequestDto createMockRoomRequestDto(String roomType, Double pricePerNight) {
        RoomRequestDto roomRequestDto = new RoomRequestDto();
        roomRequestDto.setRoomType(roomType);
        roomRequestDto.setPricePerNight(pricePerNight);
        return roomRequestDto;
    }

    public Room createMockRoom(Long roomId, String roomType, Double pricePerNight, RoomStatus status) {
        Room room = new Room();
        room.setId(roomId);
        room.setRoomType(roomType);
        room.setPricePerNight(pricePerNight);
        room.setStatus(status);
        return room;
    }

    @Test
    void updateRoom_success() {

        Long roomId = 1L;
        RoomRequestDto roomRequestDto = createMockRoomRequestDto("Suite", 350.0);

        Room mockExistingRoom = createMockRoom(roomId, "Deluxe", 200.0, RoomStatus.AVAILABLE);
        when(roomRepository.findById(eq(roomId))).thenReturn(Optional.of(mockExistingRoom));

        Room mockUpdatedRoom = createMockRoom(roomId, "Suite", 350.0, RoomStatus.AVAILABLE);
        when(roomRepository.save(any(Room.class))).thenReturn(mockUpdatedRoom);

        Room updatedRoom = roomService.updateRoom(roomId, roomRequestDto);

        assertNotNull(updatedRoom);
        assertEquals(roomId, updatedRoom.getId());
        assertEquals("Suite", updatedRoom.getRoomType());
        assertEquals(350.0, updatedRoom.getPricePerNight());
        assertEquals(RoomStatus.AVAILABLE, updatedRoom.getStatus());

        verify(roomRepository).findById(eq(roomId));
        verify(roomRepository).save(eq(mockExistingRoom));
    }


    @Test
    void updateRoom_notFound() {

        Long roomId = 99L;
        RoomRequestDto roomRequestDto = createMockRoomRequestDto("Suite", 350.0);

        when(roomRepository.findById(eq(roomId))).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            roomService.updateRoom(roomId, roomRequestDto);
        });

        assertTrue(exception.getMessage().contains("Room not found with ID: " + roomId));

        verify(roomRepository).findById(eq(roomId));
        verify(roomRepository, never()).save(any());
    }


    @Test
    void getRoomById_success() {

        Long roomId = 1L;

        Room mockRoom = createMockRoom(roomId, "Deluxe", 200.0, RoomStatus.AVAILABLE);
        when(roomRepository.findById(eq(roomId))).thenReturn(Optional.of(mockRoom));

        Room foundRoom = roomService.getRoomById(roomId);

        assertNotNull(foundRoom);
        assertEquals(roomId, foundRoom.getId());
        assertEquals("Deluxe", foundRoom.getRoomType());

        verify(roomRepository).findById(eq(roomId));
    }


    @Test
    void getRoomById_notFound() {

        Long roomId = 99L;

        when(roomRepository.findById(eq(roomId))).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            roomService.getRoomById(roomId);
        });

        assertTrue(exception.getMessage().contains("Room not found with ID: " + roomId));

        verify(roomRepository).findById(eq(roomId));
    }


    @Test
    void getAllRooms_success() {

        List<Room> mockRooms = Arrays.asList(
                createMockRoom(1L, "Deluxe", 200.0, RoomStatus.AVAILABLE),
                createMockRoom(2L, "Suite", 300.0, RoomStatus.OCCUPIED)
        );
        when(roomRepository.findAll()).thenReturn(mockRooms);

        List<Room> allRooms = roomService.getAllRooms();

        assertNotNull(allRooms);
        assertEquals(2, allRooms.size());
        assertEquals(mockRooms.get(0).getId(), allRooms.get(0).getId());

        verify(roomRepository).findAll();
    }


    @Test
    void deleteRoom_success_available() {

        Long roomId = 1L;

        when(roomRepository.existsById(eq(roomId))).thenReturn(true);

        Room mockAvailableRoom = createMockRoom(roomId, "Deluxe", 200.0, RoomStatus.AVAILABLE);
        when(roomRepository.findById(eq(roomId))).thenReturn(Optional.of(mockAvailableRoom));

        doNothing().when(roomRepository).deleteById(eq(roomId));

        assertDoesNotThrow(() -> roomService.deleteRoom(roomId));

        verify(roomRepository).existsById(eq(roomId));
        verify(roomRepository).findById(eq(roomId));
        verify(roomRepository).deleteById(eq(roomId));
    }


    @Test
    void deleteRoom_notFound() {
        Long roomId = 99L;

        when(roomRepository.existsById(eq(roomId))).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            roomService.deleteRoom(roomId);
        });

        assertTrue(exception.getMessage().contains("Room not found with ID: " + roomId));

        verify(roomRepository).existsById(eq(roomId));
        verify(roomRepository, never()).findById(anyLong());
        verify(roomRepository, never()).deleteById(anyLong());
    }

}