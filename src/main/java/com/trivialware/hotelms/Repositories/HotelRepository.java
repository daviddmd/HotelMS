package com.trivialware.hotelms.Repositories;

import com.trivialware.hotelms.Entities.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    boolean existsByCodeIgnoreCase(String code);

}
