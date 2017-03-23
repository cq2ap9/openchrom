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
package net.openchrom.chromatogram.msd.processor.supplier.massshiftdetector.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;

public class ProcessorData {

	private ProcessorModel_v1000 processorModel;
	//
	private IChromatogramMSD chromatogramReference;
	private IChromatogramMSD chromatogramShifted;
	//
	private Map<Integer, Map<Integer, Map<Integer, Double>>> massShifts;
	private Map<Integer, ValueRange> levelGranularity;

	public ProcessorData() {
		levelGranularity = new HashMap<Integer, ValueRange>();
	}

	public Map<Integer, ValueRange> getLevelGranularity() {

		return levelGranularity;
	}

	public Map<Integer, Map<Integer, Map<Integer, Double>>> getMassShifts() {

		return massShifts;
	}

	public void setMassShifts(Map<Integer, Map<Integer, Map<Integer, Double>>> massShifts) {

		this.massShifts = massShifts;
	}

	public ProcessorModel_v1000 getProcessorModel() {

		return processorModel;
	}

	public void setProcessorModel(ProcessorModel_v1000 processorModel) {

		this.processorModel = processorModel;
	}

	public IChromatogramMSD getChromatogramReference() {

		return chromatogramReference;
	}

	public void setChromatogramReference(IChromatogramMSD chromatogramReference) {

		this.chromatogramReference = chromatogramReference;
	}

	public IChromatogramMSD getChromatogramShifted() {

		return chromatogramShifted;
	}

	public void setChromatogramShifted(IChromatogramMSD chromatogramShifted) {

		this.chromatogramShifted = chromatogramShifted;
	}
}
