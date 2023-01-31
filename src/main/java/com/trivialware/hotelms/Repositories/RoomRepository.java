package com.trivialware.hotelms.Repositories;

import com.trivialware.hotelms.Entities.Room;
import com.trivialware.hotelms.Entities.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByRoomType(RoomType roomType);

    List<Room> findByIdNotIn(Collection<Long> ids);

    List<Room> findByIdIn(Collection<Long> ids);

    List<Room> findByHotel_Id(Long id);

    boolean existsByCodeIgnoreCaseAndHotel_Id(String code, Long id);


}
