import java.io.File;

public class main {

	public static void main(String[] args) {
		File file = new File("/home/vicente/eclipse-workspace/MQKPTest/Data/Instances/toyProblem.txt");
		Instance ins;
		ins = FileParser.constructInstance(file, 2);
		ins.addHighestDensityIteminHighCapacityKnapsack();
		ins.addHighestDensityIteminLowCapacityKnapsack();
		ins.printSortedKnapsacksbyCapacity();
		ins.printSortedKnapsacksbyVolume(0);
		ins.printItemsValue();
		ins.printKnapsack(1);
		if(ins.isCoherent()) {
			System.out.println("-------*--------");
			System.out.println("COHERENTE");
			System.out.println("-------*--------");
		}
		else {
			System.out.println("-------*--------");
			System.out.println("INCOHERENTE");
			System.out.println("-------*--------");
		}
	}

}
