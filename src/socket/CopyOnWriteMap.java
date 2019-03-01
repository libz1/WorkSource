package socket;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// 参考https://www.cnblogs.com/dolphin0520/p/3938914.html
// 因为有多个接口函数没有实现，所以暂时不使用

// ConcurrentHashMap
//  https://blog.csdn.net/u011328417/article/details/79284730
public class CopyOnWriteMap<K, V> implements Map<K, V>, Cloneable {
	private volatile Map<K, V> internalMap;

	public CopyOnWriteMap() {
		internalMap = new HashMap<K, V>();
	}

	public V put(K key, V value) {
		synchronized (this) {
			Map<K, V> newMap = new HashMap<K, V>(internalMap);
			V val = newMap.put(key, value);
			internalMap = newMap;
			return val;
		}
	}

	public V get(Object key) {
		return internalMap.get(key);
	}

	public void putAll(Map<? extends K, ? extends V> newData) {
		synchronized (this) {
			Map<K, V> newMap = new HashMap<K, V>(internalMap);
			newMap.putAll(newData);
			internalMap = newMap;
		}
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean containsKey(Object key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<K> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V remove(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<V> values() {
		// TODO Auto-generated method stub
		return null;
	}
}
