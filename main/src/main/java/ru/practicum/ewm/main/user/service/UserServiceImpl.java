package ru.practicum.ewm.main.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.user.User;
import ru.practicum.ewm.main.user.UserRepository;
import ru.practicum.ewm.main.user.dto.NewUserRequest;
import ru.practicum.ewm.main.user.dto.UserDto;
import ru.practicum.ewm.main.user.dto.UserMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto registerUser(NewUserRequest newUserRequest) {

        User user = UserMapper.toUser(newUserRequest);

        try {
            return UserMapper.toUserDto(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Email must be unique");
        }
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids,
                                  Integer from,
                                  Integer size) {

        if (ids != null && !ids.isEmpty()) {

            return userRepository.findAllById(ids)
                    .stream()
                    .map(UserMapper::toUserDto)
                    .toList();
        }

        return userRepository.findAll(PageRequest.of(from / size, size))
                .stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        userRepository.deleteById(userId);
    }
}