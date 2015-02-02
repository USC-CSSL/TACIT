package edu.usc.cssl.nlputils.utilities;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.widgets.Display;

public class Log {
	public static void append(IEclipseContext context, String message){
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (context == null)
					return;
				IEclipseContext parent = context.getParent();
				String currentMessage = (String) parent.get("consoleMessage"); 
				if (currentMessage==null)
					parent.set("consoleMessage", message);
				else {
					if (currentMessage.equals(message)) {
						// Set the param to null before writing the message if it is the same as the previous message. 
						// Else, the change handler will not be called.
						parent.set("consoleMessage", null);
						parent.set("consoleMessage", message);
					}
					else
						parent.set("consoleMessage", message);
				}
			}
			});
	}
}
