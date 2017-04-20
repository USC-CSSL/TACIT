package edu.usc.cssl.tacit.classify.svm.services;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_print_interface;

import org.apache.commons.math3.stat.inference.AlternativeHypothesis;
import org.apache.commons.math3.stat.inference.BinomialTest;

public class SVMPredict {

	private static svm_print_interface svm_print_null = new svm_print_interface()
	{
		public void print(String s) {}
	};

	private static svm_print_interface svm_print_stdout = new svm_print_interface()
	{
		public void print(String s)
		{
			System.out.print(s);
		}
	};

	private static svm_print_interface svm_print_string = svm_print_stdout;

	static void info(String s) 
	{
		svm_print_string.print(s);
	}

	private static double atof(String s)
	{
		return Double.valueOf(s).doubleValue();
	}

	private static int atoi(String s)
	{
		return Integer.parseInt(s);
	}

	private static double[] predict(BufferedReader input, DataOutputStream output, svm_model model, int predict_probability) throws IOException
	{
		int correct = 0;
		int total = 0;
		double error = 0, pvalue = 0, absolute_error = 0;
		double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
		double tp = 0, fn = 0, fp =0, tn = 0;
		//class 1 is considered as + and class 2 is considered -
		//			Predicted Label
		//True		|	+	|	-
		//label	+	|	tp	|	fn
		//		-	|	fp	|	tn

		List<Double> targetCollection = new ArrayList();
		
		int svm_type=svm.svm_get_svm_type(model);
		int nr_class=svm.svm_get_nr_class(model);
		double[] prob_estimates=null;

		if(predict_probability == 1)
		{
			if(svm_type == svm_parameter.EPSILON_SVR ||
			   svm_type == svm_parameter.NU_SVR)
			{
				SVMPredict.info("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="+svm.svm_get_svr_probability(model)+"\n");
			}
			else
			{
				int[] labels=new int[nr_class];
				svm.svm_get_labels(model,labels);
				prob_estimates = new double[nr_class];
				output.writeBytes("labels");
				for(int j=0;j<nr_class;j++)
					output.writeBytes(" "+labels[j]);
				output.writeBytes("\n");
			}
		}
		while(true)
		{
			String line = input.readLine();
			if(line == null) break;

			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

			double target = atof(st.nextToken());
			int m = st.countTokens()/2;
			svm_node[] x = new svm_node[m];
			for(int j=0;j<m;j++)
			{
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}

			double v;
			if (predict_probability==1 && (svm_type==svm_parameter.C_SVC || svm_type==svm_parameter.NU_SVC))
			{
				v = svm.svm_predict_probability(model,x,prob_estimates);
				output.writeBytes(v+" ");
				for(int j=0;j<nr_class;j++)
					output.writeBytes(prob_estimates[j]+" ");
				output.writeBytes("\n");
			}
			else
			{
				v = svm.svm_predict(model,x);
				output.writeBytes(v+"\n");
			}
			
			if (v == 1){
				if (target == v){
					tp++;
				}else{
					fp++;
				}
			}else{
				if(target == v){
					tn++;
				}else{
					fn++;
				}
				
			}
			targetCollection.add(target);
			if(v == target)
				++correct;
			error += (v-target)*(v-target);
			absolute_error  += Math.abs(v-target);
			sumv += v;
			sumy += target;
			sumvv += v*v;
			sumyy += target*target;
			sumvy += v*target;
			++total;
		}
		if(svm_type == svm_parameter.EPSILON_SVR ||
		   svm_type == svm_parameter.NU_SVR)
		{
			SVMPredict.info("Mean squared error = "+error/total+" (regression)\n");
			SVMPredict.info("Squared correlation coefficient = "+
				 ((total*sumvy-sumv*sumy)*(total*sumvy-sumv*sumy))/
				 ((total*sumvv-sumv*sumv)*(total*sumyy-sumy*sumy))+
				 " (regression)\n");
		}
		else{
			SVMPredict.info("Accuracy = "+(double)correct/total*100+"% ("+correct+"/"+total+") (classification)\n");
			//ConsoleView.printlInConsoleln(total + " " + correct + " " + (double)1/nr_class + " ");
			BinomialTest btest = new BinomialTest();
			pvalue = btest.binomialTest(total, correct, (double)0.5, AlternativeHypothesis.TWO_SIDED);
		}
		
		//Here we are calculating the various error statistics, considering the misclassification cost as 2(since the classes are 1/-1), 
		//This shall be confirmed in case to be implemented in future.  
		double abar= sumy/total;
		double relative_absolute_error = 0.0;
		double root_relative_squared_error = 0.0;
		for (double a: targetCollection){
			relative_absolute_error  += Math.abs(abar- a);
			root_relative_squared_error += (abar - a)*(abar - a);
		}
		relative_absolute_error = absolute_error/relative_absolute_error;
		root_relative_squared_error = error/root_relative_squared_error;
		
		double mean_absolute_error = absolute_error/total;
		double rms_error = Math.sqrt(error/total);
		return new double[]{correct,total, pvalue,mean_absolute_error,rms_error,tp,fn,fp,tn,relative_absolute_error,root_relative_squared_error};
	}

	private static void exit_with_help()
	{
		System.err.print("usage: svm_predict [options] test_file model_file output_file\n"
		+"options:\n"
		+"-b probability_estimates: whether to predict probability estimates, 0 or 1 (default 0); one-class SVM not supported yet\n"
		+"-q : quiet mode (no outputs)\n");
		System.exit(1);
	}

	public static double[] main(String argv[]) throws IOException
	{
		int i, predict_probability=0;
        	svm_print_string = svm_print_stdout;
        double[] result = new double[3];
		// parse options
		for(i=0;i<argv.length;i++)
		{
			if(argv[i].charAt(0) != '-') break;
			++i;
			switch(argv[i-1].charAt(1))
			{
				case 'b':
					predict_probability = atoi(argv[i]);
					break;
				case 'q':
					svm_print_string = svm_print_null;
					i--;
					break;
				default:
					System.err.print("Unknown option: " + argv[i-1] + "\n");
					exit_with_help();
			}
		}
		if(i>=argv.length-2)
			exit_with_help();
		try 
		{
			BufferedReader input = new BufferedReader(new FileReader(argv[i]));
			DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(argv[i+2])));
			svm_model model = svm.svm_load_model(argv[i+1]);
			if (model == null)
			{
				System.err.print("can't open model file "+argv[i+1]+"\n");
				System.exit(1);
			}
			if(predict_probability == 1)
			{
				if(svm.svm_check_probability_model(model)==0)
				{
					System.err.print("Model does not support probabiliy estimates\n");
					System.exit(1);
				}
			}
			else
			{
				if(svm.svm_check_probability_model(model)!=0)
				{
					SVMPredict.info("Model supports probability estimates, but disabled in prediction.\n");
				}
			}
			result = predict(input,output,model,predict_probability);
			input.close();
			output.close();
		} 
		catch(FileNotFoundException e) 
		{
			exit_with_help();
		}
		catch(ArrayIndexOutOfBoundsException e) 
		{
			exit_with_help();
		}
		return result;
	}
}
