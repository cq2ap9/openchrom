/*******************************************************************************
 * Copyright (c) 2015, 2018 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dr. Alexander Kerner - initial API and implementation
 *******************************************************************************/
package net.sf.bioutils.proteomics;

import net.sf.kerner.utils.collections.Selector;

public interface ComposableElementCollapsed<T> extends ComposableElement<T> {

	T getMaster();

	Selector<T> getSelector();

	void setSelector(Selector<T> selector);
}
