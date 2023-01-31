package com.trivialware.hotelms.Repositories;

import com.trivialware.hotelms.Entities.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    boolean existsByNameIgnoreCase(String name);

}
