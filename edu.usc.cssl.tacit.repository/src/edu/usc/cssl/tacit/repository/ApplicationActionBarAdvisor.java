package edu.usc.cssl.tacit.repository;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import edu.usc.cssl.tacit.common.ui.corpusmanagement.internal.CorpusManagementUIViewImageRegistry;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
	private IWorkbenchAction helpAction;
	private IWorkbenchAction introAction;
	private IWorkbenchAction helpSearch;
	private IWorkbenchAction dynamicHelpAction;
	private IWorkbenchAction preferenceAction;
	private IWorkbenchAction exitAction;
	private IWorkbenchAction aboutAction;

	// Actions - important to allocate these only in makeActions, and then use
	// them
	// in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	protected void makeActions(IWorkbenchWindow window) {

		dynamicHelpAction = ActionFactory.DYNAMIC_HELP.create(window);
		register(dynamicHelpAction);

		helpAction = ActionFactory.HELP_CONTENTS.create(window);
		register(helpAction);

		helpSearch = ActionFactory.HELP_SEARCH.create(window);
		register(helpAction);

		introAction = ActionFactory.INTRO.create(window);
		register(introAction);

		preferenceAction = ActionFactory.PREFERENCES.create(window);
		preferenceAction.setImageDescriptor(ImageDescriptor.createFromFile(CorpusManagementUIViewImageRegistry.class ,
				"/icons/PreferencesIcon.png"));
		register(preferenceAction);

		aboutAction = ActionFactory.ABOUT.create(window);
		register(aboutAction);

		exitAction = ActionFactory.QUIT.create(window);
		exitAction.setImageDescriptor(ImageDescriptor.createFromFile(CorpusManagementUIViewImageRegistry.class ,
				"/icons/ExitIcon.png"));
	
		register(exitAction);

	}

	protected void fillMenuBar(IMenuManager menuBar) {

		MenuManager helpMenu = new MenuManager("&Help",
				IWorkbenchActionConstants.M_HELP);
		MenuManager viewMenu = new MenuManager("&View", "view");

		MenuManager fileMenu = new MenuManager("&File",
				IWorkbenchActionConstants.M_FILE);
		menuBar.add(fileMenu);
		// Add a group marker indicating where action set menus will appear.
		menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menuBar.add(viewMenu);
		menuBar.add(helpMenu);

		// File

		Action restartAction = new Action("Restart") {

			@Override
			public void run() {
				PlatformUI.getWorkbench().restart();
			}
		};
		restartAction.setImageDescriptor(ImageDescriptor.createFromFile(CorpusManagementUIViewImageRegistry.class ,
			"/icons/RestartIcon.png"));
			
		fileMenu.add(restartAction);
		fileMenu.add(exitAction);

		// Help
		helpMenu.add(introAction);
		helpMenu.add(new Separator());
		helpMenu.add(helpAction);
		helpMenu.add(helpSearch);
		helpMenu.add(dynamicHelpAction);
		helpMenu.add(new Separator());
		helpMenu.add(new Action("Feedback") {
		});
		helpMenu.add(new Separator());
		helpMenu.add(aboutAction);

		viewMenu.add(preferenceAction);
	}

}
