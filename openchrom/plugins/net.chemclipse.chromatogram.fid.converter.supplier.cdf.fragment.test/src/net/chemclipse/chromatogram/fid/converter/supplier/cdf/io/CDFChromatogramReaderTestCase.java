/*******************************************************************************
 * Copyright (c) 2014 Dr. Philip Wenig.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package net.chemclipse.chromatogram.fid.converter.supplier.cdf.io;

import java.io.File;

import org.eclipse.core.runtime.NullProgressMonitor;

import net.chemclipse.chromatogram.fid.converter.chromatogram.ChromatogramConverterFID;
import net.chemclipse.chromatogram.fid.converter.processing.chromatogram.IChromatogramFIDImportConverterProcessingInfo;
import net.chemclipse.chromatogram.fid.model.core.IChromatogramFID;

import junit.framework.TestCase;

/**
 * This class initializes an AgilentChromatogramReaderTest.
 * 
 * @author eselmeister
 */
public class CDFChromatogramReaderTestCase extends TestCase {

	protected IChromatogramFID chromatogram;
	protected String pathImport;
	protected File fileImport;
	private final static String EXTENSION_POINT_ID = "net.chemclipse.chromatogram.fid.converter.supplier.cdf";

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		fileImport = new File(this.pathImport);
		IChromatogramFIDImportConverterProcessingInfo processingInfo = ChromatogramConverterFID.convert(fileImport, EXTENSION_POINT_ID, new NullProgressMonitor());
		chromatogram = processingInfo.getChromatogram();
	}

	@Override
	protected void tearDown() throws Exception {

		pathImport = null;
		fileImport = null;
		chromatogram = null;
		//
		System.gc();
		//
		super.tearDown();
	}
}
