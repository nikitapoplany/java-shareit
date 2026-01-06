package ru.practicum.shareit.user.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.user.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Интеграционные тесты для {@link UserRepository}
 */
@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    /**
     * Тест на сохранение и получение пользователя.
     * Проверяет, что пользователь успешно сохраняется в базу данных и может быть получен по ID.
     */
    @Test
    void saveAndFindUser_ShouldWork() {
        // Подготовка
        User user = new User(null, "Тестовый Пользователь", "test@example.com");

        // Действие
        User savedUser = userRepository.save(user);
        User foundUser = userRepository.findById(savedUser.getId()).orElse(null);

        // Проверка
        assertNotNull(savedUser.getId());
        assertNotNull(foundUser);
        assertEquals(savedUser.getId(), foundUser.getId());
        assertEquals("Тестовый Пользователь", foundUser.getName());
        assertEquals("test@example.com", foundUser.getEmail());
    }
}