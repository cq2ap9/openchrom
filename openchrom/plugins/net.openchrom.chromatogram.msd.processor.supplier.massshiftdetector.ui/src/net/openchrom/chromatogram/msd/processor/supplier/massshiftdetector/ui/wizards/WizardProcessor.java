/*******************************************************************************
 * Copyright (c) 2017 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package net.openchrom.chromatogram.msd.processor.supplier.massshiftdetector.ui.wizards;

import java.util.Date;

import javax.xml.bind.JAXBException;

import org.eclipse.chemclipse.support.ui.wizards.AbstractFileWizard;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import net.openchrom.chromatogram.msd.processor.supplier.massshiftdetector.core.Processor;
import net.openchrom.chromatogram.msd.processor.supplier.massshiftdetector.io.ProcessorModelWriter;
import net.openchrom.chromatogram.msd.processor.supplier.massshiftdetector.model.ProcessorModel;

public class WizardProcessor extends AbstractFileWizard {

	private IProcessorWizardElements wizardElements = new ProcessorWizardElements();
	private PageDescription pageDescription;

	public WizardProcessor() {
		super("massshiftdetector" + new Date().getTime(), Processor.PROCESSOR_FILE_EXTENSION);
	}

	@Override
	public void addPages() {

		super.addPages();
		/*
		 * Pages must implement IExtendedWizardPage / extend AbstractExtendedWizardPage
		 */
		pageDescription = new PageDescription(wizardElements);
		//
		addPage(pageDescription);
	}

	@Override
	public boolean canFinish() {

		boolean canFinish = pageDescription.canFinish();
		return canFinish;
	}

	@Override
	public void doFinish(IProgressMonitor monitor) throws CoreException {

		monitor.beginTask("massshiftdetector", IProgressMonitor.UNKNOWN);
		final IFile file = super.prepareProject(monitor);
		//
		ProcessorModel processorModel = new ProcessorModel();
		processorModel.setChromatogramName("chromatogramName");
		processorModel.setChromatogramPath("chromatogramPath");
		processorModel.setNotes("Implement");
		//
		try {
			ProcessorModelWriter processorModelWriter = new ProcessorModelWriter();
			processorModelWriter.write(file.getLocation().toFile(), processorModel, monitor);
		} catch(JAXBException e) {
			System.out.println(e);
		}
		/*
		 * Refresh
		 */
		super.refreshWorkspace(monitor);
		super.runOpenEditor(file, monitor);
	}
}
