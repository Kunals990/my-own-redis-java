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

    public void set(String key, String value,Integer time) {
        Instant setTime = Instant.now();
        store.put(key,new ValueWithExpiry(value,time,setTime));
    }

    public String get(String key) {
        ValueWithExpiry pair = store.get(key);
        if(pair==null) return null;

        if(!pair.isExpiryPresent()) return pair.getValue();

        if (pair.isExpired()) {
            store.remove(key);
            return null;
        }
        return pair.getValue();
    }

    public Set<String> getAllKeys(){
        return store.keySet();
    }

    public void clear() {
        store.clear();
    }
}



class ValueWithExpiry {
    private final String value;
    private final Integer expiryMillis;
    private final Instant createdAt;

    public ValueWithExpiry(String value, Integer time, Instant setTime) {
        this.value = value;
        this.expiryMillis = time;
        this.createdAt = setTime;
    }

    public boolean isExpiryPresent(){
        return expiryMillis != -1;
    }

    public boolean isExpired(){
        Instant now = Instant.now();
        long elapsed = Duration.between(createdAt, now).toMillis();

        return elapsed > expiryMillis;
    }

    public String getValue(){
        return this.value;
    }
}
