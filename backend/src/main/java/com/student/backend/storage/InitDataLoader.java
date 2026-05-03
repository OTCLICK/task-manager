package com.student.backend.storage;

import com.student.backend.model.FullName;
import com.student.backend.model.User;
import com.student.backend.model.UserRole;
import com.student.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class InitDataLoader {
    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                createUser(userRepository, passwordEncoder, "organizer@example.com", "OrganizerPass1!",
                        "Организатор", "Организаторов", "Организаторович", UserRole.ORGANIZER);

                createUser(userRepository, passwordEncoder, "coordinator@example.com", "CoordinatorPass1!",
                        "Координатор", "Координаторов", "Координаторович", UserRole.COORDINATOR);

                createUser(userRepository, passwordEncoder, "performer@example.com", "PerformerPass1!",
                        "Исполнитель", "Исполнительев", "Исполнительевич", UserRole.PERFORMER);

                System.out.println("Тестовые пользователи созданы:");
                System.out.println("Организатор: organizer@example.com / OrganizerPass1!");
                System.out.println("Координатор: coordinator@example.com / CoordinatorPass1!");
                System.out.println("Исполнитель: performer@example.com / PerformerPass1!");
            }
        };
    }

    private void createUser(UserRepository userRepository, PasswordEncoder passwordEncoder,
                            String email, String password, String name, String surname,
                            String patronymic, UserRole role) {
        User user = new User(
                email,
                passwordEncoder.encode(password),
                new FullName(name, surname, patronymic),
                role
        );
        userRepository.save(user);
    }
}
