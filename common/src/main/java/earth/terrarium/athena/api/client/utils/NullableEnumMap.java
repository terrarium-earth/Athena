package earth.terrarium.athena.api.client.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;

public class NullableEnumMap<K extends Enum<K>, V> implements Map<K, V> {

    private final MapEntry empty = new MapEntry(null);

    private final Class<K> enumClass;
    private final K[] keys;
    private final MapEntry[] values;
    private int size;

    @SuppressWarnings("unchecked")
    public NullableEnumMap(Class<K> enumClass) {
        this.enumClass = enumClass;
        this.keys = enumClass.getEnumConstants();
        this.values = (MapEntry[]) Array.newInstance(MapEntry.class, enumClass.getEnumConstants().length + 1);
        Arrays.fill(this.values, empty);
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    private int getIndex(Object key) {
        if (key == null) return 0;
        if (this.enumClass.isInstance(key)) return ((Enum<?>) key).ordinal() + 1;
        throw new IllegalArgumentException("Key must be of type " + this.enumClass.getName());
    }

    @Override
    public boolean containsKey(Object key) {
        return this.values[getIndex(key)] != empty;
    }

    @Override
    public boolean containsValue(Object value) {
        for (MapEntry entry : this.values) {
            if (entry != empty && Objects.equals(entry.value, value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        return this.values[getIndex(key)].value;
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        int index = getIndex(key);
        MapEntry entry = this.values[index];
        if (entry == empty) this.size++;
        this.values[index] = new MapEntry(value);
        return entry.value;
    }

    @Override
    public V remove(Object key) {
        int index = getIndex(key);
        MapEntry entry = this.values[index];
        if (entry == empty) return null;
        this.values[index] = empty;
        this.size--;
        return entry.value;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        Arrays.fill(this.values, empty);
        this.size = 0;
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        Set<K> set = new HashSet<>();
        if (this.values[0] != empty) {
            set.add(null);
        }
        for (int i = 0; i < this.values.length; i++) {
            if (this.values[i + 1] != empty) {
                set.add(this.keys[i - 1]);
            }
        }
        return set;
    }

    @NotNull
    @Override
    public Collection<V> values() {
        List<V> list = new ArrayList<>();
        for (MapEntry value : this.values) {
            if (value != empty) {
                list.add(value.value);
            }
        }
        return list;
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> set = new HashSet<>();
        if (this.values[0] != empty) {
            set.add(new AbstractMap.SimpleEntry<>(null, this.values[0].value));
        }
        for (int i = 0; i < this.values.length; i++) {
            if (this.values[i + 1] != empty) {
                set.add(new AbstractMap.SimpleEntry<>(this.keys[i - 1], this.values[i + 1].value));
            }
        }
        return set;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NullableEnumMap<?, ?> that = (NullableEnumMap<?, ?>) o;
        return Arrays.equals(this.values, that.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.values);
    }

    private class MapEntry {

        private final V value;

        public MapEntry(V value) {
            this.value = value;
        }
    }
}
