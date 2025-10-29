package com.innowise.userservice.integration;

import com.innowise.userservice.UserServiceApplication;
import com.innowise.userservice.dto.UserRequest;
import com.innowise.userservice.dto.UserResponse;
import com.innowise.userservice.it.BaseIntegrationTest;
import com.innowise.userservice.model.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = UserServiceApplication.class)
@Testcontainers
@ActiveProfiles("test")
@Transactional
class CacheIntegrationTest extends BaseIntegrationTest {

    @Container
    static final GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getFirstMappedPort().toString());
    }

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        Cache cache = cacheManager.getCache("users");
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    void whenGetUserById_thenUserIsCached() {
        User saved = userRepository.save(
                new User("Alice", "Smith", LocalDate.of(1990, 1, 1), "alice@example.com")
        );

        UserResponse firstCall = userService.getUserById(saved.getId());
        UserResponse secondCall = userService.getUserById(saved.getId());

        assertThat(firstCall.getEmail()).isEqualTo(secondCall.getEmail());

        Cache.ValueWrapper wrapper = cacheManager.getCache("users").get(saved.getId());
        assertThat(wrapper).isNotNull();
        Object cachedObj = wrapper.get();
        assertThat(cachedObj).isInstanceOf(UserResponse.class);
        UserResponse cached = (UserResponse) cachedObj;
        assertThat(cached.getEmail()).isEqualTo(firstCall.getEmail());
    }

    @Test
    void whenUpdateUser_thenCacheIsEvicted() {
        // given
        User saved = userRepository.save(
                new User("Bob", "Johnson", LocalDate.of(1985, 5, 5), "bob@example.com")
        );
        userService.getUserById(saved.getId());
        assertThat(cacheManager.getCache("users").get(saved.getId())).isNotNull();

        UserRequest update = new UserRequest();
        update.setName("Bob");
        update.setSurname("Johnson");
        update.setBirthDate(LocalDate.of(1985, 5, 5));
        update.setEmail("bob.new@example.com");

        userService.updateUser(saved.getId(), update);

        assertThat(cacheManager.getCache("users").get(saved.getId()));
    }

    @Test
    void whenDeleteUser_thenCacheIsEvicted() {
        User saved = userRepository.save(
                new User("Charlie", "Brown", LocalDate.of(1978, 7, 7), "charlie@example.com")
        );
        userService.getUserById(saved.getId());
        assertThat(cacheManager.getCache("users").get(saved.getId())).isNotNull();

        userService.deleteUserById(saved.getId());

        assertThat(cacheManager.getCache("users").get(saved.getId()));
    }
}
