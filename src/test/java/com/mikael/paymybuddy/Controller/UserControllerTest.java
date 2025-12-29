package com.mikael.paymybuddy.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikael.paymybuddy.DTO.LoginDTO;
import com.mikael.paymybuddy.DTO.RechargeDTO;
import com.mikael.paymybuddy.DTO.UserRegistrationDTO;
import com.mikael.paymybuddy.DTO.ProfileUpdateDTO;
import com.mikael.paymybuddy.Model.User;
import com.mikael.paymybuddy.Repository.UserRepository;
import com.mikael.paymybuddy.Service.UserService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void should_register_user_successfully() throws Exception {
        UserRegistrationDTO dto = new UserRegistrationDTO("alice", "alice@example.com", "pass123");

        User mockUser = new User();
        mockUser.setUsername("alice");

        when(userService.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userService.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userService.registerUser(any())).thenReturn(mockUser);

        mockMvc.perform(post("/api/inscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Bienvenue dans Paymybuddyalice ! Votre compte a été créé avec succès !"));
    }

    @Test
    void should_fail_when_email_already_exists() throws Exception {
        UserRegistrationDTO dto = new UserRegistrationDTO("john", "john@example.com", "secret");

        when(userService.existsByEmail(dto.getEmail())).thenReturn(true);

        mockMvc.perform(post("/api/inscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username or Email already in use."));
    }

    @Test
    void should_authenticate_user_successfully() throws Exception {
        LoginDTO loginDTO = new LoginDTO("john@example.com", "secret");

        when(userService.userAuthenticate(loginDTO.getEmail(), loginDTO.getPassword())).thenReturn(true);

        mockMvc.perform(post("/api/connexion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Login successful"));
    }

    @Test
    void should_return_unauthorized_on_invalid_login() throws Exception {
        LoginDTO loginDTO = new LoginDTO("john@example.com", "wrongpassword");

        when(userService.userAuthenticate(loginDTO.getEmail(), loginDTO.getPassword())).thenReturn(false);

        mockMvc.perform(post("/api/connexion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid email or password"));
    }

    @Test
    void should_update_user_profile_successfully() throws Exception {
        // Étape 1 : Identifiant cible
        Long userId = 1L;

        // Étape 2 : Préparer la réponse simulée
        User mockUpdatedUser = new User();
        mockUpdatedUser.setId(userId);
        mockUpdatedUser.setUsername("updatedUser");
        mockUpdatedUser.setEmail("updated@example.com");
        mockUpdatedUser.setPassword("hashedPassword");

        // Étape 3 : Corps JSON
        String jsonUpdate = """
        {
          "username": "updatedUser",
          "email": "updated@example.com",
          "password": "newPass123"
        }
    """;

        // Étape 4 : Simuler le comportement du service
        when(userService.updateProfile(eq(userId), any())).thenReturn(mockUpdatedUser);

        // Étape 5 : Requête MockMvc
        mockMvc.perform(put("/api/profile/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdate))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("updatedUser")));
    }

    @Test
    void should_add_connection_via_api() throws Exception {
        User userA = new User("Rick", "rick" + UUID.randomUUID() + "@example.com", "hashed");
        userA.setActive(true);
        userRepository.save(userA);

        User userB = new User("Mike", "mike" + UUID.randomUUID() + "@example.com", "hashed");
        userB.setActive(true);
        userRepository.save(userB);

        mockMvc.perform(post("/api/users/" + userA.getId() + "/connections/" + userB.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Connection added")));
    }

    @Test
    void should_fail_to_add_self_as_connection_via_api() throws Exception {
        // Création de l'utilisateur
        User user = new User("self", "self@example.com", "hashed");
        user.setActive(true);
        userRepository.save(user);

        // Mock du comportement pour lever l'exception
        doThrow(new IllegalArgumentException("You cannot add yourself as a connection."))
                .when(userService).addConnection(user.getId(), user.getId());

        // Appel au contrôleur
        mockMvc.perform(post("/api/users/" + user.getId() + "/connections/" + user.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("cannot add yourself")));
    }

    @Test
    void Recharge_success() throws Exception {

        Long userId = 1L;
        RechargeDTO dto = new RechargeDTO(new BigDecimal("20.00"));

        mockMvc.perform(put("/api/users/" + userId + "/recharge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Recharge effectuée avec succès."));

        verify(userService).rechargeAccount(userId, dto.getAmount());
    }

    @Test
    void Recharge_fail_invalid_amount() throws Exception {

        Long userId = 1L;
        RechargeDTO dto = new RechargeDTO(new BigDecimal("-1.00"));

        Mockito.doThrow(new IllegalArgumentException("Montant invalide"))
                .when(userService).rechargeAccount(userId, dto.getAmount());

        mockMvc.perform(put("/api/users/" + userId + "/recharge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Montant invalide"));
    }

    @Test
    void Update_profile() throws Exception {
        Long userId = 1L;
        ProfileUpdateDTO dto = new ProfileUpdateDTO("Laurent", "new@mail.com", "123");

        User updated = new User();
        updated.setId(userId);
        updated.setUsername("Laurent");
        updated.setEmail("new@mail.com");  // 👈 Ajoute bien l'email aussi

        Mockito.when(userService.updateProfile(eq(userId), any(ProfileUpdateDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/profile/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Profil mis à jour pour : Laurent")));  // match plus précis
    }

    @Test
    void Update_profile_failure() throws Exception {

        Long userId = 1L;
        ProfileUpdateDTO dto = new ProfileUpdateDTO("", "", "");

        Mockito.when(userService.updateProfile(eq(userId), any(ProfileUpdateDTO.class)))
                .thenThrow(new IllegalArgumentException("Tous les champs doivent être remplis."));

        mockMvc.perform(put("/api/profile/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Tous les champs doivent être remplis."));
    }

    @Test
    void test_connections_success() throws Exception {
        // Création du user principal
        User mainUser = new User();
        mainUser.setUsername("main");
        mainUser.setEmail("main@example.com");
        mainUser.setPassword("123");
        mainUser.setActive(true);

        // Création d’un ami
        User friend = new User();
        friend.setUsername("friend");
        friend.setEmail("friend@example.com");
        friend.setPassword("123");
        friend.setActive(true);

        // Sauvegarde les deux
        userRepository.save(friend);
        mainUser.getConnections().add(friend);
        userRepository.save(mainUser); // le main user avec sa connexion

        mockMvc.perform(get("/api/users/" + mainUser.getId() + "/connections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("friend"));
    }

    @Test
    void deactivate_user() throws Exception {
        Long userId = 1L;

        // On ne vérifie pas l'effet mais la réponse
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("Compte désactivé avec succès."));

        verify(userService).deactivateUser(userId);
    }

    @Test
    void deactivation_fails () throws Exception {
        Long nonExistentId = 999L;
        doThrow(new IllegalArgumentException("Utilisateur introuvable"))
                .when(userService).deactivateUser(nonExistentId);

        mockMvc.perform(delete("/api/users/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Utilisateur introuvable")));
    }

    @Test
    void activate_user() throws Exception {
        Long userId = 2L;

        mockMvc.perform(patch("/api/users/{id}/enable", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("Utilisateur réactivé avec succès."));

        verify(userService).activateUser(userId);
    }

    @Test
    void activation_fails() throws Exception {
        Long userId = 99L;
        doThrow(new IllegalArgumentException("Utilisateur introuvable ou déjà actif"))
                .when(userService).activateUser(userId);

        mockMvc.perform(patch("/api/users/{id}/enable", userId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Utilisateur introuvable ou déjà actif")));
    }
}
