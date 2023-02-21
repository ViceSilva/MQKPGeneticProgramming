import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/*...
 * Esta estructura es una especie de multimap la que se centra en entregar elementos en base a ciertos criterios
 * siendo los elementos guardados en un set dentro de cada nodo, los nodos estan ordenados por un valor K(key)
 *
 */
public class MultiMap<K,V> {
	private TreeMap<K,Set<V>> baseMap;
	private int size;
	MultiMap(){
		baseMap = new TreeMap<K,Set<V>>();
		size = 0;
	}
	
	public void addElement(K key, V value) {
		if(!baseMap.containsKey(key))
			baseMap.put(key, new HashSet<V>());
		baseMap.get(key).add(value);
		size++;
	}
	
	public void deleteElement(K key, V value) {
		if(!baseMap.containsKey(key))
			return;
		baseMap.get(key).remove(value);
		if(baseMap.get(key).isEmpty())
			baseMap.remove(key);
		size--;
	}
	
	public V pollMinElement() {
		if(baseMap.isEmpty())
			return null;
		Set<V> minSet = baseMap.firstEntry().getValue();
		V minElement = minSet.iterator().next();
		minSet.remove(minElement);
		if(minSet.isEmpty())
			baseMap.pollFirstEntry();
		size--;
		return minElement;
	}
	
	public V pollMaxElement() {
		if(baseMap.isEmpty())
			return null;
		Set<V> minSet = baseMap.lastEntry().getValue();
		V minElement = minSet.iterator().next();
		minSet.remove(minElement);
		if(minSet.isEmpty())
			baseMap.pollLastEntry();
		size--;
		return minElement;
	}

	public V getMinElement() {
		return baseMap.firstEntry().getValue().iterator().next();
	}
	
	public V getMaxElement() {
		return baseMap.lastEntry().getValue().iterator().next();
	}
	
	public V pollFloorElement(K value) {
		if(baseMap.floorEntry(value) == null)
			return null;
		Map.Entry<K,Set<V>> floorEntry = baseMap.floorEntry(value);
		K key = floorEntry.getKey();
		V element = floorEntry.getValue().iterator().next();
		this.deleteElement(key, element);
		size--;
		return element;
	}
	
	public V pollCeilingElement(K value) {
		if(baseMap.ceilingEntry(value) == null)
			return null;
		Map.Entry<K, Set<V>> ceilingEntry = baseMap.ceilingEntry(value);
		K key = ceilingEntry.getKey();
		V element = ceilingEntry.getValue().iterator().next();
		this.deleteElement(key, element);
		size--;
		return element;
	}
	
	public V getFloorElement(K value) {
		if(baseMap.floorEntry(value) == null)
			return null;
		return baseMap.floorEntry(value).getValue().iterator().next();
	}
	
	public V getCeilingElement(K value) {
		if(baseMap.ceilingEntry(value) == null)
			return null;
		return baseMap.ceilingEntry(value).getValue().iterator().next();
	}
	
	public int size() {
		return size;
	}
	
	public boolean isEmpty() {
		if(size == 0)
			return true;
		return false;
	}
	
	public boolean contains(K key, V value) {
		if(baseMap.containsKey(key) && baseMap.get(key).contains(value))
			return true;
		return false;
	}
	
	public void print() {
		System.out.println(baseMap);
	}
}
