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
package net.sf.kerner.utils.async;

public abstract class AbstractAsyncTaskRunnable<R, V> extends AbstractAsyncTask<R, V> implements Runnable {

	protected V value;

	public synchronized V getValue() {

		return value;
	}

	/**
	 * Don't override. Override {@link #run(Object)}
	 */
	public void run() {

		execute(getValue());
	}

	public synchronized void setValue(final V value) {

		this.value = value;
	}
}
