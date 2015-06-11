package edu.uc.cssl.nlputils.crawlers.supremecourt.services;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class MyThread implements IRunnableWithProgress {
	 private static final int TOTAL_TIME = 10000;

	  private static final int INCREMENT = 500;

	  private boolean indeterminate;

	  public MyThread(boolean indeterminate) {
	    this.indeterminate = indeterminate;
	  }

	  public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
	    monitor.beginTask("Running long running operation", indeterminate ? IProgressMonitor.UNKNOWN
	        : TOTAL_TIME);
	    for (int total = 0; total < TOTAL_TIME && !monitor.isCanceled(); total += INCREMENT) {
	      Thread.sleep(INCREMENT);
	      monitor.worked(INCREMENT);
	      if (total == TOTAL_TIME / 2)
	        monitor.subTask("Doing second half");
	    }
	    monitor.done();
	    if (monitor.isCanceled())
	      throw new InterruptedException("The long running operation was cancelled");
	  }
}
