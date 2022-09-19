///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2001, Eric D. Friedman All Rights Reserved.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
///////////////////////////////////////////////////////////////////////////////
// THIS FILE IS AUTOGENERATED, PLEASE DO NOT EDIT OR ELSE
package gnu.trove;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * An open addressed Map implementation for int keys and long values.
 * <p>
 * Created: Sun Nov  4 08:52:45 2001
 *
 * @author Eric D. Friedman
 */
@Deprecated
public class TIntLongHashMap extends TIntHash {

  /**
   * the values of the map
   */
  protected transient long[] _values;

  /**
   * Creates a new <code>TIntLongHashMap</code> instance with the default
   * capacity and load factor.
   */
  public TIntLongHashMap() {
  }

  /**
   * Creates a new <code>TIntLongHashMap</code> instance with a prime
   * capacity equal to or greater than <tt>initialCapacity</tt> and
   * with the default load factor.
   *
   * @param initialCapacity an <code>int</code> value
   */
  public TIntLongHashMap(int initialCapacity) {
    super(initialCapacity);
  }

  /**
   * Creates a new <code>TIntLongHashMap</code> instance with a prime
   * capacity equal to or greater than <tt>initialCapacity</tt> and
   * with the specified load factor.
   *
   * @param initialCapacity an <code>int</code> value
   * @param loadFactor      a <code>float</code> value
   */
  public TIntLongHashMap(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
  }

  /**
   * Creates a new <code>TIntLongHashMap</code> instance with the default
   * capacity and load factor.
   *
   * @param strategy used to compute hash codes and to compare keys.
   */
  public TIntLongHashMap(TIntHashingStrategy strategy) {
    super(strategy);
  }

  /**
   * Creates a new <code>TIntLongHashMap</code> instance whose capacity
   * is the next highest prime above <tt>initialCapacity + 1</tt>
   * unless that value is already prime.
   *
   * @param initialCapacity an <code>int</code> value
   * @param strategy        used to compute hash codes and to compare keys.
   */
  public TIntLongHashMap(int initialCapacity, TIntHashingStrategy strategy) {
    super(initialCapacity, strategy);
  }

  /**
   * Creates a new <code>TIntLongHashMap</code> instance with a prime
   * value at or near the specified capacity and load factor.
   *
   * @param initialCapacity used to find a prime capacity for the table.
   * @param loadFactor      used to calculate the threshold over which
   *                        rehashing takes place.
   * @param strategy        used to compute hash codes and to compare keys.
   */
  public TIntLongHashMap(int initialCapacity, float loadFactor, TIntHashingStrategy strategy) {
    super(initialCapacity, loadFactor, strategy);
  }

  /**
   * @return a deep clone of this collection
   */
  @Override
  public Object clone() {
    TIntLongHashMap m = (TIntLongHashMap)super.clone();
    m._values = _values == null ? null : _values.clone();
    return m;
  }

  /**
   * initializes the hashtable to a prime capacity which is at least
   * <tt>initialCapacity + 1</tt>.
   *
   * @param initialCapacity an <code>int</code> value
   * @return the actual capacity chosen
   */
  @Override
  protected int setUp(int initialCapacity) {
    int capacity = super.setUp(initialCapacity);
    _values = initialCapacity == JUST_CREATED_CAPACITY ? null : new long[capacity];
    return capacity;
  }

  /**
   * Inserts a key/value pair into the map.
   *
   * @param key   an <code>int</code> value
   * @param value an <code>long</code> value
   * @return the previous value associated with <tt>key</tt>,
   * or null if none was found.
   */
  public long put(int key, long value) {
    long previous = 0;
    int index = insertionIndex(key);
    boolean isNewMapping = true;
    if (index < 0) {
      index = -index - 1;
      previous = _values[index];
      isNewMapping = false;
    }
    byte previousState = _states[index];
    _set[index] = key;
    _states[index] = FULL;
    _values[index] = value;
    if (isNewMapping) {
      postInsertHook(previousState == FREE);
    }

    return previous;
  }

  /**
   * rehashes the map to the new capacity.
   *
   * @param newCapacity an <code>int</code> value
   */
  @Override
  protected void rehash(int newCapacity) {
    int oldCapacity = capacity();
    int[] oldKeys = _set;
    long[] oldVals = _values;
    byte[] oldStates = _states;

    _set = new int[newCapacity];
    _values = new long[newCapacity];
    _states = new byte[newCapacity];

    for (int i = oldCapacity; i-- > 0; ) {
      if (oldStates[i] == FULL) {
        int o = oldKeys[i];
        int index = insertionIndex(o);
        _set[index] = o;
        _values[index] = oldVals[i];
        _states[index] = FULL;
      }
    }
  }

  /**
   * retrieves the value for <tt>key</tt>
   *
   * @param key an <code>int</code> value
   * @return the value of <tt>key</tt> or null if no such mapping exists.
   */
  public long get(int key) {
    int index = index(key);
    return index < 0 ? 0 : _values[index];
  }

  /**
   * Empties the map.
   */
  @Override
  public void clear() {
    super.clear();
    int[] keys = _set;
    long[] vals = _values;
    if (vals == null) return;
    byte[] states = _states;

    for (int i = keys.length; i-- > 0; ) {
      keys[i] = 0;
      vals[i] = 0;
      states[i] = FREE;
    }
  }

  /**
   * Deletes a key/value pair from the map.
   *
   * @param key an <code>int</code> value
   * @return an <code>long</code> value
   */
  public long remove(int key) {
    long prev = 0;
    int index = index(key);
    if (index >= 0) {
      prev = _values[index];
      removeAt(index);    // clear key,state; adjust size
    }
    return prev;
  }

  /**
   * Compares this map with another map for equality of their stored
   * entries.
   *
   * @param other an <code>Object</code> value
   * @return a <code>boolean</code> value
   */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof TIntLongHashMap)) {
      return false;
    }
    TIntLongHashMap that = (TIntLongHashMap)other;
    if (that.size() != size()) {
      return false;
    }
    return forEachEntry(new EqProcedure(that));
  }

  @Override
  public int hashCode() {
    HashProcedure p = new HashProcedure();
    forEachEntry(p);
    return p.getHashCode();
  }

  private final class HashProcedure implements TIntLongProcedure {
    private int h;

    HashProcedure() {
    }

    public int getHashCode() {
      return h;
    }

    @Override
    public boolean execute(int key, long value) {
      h += _hashingStrategy.computeHashCode(key) ^ HashFunctions.hash(value);
      return true;
    }
  }

  private static final class EqProcedure implements TIntLongProcedure {
    private final TIntLongHashMap _otherMap;

    EqProcedure(TIntLongHashMap otherMap) {
      _otherMap = otherMap;
    }

    @Override
    public boolean execute(int key, long value) {
      int index = _otherMap.index(key);
      return index >= 0 && eq(value, _otherMap.get(key));
    }

    /**
     * Compare two longs for equality.
     */
    private static boolean eq(long v1, long v2) {
      return v1 == v2;
    }
  }

  /**
   * removes the mapping at <tt>index</tt> from the map.
   *
   * @param index an <code>int</code> value
   */
  @Override
  protected void removeAt(int index) {
    _values[index] = 0;
    super.removeAt(index);  // clear key, state; adjust size
  }

  /**
   * Returns the values of the map.
   *
   * @return a <code>Collection</code> value
   */
  public long[] getValues() {
    long[] vals = new long[size()];
    long[] v = _values;
    byte[] states = _states;

    if (states != null) {
      for (int i = states.length, j = 0; i-- > 0; ) {
        if (states[i] == FULL) {
          vals[j++] = v[i];
        }
      }
    }
    return vals;
  }

  /**
   * returns the keys of the map.
   *
   * @return a <code>Set</code> value
   */
  public int[] keys() {
    int[] keys = new int[size()];
    int[] k = _set;
    byte[] states = _states;

    if (states != null) {
      for (int i = states.length, j = 0; i-- > 0; ) {
        if (states[i] == FULL) {
          keys[j++] = k[i];
        }
      }
    }
    return keys;
  }

  /**
   * checks for the presence of <tt>val</tt> in the values of the map.
   *
   * @param val an <code>long</code> value
   * @return a <code>boolean</code> value
   */
  public boolean containsValue(long val) {
    byte[] states = _states;
    long[] vals = _values;
    if (states != null) {
      for (int i = states.length; i-- > 0; ) {
        if (states[i] == FULL && val == vals[i]) {
          return true;
        }
      }
    }
    return false;
  }


  /**
   * checks for the present of <tt>key</tt> in the keys of the map.
   *
   * @param key an <code>int</code> value
   * @return a <code>boolean</code> value
   */
  public boolean containsKey(int key) {
    return contains(key);
  }

  /**
   * Executes <tt>procedure</tt> for each value in the map.
   *
   * @param procedure a <code>TLongProcedure</code> value
   * @return false if the loop over the values terminated because
   * the procedure returned false for some value.
   */
  public boolean forEachValue(TLongProcedure procedure) {
    byte[] states = _states;
    long[] values = _values;
    if (states != null) {
      for (int i = states.length; i-- > 0; ) {
        if (states[i] == FULL && !procedure.execute(values[i])) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Executes <tt>procedure</tt> for each key/value entry in the
   * map.
   *
   * @param procedure a <code>TIntLongProcedure</code> value
   * @return false if the loop over the entries terminated because
   * the procedure returned false for some entry.
   */
  public boolean forEachEntry(TIntLongProcedure procedure) {
    byte[] states = _states;
    int[] keys = _set;
    long[] values = _values;
    if (states != null) {
      for (int i = states.length; i-- > 0; ) {
        if (states[i] == FULL && !procedure.execute(keys[i], values[i])) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Retains only those entries in the map for which the procedure
   * returns a true value.
   *
   * @param procedure determines which entries to keep
   * @return true if the map was modified.
   */
  public boolean retainEntries(TIntLongProcedure procedure) {
    boolean modified = false;
    byte[] states = _states;
    int[] keys = _set;
    long[] values = _values;
    if (states != null) {
      for (int i = states.length; i-- > 0; ) {
        if (states[i] == FULL && !procedure.execute(keys[i], values[i])) {
          removeAt(i);
          modified = true;
        }
      }
    }
    return modified;
  }

  /**
   * Transform the values in this map using <tt>function</tt>.
   *
   * @param function a <code>TLongFunction</code> value
   */
  public void transformValues(TLongFunction function) {
    byte[] states = _states;
    long[] values = _values;
    if (states != null) {
      for (int i = states.length; i-- > 0; ) {
        if (states[i] == FULL) {
          values[i] = function.execute(values[i]);
        }
      }
    }
  }

  /**
   * Increments the primitive value mapped to key by 1
   *
   * @param key the key of the value to increment
   * @return true if a mapping was found and modified.
   */
  public boolean increment(int key) {
    return adjustValue(key, 1);
  }

  /**
   * Adjusts the primitive value mapped to key.
   *
   * @param key    the key of the value to increment
   * @param amount the amount to adjust the value by.
   * @return true if a mapping was found and modified.
   */
  public boolean adjustValue(int key, long amount) {
    int index = index(key);
    if (index < 0) {
      return false;
    }
    else {
      _values[index] += amount;
      return true;
    }
  }


  private void writeObject(ObjectOutputStream stream)
    throws IOException {
    stream.defaultWriteObject();

    // number of entries
    stream.writeInt(_size);

    SerializationProcedure writeProcedure = new SerializationProcedure(stream);
    if (!forEachEntry(writeProcedure)) {
      throw writeProcedure.exception;
    }
  }

  private void readObject(ObjectInputStream stream)
    throws IOException, ClassNotFoundException {
    stream.defaultReadObject();

    int size = stream.readInt();
    setUp(size);
    while (size-- > 0) {
      int key = stream.readInt();
      long val = stream.readLong();
      put(key, val);
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    forEachEntry(new TIntLongProcedure() {
      @Override
      public boolean execute(int key, long value) {
        if (sb.length() != 0) {
          sb.append(',').append(' ');
        }
        sb.append(key);
        sb.append('=');
        sb.append(value);
        return true;
      }
    });
    sb.append('}');
    sb.insert(0, '{');
    return sb.toString();
  }
} // TIntLongHashMap
