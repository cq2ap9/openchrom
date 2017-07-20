/*******************************************************************************
 * Copyright (c) 2017 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package net.openchrom.xxd.processor.supplier.tracecompare.model;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "TraceCompare")
public class ProcessorModel {

	@XmlElement(name = "Version")
	private String version = "1.0.0.0";
	//
	@XmlElement(name = "DetectorType")
	private String detectorType = "";
	@XmlElement(name = "ImageDirectory")
	private String imageDirectory = "";
	@XmlElement(name = "SampleGroup")
	private String sampleGroup = "";
	@XmlElement(name = "SamplePath")
	private String samplePath = "";
	@XmlElement(name = "ReferenceGroup")
	private String referenceGroup = "";
	@XmlElement(name = "ReferencePath")
	private String referencePath = "";
	@XmlElement(name = "CalculatedResult")
	private String calculatedResult = "";
	@XmlElement(name = "GeneralNotes")
	private String generalNotes = "";
	@XmlElement(name = "ScanVelocity")
	private int scanVelocity = 0;
	@XmlElement(name = "ReferenceModels", type = ReferenceModel.class)
	private Map<String, ReferenceModel> referenceModels = new HashMap<String, ReferenceModel>();

	@XmlTransient
	public String getVersion() {

		return version;
	}

	public void setVersion(String version) {

		this.version = version;
	}

	@XmlTransient
	public String getDetectorType() {

		return detectorType;
	}

	public void setDetectorType(String detectorType) {

		this.detectorType = detectorType;
	}

	@XmlTransient
	public String getImageDirectory() {

		return imageDirectory;
	}

	public void setImageDirectory(String imageDirectory) {

		this.imageDirectory = imageDirectory;
	}

	@XmlTransient
	public String getSampleGroup() {

		return sampleGroup;
	}

	public void setSampleGroup(String sampleGroup) {

		this.sampleGroup = sampleGroup;
	}

	@XmlTransient
	public String getSamplePath() {

		return samplePath;
	}

	public void setSamplePath(String samplePath) {

		this.samplePath = samplePath;
	}

	@XmlTransient
	public String getReferenceGroup() {

		return referenceGroup;
	}

	public void setReferenceGroup(String referenceGroup) {

		this.referenceGroup = referenceGroup;
	}

	@XmlTransient
	public String getReferencePath() {

		return referencePath;
	}

	public void setReferencePath(String referencePath) {

		this.referencePath = referencePath;
	}

	@XmlTransient
	public String getCalculatedResult() {

		return calculatedResult;
	}

	public void setCalculatedResult(String calculatedResult) {

		this.calculatedResult = calculatedResult;
	}

	@XmlTransient
	public String getGeneralNotes() {

		return generalNotes;
	}

	public void setGeneralNotes(String generalNotes) {

		this.generalNotes = generalNotes;
	}

	@XmlTransient
	public int getScanVelocity() {

		return scanVelocity;
	}

	public void setScanVelocity(int scanVelocity) {

		this.scanVelocity = scanVelocity;
	}

	@XmlTransient
	public Map<String, ReferenceModel> getReferenceModels() {

		return referenceModels;
	}

	public void setReferenceModels(Map<String, ReferenceModel> referenceModels) {

		this.referenceModels = referenceModels;
	}
}
