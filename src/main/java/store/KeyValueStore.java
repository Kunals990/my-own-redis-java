package store;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class KeyValueStore {
    private static final KeyValueStore INSTANCE = new KeyValueStore();
    private final Map<String, Object> store = new HashMap<>();

    private KeyValueStore() {}

    public static KeyValueStore getInstance() {
        return INSTANCE;
    }

    public void set(String key, String value, long expiryMillis) {
        store.put(key, new ValueWithExpiry(value, expiryMillis));
    }

    public String get(String key) {
        Object value = store.get(key);

        if (value == null || !(value instanceof ValueWithExpiry)) {
            return null;
        }
        ValueWithExpiry valueWithExpiry = (ValueWithExpiry) value;

        if (valueWithExpiry.isExpired()) {
            store.remove(key);
            return null;
        }
        return valueWithExpiry.getValue();
    }


    public Set<String> getAllKeys() {
        store.entrySet().removeIf(entry -> {
            Object value = entry.getValue();
            if (value instanceof ValueWithExpiry) {
                return ((ValueWithExpiry) value).isExpired();
            }
            return false;
        });
        return store.keySet();
    }

    public void clear() {
        store.clear();
    }

    public int zadd(String key, double score, String member) {
        Object existingValue = store.get(key);
        TreeSet<MemberScore> sortedSet;

        if (existingValue == null) {
            sortedSet = new TreeSet<>();
            store.put(key, sortedSet);
        } else if (existingValue instanceof TreeSet) {
            sortedSet = (TreeSet<MemberScore>) existingValue;
        } else {
            throw new ClassCastException("Operation against a key holding the wrong kind of value");
        }

        MemberScore newMember = new MemberScore(member, score);

        boolean wasPresent = sortedSet.remove(newMember);

        sortedSet.add(newMember);

        return wasPresent ? 0 : 1;
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
