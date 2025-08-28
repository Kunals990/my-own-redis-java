package store;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class KeyValueStore {
    private static final KeyValueStore INSTANCE = new KeyValueStore();
    private final Map<String, ValueWithExpiry> store = new HashMap<>();

    private KeyValueStore() {}

    public static KeyValueStore getInstance() {
        return INSTANCE;
    }

    public void set(String key, String value, long expiryMillis) {
        store.put(key, new ValueWithExpiry(value, expiryMillis));
    }

    public String get(String key) {
        ValueWithExpiry pair = store.get(key);
        if (pair == null) {
            return null;
        }

        if (pair.isExpired()) {
            store.remove(key);
            return null;
        }
        return pair.getValue();
    }

    public Set<String> getAllKeys() {
        store.entrySet().removeIf(entry -> entry.getValue().isExpired());
        return store.keySet();
    }

    public void clear() {
        store.clear();
    }
}



class ValueWithExpiry {
    private final String value;
    private final long expiryMillis;

    public ValueWithExpiry(String value, long expiryMillis) {
        this.value = value;
        this.expiryMillis = expiryMillis;
    }

    public boolean isExpiryPresent(){
        return expiryMillis != -1;
    }

    public boolean isExpired() {
        if (expiryMillis == -1) {
            return false;
        }
        return System.currentTimeMillis() > expiryMillis;
    }

    public String getValue() {
        return this.value;
    }
}
