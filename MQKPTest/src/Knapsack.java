import java.util.LinkedHashMap;
import java.util.Map;

public class Knapsack {
	private MultiMap<Double, Integer> itemsbyProfit;
	private MultiMap<Double, Integer>  itemsbyWeight;
	double[][] profitMatrix;
	double[] itemWeight;
	private double extraProfitbyRelation[];//arreglo del profit extra que entrega cada individuo dadas sus relaciones
	private LinkedHashMap<Integer, Integer> untrackedElementsExtraProfit;
	int itemAmmount;
	double maxCapacity, currentCapacity, profitValue, extraProfitValue;
	
	public Knapsack(double maxCapacity, double[][] profitMatrix, double[] weightVector) {
		itemAmmount = 0;
		profitValue = 0;
		extraProfitValue = 0;
		this.maxCapacity = maxCapacity;
		currentCapacity = maxCapacity;
		itemsbyProfit = new MultiMap<Double, Integer>();
		itemsbyWeight = new MultiMap<Double, Integer>();
		this.profitMatrix = profitMatrix;
		this.itemWeight = weightVector;
		untrackedElementsExtraProfit = new LinkedHashMap<Integer, Integer>();
		extraProfitbyRelation = new double[weightVector.length];
	}
	
	public boolean addItem(int idItem) {
		if(itemWeight[idItem] > currentCapacity)
			return false;
		currentCapacity -= itemWeight[idItem];
		profitValue += profitMatrix[idItem][idItem];
		itemAmmount++;
		itemsbyProfit.addElement(profitMatrix[idItem][idItem], idItem);
		itemsbyWeight.addElement(itemWeight[idItem], idItem);
		untrackedElementsExtraProfit.put(idItem, 1);
		return true;
	}
	
	public boolean removeItem(int idItem) {
		if(!itemsbyWeight.contains(itemWeight[idItem], idItem))
			return false;
		currentCapacity += itemWeight[idItem];
		profitValue -= profitMatrix[idItem][idItem];
		itemAmmount = 0;
		itemsbyProfit.deleteElement(profitMatrix[idItem][idItem], idItem);
		itemsbyWeight.deleteElement(itemWeight[idItem], idItem);
		if(untrackedElementsExtraProfit.containsKey(idItem))
			untrackedElementsExtraProfit.remove(idItem);
		return true;
	}
	
	public double getCurrentCapacity() {
		return currentCapacity;
	}
	
	private void updateExtraProfitbyRelation() {
		double updatedExtraProfit = 0;
		//se suman los profit obtenidos por relaciones entre elementos
		for(int i = 0; i < extraProfitbyRelation.length; i++) {
			for(Map.Entry<Integer, Integer> entry : untrackedElementsExtraProfit.entrySet()) {
				if(entry.getKey() == i)
					continue;
				extraProfitbyRelation[i] += profitMatrix[i][entry.getKey()];
			}
			
			updatedExtraProfit+= extraProfitbyRelation[i];
		}
		//se cuenta cada relacion dos veces por lo que se divide en 2
		extraProfitValue = updatedExtraProfit/2.0;
	}
	
	private void print() {
		System.out.println(itemsbyProfit);
		System.out.println(itemsbyWeight);
	}
}
