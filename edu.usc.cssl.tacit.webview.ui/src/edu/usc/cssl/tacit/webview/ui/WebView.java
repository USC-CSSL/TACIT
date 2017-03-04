package edu.usc.cssl.tacit.webview.ui;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

import edu.usc.cssl.tacit.webview.ui.internal.IWebViewConstants;
import edu.usc.cssl.tacit.webview.ui.internal.WebViewImageRegistry;


public class WebView extends ViewPart {
	public static String ID = "edu.usc.cssl.tacit.webview.ui.view";
	
	private Browser browser;

	private Action refreshAction = new Action("Refresh") {
		public void run() {
			browser.refresh();
		}
	};
	
	
    /**
     * Finds the first web view in the given window.
     * 
     * @param window the window
     * @return the first found web view, or <code>null</code> if none found
     */
    private static WebView findBrowser(IWorkbenchWindow window) {
        IWorkbenchPage page = window.getActivePage();
        IViewPart view = page.findView(IWebViewConstants.BROWSER_VIEW_ID);
        if (view != null) {
            return (WebView) view;
        }
        IViewReference[] refs = page.getViewReferences();
        for (int i = 0; i < refs.length; i++) {
            if (IWebViewConstants.BROWSER_VIEW_ID.equals(refs[i].getId())) {
                return (WebView) refs[i].getPart(true);
            }
        }
        return null;
    }
    
	
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site);
    }

    
	public void createPartControl(Composite parent) {
		browser = createBrowser(parent, getViewSite().getActionBars());
		File file = null;
		
		Bundle bundle = Platform.getBundle("edu.usc.cssl.tacit.webview.ui");
		URL fileURL = bundle.getEntry("webpage.html");
		try {
			file = new File(FileLocator.resolve(fileURL).toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		browser.setUrl(file.getAbsolutePath());
		
	}

	public void setFocus() {
		if (browser != null && !browser.isDisposed()) {
			browser.setFocus();
		}
	}
	
	private Browser createBrowser(Composite parent, final IActionBars actionBars) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		parent.setLayout(gridLayout);
	
	
		GridData data = new GridData();
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;


		browser = new Browser(parent, SWT.NONE);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.horizontalSpan = 2;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		browser.setLayoutData(data);



        browser.addOpenWindowListener(new OpenWindowListener() {
            public void open(WindowEvent event) {
                WebView.this.openWindow(event);
            }
        });
        
        browser.addCloseWindowListener(new CloseWindowListener() {
            public void close(WindowEvent event) {
            	WebView.this.close();
            }
        });
	
		actionBars.setGlobalActionHandler("refresh", refreshAction); //$NON-NLS-1$

		return browser;
	}

    /**
     * Opens a new browser window.
     * 
     * @param event the open window event
     */
    private void openWindow(WindowEvent event) {
        try {
            IWorkbench workbench = getSite().getWorkbenchWindow().getWorkbench();
            IWorkbenchWindow window = workbench.openWorkbenchWindow(IWebViewConstants.BROWSER_PERSPECTIVE_ID, null);
            
            Shell shell = window.getShell();
            if (event.location != null)
                shell.setLocation(event.location);
            if (event.size != null)
                shell.setLocation(event.size);
                
            WebView view = findBrowser(window);
            Assert.isNotNull(view);
            event.browser = view.browser;
        } catch (WorkbenchException e) {
            Activator.getDefault().log(e);
        }
    }
    
    /**
     * Closes this browser view.  Closes the window too if there
     * are no non-secondary parts open.
     */
    private void close() {
        IWorkbenchPage page = getSite().getPage();
        IWorkbenchWindow window = page.getWorkbenchWindow();
        page.hideView(this);
        if (Activator.getNonSecondaryParts(page).size() == 0) {
            page.closePerspective(page.getPerspective(), true, true);
        }
        if (window.getActivePage() == null) {
            window.close();
        }
    }

}
