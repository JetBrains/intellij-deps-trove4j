package gnu.trove;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static gnu.trove.THash.DEFAULT_LOAD_FACTOR;
import static gnu.trove.TObjectHash.REMOVED;

// made "main method runnable" instead of JUnit runnable to avoid Junit dependency
public class MapTest {
    public static void main(String[] args) {
        testTIntObjectHashMap();
        testTHashMap();
        testTHashMapViewsEquality();
        testClone();
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

    public static void testTHashMapViewsEquality() {
      THashMap<String, Integer> oneMap = new THashMap<String, Integer>();
      THashMap<String, Integer> twoMap = new THashMap<String, Integer>();

      for (int i = 0; i < 10; i++) {
        String oneKey = String.valueOf(i);
        oneMap.put(oneKey, i);

        String twoKey = String.valueOf(9 - i);
        twoMap.put(twoKey, 9 - i);
      }

      assertEquals(oneMap, twoMap);
      assertEquals(oneMap.hashCode(), twoMap.hashCode());

      assertEquals(oneMap.keySet(), twoMap.keySet());
      assertEquals(oneMap.keySet().hashCode(), twoMap.keySet().hashCode());

      assertEquals(oneMap.values(), twoMap.values());
      assertEquals(oneMap.values().hashCode(), twoMap.values().hashCode());

      assertEquals(oneMap.entrySet(), twoMap.entrySet());
      assertEquals(oneMap.entrySet().hashCode(), twoMap.entrySet().hashCode());
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

    private static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("Expected: "+expected+"; but got: "+actual);
        }
    }


    private static void assertTrue(boolean b) {
        if (!b) throw new AssertionError();
    }
}