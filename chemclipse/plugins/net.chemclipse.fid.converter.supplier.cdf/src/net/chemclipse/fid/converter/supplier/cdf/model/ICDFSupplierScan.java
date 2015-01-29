/*******************************************************************************
 * Copyright (c) 2014, 2015 Dr. Philip Wenig.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package net.chemclipse.fid.converter.supplier.cdf.model;

import net.chemclipse.fid.model.core.IScanFID;

public interface ICDFSupplierScan extends IScanFID {

	/**
	 * Stores the total signal.
	 * 
	 * @param totalSignal
	 */
	void setTotalSignal(float totalSignal);
}
