package com.mikael.paymybuddy.Controller;


import com.mikael.paymybuddy.DTO.*;
import com.mikael.paymybuddy.Model.User;
import com.mikael.paymybuddy.Service.UserService;
import com.mikael.paymybuddy.Repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/inscription")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDTO dto) {
        // Vérification d’unicité de l’email et du username
        if (userService.existsByEmail(dto.getEmail()) || userService.existsByUsername(dto.getUsername())) {
            return ResponseEntity.badRequest().body("Username or Email already in use.");
        }

        User user = userService.registerUser(dto);
        return ResponseEntity.ok("Bienvenue dans Paymybuddy" + user.getUsername() + " ! Votre compte a été créé avec succès !");
    }

    @PostMapping("/connexion")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {

        boolean authenticated = userService.userAuthenticate(loginDTO.getEmail(), loginDTO.getPassword());

        if (authenticated) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }

    @PutMapping("/profile/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id,
                                           @RequestBody ProfileUpdateDTO dto) {
        try {
            User updatedUser = userService.updateProfile(id, dto);
            return ResponseEntity.ok("Profil mis à jour pour : " + updatedUser.getUsername());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        }
    }

    @PostMapping("/users/{id}/connections/{connectionId}")
    public ResponseEntity<?> addConnection(@PathVariable Long id, @PathVariable Long connectionId) {
        try {
            userService.addConnection(id, connectionId);
            return ResponseEntity.ok("Connection added successfully with user ID: " + connectionId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/users/{id}/recharge")
    public ResponseEntity<?> recharge(@PathVariable Long id, @RequestBody RechargeDTO dto) {
        try {
            userService.rechargeAccount(id, dto.getAmount());
            return ResponseEntity.ok("Recharge effectuée avec succès.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/users/{id}/connections")
    public ResponseEntity<?> getConnections(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        List<User> connections = user.getConnections();

        List<UserListDTO> connectionDTOs = connections.stream()
                .map(UserListDTO::fromUser)
                .toList();

        return ResponseEntity.ok(connectionDTOs);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deactivateAccount(@PathVariable Long id) {
        try {
            userService.deactivateUser(id);
            return ResponseEntity.ok("Compte désactivé avec succès.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PatchMapping("/users/{id}/enable")
    public ResponseEntity<?> enable(@PathVariable Long id) {
        try {
            userService.activateUser(id);
            return ResponseEntity.ok("Utilisateur réactivé avec succès.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}