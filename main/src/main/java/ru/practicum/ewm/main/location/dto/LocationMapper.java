package ru.practicum.ewm.main.location.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.main.location.Location;

@UtilityClass
public class LocationMapper {
    public Location toLocation(LocationDto dto) {
        return Location.builder()
                .lat(dto.getLat())
                .lon(dto.getLon())
                .build();
    }

    public LocationDto toLocationDto(Location location) {
        return LocationDto.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }
}
