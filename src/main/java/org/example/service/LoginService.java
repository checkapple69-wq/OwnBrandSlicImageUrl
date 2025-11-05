package org.example.service;



import org.example.entity.Login;
import org.example.repository.LoginRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {

    private final LoginRepository loginRepository;

    public LoginService(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    public boolean authenticate(String username, String password) {
        Optional<Login> user = loginRepository.findByUsernameAndPassword(username, password);
        return user.isPresent();
    }

    public Login registerUser(Login login) {
        return loginRepository.save(login);
    }
}
