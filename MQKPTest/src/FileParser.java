import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

public class FileParser {
	FileParser(){}
	public static Instance constructInstance(File file, int knapsackNum) {
		try {
			BufferedReader objReader = new BufferedReader(new FileReader(file));	
			String fileName = objReader.readLine();
			int nItems = Integer.parseInt(objReader.readLine());
			int[][] profitMatrix = new int[nItems][nItems];
			int[] itemWeight = new int[nItems];
			//obtenemos los valores de profit
			String[] strNums = objReader.readLine().stripIndent().split("\\s+");
			for(int i = 0; i < nItems; i++) {
				profitMatrix[i][i] = Integer.parseInt(strNums[i]);
			}
			//valores de profit por relacion
			for(int i = 0; i < nItems; i++) {
				strNums = objReader.readLine().stripIndent().split("\\s+");
				for(int j = i + 1; j < nItems; j++) {
					profitMatrix[i][j] = Integer.parseInt(strNums[j - (i + 1)]);
					profitMatrix[j][i] = Integer.parseInt(strNums[j - (i + 1)]);
				}
			}
			//saltamos lineas con informacion no necesaria
			objReader.readLine();
			objReader.readLine();
			//obtenemos el peso de cada elemento
			strNums = objReader.readLine().stripIndent().split("\\s+");
			for(int i = 0; i < nItems; i++) {
				itemWeight[i] = Integer.parseInt(strNums[i]);
			}
			objReader.close();
			return new Instance(fileName, knapsackNum, itemWeight, profitMatrix);
		} 
		catch(IOException e) {
				System.out.println("Error leyendo archivo");
				e.printStackTrace();
				return null;
		}
	}
}
