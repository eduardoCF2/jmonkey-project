package com.TFG1.service;

import com.TFG1.exception.GameException;
import com.TFG1.model.User;
import com.TFG1.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private final UserRepository userRepository = new UserRepository();

    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new GameException("USER_NOT_FOUND");
        }

        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new GameException("ERROR_LOGIN");
        }
        return user;
    }

    public User register(String username, String password) {

        if (userRepository.existsByUsername(username)) {
            throw new GameException("USER_EXISTS_ERROR");
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        User newUser = new User(username, hashedPassword);

        userRepository.save(newUser);

        return newUser;
    }
}