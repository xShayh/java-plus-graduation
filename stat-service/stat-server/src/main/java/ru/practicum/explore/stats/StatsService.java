package ru.practicum.explore.stats;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.ViewStatsDto;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

        return unique
                ? statsRepository.getUniqueStats(startTime, endTime, uris)
                : statsRepository.getStats(startTime, endTime, uris);
    }
}
