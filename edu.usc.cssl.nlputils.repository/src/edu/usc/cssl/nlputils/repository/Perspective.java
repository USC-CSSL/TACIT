package edu.usc.cssl.nlputils.repository;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {

		String editorArea = layout.getEditorArea();

		IFolderLayout outputfolder = layout.createFolder(
				"bottom", IPageLayout.BOTTOM, (float) 0.75, editorArea); //$NON-NLS-1$
		outputfolder.addView("org.eclipse.ui.views.ProgressView");
		outputfolder.addView("usc.edu.cssl.common.ui.views.console");
		/*
		 * outputfolder.addView(JavaUI.ID_SOURCE_VIEW);
		 * outputfolder.addPlaceholder(NewSearchUI.SEARCH_VIEW_ID);
		 * outputfolder.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
		 * outputfolder.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		 * outputfolder.addPlaceholder(IProgressConstants.PROGRESS_VIEW_ID);
		 */
		/*
		 * layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
		 * layout.addActionSet(JavaUI.ID_ACTION_SET);
		 * layout.addActionSet(JavaUI.ID_ELEMENT_CREATION_ACTION_SET);
		 * layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
		 */
		// views - java
		/*
		 * layout.addShowViewShortcut(JavaUI.ID_PACKAGES);
		 * layout.addShowViewShortcut(JavaUI.ID_TYPE_HIERARCHY);
		 * layout.addShowViewShortcut(JavaUI.ID_SOURCE_VIEW);
		 * layout.addShowViewShortcut(JavaUI.ID_JAVADOC_VIEW);
		 * 
		 * 
		 * // views - search
		 * layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);
		 * 
		 * // views - debugging
		 * layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
		 * 
		 * // views - standard workbench
		 * layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		 * layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		 * layout.addShowViewShortcut(JavaPlugin.ID_RES_NAV);
		 * layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		 * layout.addShowViewShortcut(IProgressConstants.PROGRESS_VIEW_ID);
		 * layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);
		 * layout.addShowViewShortcut(TemplatesView.ID);
		 * layout.addShowViewShortcut("org.eclipse.pde.runtime.LogView");
		 * //$NON-NLS-1$
		 */
		// new actions - Java project creation wizard
		/*
		 * layout.addNewWizardShortcut(
		 * "org.eclipse.jdt.ui.wizards.JavaProjectWizard"); //$NON-NLS-1$
		 * layout.addNewWizardShortcut(
		 * "org.eclipse.jdt.ui.wizards.NewPackageCreationWizard"); //$NON-NLS-1$
		 * layout
		 * .addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewClassCreationWizard"
		 * ); //$NON-NLS-1$ layout.addNewWizardShortcut(
		 * "org.eclipse.jdt.ui.wizards.NewInterfaceCreationWizard");
		 * //$NON-NLS-1$ layout.addNewWizardShortcut(
		 * "org.eclipse.jdt.ui.wizards.NewEnumCreationWizard"); //$NON-NLS-1$
		 * layout.addNewWizardShortcut(
		 * "org.eclipse.jdt.ui.wizards.NewAnnotationCreationWizard");
		 * //$NON-NLS-1$ layout.addNewWizardShortcut(
		 * "org.eclipse.jdt.ui.wizards.NewSourceFolderCreationWizard");
		 * //$NON-NLS-1$ layout.addNewWizardShortcut(
		 * "org.eclipse.jdt.ui.wizards.NewSnippetFileCreationWizard");
		 * //$NON-NLS-1$ layout.addNewWizardShortcut(
		 * "org.eclipse.jdt.ui.wizards.NewJavaWorkingSetWizard"); //$NON-NLS-1$
		 * layout
		 * .addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON
		 * -NLS-1$
		 * layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file")
		 * ;//$NON-NLS-1$ layout.addNewWizardShortcut(
		 * "org.eclipse.ui.editors.wizards.UntitledTextFileWizard"
		 * );//$NON-NLS-1$
		 */// 'Window' > 'Open Perspective' contributions
		layout.setEditorAreaVisible(true);
		layout.addPerspectiveShortcut("edu.usc.cssl.nlputils.repository.perspective");
	}

}
