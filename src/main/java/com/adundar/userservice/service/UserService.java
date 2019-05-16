package com.adundar.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.adundar.userservice.bus.producer.MessageProducerService;
import com.adundar.userservice.exception.AlreadyExistException;
import com.adundar.userservice.exception.BadRequestException;
import com.adundar.userservice.exception.NotFoundException;
import com.adundar.userservice.model.Result;
import com.adundar.userservice.model.User;
import com.adundar.userservice.repository.UserRepository;
import com.adundar.userservice.utils.Utils;

@Service
public class UserService {

    @Autowired
    UserRepository         userRepository;

    @Autowired
    MessageProducerService messageProducerService;

    public Result<?> createUser(User user) throws AlreadyExistException {
        if (userRepository.findByName(user.getName()) != null)
            throw new AlreadyExistException(Utils.getUserNameAlreadyExistError(user.getName()));

        userRepository.save(user);

        messageProducerService.sendCreateUserEvent(user);

        return Result.success(HttpStatus.CREATED, user);
    }

    public Result<?> getAllUsers() {
        return Result.success(HttpStatus.OK, userRepository.findAll());
    }

    public Result<?> retrieveUser(String userId) throws NotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null)
            throw new NotFoundException(Utils.getUserIdNotFoundError(userId));

        return Result.success(HttpStatus.OK, user);
    }

    public Result<?> retrieveUserByName(String name) throws NotFoundException {
        User user = userRepository.findByName(name);
        if (user == null)
            throw new NotFoundException(Utils.getUserNameNotFoundError(name));

        return Result.success(HttpStatus.OK, user);
    }

    public Result<?> deleteUser(String userId) throws NotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null)
            throw new NotFoundException(Utils.getUserIdNotFoundError(userId));

        userRepository.delete(user);

        messageProducerService.sendDeleteUserEvent(user);

        return Result.success(HttpStatus.OK, user);
    }

    public Result<?> updateUser(User user, String userId) throws BadRequestException, NotFoundException {
        User userOld = userRepository.findOne(userId);
        if (userOld == null)
            throw new NotFoundException(Utils.getUserIdNotFoundError(userId));

        if (!userOld.getName().equals(user.getName()))
            throw new BadRequestException(Utils.USER_NAME_CHANGE_ERROR);

        user.setId(userOld.getId());

        User updatedUser = userRepository.save(user);

        messageProducerService.sendUpdateUserEvent(updatedUser);

        return Result.success(HttpStatus.OK, updatedUser);
    }
}
