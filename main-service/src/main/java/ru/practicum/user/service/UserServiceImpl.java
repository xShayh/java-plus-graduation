package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.param.UserParams;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getUsers(UserParams userParams) {
        if (userParams.getIds() != null && !userParams.getIds().isEmpty()) {
            return userRepository.findAllById(userParams.getIds()).stream()
                    .map(userMapper::toUserDto)
                    .toList();
        } else {
            Pageable page = PageRequest.of(userParams.getFrom() / userParams.getSize(), userParams.getSize());
            Page<User> userPage = userRepository.findAll(page);
            return userPage.stream()
                    .map(userMapper::toUserDto)
                    .toList();
        }
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        User user = userMapper.toUser(userDto);
        userRepository.save(user);
        log.info("User with ID= {} has been created", user.getId());
        return userMapper.toUserDto(user);
    }

    @Override
    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with ID= " + userId + "not found");
        }
        userRepository.deleteById(userId);
        log.info("User with ID= {} has been deleted", userId);
    }

    private UserDto findUser(Integer userId) {
        return userMapper.toUserDto(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID= " + userId + "not found")));
    }
}
