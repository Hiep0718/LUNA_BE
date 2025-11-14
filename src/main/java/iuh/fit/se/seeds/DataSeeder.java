package iuh.fit.se.seeds;


import iuh.fit.se.entities.Role;
import iuh.fit.se.entities.User;
import iuh.fit.se.repositories.RoleRepository;
import iuh.fit.se.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

// DataSeeder.java (Đã cập nhật)
@Component
public class DataSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role("ROLE_CUSTOMER"));
            roleRepository.save(new Role("ROLE_ADMIN"));
        }

        if (userRepository.count() == 0) {
            Role roleCustomer = roleRepository.findByName("ROLE_CUSTOMER").get();
            Role roleAdmin = roleRepository.findByName("ROLE_ADMIN").get();

            // 1. Admin
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setEmail("admin@example.com");
            admin.setRoles(Set.of(roleAdmin));
            userRepository.save(admin);

            // 2. Customer
            User customer = new User();
            customer.setUsername("customer");
            customer.setPassword(passwordEncoder.encode("123456"));
            customer.setEmail("customer@example.com");
            customer.setRoles(Set.of(roleCustomer));
            userRepository.save(customer);
        }
    }
}
