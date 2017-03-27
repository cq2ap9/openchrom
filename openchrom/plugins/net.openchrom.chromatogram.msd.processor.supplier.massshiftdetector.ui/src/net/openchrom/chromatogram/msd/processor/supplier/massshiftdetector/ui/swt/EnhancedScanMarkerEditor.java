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
package net.openchrom.chromatogram.msd.processor.supplier.massshiftdetector.ui.swt;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.msd.model.core.IScanMSD;
import org.eclipse.chemclipse.rcp.app.ui.addons.ModelSupportAddon;
import org.eclipse.chemclipse.rcp.ui.icons.core.ApplicationImageFactory;
import org.eclipse.chemclipse.rcp.ui.icons.core.IApplicationImage;
import org.eclipse.chemclipse.support.events.IChemClipseEvents;
import org.eclipse.chemclipse.support.ui.listener.AbstractControllerComposite;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import net.openchrom.chromatogram.msd.processor.supplier.massshiftdetector.model.ProcessorData;
import net.openchrom.chromatogram.msd.processor.supplier.massshiftdetector.model.ScanMarker;
import net.openchrom.chromatogram.msd.processor.supplier.massshiftdetector.ui.editors.EditorProcessor;
import net.openchrom.chromatogram.msd.processor.supplier.massshiftdetector.ui.runnables.ScanMarkerDetectorRunnable;

public class EnhancedScanMarkerEditor extends AbstractControllerComposite {

	private static final Logger logger = Logger.getLogger(EnhancedScanMarkerEditor.class);
	//
	private EditorProcessor editorProcessor;
	//
	private Button buttonCalculate;
	private Button buttonPrevious;
	private Button buttonExport;
	private List<Button> buttons;
	//
	private ScanMarkerListUI scanMarkerListUI;
	private MassShiftListUI massShiftListUI;
	private Label scanMarkerInfoLabel;

	public EnhancedScanMarkerEditor(Composite parent, int style) {
		super(parent, style);
		buttons = new ArrayList<Button>();
		createControl();
	}

	public void setEditorProcessor(EditorProcessor editorProcessor) {

		this.editorProcessor = editorProcessor;
	}

	@Override
	public boolean setFocus() {

		return super.setFocus();
	}

	@Override
	public void setStatus(boolean readOnly) {

		for(Button button : buttons) {
			button.setEnabled(false);
		}
		/*
		 * Defaults when editable.
		 */
		if(!readOnly) {
			buttonCalculate.setEnabled(true);
			buttonPrevious.setEnabled(true);
			buttonExport.setEnabled(true);
		}
	}

	private void createControl() {

		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		//
		Composite compositeMain = new Composite(composite, SWT.NONE);
		compositeMain.setLayout(new GridLayout(2, true));
		compositeMain.setLayoutData(new GridData(GridData.FILL_BOTH));
		createListComposite(compositeMain);
		/*
		 * Button Bar
		 */
		Composite compositeButtons = new Composite(composite, SWT.NONE);
		compositeButtons.setLayout(new GridLayout(1, true));
		compositeButtons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		//
		GridData gridDataButtons = new GridData(GridData.FILL_HORIZONTAL);
		gridDataButtons.minimumWidth = 150;
		//
		buttons.add(buttonCalculate = createCalculateButton(compositeButtons, gridDataButtons));
		buttons.add(buttonPrevious = createPreviousButton(compositeButtons, gridDataButtons));
		buttons.add(createSaveButton(compositeButtons, gridDataButtons));
		buttons.add(buttonExport = createExportButton(compositeButtons, gridDataButtons));
	}

	private void createListComposite(Composite parent) {

		scanMarkerListUI = new ScanMarkerListUI(parent, SWT.BORDER);
		scanMarkerListUI.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		scanMarkerListUI.getTable().addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				IStructuredSelection selection = scanMarkerListUI.getStructuredSelection();
				Object object = selection.getFirstElement();
				if(object instanceof ScanMarker) {
					/*
					 * Display the mass shifts
					 */
					ScanMarker scanMarker = (ScanMarker)object;
					massShiftListUI.setInput(scanMarker.getMassShifts());
					/*
					 * Update the comparison UI.
					 */
					ProcessorData processorData = editorProcessor.getProcessorData();
					int scan = scanMarker.getScan();
					IChromatogramMSD referenceChromatogram = processorData.getReferenceChromatogram();
					IChromatogramMSD isotopeChromatogram = processorData.getIsotopeChromatogram();
					IScanMSD referenceMassSpectrum = (IScanMSD)referenceChromatogram.getScan(scan);
					IScanMSD isotopeMassSpectrum = (IScanMSD)isotopeChromatogram.getScan(scan);
					//
					IEventBroker eventBroker = ModelSupportAddon.getEventBroker();
					Map<String, IScanMSD> data = new HashMap<String, IScanMSD>();
					data.put(IChemClipseEvents.PROPERTY_REFERENCE_MS, referenceMassSpectrum);
					data.put(IChemClipseEvents.PROPERTY_COMPARISON_MS, isotopeMassSpectrum);
					eventBroker.post(IChemClipseEvents.TOPIC_SCAN_MSD_UPDATE_COMPARISON, data);
					/*
					 * Update the chromatogram selection.
					 */
					// IChromatogramSelectionMSD chromatogramSelectionMSD = processorData.getReferenceChromatogramSelection();
					// chromatogramSelectionMSD.setSelectedScan(referenceMassSpectrum);
					// int startRetentionTime = referenceMassSpectrum.getRetentionTime() - 5000; // -5 seconds
					// int stopRetentionTime = referenceMassSpectrum.getRetentionTime() + 5000; // +5 seconds
					// chromatogramSelectionMSD.setRanges(startRetentionTime, stopRetentionTime, chromatogramSelectionMSD.getStartAbundance(), chromatogramSelectionMSD.getStartAbundance());
					// chromatogramSelectionMSD.fireUpdateChange(true);
				}
			}
		});
		//
		massShiftListUI = new MassShiftListUI(parent, SWT.BORDER);
		massShiftListUI.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		//
		scanMarkerInfoLabel = new Label(parent, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		scanMarkerInfoLabel.setLayoutData(gridData);
	}

	private Button createCalculateButton(Composite parent, GridData gridData) {

		Shell shell = Display.getCurrent().getActiveShell();
		Button button = new Button(parent, SWT.PUSH);
		button.setText("Calculate");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_CALCULATE, IApplicationImage.SIZE_16x16));
		button.setLayoutData(gridData);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				if(scanMarkerListUI.getTable().getItemCount() == 0) {
					calculateScanMarker(shell);
				} else {
					MessageBox messageBox = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL);
					messageBox.setText("Calculate Scan List");
					messageBox.setMessage("Current results are overwritten when doing a new calculation.");
					if(messageBox.open() == SWT.YES) {
						calculateScanMarker(shell);
					}
				}
			}
		});
		return button;
	}

	private void calculateScanMarker(Shell shell) {

		ProcessorData processorData = editorProcessor.getProcessorData();
		ScanMarkerDetectorRunnable runnable = new ScanMarkerDetectorRunnable(processorData);
		ProgressMonitorDialog monitor = new ProgressMonitorDialog(shell);
		//
		try {
			monitor.run(true, true, runnable);
			List<ScanMarker> scanMarker = runnable.getScanMarker();
			processorData.setScanMarker(scanMarker);
			scanMarkerListUI.setInput(scanMarker);
			scanMarkerInfoLabel.setText("In summary: " + scanMarker.size() + " possible scan(s) containing isotope shifts have been identified.");
		} catch(InterruptedException e1) {
			logger.warn(e1);
		} catch(InvocationTargetException e1) {
			logger.warn(e1);
		}
	}

	private Button createPreviousButton(Composite parent, GridData gridData) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("Previous");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_ARROW_BACKWARD, IApplicationImage.SIZE_16x16));
		button.setLayoutData(gridData);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				editorProcessor.setActivePage(EditorProcessor.PAGE_INDEX_SHIFT_HEATMAP);
			}
		});
		return button;
	}

	private Button createSaveButton(Composite parent, GridData gridData) {

		Shell shell = Display.getCurrent().getActiveShell();
		//
		Button button = new Button(parent, SWT.PUSH);
		button.setText("Save");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_SAVE, IApplicationImage.SIZE_16x16));
		button.setLayoutData(gridData);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				ProgressMonitorDialog monitor = new ProgressMonitorDialog(shell);
				try {
					monitor.run(true, true, new IRunnableWithProgress() {

						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

							editorProcessor.doSave(monitor);
						}
					});
				} catch(InvocationTargetException e1) {
					logger.warn(e1);
				} catch(InterruptedException e1) {
					logger.warn(e1);
				}
			}
		});
		return button;
	}

	private Button createExportButton(Composite parent, GridData gridData) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("Export");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_EXPORT, IApplicationImage.SIZE_16x16));
		button.setLayoutData(gridData);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				editorProcessor.doSaveAs();
			}
		});
		return button;
	}
}
