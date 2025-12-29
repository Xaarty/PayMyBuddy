package com.mikael.paymybuddy.DTO;

import com.mikael.paymybuddy.Model.User;

public class UserListDTO {
    private Long id;
    private String username;
    private String email;

    public UserListDTO() {}

    public UserListDTO(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public static UserListDTO fromUser(User user) {
        return new UserListDTO(user.getId(), user.getUsername(), user.getEmail());
    }

    // Getters requis pour la sérialisation JSON
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}