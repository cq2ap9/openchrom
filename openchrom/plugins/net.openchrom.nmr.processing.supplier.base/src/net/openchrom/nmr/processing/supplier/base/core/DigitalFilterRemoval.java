/*******************************************************************************
 * Copyright (c) 2018 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Alexander Stark - initial API and implementation
 *******************************************************************************/
package net.openchrom.nmr.processing.supplier.base.core;

import org.apache.commons.math3.complex.Complex;
import org.eclipse.chemclipse.nmr.model.core.IScanNMR;
import org.eclipse.chemclipse.nmr.model.support.ISignalExtractor;
import org.eclipse.chemclipse.nmr.model.support.SignalExtractor;

import net.openchrom.nmr.processing.supplier.base.settings.DigitalFilterRemovalSettings;
import net.openchrom.nmr.processing.supplier.base.settings.support.ZERO_FILLING_FACTOR;

public class DigitalFilterRemoval {

	public Complex[] removeDigitalFilter(IScanNMR scanNMR, Complex[] FID, DigitalFilterRemovalSettings digitalFilterRemovalSettings) {

		FourierTransformationProcessor transform = new FourierTransformationProcessor();
		UtilityFunctions utilityFunction = new UtilityFunctions();
		ISignalExtractor signalExtractor = new SignalExtractor(scanNMR);
		//
		Complex[] freeInductionDecay = FID;
		Complex[] tempFID = new Complex[freeInductionDecay.length];
		/*
		 * Digital Filtering
		 */
		// necessary parameters for processing
		double leftRotationFid = digitalFilterRemovalSettings.getLeftRotationFid();
		double leftRotationFidOriginal = digitalFilterRemovalSettings.getLeftRotationFidOriginal();
		double dspPhaseFactor = digitalFilterRemovalSettings.getDspPhaseFactor();
		scanNMR.putProcessingParameters("dspPhaseFactor", dspPhaseFactor);
		scanNMR.putProcessingParameters("leftRotationFid", leftRotationFid);
		scanNMR.putProcessingParameters("leftRotationFidOriginal", leftRotationFidOriginal);
		//
		if(Math.abs(leftRotationFid) > 0.0) {
			// // save old size here
			// int originalFIDSize = freeInductionDecay.length;
			//
			Complex[] freeInductionDecayZeroFill = new Complex[freeInductionDecay.length];
			Complex[] filteredNMRSpectrum = null;
			//
			// automatic zero filling just in case size != 2^n
			int n = freeInductionDecay.length;
			int nextPower = (int)(Math.ceil((Math.log(n) / Math.log(2))));
			int previousPower = (int)(Math.floor(((Math.log(n) / Math.log(2)))));
			if(nextPower != previousPower) {
				// flag for used data
				double digitalFilterZeroFill = 1;
				scanNMR.putProcessingParameters("digitalFilterZeroFill", digitalFilterZeroFill);
				// zero filling
				double autoZeroFill = 1;
				scanNMR.putProcessingParameters("autoZeroFill", autoZeroFill);
				//
				double zeroFillingFactor = 0.0; // 0 = no action
				scanNMR.putProcessingParameters("zeroFillingFactor", zeroFillingFactor);
				//
				ZeroFilling zeroFiller = new ZeroFilling();
				freeInductionDecayZeroFill = zeroFiller.zerofill(signalExtractor, scanNMR, ZERO_FILLING_FACTOR.AUTO);
				// reset flags
				autoZeroFill = 0;
				scanNMR.putProcessingParameters("autoZeroFill", autoZeroFill);
				digitalFilterZeroFill = 0;
				scanNMR.putProcessingParameters("digitalFilterZeroFill", digitalFilterZeroFill);
				//
				filteredNMRSpectrum = transform.fourierTransformNmrData(freeInductionDecayZeroFill, utilityFunction);
			} else {
				// no ZF!
				double autoZeroFill = 0;
				scanNMR.putProcessingParameters("autoZeroFill", autoZeroFill);
				filteredNMRSpectrum = transform.fourierTransformNmrData(freeInductionDecay, utilityFunction);
			}
			// create filtered spectrum
			Complex[] unfilteredNMRSpectrum = new Complex[filteredNMRSpectrum.length];
			double[] digitalFilterFactor = new double[filteredNMRSpectrum.length];
			int spectrumSize = filteredNMRSpectrum.length;
			int f = 0;
			Complex complexFactor = new Complex(-0.0, -1.0);
			// remove the filter!
			for(int i = 1; i <= spectrumSize; i++) {
				double filterTermA = (double)i / spectrumSize;
				double filterTermB = Math.floor(spectrumSize / 2);
				digitalFilterFactor[f] = filterTermA - filterTermB;
				Complex exponentialFactor = complexFactor.multiply(leftRotationFid * 2 * Math.PI * digitalFilterFactor[f]);
				unfilteredNMRSpectrum[f] = filteredNMRSpectrum[f].multiply(exponentialFactor.exp());
				f++;
			}
			// ifft => revert to fid
			Complex[] tempUnfilteredSpectrum = transform.inverseFourierTransformData(unfilteredNMRSpectrum, utilityFunction);
			// remove temporary zero filling if necessary
			System.arraycopy(tempUnfilteredSpectrum, 0, tempFID, 0, tempFID.length);
			// save unfiltered fid
			long[] timeAxis = utilityFunction.generateTimeScale(scanNMR);
			signalExtractor.createScansFID(tempFID, timeAxis);
		} else {
			// no digital filter, no zero filling
			double autoZeroFill = 0;
			scanNMR.putProcessingParameters("autoZeroFill", autoZeroFill);
		}
		//
		return tempFID;
	}
}
