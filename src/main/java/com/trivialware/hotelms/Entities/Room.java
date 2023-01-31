package com.trivialware.hotelms.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@With
@Table(name = "rooms")
public class Room {
    //Adicionar outros campos depois
    //Adicionar DTOs e DAOs, separar pacotes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @NotEmpty
    private String code;
    @NotNull
    @NotEmpty
    private String name;
    @NotNull
    @NotEmpty
    private String description;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;
    @Min(0)
    private BigDecimal price;
    @ColumnDefault("true")
    @NotNull
    private boolean active;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "hotel_id")
    @JsonBackReference("hotel-room")
    private Hotel hotel;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return id.equals(room.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
