/*******************************************************************************
 * Copyright (c) 2019 Lablicate GmbH.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package net.openchrom.xxd.classifier.supplier.ratios.ui.internal.provider.time;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IInputValidator;

import net.openchrom.xxd.classifier.supplier.ratios.model.TimeRatio;
import net.openchrom.xxd.classifier.supplier.ratios.util.TimeRatioValidator;

public class TimeRatioInputValidator implements IInputValidator {

	private TimeRatioValidator validator = new TimeRatioValidator();
	private List<TimeRatio> settings = new ArrayList<>();

	public TimeRatioInputValidator(List<TimeRatio> settings) {
		if(settings != null) {
			this.settings = settings;
		}
	}

	@Override
	public String isValid(String target) {

		IStatus status = validator.validate(target);
		if(status.isOK()) {
			TimeRatio setting = validator.getSetting();
			if(settings.contains(setting)) {
				return "The element already exists.";
			}
		} else {
			return status.getMessage();
		}
		return null;
	}
}
