package com.mikael.paymybuddy.Service;

import com.mikael.paymybuddy.DTO.ProfileUpdateDTO;
import com.mikael.paymybuddy.DTO.UserRegistrationDTO;
import com.mikael.paymybuddy.Model.User;
import com.mikael.paymybuddy.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void save_with_hashed_password() {

        UserRegistrationDTO dto = new UserRegistrationDTO(
                "Léa",
                "Léa@example.com",
                "clearPassword"
        );

        User saved = userService.registerUser(dto);

        User reloaded = userRepository.findByEmail("Léa@example.com").orElseThrow();

        assertThat(reloaded.getId()).isNotNull();
        assertThat(reloaded.getUsername()).isEqualTo("Léa");

        assertThat(reloaded.getPassword()).isNotEqualTo("clearPassword");
        assertThat(reloaded.getPassword()).isNotBlank();
    }

    @Test
    void authenticate() {

        User user = new User();

        user.setUsername("Kevin");
        user.setEmail("kevin@example.com");
        user.setPassword(passwordEncoder.encode("123456789"));
        user.setActive(true);

        userRepository.save(user);
        boolean result = userService.userAuthenticate("kevin@example.com", "123456789");

        assertThat(result).isTrue();
    }

    @Test
    void invalid_password() {

        User user = new User();

        user.setUsername("Kevin");
        user.setEmail("kevin@example.com");
        user.setPassword(passwordEncoder.encode("123456789"));
        user.setActive(true);

        userRepository.save(user);
        boolean result = userService.userAuthenticate("kevin@example.com", "12345678");

        assertThat(result).isFalse();
    }

    @Test
    void invalid_email() {
        // Tentative de connexion avec un email qui n'existe pas
        boolean result = userService.userAuthenticate("incorect@example.com", "1245");
        // Echec d'authentification attendu
        assertThat(result).isFalse();
    }

    @Test
    void update_user() {
        // Création d’un utilisateur initial
        User user = new User();
        user.setUsername("Yohan");
        user.setEmail("yohan@example.com");
        user.setPassword(passwordEncoder.encode("1234"));
        user.setActive(true);
        userRepository.save(user);

        ProfileUpdateDTO dto = new ProfileUpdateDTO(
                "didier",
                "didier@example.com",
                "123"
        );

        User updated = userService.updateProfile(user.getId(), dto);

        assertThat(updated.getUsername()).isEqualTo("didier");
        assertThat(updated.getEmail()).isEqualTo("didier@example.com");
        assertThat(passwordEncoder.matches("123", updated.getPassword())).isTrue();
    }

    @Test
    void add_connection() {
        User userA = new User();
        userA.setUsername("fred");
        userA.setEmail("fred@example.com");
        userA.setPassword("1");
        userA.setActive(true);

        User userB = new User();
        userB.setUsername("jacky");
        userB.setEmail("jacky@example.com");
        userB.setPassword("2");
        userB.setActive(true);

        userRepository.save(userA);
        userRepository.save(userB);

        userService.addConnection(userA.getId(), userB.getId());

        User updated = userRepository.findById(userA.getId()).orElseThrow();
        assertThat(updated.getConnections()).extracting(User::getId).contains(userB.getId());
    }

    @Test
    void no_self_connection() {
        User user = new User();
        user.setUsername("Lilan");
        user.setEmail("lilan@example.com");
        user.setPassword("hashed");
        user.setActive(true);
        userRepository.save(user);

        assertThatThrownBy(() -> userService.addConnection(user.getId(), user.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Vous ne pouvez pas vous ajouter en tant qu'amis");
    }

    @Test
    void no_add_duplicate() {
        User userA = new User();
        userA.setUsername("Luca");
        userA.setEmail("luca@example.com");
        userA.setPassword("hashed");
        userA.setActive(true);

        User userB = new User();
        userB.setUsername("Louanna");
        userB.setEmail("louanna@example.com");
        userB.setPassword("hashed");
        userB.setActive(true);

        userA.getConnections().add(userB);

        userRepository.save(userB);
        userRepository.save(userA);

        assertThatThrownBy(() -> userService.addConnection(userA.getId(), userB.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ce user est déjà votre amis");
    }

    @Test
    void success_recharge() {
        // Cherche le user authorizé
        User dev = userRepository.findById(1L).orElseThrow();
        dev.setBalance(BigDecimal.ZERO); // Reset balance pour le test
        userRepository.save(dev);

        BigDecimal rechargeAmount = new BigDecimal("50.00");
        userService.rechargeAccount(1L, rechargeAmount);

        User recharged = userRepository.findById(1L).orElseThrow();
        assertThat(recharged.getBalance()).isEqualByComparingTo("50.00");
    }

    @Test
    void recharge_fails_not_available_for_user() {

        User user = new User();
        user.setUsername("user");
        user.setEmail("dev@example.com");
        user.setPassword("123");
        user.setActive(true);
        user.setBalance(BigDecimal.ZERO);

        userRepository.save(user);

        assertThatThrownBy(() -> userService.rechargeAccount(user.getId(), new BigDecimal("10")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Recharge non disponible");
    }

    @Test
    void recharge_fails_invalid_amount() {
        // Cherche le user authorizé
        User dev = userRepository.findById(1L).orElseThrow();

        assertThatThrownBy(() -> userService.rechargeAccount(1L, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Le montant doit être supérieur à 0.");
    }
}
