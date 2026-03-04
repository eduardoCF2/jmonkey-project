package com.TFG1.service;

import com.TFG1.exception.GameException;
import com.TFG1.model.User;
import com.TFG1.repository.UserRepository;

public class AuthService {

    private final UserRepository userRepository = new UserRepository();

    // Validar si un usuario existe

    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new GameException("USER_NOT_FOUND");
        }

        // Comprobar contraseña por texto plano OJO QUE HAY QUE USAR OTRA COSA PARA
        // ENCRIPTAR
        if (!user.getPassword().equals(password)) {
            throw new GameException("ERROR_LOGIN");
        }

        // Si es correcto el flujo devuelve el usuario con el TOKEN
        return user;
    }

    // Registrar un nuevo usuario
    public User register(String username, String password) {
        // Validar si el usuario ya existe
        if (userRepository.existsByUsername(username)) {
            throw new GameException("USER_EXISTS_ERROR");
        }

        // Si no existe, creamos la instancia del modelo User
        // El constructor de User debería asignar las monedas iniciales (ej: 100)
        User newUser = new User(username, password);

        // Guardar en postgres
        userRepository.save(newUser);

        // Devolvemos el usuario recién creado (incluyendo su nueva ID de base de
        // datos)
        return newUser;
    }
}