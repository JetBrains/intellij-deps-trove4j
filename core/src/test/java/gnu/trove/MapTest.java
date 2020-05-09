package gnu.trove;

import java.util.*;

import static gnu.trove.THash.DEFAULT_LOAD_FACTOR;
import static gnu.trove.TObjectHash.REMOVED;

// made "main method runnable" instead of JUnit runnable to avoid Junit dependency
public class MapTest {
    public static void main(String[] args) {
        testTIntObjectHashMap();
        testTHashMap();
        testMapEquality();
        testClone();
        System.out.println("All tests have passed");
    }

    private static void testTIntObjectHashMap() {
        TIntObjectHashMap<String> map = new TIntObjectHashMap<String>();
        // init correctly
        assertEquals(0, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(0, map.capacity());
        assertEquals(map.capacity(), map._free);
        assertEquals(0, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);
        map.retainEntries(new TIntObjectProcedure<String>() {
            public boolean execute(int a, String b) {
                return true;
            }
        });

        // first put placed at one
        int key = 1;
        assertEquals(1, map._hashingStrategy.computeHashCode(key));
        String value = "1";
        String prev = map.put(key, value);
        assertEquals(null, prev);

        assertEquals(0, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(7, map.capacity());
        assertEquals(map.capacity() - 1, map._free);
        assertEquals(1, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);

        int index = map.index(key);
        assertEquals(1, index);
        assertEquals(1, map.size());
        assertTrue(((Object[]) map._values)[index] == value);

        // remove leaves cell "removed"
        String remove = map.remove(key);
        assertTrue(remove == value);
        assertEquals(0, map.size());
        assertEquals(7, map.capacity());
        assertEquals(REMOVED, ((Object[]) map._values)[index]);

        assertEquals(1, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(7, map.capacity());
        assertEquals(map.capacity() - 1, map._free);
        assertEquals(0, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);

        // reinsert after remove must reuse the cell
        prev = map.put(key, value);
        assertEquals(null, prev);
        index = map.index(key);
        assertEquals(1, index);
        assertEquals(1, map.size());
        assertTrue(((Object[]) map._values)[index] == value);

        assertEquals(0, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(7, map.capacity());
        assertEquals(map.capacity() - 1, map._free);
        assertEquals(1, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);


        // second put double hashes
        int key2 = 8;
        assertEquals(8, map._hashingStrategy.computeHashCode(key2)); // conflict
        prev = map.put(key2, value);
        assertEquals(null, prev);
        index = map.index(key2);
        assertEquals(4, index);
        assertEquals(2, map.size());
        assertTrue(((Object[]) map._values)[index] == value);

        assertEquals(0, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(7, map.capacity());
        assertEquals(map.capacity() - 2, map._free);
        assertEquals(2, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);


        // remove of the first key leaves gap
        index = map.index(key);
        remove = map.remove(key);
        assertTrue(remove == value);
        assertEquals(1, map.size());
        assertEquals(REMOVED, ((Object[]) map._values)[index]);

        assertEquals(1, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(7, map.capacity());
        assertEquals(map.capacity() - 2, map._free);
        assertEquals(1, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);

        // search for the second key must probe through the gap
        String v = map.get(key2);
        assertTrue(v == value); // traverse through REMOVED

        // second remove leaves two removed cells
        index = map.index(key2);
        assertEquals(4, index);
        remove = map.remove(key2);
        assertTrue(remove == value);
        assertEquals(0, map.size());
        assertEquals(7, map.capacity());
        assertEquals(REMOVED, ((Object[]) map._values)[index]);

        assertEquals(2, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(7, map.capacity());
        assertEquals(map.capacity() - 2, map._free);
        assertEquals(0, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);

        // clear resets everything
        map.clear();
        assertEquals(0, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(7, map.capacity());
        assertEquals(map.capacity(), map._free);
        assertEquals(0, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);

        // prepare for rehash
        map.put(1, value);
        map.put(2, value);
        map.put(3, value);
        map.put(4, value);
        map.put(5, value);

        assertEquals(0, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(7, map.capacity());
        assertEquals(map.capacity() - 5, map._free);
        assertEquals(5, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);

        // nothing changes
        map.put(5, value);
        assertEquals(0, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(7, map.capacity());
        assertEquals(map.capacity() - 5, map._free);
        assertEquals(5, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);

        // rehash
        map.put(6, value);
        assertEquals(0, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(17, map.capacity());
        assertEquals(map.capacity() - 6, map._free);
        assertEquals(6, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);
    }

    private static void testTHashMap() {
        THashMap<Integer, String> map = new THashMap<Integer, String>();
        // init correctly
        assertEquals(0, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(0, map.capacity());
        assertEquals(map.capacity(), map._free);
        assertEquals(0, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);
        map.retainEntries(new TObjectObjectProcedure<Integer, String>() {
            public boolean execute(Integer a, String b) {
                return true;
            }
        });

        // first put placed at one
        int key = 1;
        assertEquals(1, map._hashingStrategy.computeHashCode(key));
        String value = "1";
        String prev = map.put(key, value);
        assertEquals(null, prev);

        assertEquals(0, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(7, map.capacity());
        assertEquals(map.capacity() - 1, map._free);
        assertEquals(1, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);

        int index = map.index(key);
        assertEquals(1, index);
        assertEquals(1, map.size());
        assertTrue(((Object[]) map._values)[index] == value);

        // remove leaves cell "removed"
        String remove = map.remove(key);
        assertTrue(remove == value);
        assertEquals(0, map.size());
        assertEquals(7, map.capacity());
        assertEquals(REMOVED, ((Object[]) map._set)[index]);

        assertEquals(1, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(7, map.capacity());
        assertEquals(map.capacity() - 1, map._free);
        assertEquals(0, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);

        // reinsert after remove must reuse the cell
        prev = map.put(key, value);
        assertEquals(null, prev);
        index = map.index(key);
        assertEquals(1, index);
        assertEquals(1, map.size());
        assertTrue(((Object[]) map._values)[index] == value);

        assertEquals(0, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(7, map.capacity());
        assertEquals(map.capacity() - 1, map._free);
        assertEquals(1, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);


        // second put double hashes
        int key2 = 8;
        assertEquals(8, map._hashingStrategy.computeHashCode(key2)); // conflict
        prev = map.put(key2, value);
        assertEquals(null, prev);
        index = map.index(key2);
        assertEquals(4, index);
        assertEquals(2, map.size());
        assertEquals(value, ((Object[]) map._values)[index]);

        assertEquals(0, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(7, map.capacity());
        assertEquals(map.capacity() - 2, map._free);
        assertEquals(2, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);


        // remove of the first key leaves gap
        index = map.index(key);
        remove = map.remove(key);
        assertTrue(remove == value);
        assertEquals(1, map.size());
        assertEquals(REMOVED, ((Object[]) map._set)[index]);

        assertEquals(1, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(7, map.capacity());
        assertEquals(map.capacity() - 2, map._free);
        assertEquals(1, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);

        // search for the second key must probe through the gap
        String v = map.get(key2);
        assertTrue(v == value); // traverse through REMOVED

        // second remove leaves two removed cells
        index = map.index(key2);
        assertEquals(4, index);
        remove = map.remove(key2);
        assertTrue(remove == value);
        assertEquals(0, map.size());
        assertEquals(7, map.capacity());
        assertEquals(REMOVED, ((Object[]) map._set)[index]);

        assertEquals(2, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(7, map.capacity());
        assertEquals(map.capacity() - 2, map._free);
        assertEquals(0, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);

        // clear resets everything
        map.clear();
        // _deadkeys was not reset due to optimisation "do nothing on empty map .clear()"
        assertEquals(2, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(7, map.capacity());
        assertEquals(map.capacity() - 2, map._free);
        assertEquals(0, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);

        // prepare for rehash
        map.put(1, value);
        map.put(2, value);
        map.put(3, value);
        map.put(4, value);
        map.put(5, value);

        assertEquals(0, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(7, map.capacity());
        assertEquals(map.capacity() - 5, map._free);
        assertEquals(5, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);

        // nothing changes
        map.put(5, "xxx");
        assertEquals(0, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(7, map.capacity());
        assertEquals(map.capacity() - 5, map._free);
        assertEquals(5, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);

        // rehash
        map.put(6, value);
        assertEquals(0, map._deadkeys);
        assertTrue(map._hashingStrategy == map);
        assertEquals(17, map.capacity());
        assertEquals(map.capacity() - 6, map._free);
        assertEquals(6, map._size);
        assertEquals(DEFAULT_LOAD_FACTOR, map._loadFactor);
        assertEquals((int) (map.capacity() * map._loadFactor), map._maxSize);
    }

    public static void testMapEquality() {
        Map<String, Integer> scenario = generateTestScenario();

        List<Map<String, Integer>> allMaps = Arrays.<Map<String, Integer>>asList(
            new THashMap<String, Integer>(),
            new THashMap<String, Integer>(),
            new HashMap<String, Integer>(),
            new HashMap<String, Integer>(),
            new TreeMap<String, Integer>(),
            new TreeMap<String, Integer>()
        );

        for (Map<String, Integer> map : allMaps) {
            putShuffledScenario(scenario, map);
        }

        for (Map<String, Integer> map1 : allMaps) {
            for (Map<String, Integer> map2 : allMaps) {
                assertMapsAreEqual(map1, map2);
            }
        }
    }

    private static Map<String, Integer> generateTestScenario() {
        int numOfValues = 1000;
        Random random = new Random();
        Map<String, Integer> scenario = new HashMap<String, Integer>();
        for (int i = 0; i < numOfValues; i++) {
            String key = String.valueOf(random.nextInt());
            int value = random.nextInt();
            scenario.put(key, value);
        }
        return scenario;
    }

    private static <K, V> void putShuffledScenario(Map<K, V> scenario, Map<K, V> target) {
        List<Map.Entry<K, V>> shuffled = new ArrayList<Map.Entry<K, V>>(scenario.entrySet());
        Collections.shuffle(shuffled);
        for (Map.Entry<K, V> entry : shuffled) {
            target.put(entry.getKey(), entry.getValue());
        }
    }

    private static void assertMapsAreEqual(Map<String, Integer> oneMap, Map<String, Integer> twoMap) {
        assertEquals(oneMap, twoMap);
        assertEquals(oneMap.size(), twoMap.size());
        assertEquals(oneMap.hashCode(), twoMap.hashCode());

        assertEquals(oneMap.keySet(), twoMap.keySet());
        assertEquals(oneMap.keySet().size(), twoMap.keySet().size());
        assertEquals(oneMap.keySet().hashCode(), twoMap.keySet().hashCode());
        for (String oneKey : oneMap.keySet()) {
            assertEquals(true, twoMap.keySet().contains(oneKey));
        }
        for (String twoKey : twoMap.keySet()) {
            assertEquals(true, oneMap.keySet().contains(twoKey));
        }

        List<Integer> oneValues = new ArrayList<Integer>(oneMap.values());
        Collections.sort(oneValues);
        List<Integer> twoValues = new ArrayList<Integer>(twoMap.values());
        Collections.sort(twoValues);
        assertEquals(oneValues, twoValues);
        assertEquals(oneValues.size(), twoValues.size());
        assertEquals(oneValues.hashCode(), twoValues.hashCode());
        for (Integer oneValue : oneMap.values()) {
            assertEquals(true, twoMap.values().contains(oneValue));
        }
        for (Integer twoValue : twoMap.values()) {
            assertEquals(true, oneMap.values().contains(twoValue));
        }

        assertEquals(oneMap.entrySet(), twoMap.entrySet());
        assertEquals(oneMap.entrySet().size(), twoMap.entrySet().size());
        assertEquals(oneMap.entrySet().hashCode(), twoMap.entrySet().hashCode());
        for (Map.Entry<String, Integer> oneEntry : oneMap.entrySet()) {
            assertEquals(true, twoMap.entrySet().contains(oneEntry));
        }
        for (Map.Entry<String, Integer> twoEntry : twoMap.entrySet()) {
            assertEquals(true, oneMap.entrySet().contains(twoEntry));
        }

        Map<String, Integer> oneClone = cloneMap(oneMap);
        Map<String, Integer> twoClone = cloneMap(twoMap);
        for (String oneKey : oneMap.keySet()) {
            twoClone.keySet().remove(oneKey);
        }
        for (String twoKey : twoMap.keySet()) {
            oneClone.keySet().remove(twoKey);
        }
        assertEquals(Collections.emptyMap(), oneClone);
        assertEquals(Collections.emptyMap(), twoClone);

        oneClone = cloneMap(oneMap);
        twoClone = cloneMap(twoMap);
        for (Integer oneValue : oneMap.values()) {
            twoClone.values().remove(oneValue);
        }
        for (Integer twoValue : twoMap.values()) {
            oneClone.values().remove(twoValue);
        }
        assertEquals(Collections.emptyMap(), oneClone);
        assertEquals(Collections.emptyMap(), twoClone);

        oneClone = cloneMap(oneMap);
        twoClone = cloneMap(twoMap);
        for (Map.Entry<String, Integer> oneEntry : oneMap.entrySet()) {
            twoClone.entrySet().remove(oneEntry);
        }
        for (Map.Entry<String, Integer> twoEntry : twoMap.entrySet()) {
            oneClone.entrySet().remove(twoEntry);
        }
        assertEquals(Collections.emptyMap(), oneClone);
        assertEquals(Collections.emptyMap(), twoClone);
    }

    private static Map<String, Integer> cloneMap(Map<String, Integer> original) {
        if (original instanceof THashMap) {
            return ((THashMap<String, Integer>) original).clone();
        }
        if (original instanceof HashMap) {
            //noinspection unchecked
            return ((Map<String, Integer>) ((HashMap<String, Integer>) original).clone());
        }
        if (original instanceof TreeMap) {
            //noinspection unchecked
            return (Map<String, Integer>) ((TreeMap<String, Integer>) original).clone();
        }
        throw new AssertionError("Unknown map implementation: " + original.getClass().getName());
    }

    public static void testClone() {
      TIntObjectHashMap<int[]> map = new TIntObjectHashMap<int[]>();
      map.put(0, new int[2]);
      map.put(1, new int[2]);

      TIntObjectHashMap<int[]> clone = map.clone();
      assertEquals(clone.size(), 2);
      int[] keys = clone.keys();
      assertEquals(keys.length, 2);
      Set set01 = new HashSet(Arrays.asList(0, 1));
      Set keySet = new HashSet(Arrays.asList(keys[0], keys[1]));
      assertEquals(set01, keySet);

      map.clear();

      assertEquals(clone.size(), 2);
      keys = clone.keys();
      assertEquals(keys.length, 2);
      keySet = new HashSet(Arrays.asList(keys[0], keys[1]));
      assertEquals(set01, keySet);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            if (expected instanceof Collection && actual instanceof Collection) {
                Set extra = new HashSet(((Collection) actual));
                extra.removeAll(((Collection) expected));

                Set missing = new HashSet(((Collection) expected));
                missing.removeAll(((Collection) actual));

                throw new AssertionError("Missing: " + missing + "\n" + "Extra: " + extra);
            }

            throw new AssertionError("Expected: "+expected+"; but got: "+actual);
        }
    }


    private static void assertTrue(boolean b) {
        if (!b) throw new AssertionError();
    }
}