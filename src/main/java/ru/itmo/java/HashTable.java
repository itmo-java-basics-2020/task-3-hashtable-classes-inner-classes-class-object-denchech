package ru.itmo.java;

public class HashTable {

    private static class Entry {
        private Object key;
        private Object value;

        public Entry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

    }

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
        this.threshold = (int) Math.floor(capacity * loadFactor);
    }

    private void resize() {
        if (this.size >= this.threshold) {
            this.capacity *= RESIZE_FACTOR;
            this.threshold = (int) (this.capacity * this.loadFactor);

            Entry[] oldElements = this.elements;
            this.elements = new Entry[this.capacity];
            this.deletedElements = new boolean[this.capacity];
            this.size = 0;
            for (Entry currentElement : oldElements) {
                if (currentElement != null) {
                    this.put(currentElement.key, currentElement.value);
                }
            }
        }
    }

    private int getHash(Object key) {
        return Math.abs(key.hashCode()) % this.capacity;
    }

    private int getIndexByKey(Object key) {
        int hashKey = this.getHash(key);
        while (deletedElements[hashKey]
                || (this.elements[hashKey] != null
                && !this.elements[hashKey].key.equals(key))
        ) {
            hashKey = (hashKey + PROBING_INTERVAL) % this.capacity;
        }
        return hashKey;
    }

    public Object put(Object key, Object value) {
        if (this.size == MAX_SIZE) {
            throw new RuntimeException("You cannot put more object");
        }

        Entry newElement = new Entry(key, value);
        int foundIndex = this.getIndexByKey(key);

        if (this.elements[foundIndex] == null) {

            foundIndex = this.getHash(key);
            while (this.elements[foundIndex] != null) {
                foundIndex = (foundIndex + PROBING_INTERVAL) % this.capacity;
            }

            deletedElements[foundIndex] = false;

            this.elements[foundIndex] = newElement;
            ++this.size;

            if (this.size >= this.threshold) {
                this.resize();
            }

            return null;
        }
        Object oldValue = this.elements[foundIndex].value;
        this.elements[foundIndex] = newElement;
        return oldValue;
    }

    public Object get(Object key) {
        Entry foundEntry = this.elements[this.getIndexByKey(key)];
        return (foundEntry == null) ? null : foundEntry.value;
    }

    public Object remove(Object key) {
        int foundIndex = this.getIndexByKey(key);

        if (this.elements[foundIndex] == null) {
            return null;
        }

        this.deletedElements[foundIndex] = true;
        Object oldValue = this.elements[foundIndex].value;
        --this.size;
        this.elements[foundIndex] = null;
        return oldValue;
    }

    public int size() {
        return this.size;
    }

}