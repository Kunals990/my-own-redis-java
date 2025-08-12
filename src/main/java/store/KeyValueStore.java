package store;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyValueStore {
    private static final KeyValueStore INSTANCE = new KeyValueStore();

    private final Map<String, String> store = new HashMap<>();

    private KeyValueStore() {}

    public static KeyValueStore getInstance() {
        return INSTANCE;
    }

    public void set(String key, String value) {
        store.put(key,value);
    }

    public String get(String key) {
        return store.get(key);
    }

    public void clear() {
        store.clear();
    }
}
