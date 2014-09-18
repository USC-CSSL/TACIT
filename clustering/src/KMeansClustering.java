import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;

import org.eclipse.swt.widgets.Text;


public class KMeansClustering{
	
	public void doClustering(Text inputDir, Text clusterSize)
	{
		try{
			File inputFiles = new File(inputDir.toString());
			int numOfClusters = Integer.parseInt(clusterSize.toString());
			SimpleKMeans kmeans = new SimpleKMeans();
			
		
			kmeans.setNumClusters(numOfClusters);
		}catch(Exception e)
		{
			System.out.println("Exception occurred in K means" + e);
		}
	}
}