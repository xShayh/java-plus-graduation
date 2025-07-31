package ru.practicum.explore.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.ViewStatsDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stats")
@Slf4j
public class StatsController {
    private final StatsService statsService;

    @GetMapping
    public List<ViewStatsDto> getStats(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique) {
        List<ViewStatsDto> viewStatsDtoList = statsService.getStats(start, end, uris, unique);
        log.info(viewStatsDtoList.toString());
        return viewStatsDtoList;
    }
}