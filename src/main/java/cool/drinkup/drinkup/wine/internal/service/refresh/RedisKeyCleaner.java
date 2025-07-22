package cool.drinkup.drinkup.wine.internal.service.refresh;

import java.util.HashSet;
import java.util.Set;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

@Service
public class RedisKeyCleaner {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisKeyCleaner(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void deleteKeysByPrefix(String prefix) {
        Set<String> keys = scanKeys(prefix + "*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private Set<String> scanKeys(String pattern) {
        Set<String> keySet = new HashSet<>();
        ScanOptions options =
                ScanOptions.scanOptions().match(pattern).count(1000).build();

        try (var cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keySet.add(cursor.next());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while scanning keys", e);
        }
        return keySet;
    }
}
