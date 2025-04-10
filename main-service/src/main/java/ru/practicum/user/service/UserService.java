package ru.practicum.user.service;

import ru.practicum.user.dto.UserDto;
import ru.practicum.user.param.UserParams;

import java.util.List;

public interface UserService {

    List<UserDto> getUsers(UserParams userParams);

    UserDto addUser(UserDto userDto);

    void deleteUser(Integer userId);
}
