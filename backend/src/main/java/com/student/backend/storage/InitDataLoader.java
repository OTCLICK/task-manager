package com.student.backend.storage;

import com.student.backend.model.*;
import com.student.backend.repository.EventRepository;
import com.student.backend.repository.ParticipationRepository;
import com.student.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class InitDataLoader {

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            EventRepository eventRepository,
            ParticipationRepository participationRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userRepository.count() == 0) {
                User organizer = createUser(userRepository, passwordEncoder,
                        "organizer@example.com", "OrganizerPass1!",
                        "Организатор", "Организаторов", "Организаторович");

                User coordinator = createUser(userRepository, passwordEncoder,
                        "coordinator@example.com", "CoordinatorPass1!",
                        "Координатор", "Координаторов", "Координаторович");

                User performer = createUser(userRepository, passwordEncoder,
                        "performer@example.com", "PerformerPass1!",
                        "Исполнитель", "Исполнительев", "Исполнительевич");

                Event event = new Event(
                        "Тестовое мероприятие",
                        "г. Москва, ул. Тестовая, д. 1",
                        100,
                        EventStatus.PLANNED,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(30),
                        organizer
                );
                eventRepository.save(event);

                participationRepository.save(new Participation(organizer, event, UserRole.ORGANIZER));
                participationRepository.save(new Participation(coordinator, event, UserRole.COORDINATOR));
                participationRepository.save(new Participation(performer, event, UserRole.PERFORMER));

                System.out.println("Тестовые данные созданы:");
                System.out.println("Пользователи: organizer@example.com, coordinator@example.com, performer@example.com");
                System.out.println("Мероприятие: \"Тестовое мероприятие\"");
            }
        };
    }

    private User createUser(UserRepository userRepository, PasswordEncoder passwordEncoder,
                            String email, String password, String name, String surname, String patronymic) {
        User user = new User(
                email,
                passwordEncoder.encode(password),
                new FullName(name, surname, patronymic)
        );
        return userRepository.save(user);
    }
}