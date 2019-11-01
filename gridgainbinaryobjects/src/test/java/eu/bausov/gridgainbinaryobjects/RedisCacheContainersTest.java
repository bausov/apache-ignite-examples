package eu.bausov.gridgainbinaryobjects;

import com.google.gson.Gson;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;
import redis.clients.jedis.Jedis;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * Created by Stanislav Bausov on 01.11.2019.
 */
@SpringBootTest
class RedisCacheContainersTest {
    // rule {
    @Rule
    public GenericContainer redis = new GenericContainer<>("redis:5.0.3-alpine")
            .withExposedPorts(6379);
    private RedisBackedCache underTest;
    // }

    @BeforeEach
    void setUp() {
        redis.start();
        String address = redis.getContainerIpAddress();
        Integer port = redis.getFirstMappedPort();
        Jedis jedis = new Jedis(address, port);
        // Now we have an address and port for Redis, no matter where it is running
        underTest = new RedisBackedCache(jedis, "testCache");
    }

    @AfterEach
    void tearDown() {
        redis.stop();
    }

    @Test
    void testSimplePutAndGet() {
        underTest.put("test", "example");

        String retrieved = underTest.get("test", String.class).get();
        assertEquals("example", retrieved);
    }

    @Test
    void testSimplePutAndGet0() {
        underTest.put("test", "example");

        String retrieved = underTest.get("test", String.class).get();
        assertEquals("example", retrieved);
    }

    public static class RedisBackedCache {

        private final Jedis jedis;
        private final String cacheName;
        private final Gson gson;

        RedisBackedCache(Jedis jedis, String cacheName) {
            this.jedis = jedis;
            this.cacheName = cacheName;
            this.gson = new Gson();
        }

        void put(String key, Object value) {
            String jsonValue = gson.toJson(value);
            this.jedis.hset(this.cacheName, key, jsonValue);
        }

        <T> Optional<T> get(String key, Class<T> expectedClass) {
            String foundJson = this.jedis.hget(this.cacheName, key);

            if (foundJson == null) {
                return Optional.empty();
            }

            return Optional.of(gson.fromJson(foundJson, expectedClass));
        }
    }
}