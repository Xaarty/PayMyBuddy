INSERT INTO users (username, email, password, is_active)
VALUES
    ('frank', 'frank@example.com', '$2a$10$WmQwQZ7H2J3F0c9kZl0M3OqUu2X7qf6H8zP6tPZQ3Yx9Zq7cS', 1),
    ('jerome',   'jerome@example.com',   '$2a$10$J8hE9r6mY2sYxZ8D3nK9fO0dJ8lZV2k7RrZ9q6bZtE7A3Hq4y', 1), 
    ('bob', 'bob@example.com', '$2a$10$Qx7F9KZsM6A1R2p5bZJ4dU7T6Yx8ZLk2C9m0VnP3EwH5aB', 1);

INSERT INTO user_connection (user_id, connection_id)
VALUES
    (1, 2),
    (2, 1),
    (1, 3),
    (3, 1);

INSERT INTO transactions (sender_id, receiver_id, description, amount)
VALUES
    (1, 2, 'Loyer', 700),
    (2, 1, 'Restaurant', 45),     
    (1, 3, 'Essence', 2); 
