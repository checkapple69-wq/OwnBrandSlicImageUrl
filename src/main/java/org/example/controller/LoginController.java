package org.example.controller;



import org.example.entity.Login;
import org.example.service.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Login login) {
        loginService.registerUser(login);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Login login) {
        boolean isAuthenticated = loginService.authenticate(login.getUsername(), login.getPassword());
        if (isAuthenticated) {
            return ResponseEntity.ok("Login successful! Welcome, " + login.getUsername());
        } else {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }
}
