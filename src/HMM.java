import java.io.*;
import java.util.*;

import com.sun.xml.internal.ws.util.StringUtils;

public class HMM {
	/*
	 * Author : LiangZHANG
	 * Date   : 2015-8-27
	 * E-mail : Liangzxdu@foxmail.com
	 * 
	 * Information: This class is HMM(hidden markov model),
	 * the training dataset is in the "trainingSet" folder.
	 * we use training dataset to creat the HMM and then use HMM to do 
	 * Chinese segmentation.See "ChineseSegmentation.doc" in this project
	 * for more information about class and method.
	 */
	
	int countOfChineseWords = 40870; // who ta ma knows. 19968 to 40869
	int countOfState = 4;
	double countB=0,countE=0,countM=0,countS=0;
    double [] initialMatrix = new double [countOfState];
    double [][] stateTransitionMatrix = new double [countOfState][countOfState];
    double [][] observaMatrix = new double [countOfState][countOfChineseWords];
	String trainingSetPath = "trainingSet/msr_training.txt";
    String dataPath = "trainingSet/data.txt";
    String matrixDataFile = "data/matrixData.txt";
    
	public void txtPretreatment () throws Exception {
		
		File trainingSetFile = new File(trainingSetPath);
		//File [] fileReadList = trainingSetFolder.listFiles();
		File outputFile = new File(dataPath);
		PrintWriter output = new PrintWriter(outputFile);
		Scanner input = new Scanner(trainingSetFile);
		while (input.hasNext()) {
			String fileLine = input.nextLine();
			fileLine = filter(fileLine); // filter all the punctuation
			System.out.println(fileLine);
			String [] lineSplit = fileLine.trim().split(" ");
			String label = createLabel (lineSplit);
			String newLine = createNewLine(lineSplit);
			System.out.println(newLine);
			System.out.println(label);
			if (newLine.length() != label.length())   System.out.println("!!!!!-----error-----!!!!!");
			if (newLine.length() == 0) continue;
			output.println(newLine);
			output.println(label);
			//break;
		}        
		output.close();
		input.close();
	}
	
	public String createNewLine(String[] lineSplit) {
		String newLine = "";
		for (int i = 0 ; i<lineSplit.length; i++) {
			if (lineSplit[i].length() == 1 && lineSplit[i].equals(" "))
			{   continue;   }
			newLine += lineSplit[i];
		}
		return newLine;
	}
	
	public String createLabel (String [] lineSplit) {
		String label = "";
		for ( int i = 0 ; i<lineSplit.length; i++) {
			// add label(BEMS) to String label for every line;
			int length = lineSplit[i].length();
			if (length == 1)
			{   label += "S";   }
			else if (length == 2)
			{   label += "BE";   }
			else if (length >=3)
			{   
				label += "B";
				for (int j = 0; j<length-2; j++) 
				    label += "M";
				label += "E";
			}
			else
			{   continue;   }
		}
		return label;
	}
	
	public String filter (String str) {
		char [] charList = str.toCharArray();
		for (int i = 0; i<str.length(); i++) {
			int c = (int)charList[i];
			if (c<19968 || c>40869) { // Chinese in unicode is from 19968 to 40869
				charList[i] = ' ';
			}
		}
		str = String.valueOf(charList);
		return str;
	}
	
	public void training () throws Exception{
		// calculate the probability of three matrix
		File dataFile = new File(dataPath);
		Scanner input = new Scanner (dataFile);
		String line, label;
		
		while (input.hasNext()) {
			line = input.nextLine();
			//if (!input.hasNext()) break;
			label = input.nextLine();
			//System.out.println(line+"\n"+label);
			for (int i =0 ; i<label.length(); i++) {
				if (label.charAt(i) == 'B') countB ++;
				if (label.charAt(i) == 'E') countE ++;
				if (label.charAt(i) == 'M') countM ++;
				if (label.charAt(i) == 'S') countS ++;
			}// for countB,E,M,S
			
			if (label.charAt(0) == 'B')
				initialMatrix[0] += 1;
			else if (label.charAt(0) == 'S')
				initialMatrix[3] += 1;
			else System.out.println("some thing is wrong~");
			//for the initialMatrix[][]
			
			for (int i = 0 ; i<label.length()-1; i++) {
				//for stateTransitionMatrix[][]
				if (label.charAt(i) == 'B') {
					if (label.charAt(i+1) == 'B')
						stateTransitionMatrix[0][0] += 1;
					else if (label.charAt(i+1) == 'E')
						stateTransitionMatrix[0][1] += 1;
					else if (label.charAt(i+1) == 'M')
						stateTransitionMatrix[0][2] += 1;
					else if (label.charAt(i+1) == 'S')
						stateTransitionMatrix[0][3] += 1;
					else System.out.println("wrong in Training function B");
				}
				else if (label.charAt(i) == 'E') {
					if (label.charAt(i+1) == 'B')
						stateTransitionMatrix[1][0] += 1;
					else if (label.charAt(i+1) == 'E')
						stateTransitionMatrix[1][1] += 1;
					else if (label.charAt(i+1) == 'M')
						stateTransitionMatrix[1][2] += 1;
					else if (label.charAt(i+1) == 'S')
						stateTransitionMatrix[1][3] += 1;
					else System.out.println("wrong in Training function E");
				}
				else if (label.charAt(i) == 'M') {
					if (label.charAt(i+1) == 'B')
						stateTransitionMatrix[2][0] += 1;
					else if (label.charAt(i+1) == 'E')
						stateTransitionMatrix[2][1] += 1;
					else if (label.charAt(i+1) == 'M')
						stateTransitionMatrix[2][2] += 1;
					else if (label.charAt(i+1) == 'S')
						stateTransitionMatrix[2][3] += 1;
					else System.out.println("wrong in Training function M");
				}
				else if (label.charAt(i) == 'S') {
					if (label.charAt(i+1) == 'B')
						stateTransitionMatrix[3][0] += 1;
					else if (label.charAt(i+1) == 'E')
						stateTransitionMatrix[3][1] += 1;
					else if (label.charAt(i+1) == 'M')
						stateTransitionMatrix[3][2] += 1;
					else if (label.charAt(i+1) == 'S')
						stateTransitionMatrix[3][3] += 1;
					else System.out.println("wrong in Training function S");
				}
				else System.out.println("wrong in Training function label");
			}// End for ,and this is for stateTransitionMatrix
			
			for (int i = 0 ; i<label.length(); i++) {
				int num = (int)line.charAt(i);
				if (label.charAt(i) == 'B')
					observaMatrix[0][num] += 1;
				else if (label.charAt(i) == 'E')
					observaMatrix[1][num] += 1;
				else if (label.charAt(i) == 'M')
					observaMatrix[2][num] += 1;
				else if (label.charAt(i) == 'S')
					observaMatrix[3][num] += 1;
				else System.out.println("some thing wrong for creat observaMatrix");
			}
		}
		double sumInitialMatrix = initialMatrix[0]+initialMatrix[1]+initialMatrix[2]+initialMatrix[3];
		initialMatrix[0] = Math.exp(initialMatrix[0]/sumInitialMatrix);
		initialMatrix[1] = Math.exp(initialMatrix[1]/sumInitialMatrix);
		initialMatrix[2] = Math.exp(initialMatrix[2]/sumInitialMatrix);
		initialMatrix[3] = Math.exp(initialMatrix[3]/sumInitialMatrix);
		// for initialMatrix
		
		for (int i = 0 ; i<stateTransitionMatrix.length; i++) {
			double count = 0;
			if (i == 0) count = countB;
			else if (i == 1) count = countE;
			else if (i == 2) count = countM;
			else if (i == 3) count = countS;
			else {System.out.println("wrong for stateTransitionMatrix,will divide zero");}
			for (int j=0 ; j<stateTransitionMatrix[i].length; j++) {
				stateTransitionMatrix[i][j] = Math.exp(stateTransitionMatrix[i][j]/count);
			}
		}// for stateTransitionMatrix
		
		for (int i = 0 ; i<observaMatrix.length; i++) {
			double count = 0;
			if (i == 0) count = countB;
			else if (i == 1) count = countE;
			else if (i == 2) count = countM;
			else if (i == 3) count = countS;
			else {System.out.println("wrong for observaMatrix,will divide zero");}
			for (int j=0 ; j<observaMatrix[i].length; j++) {
				observaMatrix[i][j] = Math.exp(observaMatrix[i][j]/count);
			}
		}// for observaMatrix
		
		
		for (int i = 0 ; i<initialMatrix.length; i++) {
			System.out.print(initialMatrix[i]+" ");
		}   System.out.println();
		for (int i = 0; i<stateTransitionMatrix.length; i++) {
			for (int j = 0; j < stateTransitionMatrix[i].length; j++) {
				System.out.print(stateTransitionMatrix[i][j] +" ");
			}
			System.out.println();
		} // print, have a look;
		
		input.close();
	}
    
	public void saveData() throws Exception{
		File fileOutput = new File(matrixDataFile);
		PrintWriter output = new PrintWriter(fileOutput);
		for (int i = 0; i<initialMatrix.length; i++) {
			output.println(initialMatrix[i]);
		}// output initialMatrix
		for (int i = 0 ; i<stateTransitionMatrix.length; i++) {
			for (int j = 0; j<stateTransitionMatrix[i].length; j++) {
				output.println(stateTransitionMatrix[i][j]);
			}
		}// output stateTransitionMatrix
		for (int i = 0 ; i<observaMatrix.length; i++) {
			for (int j = 0; j<observaMatrix[i].length; j++) {
				output.println(observaMatrix[i][j]);
			}
		}//output observaMatrix
		System.out.println("Finished Save Data.");
		output.close();
	}
	
	public void loadData() throws Exception{
		File inputFile = new File(matrixDataFile);
		Scanner input  = new Scanner(inputFile);
		for (int i = 0; i<initialMatrix.length; i++) {
			initialMatrix[i] = input.nextDouble();
		}// input initialMatrix
		
		for (int i = 0 ; i<stateTransitionMatrix.length; i++) {
			for (int j = 0; j<stateTransitionMatrix[i].length; j++) {
				stateTransitionMatrix[i][j] = input.nextDouble();
			}
		}// input stateTransitionMatrix
		for (int i = 0 ; i<observaMatrix.length; i++) {
			for (int j = 0; j<observaMatrix[i].length; j++) {
				observaMatrix[i][j] = input.nextDouble();
			}
		}//input observaMatrix
		System.out.println("Finished Load Data.");
		input.close();
	}
	
    public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
        HMM myHMM = new HMM();
        //myHMM.txtPretreatment();
        //myHMM.training(); // ok~
        //myHMM.saveData();
        myHMM.loadData();
	}

}
