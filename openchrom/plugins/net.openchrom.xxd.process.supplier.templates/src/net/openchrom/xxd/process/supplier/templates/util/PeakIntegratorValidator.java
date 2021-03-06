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
package net.openchrom.xxd.process.supplier.templates.util;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;

import net.openchrom.xxd.process.supplier.templates.model.IntegratorSetting;

public class PeakIntegratorValidator extends AbstractTemplateValidator implements ITemplateValidator {

	private static final String ERROR_ENTRY = "Please enter an item, e.g.: '" + PeakIntegratorListUtil.EXAMPLE_SINGLE + "'";
	private static final String SEPARATOR_TOKEN = PeakIntegratorListUtil.SEPARATOR_TOKEN;
	private static final String SEPARATOR_ENTRY = PeakIntegratorListUtil.SEPARATOR_ENTRY;
	private static final String ERROR_TOKEN = "The item must not contain: " + SEPARATOR_TOKEN;
	//
	private String identifier = "";
	private double startRetentionTimeMinutes = 0;
	private double stopRetentionTimeMinutes = 0;
	private String integrator = "";

	@Override
	public IStatus validate(Object value) {

		String message = null;
		if(value == null) {
			message = ERROR_ENTRY;
		} else {
			if(value instanceof String) {
				String text = ((String)value).trim();
				if(text.contains(SEPARATOR_TOKEN)) {
					message = ERROR_TOKEN;
				} else if("".equals(text.trim())) {
					message = ERROR_ENTRY;
				} else {
					/*
					 * Extract retention time, ...
					 */
					String[] values = text.trim().split("\\" + SEPARATOR_ENTRY); // The pipe needs to be escaped.
					if(values.length >= 4) {
						/*
						 * Evaluation
						 */
						startRetentionTimeMinutes = parseDouble(values, 0);
						if(startRetentionTimeMinutes < 0.0d) {
							message = "The start retention time must be not lower than 0.";
						}
						stopRetentionTimeMinutes = parseDouble(values, 1);
						identifier = parseString(values, 2);
						integrator = parseString(values, 3);
						if("".equals(integrator)) {
							message = "An integrator needs to be set.";
						} else {
							if(!(integrator.equals(IntegratorSetting.INTEGRATOR_NAME_TRAPEZOID) || integrator.equals(IntegratorSetting.INTEGRATOR_NAME_MAX))) {
								message = "The integrator must be either '" + IntegratorSetting.INTEGRATOR_NAME_TRAPEZOID + "' or '" + IntegratorSetting.INTEGRATOR_NAME_MAX + "'";
							}
						}
						/*
						 * Extended Check
						 */
						if(startRetentionTimeMinutes == 0.0d && stopRetentionTimeMinutes == 0.0d) {
							if("".equals(identifier)) {
								message = "A substance name needs to be set.";
							}
						} else {
							if(stopRetentionTimeMinutes <= startRetentionTimeMinutes) {
								message = "The stop retention time must be greater then the start retention time.";
							}
						}
					} else {
						message = ERROR_ENTRY;
					}
				}
			} else {
				message = ERROR_ENTRY;
			}
		}
		//
		if(message != null) {
			return ValidationStatus.error(message);
		} else {
			return ValidationStatus.ok();
		}
	}

	public IntegratorSetting getSetting() {

		IntegratorSetting setting = new IntegratorSetting();
		setting.setStartRetentionTimeMinutes(startRetentionTimeMinutes);
		setting.setStopRetentionTimeMinutes(stopRetentionTimeMinutes);
		setting.setIdentifier(identifier);
		setting.setIntegrator(integrator);
		return setting;
	}
}
