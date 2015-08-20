package edu.usc.cssl.tacit.common.ui.corpusmanagement;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

public class MasterDetailsPage extends MasterDetailsBlock {

	class MasterContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {

			return new Object[] { new TypeOne(), new TypeTwo() };
		}

		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub

		}
	}

	class MasterLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return obj.toString();
		}

		public Image getColumnImage(Object obj, int index) {

			return null;
		}
	}

	@Override
	protected void createMasterPart(final IManagedForm managedForm,
			Composite parent) {
		FormToolkit toolkit = managedForm.getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION
				| Section.TITLE_BAR);
		section.setText("Directories"); //$NON-NLS-1$
		section.setDescription("Enter the root directory for corpus data"); //$NON-NLS-1$
		section.marginWidth = 10;
		section.marginHeight = 5;
		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		client.setLayout(layout);
		Table t = toolkit.createTable(client, SWT.NULL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 400;
		gd.widthHint = 100;
		t.setLayoutData(gd);
		toolkit.paintBordersFor(client);
		Button addDir = toolkit.createButton(client, "Add Directory", SWT.PUSH);
		//Button removeDir = toolkit.createButton(client, "Remove", SWT.PUSH);
		
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		addDir.setLayoutData(gd);
		//removeDir.setLayoutData(gd);
		section.setClient(client);
		final SectionPart spart = new SectionPart(section);
		managedForm.addPart(spart);
		TableViewer viewer = new TableViewer(t);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				managedForm.fireSelectionChanged(spart, event.getSelection());
			}
		});
		viewer.setContentProvider(new MasterContentProvider());
		viewer.setLabelProvider(new MasterLabelProvider());
		viewer.setInput(new Object[] { new TypeOne(), new TypeTwo() });

	}

	@Override
	protected void registerPages(DetailsPart detailsPart) {

		detailsPart.registerPage(TypeOne.class, new TypeOneDetailsPage());
		detailsPart.registerPage(TypeTwo.class, new TypeTwoDetailsPage());

	}

	@Override
	protected void createToolBarActions(IManagedForm managedForm) {
		final ScrolledForm form = managedForm.getForm();
		Action haction = new Action("hor", Action.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				sashForm.setOrientation(SWT.HORIZONTAL);
				form.reflow(true);
			}
		};
		haction.setChecked(true);

		Action vaction = new Action("ver", Action.AS_RADIO_BUTTON) { //$NON-NLS-1$
			public void run() {
				sashForm.setOrientation(SWT.VERTICAL);
				form.reflow(true);
			}
		};
		vaction.setChecked(false);

		form.getToolBarManager().add(haction);
		form.getToolBarManager().add(vaction);

	}

}
