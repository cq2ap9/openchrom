/*******************************************************************************
 * Copyright (c) 2018, 2019 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Alexander Stark - initial API and implementation
 * Christoph Läubrich - Change to use a more generic API, cleanup the code and streamline the algorithm flow
 *******************************************************************************/
package net.openchrom.nmr.processing.supplier.base.core;

import org.apache.commons.math3.complex.Complex;
import org.eclipse.chemclipse.filter.Filter;
import org.eclipse.chemclipse.model.core.FilteredMeasurement;
import org.eclipse.chemclipse.model.filter.IMeasurementFilter;
import org.eclipse.chemclipse.nmr.model.core.FIDMeasurement;
import org.eclipse.chemclipse.nmr.model.core.FilteredFIDMeasurement;
import org.eclipse.chemclipse.processing.core.MessageConsumer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.service.component.annotations.Component;

import net.openchrom.nmr.processing.supplier.base.core.UtilityFunctions.ComplexFIDData;
import net.openchrom.nmr.processing.supplier.base.settings.DigitalFilterRemovalSettings;

@Component(service = {Filter.class, IMeasurementFilter.class})
public class DigitalFilterRemoval extends AbstractFIDSignalFilter<DigitalFilterRemovalSettings> {

	private static final String FILTER_NAME = "Digital Filter Removal";
	private static final String MARKER = DigitalFilterRemoval.class.getName() + ".filtered";

	public DigitalFilterRemoval() {
		super(DigitalFilterRemovalSettings.class);
	}

	@Override
	protected boolean accepts(FIDMeasurement item) {

		return item.getHeaderData(MARKER) == null;
	}

	@Override
	public String getFilterName() {

		return FILTER_NAME;
	}

	@Override
	protected FilteredMeasurement<?> doFiltering(FIDMeasurement measurement, DigitalFilterRemovalSettings settings, MessageConsumer messageConsumer, IProgressMonitor monitor) {

		double multiplicationFactor = settings.getDcOffsetMultiplicationFactor();
		double leftRotationFid = settings.getLeftRotationFid();
		if(Double.isNaN(leftRotationFid) && Double.isNaN(multiplicationFactor)) {
			messageConsumer.addInfoMessage(FILTER_NAME, "No Left Rotation value and no DC offset specified, skipp processing");
			return null;
		}
		FilteredFIDMeasurement filteredFIDMeasurement = new FilteredFIDMeasurement(measurement);
		ComplexFIDData fidData = UtilityFunctions.toComplexFIDData(measurement.getSignals());
		if(Double.isNaN(multiplicationFactor)) {
			messageConsumer.addInfoMessage(FILTER_NAME, "No DC Offset to remove");
		} else {
			fidData.signals[0] = fidData.signals[0].multiply(multiplicationFactor);
			messageConsumer.addInfoMessage(FILTER_NAME, "DC Offset was removed");
		}
		if(Double.isNaN(leftRotationFid)) {
			messageConsumer.addInfoMessage(FILTER_NAME, "No left rotation value was given, skipp processing");
		} else if(Math.abs(leftRotationFid) > 0.0) {
			DigitalFilterRemoval.removeDigitalFilter(fidData, leftRotationFid, messageConsumer);
			messageConsumer.addInfoMessage(FILTER_NAME, "Digital FIlter was removed");
		} else {
			messageConsumer.addWarnMessage(FILTER_NAME, "Left Rotation value must be greater than zero, skipp processing");
		}
		filteredFIDMeasurement.putHeaderData(MARKER, "true");
		filteredFIDMeasurement.setSignals(fidData.toSignal());
		return filteredFIDMeasurement;
	}

	private static void removeDigitalFilter(ComplexFIDData freeInductionDecay, double leftRotationFid, MessageConsumer messageConsumer) {

		// automatic zero filling just in case size != 2^n
		Complex[] freeInductionDecayZeroFill = ZeroFilling.fill(freeInductionDecay.signals);
		//
		Complex[] filteredNMRSpectrum = FourierTransformationProcessor.fourierTransformNmrData(freeInductionDecayZeroFill);
		// create filtered spectrum
		Complex[] unfilteredNMRSpectrum = new Complex[filteredNMRSpectrum.length];
		double[] digitalFilterFactor = new double[filteredNMRSpectrum.length];
		int spectrumSize = filteredNMRSpectrum.length;

		Complex complexFactor = new Complex(-0.0, -1.0);
		// remove the filter!
		for(int i = 0; i < spectrumSize; i++) {
			double filterTermA = (double)i / spectrumSize;
			double filterTermB = Math.floor(spectrumSize / 2);
			digitalFilterFactor[i] = filterTermB - filterTermA;
			Complex exponentialFactor = complexFactor.multiply(leftRotationFid * 2 * Math.PI * digitalFilterFactor[i]);
			unfilteredNMRSpectrum[i] = filteredNMRSpectrum[i].multiply(exponentialFactor.exp());
		}
		// ifft => revert to fid
		Complex[] tempUnfilteredSpectrum = FourierTransformationProcessor.inverseFourierTransformData(unfilteredNMRSpectrum);
		// copy transformed data back to datastructure
		for(int i = 0; i < freeInductionDecay.signals.length; i++) {
			freeInductionDecay.signals[i] = tempUnfilteredSpectrum[i];
		}
	}
}
