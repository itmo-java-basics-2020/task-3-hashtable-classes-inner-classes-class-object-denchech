package ru.itmo.java;

import java.util.Optional;

public class HashTable {
    private final static int RESIZE_FACTOR = 2;
    private final static int PROBING_INTERVAL = 1;
    private final static float INITIAL_LOAD_FACTOR = 0.5f;
    private final static int MAX_SIZE = Integer.MAX_VALUE - 8;

    private Entry[] elements;
    private boolean[] deletedElements;
    private int size = 0;
    private int capacity;
    private float loadFactor;
    private int threshold;

    public HashTable(int capacity) {
        this(capacity, INITIAL_LOAD_FACTOR);
    }

    public HashTable(int capacity, float loadFactor) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Cannot create a hash table with zero capacity");
        }
        if (loadFactor - 1e-9f > 1.0f) {
            throw new IllegalArgumentException("Cannot create a hash table with loading Factor, that is more than 1");
        }
        this.elements = new Entry[capacity];
        this.deletedElements = new boolean[capacity];
        this.capacity = capacity;
        this.loadFactor = loadFactor;
        this.threshold = (int) (capacity * loadFactor);
    }

    public Object put(Object key, Object value) {
        if (size == MAX_SIZE) {
            throw new RuntimeException("You cannot put more object");
        }
        Entry newElement = new Entry(key, value);
        int foundIndex = getIndexByKey(key);
        if (elements[foundIndex] == null) {
            foundIndex = getHash(key);
            while (elements[foundIndex] != null) {
                foundIndex = (foundIndex + PROBING_INTERVAL) % capacity;
            }
            deletedElements[foundIndex] = false;
            elements[foundIndex] = newElement;
            ++size;
            if (size >= threshold) {
                resize();
            }
            return null;
        }
        Object oldValue = elements[foundIndex].getValue();
        elements[foundIndex] = newElement;
        return oldValue;
    }

    public Object get(Object key) {
        return Optional.ofNullable(elements[getIndexByKey(key)]).map(Entry::getValue).orElse(null);
    }

    public Object remove(Object key) {
        int foundIndex = getIndexByKey(key);
        if (elements[foundIndex] == null) {
            return null;
        }
        deletedElements[foundIndex] = true;
        Object oldValue = elements[foundIndex].getValue();
        --size;
        elements[foundIndex] = null;
        return oldValue;
    }

    public int size() {
        return size;
    }

    private void resize() {
        if (size >= threshold) {
            capacity *= RESIZE_FACTOR;
            threshold = (int) (capacity * loadFactor);
            Entry[] oldElements = elements;
            elements = new Entry[capacity];
            deletedElements = new boolean[capacity];
            size = 0;
            for (Entry currentElement : oldElements) {
                if (currentElement != null) {
                    put(currentElement.getKey(), currentElement.value);
                }
            }
        }
    }

    private int getHash(Object key) {
        return Math.abs(key.hashCode()) % capacity;
    }

    private int getIndexByKey(Object key) {
        int hashKey = getHash(key);
        Entry foundElement = elements[hashKey];
        while (deletedElements[hashKey]
                || (foundElement != null && !foundElement.getKey().equals(key))
        ) {
            hashKey = (hashKey + PROBING_INTERVAL) % capacity;
        }
        return hashKey;
    }

    private final static class Entry {
        private Object key;
        private Object value;

        public Entry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }
    }
}