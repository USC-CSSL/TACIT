package edu.usc.cssl.tacit.topicmodel.onlinelda.services;

import java.util.Arrays;

public class Matrix {

	final double[][] values;

	public Matrix(double[][] input){
		this.values = input;
	}

	public Matrix(double[] input) {
		this.values = new double[1][];
		this.values[0] = input;
	}
	 
	//Returns the number of Rows
	public int numOfRows(){
		return values.length;
	}
	
	//Returns the number of Columns	
	public int numOfCols(){
		return values[0].length;
	}

	//Set the value of the specific row to that of the specified linear array
	public void setRow(int r, LinearArray a) {
		this.values[r] = a.values;
	}
	
	//Retrieve a row from the Matrix
	public LinearArray getRow(int r){
		return new LinearArray(values[r]);
	}

	//Retrieve a column from the Matrix
	public LinearArray getCol(int c){
		double[] result = new double[values.length];
		for(int i=0; i<values.length; i++)
			result[i] = values[i][c];
		return new LinearArray(result);
	}

	//Retrieve multiple columns from the Matrix
	public Matrix getCols(int[] c){
		double[][] result = new double[values.length][];
		for(int i=0; i<values.length; i++){
			double[] row = new double[c.length];
			for(int j=0; j<c.length; j++){
				row[j] = values[i][c[j]];	
			}
			result[i] = row;
		}
		return new Matrix(result);
	}

	public void incCols(int[] cols, Matrix m) {
		for(int i=0; i<values.length; i++) {
			for (int j=0; j<cols.length; j++) {
				values[i][cols[j]] += m.values[i][j];
			}
		}
	}

	public double sum(){
		double s = 0;
		for (int i=0; i<values.length; i++) {
			for (int j=0; j<values[0].length; j++) {
				s += values[i][j];
			}
		}
		return s;
	}
	
	//Sum over each row of the Matrix
	public LinearArray sumByRows() {
        double[] result = new double[values.length];
        for (int i=0; i<values.length; i++) {
            double rowSum = 0d;
            for (double d : values[i]) {
                rowSum += d;
            }
            result[i] = rowSum;
        }
        return new LinearArray(result);
    }
	
	//Add a value to every element of the matrix
	public Matrix add(double value) {
		double[][] result = new double[values.length][];
		for (int i=0; i<values.length; i++) {
			result[i] = new double[values[0].length];
			for (int j=0; j<values[0].length; j++) {
				result[i][j] = values[i][j] + value;
			}
		}
		return new Matrix(result);
	}

	//Add two matrices
	public Matrix add(Matrix second){
		double[][] result = new double[values.length][];
		for (int i=0; i<values.length; i++) {
			result[i] = new double[values[0].length];
			for (int j=0; j<values[0].length; j++) {
				result[i][j] = values[i][j] + second.values[i][j];
			}
		}
		return new Matrix(result);
	}
	
	//Sub a value to every element of the matrix
	public Matrix sub(double value) {
		double[][] result = new double[values.length][];
		for (int i=0; i<values.length; i++) {
			result[i] = new double[values[0].length];
			for (int j=0; j<values[0].length; j++) {
				result[i][j] = values[i][j] - value;
			}
		}
		return new Matrix(result);
	}
		
	public Matrix sub(Matrix second){
		double[][] result = new double[values.length][];
		for (int i=0; i<values.length; i++) {
			result[i] = new double[values[0].length];
			for (int j=0; j<values[0].length; j++) {
				result[i][j] = values[i][j] - second.values[i][j];
			}
		}
		return new Matrix(result);
	}

	//Multiply a value to every element of the matrix
	public Matrix product(double value) {
		double[][] result = new double[values.length][];
		for (int i=0; i<values.length; i++) {
			result[i] = new double[values[0].length];
			for (int j=0; j<values[0].length; j++) {
				result[i][j] = values[i][j] * value;
			}
		}
		return new Matrix(result);
	}

	//Multiply two matrices
	public Matrix product(Matrix second) {        
		double[][] m1 = this.values;
		double[][] m2 = second.values;

		double[][] result = new double[m1.length][];

		for (int i= 0; i<m1.length; i++) {
			result[i] = new double[m2[0].length];
			for (int j=0; j<m2[0].length; j++) {
				result[i][j] = m1[i][j] * m2[i][j];
			}
		}
		return new Matrix(result);
	}

	//Dot Product
//	public double[][] dot(double[][] m1, double[][] m2) {
//		double[][] result = new double[m1.length][];
//		for (int i=0; i<m1.length; i++) {
//			result[i] = new double[m2[0].length];
//			for (int j=0; j<m2[0].length; j++) {
//				result[i][j] = 0;
//				for (int k=0; k<m2.length; k++) {
//					result[i][j] += m1[i][k] * m2[k][j];
//				}
//			}
//		}
//		return result;
//	}

	//Create a matrix with the same number of rows and columns filled with 0s
	public Matrix zeros() {
		int rows = values.length;
		int cols = values[0].length;
		double[][] zeroMatrix = new double[rows][];
		for (int i=0; i<rows; i++) {
			zeroMatrix[i] = new double[cols];
			Arrays.fill(zeroMatrix[i], 0.0d);
		}
		return new Matrix(zeroMatrix);
	}

	//Transpose of a matrix
	public Matrix transpose() {
		int rows = values.length;
		int cols = values[0].length;
		double[][] result = new double[cols][];
		for (int i=0; i<cols; i++) {
			result[i] = new double[rows];
			for (int j=0; j<rows; j++) {
				result[i][j] = values[j][i];
			}
		}
		return new Matrix(result);
	}

	//Print
	public void display(){
		for (int i=0; i<values.length; i++) {
			for (int j=0; j<values[0].length; j++)
				System.out.print(values[i][j]+ " ");
			System.out.println();
		}
	}
}
