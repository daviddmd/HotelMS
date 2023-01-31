package com.trivialware.hotelms.Repositories;

import com.trivialware.hotelms.Entities.ServiceHotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ServiceHotelRepository extends JpaRepository<ServiceHotel, Long> {
    List<ServiceHotel> findByIdIn(Collection<Long> ids);

    boolean existsByHotel_IdAndCodeIgnoreCase(Long id, String code);


}
