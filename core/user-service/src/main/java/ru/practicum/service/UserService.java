package ru.practicum.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserRequestDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.param.UserParams;

import java.util.List;

@Transactional(readOnly = true)
public interface UserService {

    @Transactional
    UserDto addUser(UserDto userDto);

    @Transactional
    void deleteUser(Long userId);

    List<UserDto> getUsers(UserParams userParams);

    public List<UserShortDto> getUsers(List<Long> ids);

    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    public UserShortDto getById(Long userId);

    public UserDto registerUser(UserRequestDto userRequestDto);
}
