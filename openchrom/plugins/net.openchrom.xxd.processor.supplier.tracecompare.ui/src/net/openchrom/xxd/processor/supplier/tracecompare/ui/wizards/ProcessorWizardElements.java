/*******************************************************************************
 * Copyright (c) 2017 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package net.openchrom.xxd.processor.supplier.tracecompare.ui.wizards;

import org.eclipse.chemclipse.support.ui.wizards.ChromatogramWizardElements;

import net.openchrom.xxd.processor.supplier.tracecompare.model.ProcessorModel;

public class ProcessorWizardElements extends ChromatogramWizardElements implements IProcessorWizardElements {

	private ProcessorModel processorModel;

	public ProcessorWizardElements() {
		processorModel = new ProcessorModel();
	}

	@Override
	public ProcessorModel getProcessorModel() {

		return processorModel;
	}
}
