/*******************************************************************************
 * Copyright (c) 2017 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package net.openchrom.xxd.processor.supplier.tracecompare.ui.swt;

import org.eclipse.chemclipse.rcp.ui.icons.core.ApplicationImageFactory;
import org.eclipse.chemclipse.rcp.ui.icons.core.IApplicationImage;
import org.eclipse.chemclipse.swt.ui.support.Colors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.swtchart.ITitle;

public class TraceDataComparisonUI extends Composite {

	private static final String FLAG_MATCHED = "FlagMatched";
	//
	private Label labelTrace;
	private Button buttonIsMatched;
	private TraceDataUI sampleDataUI;
	private TraceDataUI referenceDataUI;
	private Text commentsText;

	public TraceDataComparisonUI(Composite parent, int style) {
		super(parent, style);
		createControl();
	}

	public void setTrace(String trace, String sample, String reference) {

		labelTrace.setText("Selected trace: " + trace);
		//
		ITitle title;
		//
		title = sampleDataUI.getBaseChart().getTitle();
		title.setText(sample + " (" + trace + ")");
		title.setForeground(Colors.BLACK);
		//
		title = referenceDataUI.getBaseChart().getTitle();
		title.setText(reference + " (" + trace + ")");
		title.setForeground(Colors.BLACK);
	}

	private void createControl() {

		setLayout(new GridLayout(2, true));
		//
		createButtonSection(this);
		createCommentsSection(this);
		createTraceDataSection(this);
		//
		showComments(false);
	}

	private void createButtonSection(Composite parent) {

		labelTrace = new Label(parent, SWT.NONE);
		labelTrace.setText("");
		labelTrace.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		//
		Composite composite = new Composite(parent, SWT.NONE);
		GridData gridDataComposite = new GridData(GridData.FILL_HORIZONTAL);
		gridDataComposite.horizontalAlignment = SWT.END;
		composite.setLayoutData(gridDataComposite);
		composite.setLayout(new GridLayout(2, false));
		//
		buttonIsMatched = new Button(composite, SWT.PUSH);
		buttonIsMatched.setText("");
		buttonIsMatched.setToolTipText("Flag as matched");
		buttonIsMatched.setData(FLAG_MATCHED, false);
		buttonIsMatched.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_DESELECTED, IApplicationImage.SIZE_16x16));
		buttonIsMatched.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				boolean isMatched = (boolean)buttonIsMatched.getData(FLAG_MATCHED);
				if(isMatched) {
					buttonIsMatched.setData(FLAG_MATCHED, false);
					buttonIsMatched.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_DESELECTED, IApplicationImage.SIZE_16x16));
				} else {
					buttonIsMatched.setData(FLAG_MATCHED, true);
					buttonIsMatched.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_SELECTED, IApplicationImage.SIZE_16x16));
				}
			}
		});
		//
		Button buttonFlipComments = new Button(composite, SWT.PUSH);
		buttonFlipComments.setText("");
		buttonFlipComments.setToolTipText("Show/Hide Comments");
		buttonFlipComments.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_EDIT, IApplicationImage.SIZE_16x16));
		buttonFlipComments.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				boolean isVisible = !commentsText.isVisible();
				showComments(isVisible);
				//
				if(isVisible) {
					buttonFlipComments.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_COLLAPSE_ALL, IApplicationImage.SIZE_16x16));
				} else {
					buttonFlipComments.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_EDIT, IApplicationImage.SIZE_16x16));
				}
			}
		});
	}

	private void createCommentsSection(Composite parent) {

		commentsText = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
		commentsText.setText("");
		commentsText.setLayoutData(getGridData());
	}

	private void createTraceDataSection(Composite parent) {

		sampleDataUI = new TraceDataUI(parent, SWT.NONE, true, false, false);
		sampleDataUI.setLayoutData(getGridData());
		//
		referenceDataUI = new TraceDataUI(parent, SWT.NONE, false, true, true);
		referenceDataUI.setLayoutData(getGridData());
		/*
		 * Link both charts.
		 */
		sampleDataUI.addLinkedScrollableChart(referenceDataUI);
		referenceDataUI.addLinkedScrollableChart(sampleDataUI);
	}

	private GridData getGridData() {

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		return gridData;
	}

	private void showComments(boolean isVisible) {

		GridData gridData = (GridData)commentsText.getLayoutData();
		gridData.exclude = !isVisible;
		commentsText.setVisible(isVisible);
		Composite parent = commentsText.getParent();
		parent.layout(false);
		parent.redraw();
	}
}
