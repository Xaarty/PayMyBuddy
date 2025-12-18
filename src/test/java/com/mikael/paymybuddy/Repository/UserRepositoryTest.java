package com.mikael.paymybuddy.Repository;


import com.mikael.paymybuddy.Model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void find_user_by_email() {
        User user = new User();
        user.setUsername("anna");
        user.setEmail("anna@example.com");
        user.setPassword("hash"); // plus tard: hash BCrypt

        userRepository.save(user);

        Optional<User> result = userRepository.findByEmail("anna@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("anna");
    }

    @Test
    void existing_email() {
        User user = new User();
        user.setUsername("elise");
        user.setEmail("elise@example.com");
        user.setPassword("hash");
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("elise@example.com");

        assertThat(exists).isTrue();
    }


}
