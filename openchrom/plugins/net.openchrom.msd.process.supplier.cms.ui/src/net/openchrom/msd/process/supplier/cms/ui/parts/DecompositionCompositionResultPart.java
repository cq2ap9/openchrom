/*******************************************************************************
 * Copyright (c) 2017 whitlow.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * whitlow - initial API and implementation
 *******************************************************************************/
package net.openchrom.msd.process.supplier.cms.ui.parts;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import net.openchrom.msd.process.supplier.cms.ui.parts.swt.DecompositionCompositionResultUI;

public class DecompositionCompositionResultPart {

	@Inject
	private Composite parent;
	private DecompositionCompositionResultUI decompositionCompositionResultUI;

	@PostConstruct
	private void createControl() {

		parent.setLayout(new FillLayout());
		decompositionCompositionResultUI = new DecompositionCompositionResultUI(parent, SWT.NONE);
	}

	@PreDestroy
	private void preDestroy() {

	}

	@Focus
	public void setFocus() {

		decompositionCompositionResultUI.setFocus();
	}
}
