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
package net.openchrom.xxd.process.supplier.templates.peaks;

import java.util.List;

import org.eclipse.chemclipse.chromatogram.msd.peak.detector.core.IPeakDetectorMSD;
import org.eclipse.chemclipse.chromatogram.msd.peak.detector.settings.IPeakDetectorMSDSettings;
import org.eclipse.chemclipse.chromatogram.peak.detector.core.AbstractPeakDetector;
import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.model.core.AbstractChromatogram;
import org.eclipse.chemclipse.model.exceptions.PeakException;
import org.eclipse.chemclipse.model.support.IScanRange;
import org.eclipse.chemclipse.model.support.ScanRange;
import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.msd.model.core.IChromatogramPeakMSD;
import org.eclipse.chemclipse.msd.model.core.selection.IChromatogramSelectionMSD;
import org.eclipse.chemclipse.msd.model.core.support.PeakBuilderMSD;
import org.eclipse.chemclipse.processing.core.IProcessingInfo;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import net.openchrom.xxd.process.supplier.templates.preferences.PreferenceSupplier;
import net.openchrom.xxd.process.supplier.templates.settings.PeakDetectorSettingsMSD;
import net.openchrom.xxd.process.supplier.templates.util.PeakDetectorListUtil;
import net.openchrom.xxd.process.supplier.templates.util.PeakDetectorValidator;

public class PeakDetectorMSD extends AbstractPeakDetector implements IPeakDetectorMSD {

	private static final Logger logger = Logger.getLogger(PeakDetectorMSD.class);
	//
	private static final String DETECTOR_DESCRIPTION = "Template Peak Detector";

	@Override
	public IProcessingInfo detect(IChromatogramSelectionMSD chromatogramSelection, IPeakDetectorMSDSettings settings, IProgressMonitor monitor) {

		IProcessingInfo processingInfo = super.validate(chromatogramSelection, settings, monitor);
		if(!processingInfo.hasErrorMessages()) {
			if(settings instanceof PeakDetectorSettingsMSD) {
				IChromatogramMSD chromatogram = chromatogramSelection.getChromatogramMSD();
				PeakDetectorSettingsMSD settingsMSD = (PeakDetectorSettingsMSD)settings;
				for(DetectorSettings detectorSettings : settingsMSD.getDetectorSettings()) {
					setPeakBySettings(chromatogram, detectorSettings);
				}
			} else {
				processingInfo.addErrorMessage(DETECTOR_DESCRIPTION, "The settings instance is wrong.");
			}
		}
		return processingInfo;
	}

	@Override
	public IProcessingInfo detect(IChromatogramSelectionMSD chromatogramSelection, IProgressMonitor monitor) {

		PeakDetectorSettingsMSD settings = new PeakDetectorSettingsMSD();
		PeakDetectorListUtil util = new PeakDetectorListUtil();
		PeakDetectorValidator validator = new PeakDetectorValidator();
		//
		List<String> ranges = util.getList(PreferenceSupplier.INSTANCE().getPreferences().get(PreferenceSupplier.P_PEAK_DETECTOR_LIST_MSD, ""));
		for(String range : ranges) {
			IStatus status = validator.validate(range);
			if(status.isOK()) {
				settings.getDetectorSettings().add(validator.getDetectorSettings());
			}
		}
		//
		return detect(chromatogramSelection, settings, monitor);
	}

	private void setPeakBySettings(IChromatogramMSD chromatogram, DetectorSettings detectorSettings) {

		int start = (int)(detectorSettings.getStartRetentionTime() * AbstractChromatogram.MINUTE_CORRELATION_FACTOR);
		int stop = (int)(detectorSettings.getStopRetentionTime() * AbstractChromatogram.MINUTE_CORRELATION_FACTOR);
		setPeakByRetentionTimeRange(chromatogram, start, stop, detectorSettings.isIncludeBackground());
	}

	private void setPeakByRetentionTimeRange(IChromatogramMSD chromatogram, int startRetentionTime, int stopRetentionTime, boolean includeBackground) {

		int startScan = chromatogram.getScanNumber(startRetentionTime);
		int stopScan = chromatogram.getScanNumber(stopRetentionTime);
		setPeakByScanRange(chromatogram, startScan, stopScan, includeBackground);
	}

	private void setPeakByScanRange(IChromatogramMSD chromatogram, int startScan, int stopScan, boolean includeBackground) {

		try {
			if(startScan > 0 && startScan < stopScan) {
				IScanRange scanRange = new ScanRange(startScan, stopScan);
				IChromatogramPeakMSD peak = PeakBuilderMSD.createPeak(chromatogram, scanRange, includeBackground);
				peak.setDetectorDescription(DETECTOR_DESCRIPTION);
				chromatogram.addPeak(peak);
			}
		} catch(PeakException e) {
			logger.warn(e);
		}
	}
}
