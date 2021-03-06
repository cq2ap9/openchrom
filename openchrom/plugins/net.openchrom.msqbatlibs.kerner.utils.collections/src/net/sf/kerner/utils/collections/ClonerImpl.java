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
package net.sf.kerner.utils.collections;

import java.util.List;

import net.sf.kerner.utils.Cloneable;
import net.sf.kerner.utils.Cloner;
import net.sf.kerner.utils.collections.list.AbstractTransformingListFactory;

public class ClonerImpl<T extends Cloneable<T>> extends AbstractTransformingListFactory<T, T> implements Cloner<T>, ClonerList<T> {

	public T clone(final T element) {

		return element.clone();
	}

	public List<T> cloneList(final List<? extends T> elements) {

		return transformCollection(elements);
	}

	public T transform(final T element) {

		return clone(element);
	}
}
