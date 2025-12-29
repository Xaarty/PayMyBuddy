package com.mikael.paymybuddy.Service;

import com.mikael.paymybuddy.DTO.ProfileUpdateDTO;
import com.mikael.paymybuddy.DTO.UserListDTO;
import com.mikael.paymybuddy.DTO.UserRegistrationDTO;
import com.mikael.paymybuddy.Model.User;
import com.mikael.paymybuddy.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    //Inscription
    @Transactional
    public User registerUser(UserRegistrationDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("email already exist");
        }

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("username already exist");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());

        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        user.setPassword(hashedPassword);

        user.setActive(true);

        return userRepository.save(user);
    }

    //Connexion
    public boolean userAuthenticate(String email, String password) {
        // Récupèration de l'utilisateur à partir de l'email saisi
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) return false;
        //vérification de l'existance du mot de passe
        String stored = user.get().getPassword();
        // évite le WARN + refuse les anciens passwords non-hashés
        if (stored == null || !stored.startsWith("$2a$") && !stored.startsWith("$2b$") && !stored.startsWith("$2y$")) {
            return false;
        }


        return passwordEncoder.matches(password, stored);
    }

    @Transactional
    public User updateProfile(Long userId, ProfileUpdateDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Si le username est renseigné et différent, vérifier l'unicité
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            if (!user.getUsername().equals(dto.getUsername())
                    && userRepository.existsByUsername(dto.getUsername())) {
                throw new IllegalArgumentException("Username already in use");
            }
            user.setUsername(dto.getUsername());
        }

        // Si l'email est renseigné et différent, vérifier l'unicité
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            if (!user.getEmail().equals(dto.getEmail())
                    && userRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email already in use");
            }
            user.setEmail(dto.getEmail());
        }

        // Si le mot de passe est renseigné et différent, vérifier l'unicité
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public void addConnection(Long userId, Long connectionId) {
        // Règle d'unicitée d'user
        if (userId.equals(connectionId)) {
            throw new IllegalArgumentException("Vous ne pouvez pas vous ajouter en tant qu'amis");
        }

        User user = userRepository.findById(userId).orElseThrow();
        User connection = userRepository.findById(connectionId).orElseThrow();

        // Vérifie que la relation n'existe pas déjà
        if (user.getConnections().contains(connection)) {
            throw new IllegalStateException("Ce user est déjà votre amis");
        }

        user.getConnections().add(connection);
        userRepository.save(user);
    }

    @Transactional
    public User rechargeAccount(Long userId, BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être supérieur à 0.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        user.setBalance(user.getBalance().add(amount));
        return userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(Long userId) {
        User user = userRepository.findByIdAndActiveFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable ou déjà actif"));
        user.setActive(true);
        userRepository.save(user);
    }
}

