/*******************************************************************************
 * Copyright (c) 2013 Marwin Wollschläger.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Marwin Wollschläger - initial API and implementation
 *******************************************************************************/
package net.openchrom.supplier.cdk.ui.internal.provider;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class FormulaListTableSorter extends ViewerSorter {

	private int propertyIndex;
	private static final int ASCENDING = 0;
	// private static final int DESCENDING = -1;
	private int direction = ASCENDING;

	public FormulaListTableSorter() {

		propertyIndex = 0;
		direction = ASCENDING;
	}

	public void setColumn(int column) {

		if(column == this.propertyIndex) {
			// Toggle the direction
			direction = 1 - direction;
		} else {
			this.propertyIndex = column;
			direction = ASCENDING;
		}
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {

		/*
		 * SYNCHRONIZE: PeakListLabelProvider PeakListLabelSorter PeakListView
		 */
		int sortOrder = 0;
		if(e1 instanceof String && e2 instanceof String) {
			String element1 = (String)e1;
			String element2 = (String)e2;
			switch(propertyIndex) {
				case 0: // RT
					sortOrder = element2.compareTo(element1);
					break;
				case 1: // Area
					sortOrder = -1;
					break;
				default:
					sortOrder = 0;
			}
		}
		if(direction == ASCENDING) {
			sortOrder = -sortOrder;
		}
		return sortOrder;
	}
}
