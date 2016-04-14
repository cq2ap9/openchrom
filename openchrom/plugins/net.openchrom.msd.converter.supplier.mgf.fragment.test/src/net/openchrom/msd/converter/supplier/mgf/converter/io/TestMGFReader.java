/*******************************************************************************
 * Copyright (c) 2015 Lablicate UG (haftungsbeschränkt).
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dr. Alexander Kerner - initial API and implementation
 *******************************************************************************/
package net.openchrom.msd.converter.supplier.mgf.converter.io;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sf.jmgf.MGFFile;
import net.sf.jmgf.MGFFileReader;
import net.sf.jmgf.impl.MGFFileReaderImpl;
import net.sf.kerner.utils.io.CloserProperly;

public class TestMGFReader {

	private final static Logger logger = Logger.getLogger(TestMGFReader.class);
	private MGFFileReader reader;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

	}

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {

		new CloserProperly().closeProperly(reader);
	}

	@Test
	public final void testMS101() throws IOException {

		reader = new MGFFileReaderImpl(new File("testData/files/import/janko/ppw_L10_142651860914.txt"));
		MGFFile result = reader.read();
		// one MS1 element, four MS2 elements
		assertEquals(5, result.getElements().size());
		assertEquals(1, result.getElements().get(0).getMSLevel());
		for(int i = 1; i < result.getElements().size(); i++) {
			assertEquals(2, result.getElements().get(i).getMSLevel());
		}
	}
}
