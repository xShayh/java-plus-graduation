package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.param.UserParams;
import ru.practicum.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Override
    public UserDto addUser(UserDto userDto) {
        log.info("Beginning create new user");
        User user = userRepository.save(userMapper.toUser(userDto));
        log.info("User with ID= {} has been created", user.getId());
        return userMapper.toUserDto(user);
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with ID= " + userId + " not found");
        }
        userRepository.deleteById(userId);
        log.info("User with ID= {} has been deleted", userId);
    }

    @Override
    public List<UserDto> getUsers(UserParams userParam) {
        Pageable page = PageRequest.of(userParam.getFrom() / userParam.getSize(), userParam.getSize());
        return userParam.getIds() != null && !userParam.getIds().isEmpty() ?
                userRepository.findAllById(userParam.getIds()).stream().map(userMapper::toUserDto).toList() :
                userRepository.findAll(page).stream().map(userMapper::toUserDto).toList();
    }
}
