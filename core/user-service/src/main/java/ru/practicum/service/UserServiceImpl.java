package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserRequestDto;
import ru.practicum.dto.user.UserShortDto;
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

    @Override
    public List<UserShortDto> getUsers(List<Long> ids) {
        log.info("getUsers params: ids = {}", ids);
        return userRepository.findAllById(ids)
                .stream()
                .map(userMapper::toUserShortDto)
                .toList();
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.info("getUsers params: ids = {}, from = {}, size = {}", ids, from, size);
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);

        if (ids == null || ids.isEmpty()) {
            log.info("getUsers call: findAll");
            return userRepository.findAll(page)
                    .map(userMapper::toUserDto)
                    .getContent();
        }
        log.info("getUsers call: findAllByIdIn");
        return userRepository.findAllByIdIn(ids, page)
                .map(userMapper::toUserDto)
                .getContent();
    }

    @Override
    public UserShortDto getById(Long userId) {
        log.info("getById params: id = {}", userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(
                String.format("Пользователь с ид %s не найден", userId))
        );
        log.info("getById result user = {}", user);
        return userMapper.toUserShortDto(user);
    }

    @Override
    @Transactional
    public UserDto registerUser(UserRequestDto userRequestDto) {
        log.info("registerUser params: userRequestDto = {}", userRequestDto);
        User user = userRepository.save(userMapper.toEntity(userRequestDto));
        log.info("registerUser result user = {}", user);
        return userMapper.toUserDto(user);
    }
}
