package edu.usc.cssl.tacit.common.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import edu.usc.cssl.tacit.common.queryprocess.QueryDataType;
import edu.usc.cssl.tacit.common.queryprocess.QueryOperatorType;

public class TacitCorpusFilterDialog extends Dialog {

	private Combo jsonFieldCombo;

	private Combo operationCombo;
	private Button addFilterButton;

	private Text valueText;
	private List<String> selectedFilters;
	private Table filterTable;
	private TableViewer tableViewer;
	FormToolkit toolkit;

	private Button removeFilterButton;

	private Map<String, QueryDataType> jsonKeys;

	public TacitCorpusFilterDialog(Shell parent) {
		super(parent);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		toolkit = new FormToolkit(container.getDisplay());
		createWidgets(container);
		return container;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Corpus Filter");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 600);
	}

	private Composite addSection(Composite parent) {

		Section section = toolkit.createSection(parent, Section.TITLE_BAR
				| Section.EXPANDED);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(section);
		section.setExpanded(true);

		ScrolledComposite scComposite = new ScrolledComposite(section,
				SWT.H_SCROLL | SWT.V_SCROLL);
		scComposite.setExpandHorizontal(true);
		scComposite.setExpandVertical(true);

		Composite sectionClient = toolkit.createComposite(section);
		sectionClient.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 8;
		sectionClient.setLayout(layout);
		scComposite.setContent(sectionClient);
		section.setClient(sectionClient);

		return sectionClient;
	}

	private void updateFilterTable(String value) {
		if (selectedFilters == null) {
			selectedFilters = new ArrayList<String>();
		}

		selectedFilters.add((String) value);

		Collections.sort(selectedFilters);
		filterTable.removeAll();
		for (String itemName : selectedFilters) {

			TableItem item = new TableItem(filterTable, 0);
			item.setText(itemName);
		}

	}

	private void createWidgets(Composite parent) {

		Composite additionSectionClient = addSection(parent);

		Label jsonFieldLabel = toolkit.createLabel(additionSectionClient,
				"Field", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(0, 0)
				.applyTo(jsonFieldLabel);
		jsonFieldCombo = new Combo(additionSectionClient, SWT.READ_ONLY);
		jsonFieldCombo.setBounds(100, 100, 150, 100);
		GridDataFactory.fillDefaults().grab(false, false).span(2, 0)
				.applyTo(jsonFieldCombo);

		Label operationLabel = toolkit.createLabel(additionSectionClient,
				"Operation", SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(operationLabel);
		operationCombo = new Combo(additionSectionClient, SWT.READ_ONLY);
		operationCombo.setBounds(50, 50, 150, 65);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(operationCombo);

		Label valueLabel = toolkit.createLabel(additionSectionClient, "Value",
				SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(valueLabel);
		valueText = toolkit.createText(additionSectionClient, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(operationCombo);

		addFilterButton = toolkit.createButton(additionSectionClient, "ADD",
				SWT.BUTTON1);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 0)
				.applyTo(addFilterButton);

		addFilterButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				String field = jsonFieldCombo.getText();
				String operation = operationCombo.getText();
				String val = valueText.getText();
				updateFilterTable(field + " " + operation + " " + val);

			}
		});

		Composite reviewSectionClient = addSection(parent);

		filterTable = toolkit.createTable(reviewSectionClient, SWT.BORDER
				| SWT.MULTI);
		GridDataFactory.fillDefaults().grab(true, true).span(6, 3)
				.hint(150, 300).applyTo(filterTable);
		tableViewer = new TableViewer(filterTable);
		// filterTable.setBounds(100, 100, 100, 500);

		removeFilterButton = toolkit.createButton(reviewSectionClient,
				"Remove...", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1)
				.applyTo(removeFilterButton);
		
		jsonFieldCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				String selectedKey = jsonFieldCombo.getText();
				QueryDataType dataType = jsonKeys.get(selectedKey);
				List<QueryOperatorType> operations = QueryDataType.supportedOperations(dataType);
				String [] operationList = new String[operations.size()];
				int i = 0;
				for(QueryOperatorType opType : operations){
					operationList[i++] = opType.toString();
				}
				
				operationCombo.setItems(operationList);
				

			}
		});

		removeFilterButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				for (TableItem item : filterTable.getSelection()) {

					selectedFilters.remove(item.getText());
					item.dispose();

				}

			}
		});
		setWidgetValues();
	}

	public void setFilterDetails(Map<String, QueryDataType> keys) {
		this.jsonKeys = keys;
	}

	private void setWidgetValues() {
		String items[] = { "Item One", "Item Two", "Item Three", "Item Four",
				"Item Five" };
		String[] jsonItems = new String[jsonKeys.keySet().size()];
		int i = 0;
		for(String keys : jsonKeys.keySet()){
			jsonItems[i++] = keys;
		}
		
		jsonFieldCombo.setItems(jsonItems);
	//	operationCombo.setItems(items);

	}
	/*
	 * IQuerryProcessor qp = new QueryProcessor(CorpusClass cls);
	 * qp.getJsonKeys() // cache & retrieves once
	 * 
	 * 
	 * //use same instance of corpus to add filter Filter f1 = new
	 * Filter("comments.score", QueryDataType
	 * 
	 * .supportedOperations(QueryDataType.DOUBLE).get(0), "100",
	 * 
	 * QueryDataType.DOUBLE);
	 * 
	 * cls.addFilter(f1) or cls.addFilterAll(List<Filters> filters);
	 */

}