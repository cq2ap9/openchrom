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
package net.openchrom.xxd.process.supplier.templates.ui.internal.provider;

import org.eclipse.chemclipse.support.ui.swt.AbstractRecordTableComparator;
import org.eclipse.chemclipse.support.ui.swt.IRecordTableComparator;
import org.eclipse.jface.viewers.Viewer;

import net.openchrom.xxd.process.supplier.templates.model.AssignerReference;

public class StandardsReferencerComparator extends AbstractRecordTableComparator implements IRecordTableComparator {

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {

		int sortOrder = 0;
		if(e1 instanceof AssignerReference && e2 instanceof AssignerReference) {
			//
			AssignerReference setting1 = (AssignerReference)e1;
			AssignerReference setting2 = (AssignerReference)e2;
			//
			switch(getPropertyIndex()) {
				case 0:
					sortOrder = setting2.getName().compareTo(setting1.getName());
					break;
				case 1:
					sortOrder = Double.compare(setting2.getStartRetentionTime(), setting1.getStartRetentionTime());
					break;
				case 2:
					sortOrder = Double.compare(setting2.getStopRetentionTime(), setting1.getStopRetentionTime());
					break;
				default:
					sortOrder = 0;
			}
		}
		if(getDirection() == ASCENDING) {
			sortOrder = -sortOrder;
		}
		return sortOrder;
	}
}
