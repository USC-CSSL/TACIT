package edu.usc.cssl.tacit.topicmodel.onlinelda.services;

import java.util.*;

public class LinearArray {

	double[] values;

	public LinearArray(int len) {
		values = new double[len];
		Arrays.fill(values, 0d);
	}
	
	public LinearArray(double[] input) {
		this.values = input;
	}

	public LinearArray(int[] input) {
		values = new double[input.length];
		for (int i = 0; i < input.length; i++){
			values[i] = (double) input[i];
		}
	}

	//Number of Columns
	public int size(){
		return values.length;
	}
	
	//Set the column value to a specific value
	public void set(int col, double val){
        values[col] = val;
    }
	
	//Sum of all the values in the Linear Array
	public double sum(){
		double s = 0;
		for (int i=0; i<values.length; i++) {
			s += values[i];
		}
		return s;
	}
	
	//Add a value to each element of the Linear Array
	public LinearArray add(double value) {
		if (values.length == 0){
			return new LinearArray(new double[0]);
		}
		double[] result = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			result[i] = values[i] + value;
		}
		return new LinearArray(result);
	}

	//Add two Linear Arrays
	public LinearArray add(LinearArray second) {
		double[] result = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			result[i] = values[i] + second.values[i];
		}
		return new LinearArray(result);
	}

	//Sub a value to each element of the Linear Array
	public LinearArray sub(double value) {
		double[] result = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			result[i] = values[i] - value;
		}
		return new LinearArray(result);
	}
	
	//Sub two Linear Arrays
	public LinearArray sub(LinearArray second) {
		double[] result = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			result[i] = values[i] - second.values[i];
		}
		return new LinearArray(result);
	}

	//Multiply two Linear Arrays
	public LinearArray product(LinearArray second) {
		if (values.length == 0 || second.values.length == 0){
			return new LinearArray(new double[values.length>second.values.length?values.length:second.values.length]);
		}
		double[] result = new double[values.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = values[i] * second.values[i];
		}
		return new LinearArray(result);
	}

	//Divide each element of the Linear Array by a value
	public LinearArray div(double value) {
		if (values.length == 0){
			return new LinearArray(new double[0]);
		}
		double[] result = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			result[i] = values[i] / value;
		}
		return new LinearArray(result);
	}
	
	//Divide two Linear Arrays
	public LinearArray div(LinearArray second) {
		double[] result = new double[values.length];
		if (values.length == 0){
			return new LinearArray(result);
		}
		for (int i = 0; i < values.length; i++) {
			result[i] = values[i] / second.values[i];
		}
		return new LinearArray(result);
	}

	//Dot product between a Linear Array and Matrix
	public LinearArray dot(Matrix mat) {
		
		if (values.length == 0 || mat.numOfCols() == 0 || mat.numOfRows() == 0){
			return new LinearArray(new double[0]);
		}
		
		double[][] m = mat.values;
		double[] result = new double[m[0].length];
		Arrays.fill(result, 0d);
		for (int j = 0; j < m[0].length; j++) {
			for (int i = 0; i < values.length; i++) {
				result[j] += values[i] * m[i][j];
			}
		}
		
		double[] result2 = new double[m[0].length];
		for(int x=0; x<m[0].length;x++)
			result2[x] = result[x];
		
		return new LinearArray(result2);
	}
	
	//Outer product of two Linear Arrays
	public Matrix outer(LinearArray arr){
		double[] a = arr.values; 
		double[][] result = new double[values.length][];
		for(int i=0; i< values.length; i++){
			result[i] = new double[a.length];
			for(int j=0; j<a.length; j++){
				result[i][j] = values[i] * a[j];
			}
		}
		return new Matrix(result);
	}
	
	// Average
	public double mean(){
		double result = 0;
		for(int i=0; i<values.length; i++)
			result += Math.abs(values[i]);
		result = result / values.length;
		return result;
	}

	//Maximum value in the Linear Array
	public double max(){
		double maxValue = -Double.MAX_VALUE;
		for(double d: values){
			if(d >= maxValue){
				maxValue = d;
			}
		}
		return maxValue;
	}

	//Print
	public void display(){
		for(double x:values){
			System.out.print(x+" ");
		}
	}

}
