package edu.usc.cssl.tacit.common.ui.views;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import edu.usc.cssl.tacit.common.ui.utility.INlpCommonUiConstants;
import edu.usc.cssl.tacit.common.ui.utility.IconRegistry;

public class ConsoleView extends ViewPart implements INlpCommonUiConstants {
	public static final String ID = "usc.edu.cssl.common.ui.views.console";

	private static Text text;

	private static Label header;

	public void createPartControl(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		Composite client = toolkit.createComposite(parent);
		GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(1)
				.applyTo(client);
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(client);

		header = toolkit.createLabel(client, "Nothing to display");
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
				.applyTo(header);
		text = toolkit.createText(client, "", SWT.READ_ONLY | SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).span(1, 1)
				.applyTo(text);
		this.setPartName("Console");
		this.setTitleToolTip(this.getPartName());
		this.setTitleImage(IconRegistry.getImageIconFactory().getImage(
				IMAGE_CONSOLE_VIEW));
		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
		configureToolBar(tbm);

	}

	public static void writeInConsoleHeader(final String head) {
		// update status bar
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (header != null) {
					header.setText(head);
				}
			}
		});
	}

	public static void printlInConsoleln() {
		// update status bar
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (text != null) {
					text.append("\n");
				}
			}
		});
	}

	public static void printlInConsoleln(final Object log) {
		// update status bar
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (text != null) {
					text.append(String.valueOf(log));
					text.append("\n");
				}
			}
		});
	}

	public static void printlInConsole(final Object log) {
		// update status bar
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (text != null) {
					text.append(String.valueOf(log));
				}
			}
		});
	}

	private void configureToolBar(IToolBarManager mgr) {
		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (IconRegistry.getImageIconFactory()
						.getImageDescriptor(IMAGE_SAVE_CO));
			}

			@Override
			public String getToolTipText() {
				return "Save Console";
			}

			public void run() {
				SafeSaveDialog openDialog = new SafeSaveDialog(Display
						.getCurrent().getActiveShell());
				String fileName = openDialog.open();
				PrintWriter writer = null;
				try {
					writer = new PrintWriter(fileName, "UTF-8");
					writer.println(text.getText());

				} catch (FileNotFoundException e) {
					printlInConsoleln("Not able to save log into the file because of encoding. Please create issues in GitHub");
					return;
                 					 
				} catch (UnsupportedEncodingException e) {

					printlInConsoleln("Not able to save log into the file because of encoding. Please create issues in GitHub");
					return;
				} finally {
					if (writer != null){
						writer.close();
					}
				}
				printlInConsoleln("Saved Succesfully");
			};
		});

		mgr.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return (IconRegistry.getImageIconFactory()
						.getImageDescriptor(IMAGE_CEAR_CO));
			}

			@Override
			public String getToolTipText() {
				return "Clear Console";
			}

			public void run() {
				text.setText("");
				header.setText("Nothing to display");
			};
		});

	}

	public void setFocus() {
		text.setFocus();
	}
}
