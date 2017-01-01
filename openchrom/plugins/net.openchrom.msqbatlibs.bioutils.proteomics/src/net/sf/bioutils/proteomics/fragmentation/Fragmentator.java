/*******************************************************************************
 * Copyright (c) 2015, 2017 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dr. Alexander Kerner - initial API and implementation
 *******************************************************************************/
package net.sf.bioutils.proteomics.fragmentation;

import java.util.List;

import net.sf.bioutils.proteomics.peptides.Peptide;

public interface Fragmentator {

	List<Peptide> fractionate(final Peptide peptide);
}
