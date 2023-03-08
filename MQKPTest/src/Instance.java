package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

public class Instance {
	//representa a los knapSack
	Vector<Integer> knapsackList;
	private int knapsackNum, maxAvailableCapacity;
	private int[][] extraProfitbyRelation;
	private int[] currentCapacity, simpleProfitValue, relationProfitValue;
	private int maxCapacity;
	private Vector<Integer>[] addedItemsKnapsack;
	//representa elementos no ingresados
	private Vector<Integer> items;
	private LinkedHashMap<Integer, Integer> invalidItems;
	//informacion del problema
	private int[][] profitMatrix;
	private int[] itemWeight;
	private double[] itemValue;
	private int optimal;
	private String fileName;
	
	
	
	
	public Instance() {
	}
	
	public Instance(String fileName, int knapsackNum, int itemWeight[], int[][] profitMatrix) {
		this.knapsackNum = knapsackNum;
		this.fileName = fileName;
		this.profitMatrix = profitMatrix;
		this.itemWeight = itemWeight;
		invalidItems = new LinkedHashMap<>();
		itemValue = new double[itemWeight.length];
		for(int i = 0; i < itemWeight.length; i++)
			itemValue[i] = (double)profitMatrix[i][i]/itemWeight[i];
		items = new Vector<Integer>();
		for(int i = 0; i < itemWeight.length; i++)
			items.add(i);
		items.sort((a,b)->{
			Double valA = itemValue[a];
			Double valB = itemValue[b];
			return valA.compareTo(valB);
		});
		//agregamos elementos a elementos disponibles y se calcula suma de peso
		double totalWeight = 0.0;
		for(int i = 0; i < itemWeight.length; i++) {
			totalWeight += itemWeight[i];
		}
		//inicializamos knapsacks
		knapsackList = new Vector<Integer>();
		maxCapacity = (int)((totalWeight * 0.8)/knapsackNum);
		maxAvailableCapacity = maxCapacity;
		extraProfitbyRelation = new int[knapsackNum][itemWeight.length];
		currentCapacity = new int[knapsackNum];
		simpleProfitValue = new int[knapsackNum];
		relationProfitValue = new int[knapsackNum];
		addedItemsKnapsack = new Vector[knapsackNum];
		Arrays.fill(currentCapacity, maxCapacity);
		Arrays.fill(simpleProfitValue, 0);
		Arrays.fill(relationProfitValue, 0);
		for(int i = 0; i < knapsackNum; i++) {
			Arrays.fill(extraProfitbyRelation[i], 0);
			addedItemsKnapsack[i] = new Vector<Integer>();
			knapsackList.add(i);
		}
	}
	
	private Instance(Instance base) {
		this.knapsackNum = base.knapsackNum;
		this.fileName = base.fileName;
		this.profitMatrix = base.profitMatrix;
		this.itemWeight = base.itemWeight;
		invalidItems = new LinkedHashMap<>(base.invalidItems);
		itemValue = base.itemValue;
		items = new Vector<Integer>(base.items);
		items.sort((a,b)->{
			Double valA = itemValue[a];
			Double valB = itemValue[b];
			return valA.compareTo(valB);
		});
		//inicializamos knapsacks
		knapsackList = new Vector<Integer>(base.knapsackList);
		maxCapacity = base.maxCapacity;
		maxAvailableCapacity = base.maxAvailableCapacity;
		extraProfitbyRelation = new int[knapsackNum][];
		for(int i = 0; i < base.extraProfitbyRelation.length; i++) {
			extraProfitbyRelation[i] = base.extraProfitbyRelation[i].clone();
		}
		currentCapacity = base.currentCapacity.clone();
		simpleProfitValue = base.simpleProfitValue.clone();
		relationProfitValue = base.relationProfitValue.clone();
		addedItemsKnapsack = new Vector[knapsackNum];
		for(int i = 0; i < knapsackNum; i++) {
			addedItemsKnapsack[i] = new Vector<Integer>(base.addedItemsKnapsack[i]);
		}
	}
	//revisar override
	public Instance clone() {
		Instance clone = new Instance(this);
		return clone;
	}
	
	public void setOptimal(int optimal) {
		this.optimal = optimal;
	}
	//agrega un item segun indice en la lista de items
	private boolean addItembyIndex(int knapsack, int itemIndex) {
		int item = items.get(itemIndex);
		if(currentCapacity[knapsack] < itemWeight[item])
			return false;
		items.remove(itemIndex);
		currentCapacity[knapsack] -= itemWeight[item];
		simpleProfitValue[knapsack] += profitMatrix[item][item];
		int newIndex = findFirstLowerElement(addedItemsKnapsack[knapsack], item) + 1;
		addedItemsKnapsack[knapsack].add(newIndex, item);
		addUpdateExtraProfit(knapsack, item);
		return true;
	}
	
	private boolean removeItembyIndex(int knapsack, int itemIndex) {
		int item = addedItemsKnapsack[knapsack].get(itemIndex);
		addedItemsKnapsack[knapsack].remove(itemIndex);
		currentCapacity[knapsack] += itemWeight[item];
		simpleProfitValue[knapsack] -= profitMatrix[item][item];
		int newIndex = findFirstLowerElement(items, item) + 1;
		items.add(newIndex, item);
		removeUpdateExtraProfit(knapsack, item);
		ReinsertValidItems();
		return true;
	}
	
	private boolean addItem(int knapsack, int item) {
		if(currentCapacity[knapsack] < itemWeight[item])
			return false;
		items.remove((Integer)item);
		currentCapacity[knapsack] -= itemWeight[item];
		simpleProfitValue[knapsack] += profitMatrix[item][item];
		int newIndex = findFirstLowerElement(addedItemsKnapsack[knapsack], item) + 1;
		addedItemsKnapsack[knapsack].add(newIndex, item);
		addUpdateExtraProfit(knapsack, item);
		return true;
	}
	
	private boolean removeItem(int knapsack, int item) {
		addedItemsKnapsack[knapsack].remove((Integer)item);
		currentCapacity[knapsack] += itemWeight[item];
		simpleProfitValue[knapsack] -= profitMatrix[item][item];
		int newIndex = findFirstLowerElement(items, item) + 1;
		items.add(newIndex, item);
		removeUpdateExtraProfit(knapsack, item);
		ReinsertValidItems();
		return true;
	}
	
	private int findFirstLowerElement(Vector<Integer> vec, int item) {
		int low = 0, high = vec.size()-1;
		int result = -1;
		while(low <= high) {
			int mid = low + (high - low)/2;
			if(itemValue[vec.get(mid)] < itemValue[item]) {
				result = mid;
				low = mid + 1;
			}
			else {
				high = mid - 1;
			}
		}
		return result;
	}
	
	
	
	//se debe ingresar el elemento antes de utilizar esto para correcto funcionamiento
	private void addUpdateExtraProfit(int knapsack, int item) {
		Vector<Integer> addedItems = addedItemsKnapsack[knapsack];
		relationProfitValue[knapsack] = 0;
		for(int i = 0; i < extraProfitbyRelation[knapsack].length; i++) {
			if(item == i)
				continue;
			extraProfitbyRelation[knapsack][i] += profitMatrix[i][item];
		}
		for(int i = 0; i < addedItems.size(); i++)
			relationProfitValue[knapsack] += extraProfitbyRelation[knapsack][addedItems.get(i)];
		relationProfitValue[knapsack] /= 2;
	}
	
	private void removeUpdateExtraProfit(int knapsack, int item) {
		Vector<Integer> addedItems = addedItemsKnapsack[knapsack];
		relationProfitValue[knapsack] = 0;
		for(int i = 0; i < extraProfitbyRelation[knapsack].length; i++) {
			if(item == i)
				continue;
			extraProfitbyRelation[knapsack][i] -= profitMatrix[i][item];
		}
		for(int i = 0; i < addedItems.size(); i++)
			relationProfitValue[knapsack] += extraProfitbyRelation[knapsack][addedItems.get(i)];
		relationProfitValue[knapsack] /= 2;
	}
	
	private int getMinAverageVolumeItemIndex(Vector<Integer> itemList){
		int currentIndex = -1;
		int minValue = Integer.MAX_VALUE;
		for(int i = 0; i < itemList.size(); i++) {
			int item = itemList.get(i);
			int itemVolume = profitMatrix[item][item];
			for(int j = 0; j < knapsackNum; j++) {
				itemVolume += extraProfitbyRelation[j][item];
			}
			if(itemVolume < minValue) {
				minValue = itemVolume;
				currentIndex = i;
			}
		}
		return currentIndex;
	}
	
	private int getMaxAverageVolumeItemIndex(Vector<Integer> itemList){
		int currentIndex = -1;
		int maxValue = Integer.MIN_VALUE;
		for(int i = 0; i < itemList.size(); i++) {
			int item = itemList.get(i);
			int itemVolume = profitMatrix[item][item];
			for(int j = 0; j < knapsackNum; j++) {
				itemVolume += extraProfitbyRelation[j][item];
			}
			if(itemVolume > maxValue) {
				maxValue = itemVolume;
				currentIndex = i;
			}
		}
		return currentIndex;
	}
	
	private int getMinAverageDensityItemIndex(Vector<Integer> itemList){
		int currentIndex = -1;
		double minValue = Double.MAX_VALUE;
		for(int i = 0; i < itemList.size(); i++) {
			int item = itemList.get(i);
			int itemVolume = profitMatrix[item][item];
			for(int j = 0; j < knapsackNum; j++) {
				itemVolume += extraProfitbyRelation[j][item];
			}
			itemVolume /= itemWeight[item];
			if(itemVolume < minValue) {
				minValue = itemVolume;
				currentIndex = i;
			}
		}
		return currentIndex;
	}
	
	private int getMaxAverageDensityItemIndex(Vector<Integer> itemList){
		int currentIndex = -1;
		double maxValue = Double.MIN_VALUE;
		for(int i = 0; i < itemList.size(); i++) {
			int item = itemList.get(i);
			int itemVolume = profitMatrix[item][item];
			for(int j = 0; j < knapsackNum; j++) {
				itemVolume += extraProfitbyRelation[j][item];
			}
			itemVolume /= itemWeight[item];
			if(itemVolume > maxValue) {
				maxValue = itemVolume;
				currentIndex = i;
			}
		}
		return currentIndex;
	}
	
	private int getMinVolumeItemIndexinKnapsack(int knapsack) {
		Vector<Integer> itemList = addedItemsKnapsack[knapsack];
		int minVolume = Integer.MAX_VALUE;
		int itemIndex = -1;
		for(int i = 0; i < itemList.size(); i++) {
			int item = itemList.get(i);
			if(minVolume > (profitMatrix[item][item]+extraProfitbyRelation[knapsack][item])) {
				minVolume = (profitMatrix[item][item]+extraProfitbyRelation[knapsack][item]);
				itemIndex = i;
			}
		}
		return itemIndex;
	}
	
	private int getMaxVolumeItemIndexinKnapsack(int knapsack) {
		Vector<Integer> itemList = addedItemsKnapsack[knapsack];
		int maxVolume = Integer.MIN_VALUE;
		int itemIndex = -1;
		for(int i = 0; i < itemList.size(); i++) {
			int item = itemList.get(i);
			if(maxVolume < (profitMatrix[item][item]+extraProfitbyRelation[knapsack][item])) {
				maxVolume = (profitMatrix[item][item]+extraProfitbyRelation[knapsack][item]);
				itemIndex = i;
			}
		}
		return itemIndex;
	}
	
	private int getMinDensityItemIndexinKnapsack(int knapsack) {
		Vector<Integer> itemList = addedItemsKnapsack[knapsack];
		double minDensity = Double.MAX_VALUE;
		int itemIndex = -1;
		for(int i = 0; i < itemList.size(); i++) {
			int item = itemList.get(i);
			double itemDensity = (profitMatrix[item][item]+extraProfitbyRelation[knapsack][item])/itemWeight[item];
			if(minDensity > itemDensity) {
				minDensity = (profitMatrix[item][item]+extraProfitbyRelation[knapsack][item]);
				itemIndex = i;
			}
		}
		return itemIndex;
	}
	
	private int getMaxDensityItemIndexinKnapsack(int knapsack) {
		Vector<Integer> itemList = addedItemsKnapsack[knapsack];
		double maxDensity = Double.MIN_VALUE;
		int itemIndex = -1;
		for(int i = 0; i < itemList.size(); i++) {
			int item = itemList.get(i);
			double itemDensity = (profitMatrix[item][item]+extraProfitbyRelation[knapsack][item])/itemWeight[item];
			if(maxDensity < itemDensity) {
				maxDensity = (profitMatrix[item][item]+extraProfitbyRelation[knapsack][item]);
				itemIndex = i;
			}
		}
		return itemIndex;
	}
	
	
	private void ReinsertValidItems() {
		maxAvailableCapacity = Integer.MIN_VALUE;
		boolean changed = false;
		for(int i = 0; i < knapsackNum; i++)
			if(maxAvailableCapacity < currentCapacity[i]) {
				maxAvailableCapacity = currentCapacity[i];
				changed = true;
			}
		if(!changed)
			return;
		Set<Entry<Integer, Integer>> entrySet = invalidItems.entrySet();
		for(Map.Entry<Integer, Integer> entry: entrySet) {
			if(maxAvailableCapacity > itemWeight[entry.getKey()]) {
				items.add(entry.getKey());
				invalidItems.remove(entry.getKey());
			}	
		}
		items.sort((a,b)->{
			Double valA = itemValue[a];
			Double valB = itemValue[b];
			return valA.compareTo(valB);
		});
	}
	
	private void sortKnapsackbyVolume(int item) {
		knapsackList.sort((a,b) -> {
			Integer valA = extraProfitbyRelation[a][item];
			Integer valB = extraProfitbyRelation[b][item];
			return valA.compareTo(valB);
		});
	}
	
	private void sortKnapsackbyCurrentCapacity() {
		knapsackList.sort((a,b) -> {
			Integer valA = currentCapacity[a];
			Integer valB = currentCapacity[b];
			return valA.compareTo(valB);
		});
	}
	
	private boolean tryAddItemIncreasingOrder(int itemIndex) {
		for(int i = 0; i < knapsackList.size(); i++) {
			if(this.addItembyIndex(knapsackList.get(i), itemIndex))
				return true;
		}
		invalidItems.put(items.get(itemIndex), items.get(itemIndex));
		items.remove(itemIndex);
		return false;
	}
	
	private boolean tryAddItemDecreasingOrder(int itemIndex) {
		for(int i = knapsackList.size()-1; i >= 0; i--) {
			if(this.addItembyIndex(knapsackList.get(i), itemIndex))
				return true;
		}
		invalidItems.put(items.get(itemIndex), items.get(itemIndex));
		items.remove(itemIndex);
		return false;
	}
	
	//Heuristicas
	public boolean addHighestValueIteminHighVolumeKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = items.size()-1;
		this.sortKnapsackbyVolume(items.get(itemIndex));
		return tryAddItemDecreasingOrder(itemIndex);
	}
	
	public boolean addHighestValueIteminLowVolumeKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = items.size()-1;
		this.sortKnapsackbyVolume(items.get(itemIndex));
		return tryAddItemIncreasingOrder(itemIndex);
	}
	
	public boolean addHighestValueIteminHighCapacityKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = items.size()-1;
		this.sortKnapsackbyCurrentCapacity();
		return tryAddItemDecreasingOrder(itemIndex);
	}
	
	public boolean addHighestValueIteminLowCapacityKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = items.size()-1;
		this.sortKnapsackbyCurrentCapacity();
		return tryAddItemIncreasingOrder(itemIndex);
	}
	
	public boolean addLowestValueIteminHighVolumeKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = 0;
		this.sortKnapsackbyVolume(items.get(itemIndex));
		return tryAddItemDecreasingOrder(itemIndex);
	}
	
	public boolean addLowestValueIteminLowVolumeKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = 0;
		this.sortKnapsackbyVolume(items.get(itemIndex));
		return tryAddItemIncreasingOrder(itemIndex);
	}
	
	public boolean addLowestValueIteminHighCapacityKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = 0;
		this.sortKnapsackbyCurrentCapacity();
		return tryAddItemDecreasingOrder(itemIndex);
	}
	
	public boolean addLowestValueIteminLowCapacityKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = 0;
		this.sortKnapsackbyCurrentCapacity();
		return tryAddItemIncreasingOrder(itemIndex);
	}
	
	public boolean addHighestVolumeIteminHighVolumeKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = this.getMaxAverageVolumeItemIndex(items);
		this.sortKnapsackbyVolume(itemIndex);
		return tryAddItemDecreasingOrder(itemIndex);
	}
	
	public boolean addHighestVolumeIteminLowVolumeKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = this.getMaxAverageVolumeItemIndex(items);
		this.sortKnapsackbyVolume(itemIndex);
		return tryAddItemIncreasingOrder(itemIndex);
	}
	
	public boolean addHighestVolumeIteminHighCapacityKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = this.getMaxAverageVolumeItemIndex(items);
		this.sortKnapsackbyCurrentCapacity();
		return tryAddItemDecreasingOrder(itemIndex);
	}
	
	public boolean addHighestVolumeIteminLowCapacityKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = this.getMaxAverageVolumeItemIndex(items);
		this.sortKnapsackbyCurrentCapacity();
		return tryAddItemIncreasingOrder(itemIndex);
	}
	
	public boolean addLowestVolumeIteminHighVolumeKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = this.getMinAverageVolumeItemIndex(items);
		this.sortKnapsackbyVolume(itemIndex);
		return tryAddItemDecreasingOrder(itemIndex);
	}
	
	public boolean addLowestVolumeIteminLowVolumeKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = this.getMinAverageVolumeItemIndex(items);
		this.sortKnapsackbyVolume(itemIndex);
		return tryAddItemIncreasingOrder(itemIndex);
	}
	
	public boolean addLowsestVolumeIteminHighCapacityKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = this.getMinAverageVolumeItemIndex(items);
		this.sortKnapsackbyCurrentCapacity();
		return tryAddItemDecreasingOrder(itemIndex);
	}
	
	public boolean addLowestVolumeIteminLowCapacityKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = this.getMinAverageVolumeItemIndex(items);
		this.sortKnapsackbyCurrentCapacity();
		return tryAddItemIncreasingOrder(itemIndex);
	}
	
	public boolean addHighestDensityIteminHighVolumeKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = this.getMaxAverageDensityItemIndex(items);
		this.sortKnapsackbyVolume(itemIndex);
		return tryAddItemDecreasingOrder(itemIndex);
	}
	
	public boolean addHighestDensityIteminLowVolumeKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = this.getMaxAverageDensityItemIndex(items);
		this.sortKnapsackbyVolume(itemIndex);
		return tryAddItemIncreasingOrder(itemIndex);
	}
	
	public boolean addHighestDensityIteminHighCapacityKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = this.getMaxAverageDensityItemIndex(items);
		this.sortKnapsackbyCurrentCapacity();
		return tryAddItemDecreasingOrder(itemIndex);
	}
	
	public boolean addHighestDensityIteminLowCapacityKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = this.getMaxAverageDensityItemIndex(items);
		this.sortKnapsackbyCurrentCapacity();
		return tryAddItemIncreasingOrder(itemIndex);
	}
	
	public boolean addLowestDensityIteminHighVolumeKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = this.getMinAverageDensityItemIndex(items);
		this.sortKnapsackbyVolume(itemIndex);
		return tryAddItemDecreasingOrder(itemIndex);
	}
	
	public boolean addLowestDensityIteminLowVolumeKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = this.getMinAverageDensityItemIndex(items);
		this.sortKnapsackbyVolume(itemIndex);
		return tryAddItemIncreasingOrder(itemIndex);
	}
	
	public boolean addLowsestDensityIteminHighCapacityKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = this.getMinAverageDensityItemIndex(items);
		this.sortKnapsackbyCurrentCapacity();
		return tryAddItemDecreasingOrder(itemIndex);
	}
	
	public boolean addLowestDensityIteminLowCapacityKnapsack() {
		if(items.isEmpty())
			return false;
		int itemIndex = this.getMinAverageDensityItemIndex(items);
		this.sortKnapsackbyCurrentCapacity();
		return tryAddItemIncreasingOrder(itemIndex);
	}
	
	public boolean removeHighestValueItemFromLowCapacityKnapsack() {
		int knapsack = this.getMinCapacityKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = addedItemsKnapsack[knapsack].size() - 1;
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	public boolean removeLowestValueItemFromLowCapacityKnapsack() {
		int knapsack = this.getMinCapacityKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = 0;
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	public boolean removeHighestVolumeItemFromLowCapacityKnapsack() {
		int knapsack = this.getMinCapacityKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = this.getMaxVolumeItemIndexinKnapsack(knapsack);
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	public boolean removeLowestVolumeItemFromLowCapacityKnapsack() {
		int knapsack = this.getMinCapacityKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = this.getMinVolumeItemIndexinKnapsack(knapsack);
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	public boolean removeHighestDensityItemFromLowCapacityKnapsack() {
		int knapsack = this.getMinCapacityKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = this.getMaxDensityItemIndexinKnapsack(knapsack);
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	public boolean removeLowestDensityItemFromLowCapacityKnapsack() {
		int knapsack = this.getMinCapacityKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = this.getMinDensityItemIndexinKnapsack(knapsack);
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	//////
	
	public boolean removeHighestValueItemFromHighCapacityKnapsack() {
		int knapsack = this.getMaxCapacityKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = addedItemsKnapsack[knapsack].size() - 1;
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	public boolean removeLowestValueItemFromHighCapacityKnapsack() {
		int knapsack = this.getMaxCapacityKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = 0;
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	public boolean removeHighestVolumeItemFromHighCapacityKnapsack() {
		int knapsack = this.getMaxCapacityKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = this.getMaxVolumeItemIndexinKnapsack(knapsack);
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	public boolean removeLowestVolumeItemFromHighCapacityKnapsack() {
		int knapsack = this.getMaxCapacityKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = this.getMinVolumeItemIndexinKnapsack(knapsack);
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	public boolean removeHighestDensityItemFromHighCapacityKnapsack() {
		int knapsack = this.getMaxCapacityKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = this.getMaxDensityItemIndexinKnapsack(knapsack);
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	public boolean removeLowestDensityItemFromHighCapacityKnapsack() {
		int knapsack = this.getMaxCapacityKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = this.getMinDensityItemIndexinKnapsack(knapsack);
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	///////////////////////////////////////////////////////////////////////////////
	
	public boolean removeHighestValueItemFromLowVolumeKnapsack() {
		int knapsack = this.getMinCapacityKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = addedItemsKnapsack[knapsack].size() - 1;
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	public boolean removeLowestValueItemFromLowVolumeKnapsack() {
		int knapsack = this.getMinCapacityKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = 0;
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	public boolean removeHighestVolumeItemFromLowVolumeKnapsack() {
		int knapsack = this.getMinCapacityKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = this.getMaxVolumeItemIndexinKnapsack(knapsack);
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	public boolean removeLowestVolumeItemFromLowVolumeKnapsack() {
		int knapsack = this.getMinCapacityKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = this.getMinVolumeItemIndexinKnapsack(knapsack);
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	public boolean removeHighestDensityItemFromLowVolumeKnapsack() {
		int knapsack = this.getMinCapacityKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = this.getMaxDensityItemIndexinKnapsack(knapsack);
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	public boolean removeLowestDensityItemFromLowVolumeKnapsack() {
		int knapsack = this.getMinVolumeKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = this.getMinDensityItemIndexinKnapsack(knapsack);
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	//////
	
	public boolean removeHighestValueItemFromHighVolumeKnapsack() {
		int knapsack = this.getMaxVolumeKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = addedItemsKnapsack[knapsack].size() - 1;
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	public boolean removeLowestValueItemFromHighVolumeKnapsack() {
		int knapsack = this.getMaxVolumeKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = 0;
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	public boolean removeHighestVolumeItemFromHighVolumeKnapsack() {
		int knapsack = this.getMaxVolumeKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = this.getMaxVolumeItemIndexinKnapsack(knapsack);
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	public boolean removeLowestVolumeItemFromHighVolumeKnapsack() {
		int knapsack = this.getMaxVolumeKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = this.getMinVolumeItemIndexinKnapsack(knapsack);
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	public boolean removeHighestDensityItemFromHighVolumeKnapsack() {
		int knapsack = this.getMaxVolumeKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = this.getMaxDensityItemIndexinKnapsack(knapsack);
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	public boolean removeLowestDensityItemFromHighVolumeKnapsack() {
		int knapsack = this.getMaxVolumeKnapsack();
		if(addedItemsKnapsack[knapsack].isEmpty())
			return false;
		int itemIndex = this.getMinDensityItemIndexinKnapsack(knapsack);
		return this.removeItembyIndex(knapsack, itemIndex);
	}
	
	public boolean bestItemSwap() {
		int item1 = -1,item2 = -1;
		int knapsack1 = -3,knapsack2 = -1;
		int bestDelta = Integer.MIN_VALUE;
		//knapsack1 se asignara -2 si el item elegido viene de los elementos invalidos y -1 si viene de elementos no agregados validos
		//revisamos items en lista invalida como item1
		for(Map.Entry<Integer, Integer> entry: invalidItems.entrySet()) {
			for(int i = 0; i < knapsackNum; i++) {
				int candidateItem = this.bestItemSwapinKnapsack(-2, i, entry.getKey());
				if(candidateItem < 0)
					continue;
				//se resta profitMatrix[entry.getKey()][candidateItem] dado que extraProfitbyRelation al representar el profit extra que daria el item
				//al ser agregado y no se esta quitando de inmediato el primer item esta considerando intrinsecamente el profit extra entre estos por lo que
				//hay que restarlo
				int delta = -profitMatrix[candidateItem][candidateItem] + profitMatrix[entry.getKey()][entry.getKey()] - 
						extraProfitbyRelation[i][candidateItem] + extraProfitbyRelation[i][entry.getKey()] - profitMatrix[entry.getKey()][candidateItem];
				if(delta > 0 && delta > bestDelta) {
					bestDelta = delta;
					item1 = entry.getKey();
					item2 = candidateItem;
					knapsack1 = -2;
					knapsack2 = i;
				}
			}
		}
		//revisamos items lista general como item1
		for(int i = 0; i < items.size(); i++) {
			for(int j = 0; j < knapsackNum; j++) {
				int candidateItem = this.bestItemSwapinKnapsack(-1, i, items.get(i));
				if(candidateItem < 0)
					continue;
				int delta = -profitMatrix[candidateItem][candidateItem] + profitMatrix[items.get(i)][items.get(i)] - 
						extraProfitbyRelation[i][candidateItem] + extraProfitbyRelation[i][items.get(i)] - profitMatrix[items.get(i)][candidateItem];
				if(delta > 0 && delta > bestDelta) {
					bestDelta = delta;
					item1 = items.get(i);
					item2 = candidateItem;
					knapsack1 = -1;
					knapsack2 = i;
				}
			}
		}
		//revisamos item en knapsacks como item1
		for(int i = 0; i < knapsackNum - 1; i++) {
			for(int j = i+1; j < knapsackNum; j++) {
				for(int item : addedItemsKnapsack[i]) {
					int candidateItem = this.bestItemSwapinKnapsack(i, j, items.get(i));
					if(candidateItem < 0)
						continue;
					int delta1 = -extraProfitbyRelation[i][item] + extraProfitbyRelation[i][candidateItem] - profitMatrix[item][candidateItem];
					int delta2 = -extraProfitbyRelation[j][candidateItem] + extraProfitbyRelation[j][item] - profitMatrix[item][candidateItem];
					if((delta1 + delta2) > 0 && (delta1 + delta2) > bestDelta) {
						bestDelta = (delta1 + delta2);
						item1 = item;
						item2 = candidateItem;
						knapsack1 = i;
						knapsack2 = j;
					}
				}
			}
		}
		if(knapsack2 == -1)
			return false;
		
		if(knapsack1 >= 0) {
			this.removeItem(knapsack1, item1);
			this.removeItem(knapsack2,item2);
			this.addItem(knapsack2, item1);
			this.addItem(knapsack1, item2);
			return true;
		}
		this.removeItem(knapsack2,item2);
		this.addItem(knapsack2, item1);
		
		if(knapsack1 == -2)
			invalidItems.remove(item1);
		return true;
	}
	
	private int bestItemSwapinKnapsack(int originKnapsack, int knapsackNum, int itemId) {
		int bestItemSwap = -1;
		int bestSwapDelta = Integer.MIN_VALUE;
		if(originKnapsack < 0) {//el item no viende de un knpasack
			for(int i = 0; i < addedItemsKnapsack[knapsackNum].size(); i++) {
				int itemId2 = addedItemsKnapsack[knapsackNum].get(i);
				if(currentCapacity[knapsackNum] - itemWeight[itemId2] + itemWeight[itemId] < 0)
					continue;
				int deltaKnapsack = -extraProfitbyRelation[knapsackNum][itemId2] + extraProfitbyRelation[knapsackNum][itemId]-profitMatrix[itemId][itemId2]+profitMatrix[itemId2][itemId2];
				if(deltaKnapsack > bestSwapDelta) {
					bestSwapDelta = deltaKnapsack;
					bestItemSwap = itemId2;
				}
			}
			return bestItemSwap;	
		}
		//el item viene de un knapsack
		for(int i = 0; i < addedItemsKnapsack[knapsackNum].size(); i++) {
			int itemId2 = addedItemsKnapsack[knapsackNum].get(i);
			if(currentCapacity[originKnapsack] - itemWeight[itemId] + itemWeight[itemId2] < 0 || 
					currentCapacity[knapsackNum] - itemWeight[itemId2] + itemWeight[itemId] < 0)
				continue;
			int deltaOrigin = -extraProfitbyRelation[originKnapsack][itemId] + extraProfitbyRelation[originKnapsack][itemId2]-profitMatrix[itemId][itemId2];
			int deltaKnapsack = -extraProfitbyRelation[knapsackNum][itemId2] + extraProfitbyRelation[knapsackNum][itemId]-profitMatrix[itemId][itemId2];
			if(deltaOrigin + deltaKnapsack > bestSwapDelta) {
				bestSwapDelta = deltaOrigin + deltaKnapsack;
				bestItemSwap = itemId2;
			}
		}
		return bestItemSwap;
	}
	
	
	public int getTotalProfit() {
		int totalProfit = 0;
		for(int i = 0; i < knapsackNum; i++) {
			totalProfit += simpleProfitValue[i];
			totalProfit += relationProfitValue[i];
		}
		return totalProfit;
	}
	
	public int getOptimal() {
		return optimal;
	}
	
	
	
	private int getMinCapacityKnapsack() {
		int knapsack = -1;
		int minCapacity = Integer.MAX_VALUE;
		for(int i = 0; i < knapsackNum; i++)
			if(currentCapacity[i] < minCapacity) {
				minCapacity = currentCapacity[i];
				knapsack = i;
			}
		return knapsack;
	}
	
	private int getMaxCapacityKnapsack() {
		int knapsack = -1;
		int maxCapacity = Integer.MIN_VALUE;
		for(int i = 0; i < knapsackNum; i++)
			if(currentCapacity[i] > maxCapacity) {
				maxCapacity = currentCapacity[i];
				knapsack = i;
			}
		return knapsack;
	}
	
	private int getMinVolumeKnapsack() {
		int knapsack = -1;
		int minVolume = Integer.MAX_VALUE;
		for(int i = 0; i < knapsackNum; i++)
			if(simpleProfitValue[i]+relationProfitValue[i] < minVolume) {
				minVolume = simpleProfitValue[i]+relationProfitValue[i];
				knapsack = i;
			}
		return knapsack;
	}
	
	private int getMaxVolumeKnapsack() {
		int knapsack = -1;
		int maxVolume = Integer.MIN_VALUE;
		for(int i = 0; i < knapsackNum; i++)
			if(simpleProfitValue[i]+relationProfitValue[i] > maxVolume) {
				maxVolume = simpleProfitValue[i]+relationProfitValue[i];
				knapsack = i;
			}
		return knapsack;
	}
	
	
	public void printKnapsack(int knapsack) {
		System.out.println("KNAPSACK NUMERO "+knapsack);
		System.out.println("CAPACIDAD MAXIMA: "+ maxCapacity);
		System.out.println("PESO AGREGADO: "+ (maxCapacity - currentCapacity[knapsack]));
		System.out.println("profitSimple: "+ simpleProfitValue[knapsack]);
		System.out.println("profitExtra: "+ relationProfitValue[knapsack]);
		System.out.println("ITEMS");
		System.out.println(addedItemsKnapsack[knapsack]);
		System.out.println("VOLUME ITEMS");
		for(int i = 0; i < addedItemsKnapsack[knapsack].size(); i++) {
			System.out.println("Item: " +addedItemsKnapsack[knapsack].get(i) + " "+ itemValue[addedItemsKnapsack[knapsack].get(i)]);
		}
		System.out.println("PROFIT ITEMS");
		for(int i = 0; i < addedItemsKnapsack[knapsack].size(); i++) {
			System.out.println("Item: " +addedItemsKnapsack[knapsack].get(i) + " "+ profitMatrix[addedItemsKnapsack[knapsack].get(i)][addedItemsKnapsack[knapsack].get(i)]);
		}
		System.out.println("WEIGHT ITEMS");
		for(int i = 0; i < addedItemsKnapsack[knapsack].size(); i++) {
			System.out.println("Item: " +addedItemsKnapsack[knapsack].get(i) + " "+ itemWeight[addedItemsKnapsack[knapsack].get(i)]);
		}
		System.out.println("EXTRA VALUE ARRAY");
		for(int i = 0; i < addedItemsKnapsack[knapsack].size(); i++) {
			System.out.println("Item: " +addedItemsKnapsack[knapsack].get(i) + " "+ extraProfitbyRelation[knapsack][addedItemsKnapsack[knapsack].get(i)]);
		}
	}
	
	public boolean isCoherent() {
		
		for(int i = 0; i < itemWeight.length; i++) {
			boolean isPresent = false;
			for(int j = 0; j < knapsackNum; j++) {
				if(addedItemsKnapsack[j].contains(i)) {
					isPresent = true;
					break;
				}
			}

			if(items.contains(i) || invalidItems.containsKey(i))
				isPresent = true;
			if(isPresent)
				continue;
			return false;
		}
		int itemCount = 0;
		itemCount+= items.size();
		itemCount+= invalidItems.size();
		for(int i = 0; i < knapsackNum; i++) {
			itemCount += addedItemsKnapsack[i].size();
			Vector<Integer> itemList = addedItemsKnapsack[i];
			int weight = 0, simpleProfit = 0, extraProfit = 0, extraProfit2 = 0;
			for(int j = 0; j < itemList.size(); j++) {
				weight += itemWeight[itemList.get(j)];
				simpleProfit += profitMatrix[itemList.get(j)][itemList.get(j)];
			}
			if(weight != (maxCapacity - currentCapacity[i]))
				return false;
			if(simpleProfit != simpleProfitValue[i])
				return false;
			for(int j = 0; j < itemList.size() - 1; j++)
				for(int k = j+1; k < itemList.size(); k++) {
					extraProfit += profitMatrix[itemList.get(j)][itemList.get(k)];
				}
			for(int j = 0; j < itemList.size(); j++) {
				extraProfit2=extraProfitbyRelation[i][itemList.get(j)];
			}
			if(extraProfit != relationProfitValue[i] || extraProfit2/2 != relationProfitValue[i])
				return false;
		}
		if(itemCount != itemWeight.length)
			return false;
		return true;
	}
	
	public void printExtraProfit(int x, int y) {
		System.out.println(profitMatrix[x][y]);
	}
	
	public void printSortedKnapsacksbyVolume(int item) {
		this.sortKnapsackbyVolume(item);
		for(int i = 0; i < knapsackList.size(); i++)
			System.out.println(knapsackList.get(i)+": "+  extraProfitbyRelation[knapsackList.get(i)][item]);
	}
	
	public void printSortedKnapsacksbyCapacity() {
		this.sortKnapsackbyCurrentCapacity();
		for(int i = 0; i < knapsackList.size(); i++)
			System.out.println(knapsackList.get(i)+": "+  currentCapacity[knapsackList.get(i)]);
	}
	
	public void printItemsValue() {
		for(int i = 0; i < items.size(); i++) {
			System.out.println(items.get(i)+" "+ itemValue[items.get(i)]);
		}
	}
	
	public void printItemData(int item) {
		System.out.println("Weight "+ itemWeight[item]);
		System.out.println("profit "+ profitMatrix[item][item]);
	}
}