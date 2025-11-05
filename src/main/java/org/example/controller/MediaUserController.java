package org.example.controller;

import org.example.repository.LoginRepository;
import org.example.repository.PdfRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/media")
@CrossOrigin(origins = "*")
public class MediaUserController {

    @Autowired
    private PdfRepository pdfRepository;
    @Autowired
    private LoginRepository loginRepository;

    @GetMapping("/verify/{username}")
    public ResponseEntity<String> verifyUser(@PathVariable String username) {
        boolean exists = loginRepository.existsByUsername(username);
        if (exists) {
            return ResponseEntity.ok("User exists in Media Service");
        } else {
            return ResponseEntity.status(404).body("User not found in Media Service");
        }
    }
}
