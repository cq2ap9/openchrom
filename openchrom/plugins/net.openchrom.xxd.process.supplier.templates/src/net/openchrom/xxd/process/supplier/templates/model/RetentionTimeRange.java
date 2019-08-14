/*******************************************************************************
 * Copyright (c) 2019 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package net.openchrom.xxd.process.supplier.templates.model;

public class RetentionTimeRange {

	private int startRetentionTime = 0;
	private int stopRetentionTime = 0;

	public RetentionTimeRange(int startRetentionTime, int stopRetentionTime) {
		this.startRetentionTime = startRetentionTime;
		this.stopRetentionTime = stopRetentionTime;
	}

	public int getStartRetentionTime() {

		return startRetentionTime;
	}

	public int getStopRetentionTime() {

		return stopRetentionTime;
	}
}