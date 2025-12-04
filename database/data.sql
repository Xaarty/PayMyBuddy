INSERT INTO users (username, email, password, is_active)
VALUES
    ('frank', 'frank@example.com', '_-ta96zadftdcashhmpdrtash1', 1),
    ('jerome',   'jerome@example.com',   '@91jywne2rzcgerhammv-trhash2', 1), 
    ('bob', 'bob@example.com', '$51y8ulovcyaspa(vshh961getr', 1);

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
