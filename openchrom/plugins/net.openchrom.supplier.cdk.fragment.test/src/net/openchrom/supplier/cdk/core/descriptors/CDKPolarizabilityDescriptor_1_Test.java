/*******************************************************************************
 * Copyright (c) 2013 Marwin Wollschläger, Dr. Philip Wenig.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Marwin Wollschläger - initial API and implementation
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package net.openchrom.supplier.cdk.core.descriptors;

import java.io.IOException;

import org.openscience.cdk.charges.MMFF94PartialCharges;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.modeling.builder3d.ModelBuilder3D;

import junit.framework.TestCase;

import net.openchrom.supplier.cdk.core.CDKSmilesToIMoleculeConverter;
import net.openchrom.supplier.cdk.core.OPSINIupacToIMoleculeConverter;
import net.openchrom.supplier.cdk.core.descriptors.CDKPolarizabilityDescriptor;

public class CDKPolarizabilityDescriptor_1_Test extends TestCase {

	private CDKPolarizabilityDescriptor descriptor;
	private CDKSmilesToIMoleculeConverter smilesToIMoleculeConverter;
	private OPSINIupacToIMoleculeConverter iupacToIMoleculeConverter;

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		descriptor = new CDKPolarizabilityDescriptor();
		smilesToIMoleculeConverter = new CDKSmilesToIMoleculeConverter();
		iupacToIMoleculeConverter = new OPSINIupacToIMoleculeConverter();
	}

	@Override
	protected void tearDown() throws Exception {

		descriptor = null;
		smilesToIMoleculeConverter = null;
		iupacToIMoleculeConverter = null;
		super.tearDown();
	}

	public void testDescribeMethod_1() {

		IMolecule benz = new CDKSmilesToIMoleculeConverter().generate("c1=cc=cc=c1");
		assertEquals("10.542000000000003", descriptor.describe(benz));
	}

	public void testDescribeMethod_2() {

		IMolecule ethane = smilesToIMoleculeConverter.generate("cccccc");
		assertEquals("9.480000000000004", descriptor.describe(ethane));
	}

	public void testDescribeMethod_3() {

		IMolecule triFluoroEthane = smilesToIMoleculeConverter.generate("c[F]c");
		assertEquals("2.4240000000000004", descriptor.describe(triFluoroEthane));
	}

	public void testDescribeMethod_4() {

		IMolecule bromFluorEthane = iupacToIMoleculeConverter.generate("1,1,2-Tribromo-1,2,2-trifluoroethane");
		assertEquals("13.516", descriptor.describe(bromFluorEthane));
	}

	public void testDescribeMethod_5() {

		IMolecule ethanol = iupacToIMoleculeConverter.generate("2-chloro-3-fluoro(biphenyl)");
		assertEquals("22.223000000000006", descriptor.describe(ethanol));
	}

	public void testDescribeMethod_6() {

		IMolecule ethanol = iupacToIMoleculeConverter.generate("2-chloro-3-fluoro(biphenyl)");
		MMFF94PartialCharges charges = new MMFF94PartialCharges();
		try {
			ModelBuilder3D mb3d = ModelBuilder3D.getInstance();
			ethanol = mb3d.generate3DCoordinates(ethanol, false);
			charges.calculateCharges(ethanol);
			ethanol = (IMolecule)charges.assignMMFF94PartialCharges(ethanol);
			assertNotNull(ethanol);
		} catch(CDKException e1) {
			assertTrue(false);
		} catch(IOException e2) {
			assertTrue(false);
		} catch(CloneNotSupportedException e3) {
			assertTrue(false);
		} catch(Exception e4) {
			assertTrue(false);
		}
	}
}
