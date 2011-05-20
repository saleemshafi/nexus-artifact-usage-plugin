package com.paypal.nexus;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "node")
public class Dependee {
	public Dependee(String groupId, String artifactId, String version, String type) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.type = type;
		
		this.text = this.groupId + ":" + this.artifactId + ":" + this.version + "."
		+ this.type;
	}
	@Override
	public String toString() {
		return "Dependee [repository=" + repository + ", leaf=" + leaf
				+ ", groupId=" + groupId + ", resourceURI=" + resourceURI
				+ ", artifactId=" + artifactId + ", version=" + version
				+ ", type = " + type + ", classifier =" + classifier + "]";
	}

	private String resourceURI = "";

	private String groupId = "";

	private String artifactId = "";

	private boolean leaf;

	private String version = "";

	private String repository = "";

	private String classifier = "";

	private String text = "";
	
	private String type = "";

	public String getText() {
		return this.text;
	}

	public String getResourceURI() {
		return resourceURI;
	}

	public void setResourceURI(String baseUrl) {
		this.resourceURI = baseUrl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((resourceURI == null) ? 0 : resourceURI.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Dependee other = (Dependee) obj;
		if (resourceURI == null) {
			if (other.resourceURI != null) {
				return false;
			}
		} else if (!resourceURI.equals(other.resourceURI)) {
			return false;
		}
		return true;
	}

	public boolean isLeaf() {
		return leaf;
	}

	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getVersion() {
		return version;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getClassifier() {
		return classifier;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	public String getType() {
		return type;
	}

}