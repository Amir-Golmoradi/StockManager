package dev.amirgol.stockmanager.repository;

import dev.amirgol.stockmanager.model.User;
import dev.amirgol.stockmanager.model.enums.Role;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .roles(Set.of(Role.USER))
                .enabled(true)
                .build();
    }

    @Test
    void save_Success() {
        User saved = userRepository.save(testUser);
        entityManager.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("testuser");
    }

    @Test
    void findByUsername_Success() {
        entityManager.persist(testUser);
        entityManager.flush();

        Optional<User> found = userRepository.findByUsername("testuser");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByEmail_Success() {
        entityManager.persist(testUser);
        entityManager.flush();

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void existsByUsername_True() {
        entityManager.persist(testUser);
        entityManager.flush();

        assertThat(userRepository.existsByUsername("testuser")).isTrue();
    }

    @Test
    void existsByUsername_False() {
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    void existsByEmail_True() {
        entityManager.persist(testUser);
        entityManager.flush();

        assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
    }

    @Test
    void save_DuplicateUsername_ThrowsException() {
        entityManager.persist(testUser);
        entityManager.flush();

        User duplicate = User.builder()
                .username("testuser")
                .email("another@example.com")
                .password("password")
                .roles(Set.of(Role.USER))
                .build();

        assertThatThrownBy(() -> {
            userRepository.save(duplicate);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    void save_DuplicateEmail_ThrowsException() {
        entityManager.persist(testUser);
        entityManager.flush();

        User duplicate = User.builder()
                .username("anotheruser")
                .email("test@example.com")
                .password("password")
                .roles(Set.of(Role.USER))
                .build();

        assertThatThrownBy(() -> {
            userRepository.save(duplicate);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    void save_MultipleRoles_Success() {
        testUser.setRoles(Set.of(Role.USER, Role.MANAGER, Role.ADMIN));

        User saved = userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        User found = entityManager.find(User.class, saved.getId());
        assertThat(found.getRoles()).hasSize(3);
        assertThat(found.getRoles()).containsExactlyInAnyOrder(Role.USER, Role.MANAGER, Role.ADMIN);
    }
}