package ru.practicum.ewm.main.location.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.ewm.main.location.Location;

import static org.junit.jupiter.api.Assertions.*;

class LocationMapperTest {
    @Test
    void shouldConvertLocationDtoToLocation() {
        LocationDto dto = LocationDto.builder()
                .lat(55.75f)
                .lon(37.62f)
                .build();

        Location location = LocationMapper.toLocation(dto);

        assertNotNull(location);
        assertNull(location.getId());
        assertEquals(dto.getLat(), location.getLat());
        assertEquals(dto.getLon(), location.getLon());
    }

    @Test
    void shouldConvertLocationToLocationDto() {
        Location location = Location.builder()
                .id(1L)
                .lat(55.75f)
                .lon(37.62f)
                .build();

        LocationDto dto = LocationMapper.toLocationDto(location);

        assertNotNull(dto);
        assertEquals(location.getLat(), dto.getLat());
        assertEquals(location.getLon(), dto.getLon());
    }
}
