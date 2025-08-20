package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.client.UserClient;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/internal/api/users")
@RequiredArgsConstructor
public class ClientController implements UserClient {
    private final UserService userService;

    @Override
    public UserShortDto getById(Long userId) {
        return userService.getById(userId);
    }

    @Override
    public List<UserShortDto> getByIds(List<Long> ids) {
        return userService.getUsers(ids);
    }
}
