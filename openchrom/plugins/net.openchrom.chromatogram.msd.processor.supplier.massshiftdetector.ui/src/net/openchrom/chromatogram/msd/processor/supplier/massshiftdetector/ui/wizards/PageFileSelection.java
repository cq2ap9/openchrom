/*******************************************************************************
 * Copyright (c) 2017, 2019 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 * Christoph Läubrich - change to new Wizard API, fix shell access
 *******************************************************************************/
package net.openchrom.chromatogram.msd.processor.supplier.massshiftdetector.ui.wizards;

import java.io.File;
import java.util.Set;

import org.eclipse.chemclipse.model.types.DataType;
import org.eclipse.chemclipse.support.ui.wizards.AbstractExtendedWizardPage;
import org.eclipse.chemclipse.ux.extension.xxd.ui.wizards.InputEntriesWizard;
import org.eclipse.chemclipse.ux.extension.xxd.ui.wizards.InputWizardSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import net.openchrom.chromatogram.msd.processor.supplier.massshiftdetector.model.IProcessorModel;
import net.openchrom.chromatogram.msd.processor.supplier.massshiftdetector.preferences.PreferenceSupplier;
import net.openchrom.chromatogram.msd.processor.supplier.massshiftdetector.ui.Activator;

public class PageFileSelection extends AbstractExtendedWizardPage {

	//
	private IProcessorWizardElements wizardElements;
	//
	private Text referenceChromatogramText;
	private Text isotopeChromatogramText;
	private Text notesText;
	private Text descriptionText;

	public PageFileSelection(IProcessorWizardElements wizardElements) {
		//
		super(PageFileSelection.class.getName());
		setTitle("File Selection");
		setDescription("Select the reference and isotope chromatogram.");
		this.wizardElements = wizardElements;
	}

	@Override
	public boolean canFinish() {

		IProcessorModel processorModel = wizardElements.getProcessorModel();
		//
		if(getErrorMessage() != null) {
			return false;
		}
		//
		if(processorModel.getReferenceChromatogramPath() == null || "".equals(processorModel.getReferenceChromatogramPath())) {
			return false;
		}
		//
		if(processorModel.getIsotopeChromatogramPath() == null || "".equals(processorModel.getIsotopeChromatogramPath())) {
			return false;
		}
		//
		if(processorModel.getNotes() == null) {
			return false;
		}
		//
		if(processorModel.getDescription() == null) {
			return false;
		}
		//
		return true;
	}

	@Override
	public void setDefaultValues() {

	}

	@Override
	public void setVisible(boolean visible) {

		super.setVisible(visible);
		if(visible) {
			IProcessorModel processorModel = wizardElements.getProcessorModel();
			referenceChromatogramText.setText((processorModel.getReferenceChromatogramPath() != null) ? processorModel.getReferenceChromatogramPath() : "");
			isotopeChromatogramText.setText((processorModel.getIsotopeChromatogramPath() != null) ? processorModel.getIsotopeChromatogramPath() : "");
			notesText.setText((processorModel.getNotes() != null) ? processorModel.getNotes() : "");
			descriptionText.setText((processorModel.getDescription() != null) ? processorModel.getDescription() : "");
			validateData();
		}
	}

	@Override
	public void createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		//
		createReferenceChromatogramSection(composite);
		createIstopeChromatogramSection(composite);
		createNoteSection(composite);
		createDescriptionSection(composite);
		//
		validateData();
		//
		setControl(composite);
	}

	private void createReferenceChromatogramSection(Composite parent) {

		Label label = new Label(parent, SWT.NONE);
		label.setText("Reference - Chromatogram");
		GridData gridDataLabel = new GridData(GridData.FILL_HORIZONTAL);
		gridDataLabel.horizontalSpan = 2;
		label.setLayoutData(gridDataLabel);
		//
		referenceChromatogramText = new Text(parent, SWT.BORDER);
		referenceChromatogramText.setText("");
		referenceChromatogramText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		//
		Button button = new Button(parent, SWT.PUSH);
		button.setText("Select");
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				InputWizardSettings inputWizardSettings = InputWizardSettings.create(Activator.getDefault().getPreferenceStore(), PreferenceSupplier.P_FILTER_PATH_REFERENCE_CHROMATOGRAM, DataType.MSD);
				inputWizardSettings.setTitle("Reference - Chromatogram");
				inputWizardSettings.setDescription("Select the reference chromatogram.");
				Set<File> selected = InputEntriesWizard.openWizard(getShell(), inputWizardSettings).keySet();
				for(File file : selected) {
					referenceChromatogramText.setText(file.getAbsolutePath());
					break;
				}
				validateData();
			}
		});
	}

	private void createIstopeChromatogramSection(Composite parent) {

		Label label = new Label(parent, SWT.NONE);
		label.setText("Isotope - Chromatogram");
		GridData gridDataLabel = new GridData(GridData.FILL_HORIZONTAL);
		gridDataLabel.horizontalSpan = 2;
		label.setLayoutData(gridDataLabel);
		//
		isotopeChromatogramText = new Text(parent, SWT.BORDER);
		isotopeChromatogramText.setText("");
		isotopeChromatogramText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		//
		Button button = new Button(parent, SWT.PUSH);
		button.setText("Select");
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				InputWizardSettings inputWizardSettings = InputWizardSettings.create(Activator.getDefault().getPreferenceStore(), PreferenceSupplier.P_FILTER_PATH_ISOTOPE_CHROMATOGRAM, DataType.MSD);
				inputWizardSettings.setTitle("Isotope - Chromatogram");
				inputWizardSettings.setDescription("Select the isotope chromatogram.");
				Set<File> selected = InputEntriesWizard.openWizard(getShell(), inputWizardSettings).keySet();
				for(File file : selected) {
					isotopeChromatogramText.setText(file.getAbsolutePath());
					break;
				}
				validateData();
			}
		});
	}

	private void createNoteSection(Composite parent) {

		Label label = new Label(parent, SWT.NONE);
		label.setText("Notes");
		GridData gridDataLabel = new GridData(GridData.FILL_HORIZONTAL);
		gridDataLabel.horizontalSpan = 2;
		label.setLayoutData(gridDataLabel);
		//
		notesText = new Text(parent, SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		notesText.setLayoutData(gridData);
		notesText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {

				validateData();
			}
		});
	}

	private void createDescriptionSection(Composite parent) {

		Label label = new Label(parent, SWT.NONE);
		label.setText("Description");
		GridData gridDataLabel = new GridData(GridData.FILL_HORIZONTAL);
		gridDataLabel.horizontalSpan = 2;
		label.setLayoutData(gridDataLabel);
		//
		descriptionText = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.WRAP);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		descriptionText.setLayoutData(gridData);
		descriptionText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {

				validateData();
			}
		});
	}

	private void validateData() {

		String message = null;
		IProcessorModel processorModel = wizardElements.getProcessorModel();
		//
		String referenceChromatogramPath = referenceChromatogramText.getText().trim();
		if(!new File(referenceChromatogramPath).exists()) {
			message = "Please select the reference chromatogram.";
		} else {
			processorModel.setReferenceChromatogramPath(referenceChromatogramPath);
		}
		//
		String isotopeChromatogramPath = isotopeChromatogramText.getText().trim();
		if(!new File(isotopeChromatogramPath).exists()) {
			message = "Please select the isotope chromatogram.";
		} else {
			processorModel.setIsotopeChromatogramPath(isotopeChromatogramPath);
		}
		//
		processorModel.setNotes(notesText.getText().trim());
		processorModel.setDescription(descriptionText.getText().trim());
		/*
		 * Updates the status
		 */
		updateStatus(message);
	}
}
