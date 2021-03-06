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
package net.sf.jfasta.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.jfasta.HeaderDialect;
import net.sf.kerner.utils.UtilString;

import org.apache.log4j.Logger;

/**
 *
 * TODO description
 * </p>
 *
 * <a href="http://www.uniprot.org/help/fasta-headers">See definition at
 * uniprot.org</a>
 *
 *
 * <p>
 * last reviewed: 0000-00-00
 * </p>
 *
 * @author <a href="mailto:alexanderkerner24@gmail.com">Alexander Kerner</a>
 *
 */
public class HeaderDialectUniprot implements HeaderDialect {

	private final static Logger logger = Logger.getLogger(HeaderDialectUniprot.class);
	//
	public final static String REGEX_OS_ONLY = ".*OS=(.+).*";
	public final static String REGEX_OS_GN = ".*OS=(.+)GN=(.+).*";
	public final static String REGEX_ACC_ONLY = "[sp\\|]*(.+)\\|.*";
	public final static String REGEX_DB_ONLY = ".*?(.*)\\|.*\\|.*";
	public final static String REGEX_PROTEIN_ONLY = ".+?[\\s*](.+)\\s*(OS=.*)";
	public final static String REGEX_ACC_NAMES_OS_GN = "([^\\s]+)(.+)OS=(.+)GN=(.+).*";
	public final static String REGEX_ACC_NAMES_OS_ONLY = "([^\\s]+)(.+)OS=(.+).*";
	public final static String REGEX_GN = ".*GN=([^\\s]+).*";

	public static String getString(final String headerString, final String regex) {

		final Pattern p;
		p = Pattern.compile(regex);
		final Matcher m = p.matcher(headerString);
		final boolean b = m.matches();
		if(b) {
			return m.group(1).trim();
		} else {
			logger.info("no accession for " + headerString);
			return null;
		}
	}

	private String headerString;

	public HeaderDialectUniprot() {
	}

	public HeaderDialectUniprot(final String header) {
		setHeaderString(header);
	}

	public synchronized String getAccessionNumber() {

		final String result = getString(REGEX_ACC_ONLY);
		if(UtilString.emptyString(result)) {
			logger.info("no accession number for " + headerString);
		}
		return result;
	}

	public synchronized String getDBIdentifier() {

		return getString(REGEX_DB_ONLY);
	}

	public synchronized String getGeneName() {

		final String result = getString(REGEX_GN);
		if(UtilString.emptyString(result)) {
			logger.info("no gene name for " + headerString);
		}
		return result;
	}

	public synchronized String getHeaderString() {

		return headerString;
	}

	public synchronized String getProteinName() {

		final String result = getString(REGEX_PROTEIN_ONLY);
		if(UtilString.emptyString(result)) {
			logger.info("no protein name for " + headerString);
		}
		return result;
	}

	public String getSpeciesName() {

		final Pattern p;
		if(headerString.contains("OS=") && headerString.contains("GN=")) {
			p = Pattern.compile(REGEX_ACC_NAMES_OS_GN);
			final Matcher m = p.matcher(getHeaderString());
			final boolean b = m.matches();
			if(b) {
				return m.group(3).trim();
			}
		} else if(headerString.contains("OS=")) {
			p = Pattern.compile(REGEX_ACC_NAMES_OS_ONLY);
			final Matcher m = p.matcher(getHeaderString());
			final boolean b = m.matches();
			if(b) {
				return m.group(1).trim();
			}
		}
		logger.info("no species name for " + headerString);
		return null;
	}

	public synchronized String getString(final String regex) {

		final Pattern p;
		p = Pattern.compile(regex);
		final Matcher m = p.matcher(getHeaderString());
		final boolean b = m.matches();
		if(b) {
			return m.group(1).trim();
		} else {
			logger.debug("no match (" + regex + ") for " + getHeaderString());
			return null;
		}
	}

	public synchronized void setHeaderString(final String headerString) {

		if(headerString.startsWith(">")) {
			this.headerString = headerString.substring(1, headerString.length());
		} else {
			this.headerString = headerString;
		}
	}
}
