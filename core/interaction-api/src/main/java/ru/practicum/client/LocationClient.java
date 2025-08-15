package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.events.LocationDto;

import java.util.List;

@FeignClient(name = "location-service", path = "/internal/api/locations")
public interface LocationClient {

    @PostMapping
    LocationDto addOrGetLocation(@RequestBody LocationDto locationDto);

    @GetMapping
    List<LocationDto> getByRadius(@RequestParam(name = "latitude") Double lat,
                                  @RequestParam(name = "longitude") Double lon,
                                  @RequestParam(name = "radius") Double radius);

    @GetMapping("/{locationId}")
    LocationDto getById(@PathVariable Long locationId);
}
