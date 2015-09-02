import java.io.*;
import java.util.*;

//import com.sun.xml.internal.ws.util.StringUtils;

public class HMM {
	/*
	 * Author : LiangZHANG
	 * Date   : 2015-8-27
	 * E-mail : Liangzxdu@foxmail.com
	 * 
	 * Information: This class is HMM(Hidden Markov Model),
	 * the training dataSet is in the "trainingSet" folder.
	 * we use training dataSet to create the HMM and then use HMM to do 
	 * Chinese segmentation. See "ChineseSegmentation.doc" in this project
	 * for more information about class and method.
	 */
	
	int countOfChineseWords = 40870; // Chinese in unicode is from 19968 to 40869
	int countOfState = 4; // there are B,E,M,S four state.
	double countB=0,countE=0,countM=0,countS=0;// count the number of BEMS
    double [] initialMatrix = new double [countOfState];
    double [][] stateTransitionMatrix = new double [countOfState][countOfState];
    double [][] observaMatrix = new double [countOfState][countOfChineseWords];
	String trainingSetPath = "trainingSet/msr_training.txt";
    String dataPath = "trainingSet/data.txt";
    String matrixDataFile = "data/matrixData.txt";
    
    
	public void txtPretreatment () throws Exception {
		// process the text,let the program much easy to train HMM model
		// for every line in the text, first filter the punctuation
		// then split to a String array and filter whitespace
		// at least,create the label and rebuilt the String
		File trainingSetFile = new File(trainingSetPath);
		File outputFile = new File(dataPath);
		PrintWriter output = new PrintWriter(outputFile);
		Scanner input = new Scanner(trainingSetFile);
		System.out.println("Read File address = "+trainingSetPath);
		
		while (input.hasNext()) {
			String fileLine = input.nextLine();
			fileLine = filter(fileLine); // filter all the punctuation
			//System.out.println(fileLine);
			String [] lineSplit = fileLine.trim().split(" ");
			String label = createLabel (lineSplit);
			String newLine = createNewLine(lineSplit);
			//System.out.println(newLine);
			//System.out.println(label);
			if (newLine.length() != label.length())   
				System.out.println("-----error-----");
			if (newLine.length() == 0) continue; // filter the whitespace
			output.println(newLine);
			output.println(label);
			//break;
		}        
		output.close(); 
		input.close();
	}
	
	public String createNewLine(String[] lineSplit) {
		// create a String from the String[] lineSplit(ignore the whitespace)
		String newLine = "";
		for (int i = 0 ; i<lineSplit.length; i++) {
			if (lineSplit[i].length() == 1 && lineSplit[i].equals(" "))
			{   continue;   } // filter whitespace;
			newLine += lineSplit[i];
		}
		return newLine;
	}
	
	public String createLabel (String [] lineSplit) {
		// create label(BEMS) for training dataSet then return it
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
			{   continue;   } //ignore zero
		}
		return label;
	}
	
	public String filter (String str) {
		// filter all the char which is not Chinese
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
		// calculate the probability of three matrix(training HMM)
		File dataFile = new File(dataPath);
		Scanner input = new Scanner (dataFile);
		String line, label;
		
		while (input.hasNext()) {
			line = input.nextLine();
			label = input.nextLine();
			//System.out.println(line+"\n"+label);
			for (int i =0 ; i<label.length(); i++) {
				// for countB,E,M,S
				if (label.charAt(i) == 'B') countB ++;
				if (label.charAt(i) == 'E') countE ++;
				if (label.charAt(i) == 'M') countM ++;
				if (label.charAt(i) == 'S') countS ++;
			}   countE --;
		    
			for (int i = 0 ; i<label.length(); i++) {
				// for initialMatrix
			    if (label.charAt(i) == 'B')
				    initialMatrix[0] += 1;
			    else if (label.charAt(i) == 'S')
				    initialMatrix[3] += 1;
			    //else System.out.println("some thing is wrong~");
			}
			
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
			}// End for ,and this is training for stateTransitionMatrix[][];
			
			for (int i = 0 ; i<label.length(); i++) {
				// this is training for the observaMatrix
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
		} // end for while (input)
		
		System.out.println("\nThe count:"+countB+" "+countE+" "+countM+" "+countS+"\n");
		
		double sumInitialMatrix = initialMatrix[0]+initialMatrix[1]+initialMatrix[2]+initialMatrix[3];
		for (int i = 0; i<countOfState; i++) {
			initialMatrix[i] = Math.exp(initialMatrix[i]/sumInitialMatrix);
		}   // Math.exp for initialMatrix		
		
		for (int i = 0 ; i<stateTransitionMatrix.length; i++) {
			// Math.exp for stateTransitionMatrix
			double count = 0;
			if (i == 0) count = countB;
			else if (i == 1) count = countE;
			else if (i == 2) count = countM;
			else if (i == 3) count = countS;
			else {System.out.println("wrong for stateTransitionMatrix,will divide zero");}
			for (int j=0 ; j<stateTransitionMatrix[i].length; j++) {
				stateTransitionMatrix[i][j] = Math.exp(stateTransitionMatrix[i][j]/count);
			}
		}
		
		for (int i = 0 ; i<observaMatrix.length; i++) {
			// Math.exp for observaMatrix
			double count = 0;
			if (i == 0) count = countB;
			else if (i == 1) count = countE;
			else if (i == 2) count = countM;
			else if (i == 3) count = countS;
			else {System.out.println("wrong for observaMatrix,will divide zero");}
			for (int j=0 ; j<observaMatrix[i].length; j++) {
				observaMatrix[i][j] = Math.exp(observaMatrix[i][j]/count);
			} 
		}
		
		//printObservaMatrix();
		printVector(initialMatrix, "in TrainMatrix,print the initialMatrix");
		printMatrix(stateTransitionMatrix, "in TrainMatrix,print the stateTransitionMatrix");
		
		input.close();
	}
	
	public void printObservaMatrix () {
		// print the observaMatrix(only print the column which is not all zero!)
		print("print the value of the observaMatrix.");
		for (int i = 0 ; i<countOfChineseWords; i++) {
			if (! allZero(i)) {
				System.out.printf("%15c %15f %15f %15f %15f\n",(char)i,observaMatrix[0][i]
						,observaMatrix[1][i],observaMatrix[2][i],observaMatrix[3][i]);
			}
		}   print("");
	}
	
	public boolean allZero (int index) {
		// whether a vector(countOfState*1) is all zero,true or false
		for (int i = 0 ; i<countOfState; i++) {
			if (observaMatrix[i][index] != 1.0) //1.0 == Math.exp(0)
				return false;
		}
		return true;
	}
	
    public void printVector(double [] vector, String str) {
    	// print a vector(countOfState*1) also print some information
    	print(str);
    	for (int i = 0 ; i<vector.length; i++) {
    		System.out.printf("%15f",vector[i]);
    	}   print("\n");
    }
    
	public void saveData() throws Exception{
		// save three matrix(initialMatrix,stateTransitionMatrix,observaMatrix)
		// to file (data/matrixData.txt)
		File fileOutput = new File(matrixDataFile);
		PrintWriter output = new PrintWriter(fileOutput);
		for (int i = 0; i<initialMatrix.length; i++) {
			output.println(initialMatrix[i]);
		}// output initialMatrix to file
		
		for (int i = 0 ; i<stateTransitionMatrix.length; i++) {
			for (int j = 0; j<stateTransitionMatrix[i].length; j++) {
				output.println(stateTransitionMatrix[i][j]);
			}
		}// output stateTransitionMatrix to file
		
		for (int i = 0 ; i<observaMatrix.length; i++) {
			for (int j = 0; j<observaMatrix[i].length; j++) {
				output.println(observaMatrix[i][j]);
			}
		}//output observaMatrix to file
		
		System.out.println("Save Data Successful.\n");
		output.close();
	}
	
	public void loadData() throws Exception{
		// read data (initialMatrix,stateTransitionmatrix,observaMatrix) 
		// from file (data/matrixData.txt).
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
		
		System.out.println("Load Data Successful.\n");
		//printObservaMatrix();
		printVector(initialMatrix, "in loadData fun,print the initialMatrix");
		printMatrix(stateTransitionMatrix, "in loadData fun,print the stateTransitionMatrix");
		
		input.close();
	}
	
	public String segmentation(String str) {
		// using HMM model to segment the input String;
		// viterbi matrix is count the calculate number of every step
		// viterCount matrix is count the step how i's is come from i-1's
		// answerLabel just with the "B/E/M/S";
		// we output answer.split(" ");
		if (str.length() == 0) return null;
		System.out.println("testStr = "+str);
		double [][] viterbi = new double[4][str.length()];
		int [][] viterbiCount = new int [4][str.length()];
		char [] answerLabel = new char[str.length()]; 
		String labels= "BEMS";
		String answer = "";
		viterbi[0][0] = initialMatrix[0];
		viterbi[1][0] = initialMatrix[1];
		viterbi[2][0] = initialMatrix[2];
		viterbi[3][0] = initialMatrix[3];
		vectorDotVector(viterbi,0,(int)str.charAt(0));// the first step
		//getMaxAndRecord();
		
		for (int i = 1 ; i<str.length(); i++) { // the most important step!
			vectorDotMatrix(viterbi,i-1,viterbiCount); //生成观测
			//return to viterbi[i] and need getmaxAndRecord()
			vectorDotVector(viterbi,i,(int)str.charAt(i));//状态转移
			//return to viterbi[i]
		}
		
		int maxIndex = findMax(viterbi, str.length()-1);
		answerLabel[str.length()-1] = labels.charAt(maxIndex);
		for (int i = str.length()-2; i>=0; i--) { 
			// find the way(BEMS) from first to end
			maxIndex = viterbiCount[maxIndex][i+1];
			answerLabel[i] = labels.charAt(maxIndex);
		}
		
		System.out.print("answerLabel= ");
		for (int i = 0 ; i<answerLabel.length; i++) 
			System.out.print(answerLabel[i]);
		print("\n");
		
		for (int i = 0,j = 0 ; i<str.length(); i++) { 
			// this is decode,use BEMS to split String(add a whitespace to string)
			if (answerLabel[i] == 'E' || answerLabel[i] == 'S'){
				answer += (str.substring(j, i+1)+" ");
				j = i+1;
			}
			else if (i == str.length()-1) {
				answer += (str.substring(j, i+1)+" ");
			}
		}
		
		printMatrix(viterbi, "in segmentation fun,print viterbi[][]");
		System.out.println(answer);
		return answer.trim();	
	}
	
	
	public void vectorDotMatrix(double[][] viterbi,int index,int [][] viterbiCount) {
		// use a vector dot stateTransitionMatrix(not times),then we get
		// a answer matrix. for this answer matrix,we find the max number
		// for every column(get the index),put the max number of every column
		// in the viterbi[][index+1] matrix,then use viterbiCount matrix to 
		// note the maxIndex,
		int viterbiIndex = index + 1; // target index
		double [][] answer = new double [countOfState][countOfState];
		for (int i = 0 ; i<countOfState; i++) {// for column
			for (int j = 0 ; j<countOfState; j++) { // for row
				answer[j][i] = viterbi[j][index]*stateTransitionMatrix[j][i];
			}
		}// calculate the answer matrix
		
		for (int i = 0 ; i<countOfState; i++) {
			int indexMax = findMax(answer, i); // find the max at column i;
			viterbi[i][viterbiIndex] = answer[indexMax][i];
			viterbiCount[i][viterbiIndex] = indexMax;
		}   // use viterbiCount matrix to note the index
		    // put the max number in the every column of 
		    // answer into the viterbi[][index+1]
	}
	
	public int findMax (double[][] answer, int index) {
		// find the max number at a countOfState*1 vector then return the index.
		// we initial the max = 3,because the state is BEMS,S is 3'rd and if 
		// there is problem with segmentation,it will output S,and this is not too bed.
		// just one is incorrect.
		int max = 3;
		for (int i = 0 ; i<countOfState; i++) {
			if(answer[i][index] >answer[max][index])
				max = i;
		}
		return max;
	}
	
	public void vectorDotVector(double[][] viterbi,int index1,int index2) {
		// a vector(countOfState*1) dot vector(also countOfState*1),
		// and the answer is also a countOfState*1 vector(not times);
		// Just create observe
		for (int i = 0 ; i<viterbi.length; i++) {
			viterbi[i][index1] *= observaMatrix[i][index2];
		}
	}
	
	public void printMatrix(double [][] matrix, String str) {
		// print a double [][] Matrix and Information(String type)
	    print(str);
		for (int i = 0 ; i<matrix.length; i++) {
			for (int j = 0 ; j<matrix[i].length; j++) {
				System.out.printf("%15f",matrix[i][j]);
			}   System.out.println("");
		}   print("");
	}
	
	public void print(String str) { 
		// just a short type print
		System.out.println(str);
	}
	
    public static void main(String[] args) throws Exception{
		// Main method: just test the HMM class.
    	
        HMM myHMM = new HMM();
        myHMM.txtPretreatment();
        myHMM.training(); 
        
        //myHMM.saveData();
        //myHMM.loadData();
        //System.out.println("go~~~~");
        String str = "西安大学生是年轻人"; // test String
        System.out.println(str);
        String ans = myHMM.segmentation(str);
        // print answer
        System.out.println(ans);
        System.out.println("SystemTime = "+System.currentTimeMillis());
	}

}
