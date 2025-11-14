package iuh.fit.se.services;


import iuh.fit.se.entities.User;

import java.util.Optional;

public interface UserService {
    public Optional<User> findByUsername(String username);
    public boolean existsByUsername(String username);
    public void save(User user);
}
