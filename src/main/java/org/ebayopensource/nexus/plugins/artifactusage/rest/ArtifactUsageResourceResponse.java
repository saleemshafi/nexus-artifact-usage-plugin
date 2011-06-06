package org.ebayopensource.nexus.plugins.artifactusage.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.ebayopensource.nexus.plugins.artifactusage.store.Artifact;
import org.sonatype.nexus.rest.model.NexusResponse;

@XmlRootElement(name = "artifactusageresponse")
public class ArtifactUsageResourceResponse extends NexusResponse implements
		Serializable {

	private static final long serialVersionUID = -4492092221347656388L;

	private List<Artifact> data = new ArrayList<Artifact>();

	@XmlElementWrapper(name = "data")
	@XmlElement(name = "node")
	public List<Artifact> getData() {
		return data;
	}

	public void addArtifact(Artifact artifactUser) {
		this.data.add(artifactUser);
	}
}
