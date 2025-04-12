package ru.practicum.user.mapper;

import org.mapstruct.Mapper;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

@Mapper
public interface UserMapper {

    UserDto toUserDto(User user);

    User toUser(UserDto userDto);

    UserShortDto toUserShortDro(User user);
}
