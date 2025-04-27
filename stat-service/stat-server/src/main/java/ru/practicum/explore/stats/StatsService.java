package ru.practicum.explore.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.explore.exception.EventDateValidationException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {
    private final StatsRepository statsRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
        String decodedStart = URLDecoder.decode(start, StandardCharsets.UTF_8);
        String decodedEnd = URLDecoder.decode(end, StandardCharsets.UTF_8);

        LocalDateTime startTime = LocalDateTime.parse(decodedStart, FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse(decodedEnd, FORMATTER);
        log.info("StartTime={}, endTime={}", startTime, endTime);

        if (startTime.isAfter(endTime)) {
            throw new EventDateValidationException("Start date should be before end");
        }

        return unique
                ? statsRepository.getUniqueStats(startTime, endTime, uris)
                : statsRepository.getStats(startTime, endTime, uris);
    }
}
