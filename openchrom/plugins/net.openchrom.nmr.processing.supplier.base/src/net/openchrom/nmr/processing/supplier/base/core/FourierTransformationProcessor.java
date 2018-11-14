/*******************************************************************************
 * Copyright (c) 2018 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package net.openchrom.nmr.processing.supplier.base.core;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.eclipse.chemclipse.nmr.model.core.IScanNMR;
import org.eclipse.chemclipse.nmr.model.support.ISignalExtractor;
import org.eclipse.chemclipse.nmr.model.support.SignalExtractor;
import org.eclipse.chemclipse.nmr.processor.core.AbstractScanProcessor;
import org.eclipse.chemclipse.nmr.processor.core.IScanProcessor;
import org.eclipse.chemclipse.nmr.processor.settings.IProcessorSettings;
import org.eclipse.chemclipse.processing.core.IProcessingInfo;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import net.openchrom.nmr.processing.supplier.base.settings.FourierTransformationSettings;

public class FourierTransformationProcessor extends AbstractScanProcessor implements IScanProcessor {

	@Override
	public IProcessingInfo process(final IScanNMR scanNMR, final IProcessorSettings processorSettings, final IProgressMonitor monitor) {

		IProcessingInfo processingInfo = validate(scanNMR, processorSettings);
		if(!processingInfo.hasErrorMessages()) {
			FourierTransformationSettings settings = (FourierTransformationSettings)processorSettings;
			ISignalExtractor signalExtractor = new SignalExtractor(scanNMR);
			Complex[] fourierTransformedData = transform(scanNMR, settings);
			UtilityFunctions utilityFunction = new UtilityFunctions();
			double[] chemicalShift = utilityFunction.generateChemicalShiftAxis(scanNMR);
			signalExtractor.createScans(fourierTransformedData, chemicalShift);
			processingInfo.setProcessingResult(scanNMR);
		}
		return processingInfo;
	}

	private Complex[] transform(IScanNMR scanNMR, FourierTransformationSettings processorSettings) {

		/*
		 * Header Data
		 */
		// ==> done in each respective reader
		/*
		 * Raw Data
		 */
		ISignalExtractor signalExtractor = new SignalExtractor(scanNMR);
		/*
		 * according to J.Holy:
		 * ~~~~~~~
		 * extractRawIntesityFID(); will be used for data where/until the digital filter is removed
		 * and
		 * extractIntesityFID(); will hold the data after apodization (=> setScanFIDCorrection();)
		 */
		Complex[] complexSignals = signalExtractor.extractRawIntesityFID();
		/*
		 * Digital Filtering
		 */
		double groupDelayOfDigitallyFilteredData = determineDigitalFilter(scanNMR);
		// necessary parameters for processing
		double leftRotationFid = groupDelayOfDigitallyFilteredData;
		double leftRotationFidOriginal = 0;
		double dspPhaseFactor = 0;
		scanNMR.putProcessingParameters("dspPhaseFactor", dspPhaseFactor);
		scanNMR.putProcessingParameters("leftRotationFid", leftRotationFid);
		scanNMR.putProcessingParameters("leftRotationFidOriginal", leftRotationFidOriginal);
		/*
		 * data pre-processing
		 */
		// Get Timescale
		double[] timeScale = signalExtractor.extractTimeFID();// generateTimeScale(scanNMR);
		for(int i = 0; i < timeScale.length; i++) {
			timeScale[i] = timeScale[i] / 1000000;
		}
		// exponential apodization
		double[] exponentialLineBroadening = new double[timeScale.length];
		exponentialLineBroadening = exponentialApodizationFunction(timeScale, scanNMR);
		// gaussian apodization; is procs_GB the correct parameter?
		double[] gaussianLineBroadening = new double[timeScale.length];
		gaussianLineBroadening = gaussianApodizationFunction(timeScale, scanNMR);
		// apodization window function; to be applied to the fid
		double[] lineBroadeningWindwowFunction = new double[timeScale.length];
		for(int i = 0; i < timeScale.length; i++) {
			lineBroadeningWindwowFunction[i] = gaussianLineBroadening[i] * exponentialLineBroadening[i]; // for ft data
		}
		/*
		 * leftshift the fid - complex
		 */
		// int dataArrayDimension = 0;
		// dataArrayDimension = brukerVariableImporter.importBrukerVariable(dataArrayDimension, scanNMR, "dataArrayDimension");;
		Complex[] complexSignalsShifted = new Complex[complexSignals.length];
		System.arraycopy(complexSignals, 0, complexSignalsShifted, 0, complexSignals.length);
		ShiftNmrData dataShifter = new ShiftNmrData();
		// if (leftRotationFid > 0) {
		// //for (int k = 1; k <= dataArrayDimension; k++) {
		// dataShifter.leftShiftNMRComplexData(complexSignalsShifted, (int)leftRotationFid);
		// // expand for nD dimensions
		// //}
		// }
		//
		// Direct Current correction; FID
		Complex[] freeInductionDecay = new Complex[complexSignals.length];
		freeInductionDecay = directCurrentCorrectionFID(complexSignalsShifted, scanNMR);
		/*
		 * 1. remove shifted points
		 * 2. apply window multiplication = apodization is the technical term for changing the shape of e.g. an electrical signal
		 */
		double numberOfPoints = scanNMR.getProcessingParameters("numberOfPoints");
		double numberOfFourierPoints = scanNMR.getProcessingParameters("numberOfFourierPoints");
		Complex[] freeInductionDecayShiftedWindowMultiplication = new Complex[complexSignals.length];
		for(int i = 0; i < complexSignals.length; i++) {
			freeInductionDecayShiftedWindowMultiplication[i] = new Complex(0, 0);
		}
		if(numberOfFourierPoints >= numberOfPoints) {
			Complex[] tempFID = new Complex[freeInductionDecay.length];
			if(Math.abs(leftRotationFid) > 0.0) {
				// automatic zero filling just in case size != 2^n
				int checkPowerOfTwo = freeInductionDecay.length % 256;
				Complex[] freeInductionDecayZeroFill = new Complex[freeInductionDecay.length];
				// // save old size here
				// int originalFIDSize = freeInductionDecay.length;
				//
				if(checkPowerOfTwo > 0) {
					double autoZeroFill = 1;
					scanNMR.putProcessingParameters("autoZeroFill", autoZeroFill);
					//
					double zeroFillingFactor = 0.0; // 0 = no action
					scanNMR.putProcessingParameters("zeroFillingFactor", zeroFillingFactor);
					//
					ZeroFilling zeroFiller = new ZeroFilling();
					zeroFiller.process(scanNMR, null, new NullProgressMonitor());
					freeInductionDecayZeroFill = signalExtractor.extractRawIntesityFID();
					autoZeroFill = 0;
					scanNMR.putProcessingParameters("autoZeroFill", autoZeroFill);
				} else {
					double autoZeroFill = 0;
					scanNMR.putProcessingParameters("autoZeroFill", autoZeroFill);
				}
				// fft => create filtered spectrum
				Complex[] filteredNMRSpectrum = null;
				if(!Arrays.asList(freeInductionDecayZeroFill).contains(null)) {
					filteredNMRSpectrum = fourierTransformNmrData(freeInductionDecayZeroFill, dataShifter);
				} else {
					filteredNMRSpectrum = fourierTransformNmrData(freeInductionDecay, dataShifter);
				}
				//
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
				Complex[] tempUnfilteredSpectrum = inverseFourierTransformData(unfilteredNMRSpectrum, dataShifter);
				// remove temporary zero filling if necessary
				System.arraycopy(tempUnfilteredSpectrum, 0, tempFID, 0, tempFID.length);
				// save unfiltered spectrum
				UtilityFunctions utilityFunction = new UtilityFunctions();
				int[] timeAxis = utilityFunction.generateTimeScale(scanNMR);
				signalExtractor.createScansFID(tempFID, timeAxis);
			} else {
				// no digital filter, no zero filling
				double autoZeroFill = 0;
				scanNMR.putProcessingParameters("autoZeroFill", autoZeroFill);
			}
			/*
			 * applying apodization here
			 */
			// signals to be modified
			tempFID = signalExtractor.extractRawIntesityFID();
			//
			if(!Arrays.asList(tempFID).contains(null)) {
				double[] tempRealArray = new double[freeInductionDecay.length];
				for(int i = 0; i < freeInductionDecay.length; i++) {
					tempRealArray[i] = tempFID[i].getReal();
				}
				UtilityFunctions utilityFunction = new UtilityFunctions();
				double tempFIDmin = utilityFunction.getMinValueOfArray(tempRealArray);
				double tempFIDmax = utilityFunction.getMaxValueOfArray(tempRealArray);
				//
				if(Math.abs(tempFIDmax) > Math.abs(tempFIDmin)) {
					// System.out.println("neg, *-1");
					// introduced "-"lineBroadeningWindwowFunction after refactoring the removal of dig. filter to flip spectrum up-down
					for(int i = 0; i < lineBroadeningWindwowFunction.length; i++) {
						lineBroadeningWindwowFunction[i] *= -1;
					}
					signalExtractor.setScansFIDCorrection(lineBroadeningWindwowFunction, false);
				} else {
					// System.out.println("pos");
					signalExtractor.setScansFIDCorrection(lineBroadeningWindwowFunction, false);
				}
			} else {
				// without removal of dig. filter
				signalExtractor.setScansFIDCorrection(lineBroadeningWindwowFunction, false);
			}
		} else {
			signalExtractor.setScansFIDCorrection(lineBroadeningWindwowFunction, false);
		}
		// modified signals after apodization
		freeInductionDecayShiftedWindowMultiplication = signalExtractor.extractIntesityFID();
		//
		if(scanNMR.getHeaderDataMap().containsValue("Bruker BioSpin GmbH")) {
			// On A*X data, FCOR (from proc(s)) allows you to control the DC offset of the spectrum; value between 0.0 and 2.0
			double firstFIDDataPointMultiplicationFactor = scanNMR.getProcessingParameters("firstFIDDataPointMultiplicationFactor");
			// multiply first data point
			freeInductionDecayShiftedWindowMultiplication[0].multiply(firstFIDDataPointMultiplicationFactor);
		} else if(scanNMR.getHeaderDataMap().containsValue("Nanalysis Corp.")) {
			// no multiplication necessary?
		} else {
			// another approach
		}
		// zero filling // Automatic zero filling if size != 2^n
		Complex[] freeInductionDecayShiftedWindowMultiplicationZeroFill = new Complex[freeInductionDecayShiftedWindowMultiplication.length];
		int checkPowerOfTwo = freeInductionDecayShiftedWindowMultiplication.length % 256;
		boolean automaticZeroFill = true;
		if(checkPowerOfTwo > 0) {
			double autoZeroFill = 1;
			scanNMR.putProcessingParameters("autoZeroFill", autoZeroFill);
			ZeroFilling zeroFiller = new ZeroFilling();
			zeroFiller.process(scanNMR, null, new NullProgressMonitor());
			freeInductionDecayShiftedWindowMultiplicationZeroFill = signalExtractor.extractIntesityFID();
			autoZeroFill = 0;
			scanNMR.putProcessingParameters("autoZeroFill", autoZeroFill);
			// for(int i = 10; i < 17; i++) {
			// int automaticSize = (int)Math.pow(2, i);
			// if(automaticSize > freeInductionDecayShiftedWindowMultiplication.length) {
			// freeInductionDecayShiftedWindowMultiplicationZeroFill = new Complex[automaticSize];
			// for(int j = 0; j < automaticSize; j++) {
			// freeInductionDecayShiftedWindowMultiplicationZeroFill[j] = new Complex(0, 0);
			// }
			// int copySize = freeInductionDecayShiftedWindowMultiplication.length;
			// System.arraycopy(freeInductionDecayShiftedWindowMultiplication, 0, freeInductionDecayShiftedWindowMultiplicationZeroFill, 0, copySize);
			// numberOfFourierPoints = automaticSize;
			// scanNMR.putProcessingParameters("numberOfFourierPoints", numberOfFourierPoints);
			// automaticZeroFill = false;
			// break;
			// }
			// }
		}
		//
		double zeroFillingFactor = 0.0; // 0 = no action, 16 = 16k, 32 = 32k, 64 = 64k
		scanNMR.putProcessingParameters("zeroFillingFactor", zeroFillingFactor);
		//
		ZeroFilling zeroFiller = new ZeroFilling();
		zeroFiller.process(scanNMR, null, new NullProgressMonitor());
		freeInductionDecayShiftedWindowMultiplicationZeroFill = signalExtractor.extractIntesityFID();
		//
		if(zeroFillingFactor == 1) { // 16k
			// int newDataSize = (int)Math.pow(2, 14);
			// freeInductionDecayShiftedWindowMultiplicationZeroFill = new Complex[newDataSize];
			// for(int i = 0; i < newDataSize; i++) {
			// freeInductionDecayShiftedWindowMultiplicationZeroFill[i] = new Complex(0, 0);
			// }
			// int copySize = freeInductionDecayShiftedWindowMultiplication.length;
			// System.arraycopy(freeInductionDecayShiftedWindowMultiplication, 0, freeInductionDecayShiftedWindowMultiplicationZeroFill, 0, copySize);
			// numberOfFourierPoints = newDataSize;
			// scanNMR.putProcessingParameters("numberOfFourierPoints", numberOfFourierPoints);
		} else if(zeroFillingFactor == 2) { // 32k
			// int newDataSize = (int)Math.pow(2, 15);
			// freeInductionDecayShiftedWindowMultiplicationZeroFill = new Complex[newDataSize];
			// for(int i = 0; i < newDataSize; i++) {
			// freeInductionDecayShiftedWindowMultiplicationZeroFill[i] = new Complex(0, 0);
			// }
			// int copySize = freeInductionDecayShiftedWindowMultiplication.length;
			// System.arraycopy(freeInductionDecayShiftedWindowMultiplication, 0, freeInductionDecayShiftedWindowMultiplicationZeroFill, 0, copySize);
			// numberOfFourierPoints = newDataSize;
			// scanNMR.putProcessingParameters("numberOfFourierPoints", numberOfFourierPoints);
		} else if(zeroFillingFactor == 3) { // 64k
			// int newDataSize = (int)Math.pow(2, 16);
			// freeInductionDecayShiftedWindowMultiplicationZeroFill = new Complex[newDataSize];
			// for(int i = 0; i < newDataSize; i++) {
			// freeInductionDecayShiftedWindowMultiplicationZeroFill[i] = new Complex(0, 0);
			// }
			// int copySize = freeInductionDecayShiftedWindowMultiplication.length;
			// System.arraycopy(freeInductionDecayShiftedWindowMultiplication, 0, freeInductionDecayShiftedWindowMultiplicationZeroFill, 0, copySize);
			// numberOfFourierPoints = newDataSize;
			// scanNMR.putProcessingParameters("numberOfFourierPoints", numberOfFourierPoints);
		} else {
			// // do nothing
			// if(automaticZeroFill) {
			// int dataSize = freeInductionDecayShiftedWindowMultiplication.length;
			// freeInductionDecayShiftedWindowMultiplicationZeroFill = new Complex[dataSize];
			// System.arraycopy(freeInductionDecayShiftedWindowMultiplication, 0, freeInductionDecayShiftedWindowMultiplicationZeroFill, 0, dataSize);
			// numberOfFourierPoints = dataSize;
			// scanNMR.putProcessingParameters("numberOfFourierPoints", numberOfFourierPoints);
			// }
		}
		// Fourier transform, shift and flip the data
		Complex[] nmrSpectrumProcessed = fourierTransformNmrData(freeInductionDecayShiftedWindowMultiplicationZeroFill, dataShifter);
		if(scanNMR.getProcessingParameters("ProcessedDataFlag").equals(1.0)) {
			// shift processed data once more
			dataShifter.leftShiftNMRComplexData(nmrSpectrumProcessed, nmrSpectrumProcessed.length / 2);
		}
		return nmrSpectrumProcessed;
	}

	private static double determineDigitalFilter(IScanNMR scanNMR) {

		double[][] brukerDigitalFilter = {{2, 44.750, 46.000, 46.311}, {3, 33.500, 36.500, 36.530}, {4, 66.625, 48.000, 47.870}, {6, 59.083, 50.167, 50.229}, {8, 68.563, 53.250, 53.289}, {12, 60.375, 69.500, 69.551}, {16, 69.531, 72.250, 71.600}, {24, 61.021, 70.167, 70.184}, {32, 70.016, 72.750, 72.138}, {48, 61.344, 70.500, 70.528}, {64, 70.258, 73.000, 72.348}, {96, 61.505, 70.667, 70.700}, {128, 70.379, 72.500, 72.524}, {192, 61.586, 71.333, 0}, {256, 70.439, 72.250, 0}, {384, 61.626, 71.667, 0}, {512, 70.470, 72.125, 0}, {768, 61.647, 71.833, 0}, {1024, 70.485, 72.063, 0}, {1536, 61.657, 71.917, 0}, {2048, 70.492, 72.031, 0}};
		// System.out.println(BrukerDigitalFilter[11][1]);
		//
		int decimationFactorOfDigitalFilter = 1; // DECIM
		int decimationFactorOfDigitalFilterRow = 0;
		int dspFirmwareVersion = 1; // DSPFVS
		int dspFirmwareVersionRow = 0;
		double groupDelayOfDigitallyFilteredData = 0; // GRPDLY corresponds to digital shift
		// if (scanNMR.getHeaderDataMap().containsKey("acqus_GRPDLY")) {
		if(scanNMR.processingParametersContainsKey("groupDelay") && scanNMR.getProcessingParameters("groupDelay") != -1) {
			groupDelayOfDigitallyFilteredData = scanNMR.getProcessingParameters("groupDelay");
			// }
		} else {
			if(scanNMR.processingParametersContainsKey("decimationFactorOfDigitalFilter")) {
				decimationFactorOfDigitalFilter = scanNMR.getProcessingParameters("decimationFactorOfDigitalFilter").intValue();
				// brukerParameterMap.put("decimationFactorOfDigitalFilter", (double)decimationFactorOfDigitalFilter);
				//
				switch(decimationFactorOfDigitalFilter) {
					case 2:
						decimationFactorOfDigitalFilterRow = 0;
						break;
					case 3:
						decimationFactorOfDigitalFilterRow = 1;
						break;
					case 4:
						decimationFactorOfDigitalFilterRow = 2;
						break;
					case 6:
						decimationFactorOfDigitalFilterRow = 3;
						break;
					case 8:
						decimationFactorOfDigitalFilterRow = 4;
						break;
					case 12:
						decimationFactorOfDigitalFilterRow = 5;
						break;
					case 16:
						decimationFactorOfDigitalFilterRow = 6;
						break;
					case 24:
						decimationFactorOfDigitalFilterRow = 7;
						break;
					case 32:
						decimationFactorOfDigitalFilterRow = 8;
						break;
					case 48:
						decimationFactorOfDigitalFilterRow = 9;
						break;
					case 64:
						decimationFactorOfDigitalFilterRow = 10;
						break;
					case 96:
						decimationFactorOfDigitalFilterRow = 11;
						break;
					case 128:
						decimationFactorOfDigitalFilterRow = 12;
						break;
					case 192:
						decimationFactorOfDigitalFilterRow = 13;
						break;
					case 256:
						decimationFactorOfDigitalFilterRow = 14;
						break;
					case 384:
						decimationFactorOfDigitalFilterRow = 15;
						break;
					case 512:
						decimationFactorOfDigitalFilterRow = 16;
						break;
					case 768:
						decimationFactorOfDigitalFilterRow = 17;
						break;
					case 1024:
						decimationFactorOfDigitalFilterRow = 18;
						break;
					case 1536:
						decimationFactorOfDigitalFilterRow = 19;
						break;
					case 2048:
						decimationFactorOfDigitalFilterRow = 20;
						break;
					default:
						// unknown value
						decimationFactorOfDigitalFilter = 0;
						decimationFactorOfDigitalFilterRow = 666; // Matlab => Double.POSITIVE_INFINITY;
				}
			} else {
				// no DECIM parameter in acqus
				decimationFactorOfDigitalFilter = 0;
				decimationFactorOfDigitalFilterRow = 666; // Matlab => Double.POSITIVE_INFINITY;
			}
			if(scanNMR.processingParametersContainsKey("dspFirmwareVersion")) {
				dspFirmwareVersion = scanNMR.getProcessingParameters("dspFirmwareVersion").intValue();
				// brukerParameterMap.put("dspFirmwareVersion", (double)dspFirmwareVersion);
				//
				switch(dspFirmwareVersion) {
					case 10:
						dspFirmwareVersionRow = 1;
						break;
					case 11:
						dspFirmwareVersionRow = 2;
						break;
					case 12:
						dspFirmwareVersionRow = 3;
						break;
					default:
						// unknown value
						dspFirmwareVersion = 0;
						dspFirmwareVersionRow = 0;
				}
			} else {
				// no DSPFVS parameter in acqus
				dspFirmwareVersion = 0;
			}
			if(decimationFactorOfDigitalFilterRow > 13 && dspFirmwareVersionRow == 3) {
				// unknown combination of DSPVFS and DECIM parameters
				decimationFactorOfDigitalFilter = 0;
				dspFirmwareVersion = 0;
			}
		}
		//
		if(decimationFactorOfDigitalFilter == 0 && dspFirmwareVersion == 0) {
			// No digital filtering
			groupDelayOfDigitallyFilteredData = 0;
		} else if(decimationFactorOfDigitalFilter == 1 && dspFirmwareVersion == 1) {
			// digital filtering set by GRPDLY => do nothing
		} else {
			groupDelayOfDigitallyFilteredData = brukerDigitalFilter[decimationFactorOfDigitalFilterRow][dspFirmwareVersionRow];
		}
		//
		if(scanNMR.getProcessingParameters("ProcessedDataFlag").equals(1.0)) {
			// processed data only
			groupDelayOfDigitallyFilteredData = 0;
		}
		//
		// digital filter for further calculation
		groupDelayOfDigitallyFilteredData = Math.round(groupDelayOfDigitallyFilteredData);
		scanNMR.putProcessingParameters("groupDelayOfDigitallyFilteredData", groupDelayOfDigitallyFilteredData);
		return groupDelayOfDigitallyFilteredData;
	}

	private static double[] exponentialApodizationFunction(double[] timeScale, IScanNMR scanNMR) {

		double exponentialLineBroadeningFactor = 0;
		if(scanNMR.processingParametersContainsKey("exponentialLineBroadeningFactor")) {
			exponentialLineBroadeningFactor = scanNMR.getProcessingParameters("exponentialLineBroadeningFactor");
		}
		double[] exponentialLineBroadening = new double[timeScale.length];
		double exponentialLineBroadenigTerm;
		if(exponentialLineBroadeningFactor > 0) {
			for(int i = 0; i < timeScale.length; i++) { // Lbfunc=exp(-Timescale'*pi*NmrData.lb);
				exponentialLineBroadenigTerm = (-timeScale[i] * Math.PI * exponentialLineBroadeningFactor);
				exponentialLineBroadening[i] = Math.exp(exponentialLineBroadenigTerm);
			}
		} else {
			for(int i = 0; i < timeScale.length; i++) {
				exponentialLineBroadening[i] = (timeScale[i] * 0 + 1);
			}
		}
		return exponentialLineBroadening;
	}

	private static double[] gaussianApodizationFunction(double[] timeScale, IScanNMR scanNMR) {

		double gaussianLineBroadeningFactor = 0;
		if(scanNMR.processingParametersContainsKey("gaussianLineBroadeningFactor")) {
			gaussianLineBroadeningFactor = scanNMR.getProcessingParameters("gaussianLineBroadeningFactor");
		}
		double[] gaussianLineBroadening = new double[timeScale.length];
		double gaussianLineBroadenigTermA;
		double gaussianLineBroadenigTermB;
		if(gaussianLineBroadeningFactor > 0) {
			// gf=2*sqrt(log(2))/(pi*NmrData.gw);
			// Gwfunc=exp(-(Timescale'/gf).^2);
			gaussianLineBroadenigTermA = (Math.PI * gaussianLineBroadeningFactor);
			double gaussFactor = 2 * Math.sqrt(Math.log(2)) / gaussianLineBroadenigTermA;
			for(int i = 0; i < timeScale.length; i++) {
				gaussianLineBroadenigTermB = -(timeScale[i] / gaussFactor);
				gaussianLineBroadening[i] = Math.exp(Math.pow(gaussianLineBroadenigTermB, 2));
			}
		} else {
			for(int i = 0; i < timeScale.length; i++) {
				gaussianLineBroadening[i] = (timeScale[i] * 0 + 1);
			}
		}
		return gaussianLineBroadening;
	}

	class ShiftNmrData {

		@SuppressWarnings("unused")
		private void leftShiftNMRData(int[] dataArray, int pointsToShift) {

			pointsToShift = pointsToShift % dataArray.length;
			while(pointsToShift-- > 0) {
				int tempArray = dataArray[0];
				for(int i = 1; i < dataArray.length; i++) {
					dataArray[i - 1] = dataArray[i];
				}
				dataArray[dataArray.length - 1] = tempArray;
			}
		}

		public int[] rightShiftNMRData(int[] dataArray, int pointsToShift) {

			for(int i = 0; i < pointsToShift; i++) {
				int tempArray = dataArray[dataArray.length - 1];
				for(int g = dataArray.length - 2; g > -1; g--) {
					dataArray[g + 1] = dataArray[g];
				}
				dataArray[0] = tempArray;
			}
			return dataArray;
		}

		private void leftShiftNMRComplexData(Complex[] dataArray, int pointsToShift) {

			pointsToShift = pointsToShift % dataArray.length;
			while(pointsToShift-- > 0) {
				Complex tempArray = dataArray[0];
				for(int i = 1; i < dataArray.length; i++) {
					dataArray[i - 1] = dataArray[i];
				}
				dataArray[dataArray.length - 1] = tempArray;
			}
		}

		public Complex[] rightShiftNMRComplexData(Complex[] dataArray, int pointsToShift) {

			for(int i = 0; i < pointsToShift; i++) {
				Complex tempArray = dataArray[dataArray.length - 1];
				for(int g = dataArray.length - 2; g > -1; g--) {
					dataArray[g + 1] = dataArray[g];
				}
				dataArray[0] = tempArray;
			}
			return dataArray;
		}
	}

	private static Complex[] directCurrentCorrectionFID(Complex[] complexSignals, IScanNMR scanNMR) {

		// following as used in GNAT
		int numberOfFourierPoints = scanNMR.getProcessingParameters("numberOfFourierPoints").intValue();
		int numberOfPoints = scanNMR.getProcessingParameters("numberOfPoints").intValue();
		int directCurrentPointsTerm = 5 * numberOfFourierPoints / 20;
		int directCurrentPoints = Math.round(directCurrentPointsTerm);
		// select direct current correction for FID
		int directCurrentFID = 0; // 0 = No, 1 = Yes
		//
		Complex[] freeInductionDecay = new Complex[complexSignals.length];
		Complex[] complexSignalsDCcopy = new Complex[complexSignals.length - directCurrentPoints];
		Complex complexSignalsDCcopyAverage = new Complex(0, 0);
		double[] complexSignalsReal = new double[complexSignals.length];
		double[] complexSignalsImag = new double[complexSignals.length];
		// for (int k = 1; k <= dataArrayDimension; k++) { // expand for nD dimensions
		if(numberOfFourierPoints >= numberOfPoints) {
			if(directCurrentFID > 0) {
				complexSignalsDCcopy = Arrays.copyOfRange(complexSignals, directCurrentPoints, complexSignals.length);
				for(int i = 0; i < complexSignalsDCcopy.length; i++) {
					complexSignalsReal[i] = (complexSignalsDCcopy[i].getReal());
					complexSignalsImag[i] = (complexSignalsDCcopy[i].getImaginary());
				}
				double realAverage = Arrays.stream(complexSignalsReal).average().getAsDouble();
				double imagAverage = Arrays.stream(complexSignalsImag).average().getAsDouble();
				complexSignalsDCcopyAverage = new Complex(realAverage, imagAverage);
				for(int i = 0; i < numberOfPoints; i++) {
					freeInductionDecay[i] = complexSignals[i].subtract(complexSignalsDCcopyAverage);
				}
			} else {
				freeInductionDecay = Arrays.copyOfRange(complexSignals, 0, numberOfPoints);
			}
		} else {
			if(directCurrentFID > 0) {
				complexSignalsDCcopy = Arrays.copyOfRange(complexSignals, directCurrentPoints, complexSignals.length);
				for(int i = 0; i < complexSignalsDCcopy.length; i++) {
					complexSignalsReal[i] = (complexSignalsDCcopy[i].getReal());
					complexSignalsImag[i] = (complexSignalsDCcopy[i].getImaginary());
				}
				double realAverage = Arrays.stream(complexSignalsReal).average().getAsDouble();
				double imagAverage = Arrays.stream(complexSignalsImag).average().getAsDouble();
				complexSignalsDCcopyAverage = new Complex(realAverage, imagAverage);
				for(int i = 0; i < numberOfFourierPoints; i++) {
					freeInductionDecay[i] = complexSignals[i].subtract(complexSignalsDCcopyAverage);
				}
			} else {
				freeInductionDecay = Arrays.copyOfRange(complexSignals, 0, numberOfFourierPoints);
			}
		}
		// }
		return freeInductionDecay;
	}

	private static Complex[] fourierTransformNmrData(Complex[] fid, ShiftNmrData dataShifter) {

		FastFourierTransformer fFourierTransformer = new FastFourierTransformer(DftNormalization.STANDARD);
		Complex[] nmrSpectrum = fFourierTransformer.transform(fid, TransformType.FORWARD);
		Complex[] nmrSpectrumProcessed = new Complex[nmrSpectrum.length];
		System.arraycopy(nmrSpectrum, 0, nmrSpectrumProcessed, 0, nmrSpectrum.length); // NmrData.SPECTRA
		dataShifter.rightShiftNMRComplexData(nmrSpectrumProcessed, nmrSpectrumProcessed.length / 2);
		ArrayUtils.reverse(nmrSpectrumProcessed);
		return nmrSpectrumProcessed;
	}

	private static Complex[] inverseFourierTransformData(Complex[] spectrum, ShiftNmrData datashifter) {

		ArrayUtils.reverse(spectrum);
		datashifter.rightShiftNMRComplexData(spectrum, spectrum.length / 2);
		FastFourierTransformer fFourierTransformer = new FastFourierTransformer(DftNormalization.STANDARD);
		Complex[] unfilteredFID = fFourierTransformer.transform(spectrum, TransformType.INVERSE);
		Complex[] analogFID = new Complex[unfilteredFID.length];
		System.arraycopy(unfilteredFID, 0, analogFID, 0, unfilteredFID.length);
		return analogFID;
	}
}
