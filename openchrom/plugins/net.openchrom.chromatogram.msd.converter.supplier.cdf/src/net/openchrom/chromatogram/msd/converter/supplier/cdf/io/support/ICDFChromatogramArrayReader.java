/*******************************************************************************
 * Copyright (c) 2008, 2013 Philip (eselmeister) Wenig.
 * 
 * This library is free
 * software; you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307, USA
 * 
 * 
 * Contributors: Philip (eselmeister) Wenig - initial API and implementation
 *******************************************************************************/
package net.openchrom.chromatogram.msd.converter.supplier.cdf.io.support;

import net.openchrom.chromatogram.msd.converter.supplier.cdf.exceptions.NoSuchScanStored;
import net.openchrom.chromatogram.msd.converter.supplier.cdf.model.CDFMassSpectrum;

public interface ICDFChromatogramArrayReader extends IAbstractCDFChromatogramArrayReader {

	/**
	 * Returns a valid mass spectrum of the given scan.
	 * 
	 * @param scan
	 * @return CDFMassSpectrum
	 * @throws NoSuchScanStored
	 */
	public CDFMassSpectrum getMassSpectrum(int scan) throws NoSuchScanStored;
}
