package org.ebayopensource.nexus.plugins.artifactusage.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.ebayopensource.nexus.plugins.artifactusage.store.ArtifactUser;
import org.sonatype.nexus.rest.model.NexusResponse;

@XmlRootElement(name = "artifactusageeresponse")
public class ArtifactUsageGraphResourceResponse extends NexusResponse implements
		Serializable {

	private static final long serialVersionUID = -4492092221347656388L;

	private List<ArtifactUser> data = new ArrayList<ArtifactUser>();

	@XmlElementWrapper(name = "data")
	@XmlElement(name = "node")
	public List<ArtifactUser> getData() {
		return data;
	}

	public void addArtifactUser(ArtifactUser artifactUser) {
		this.data.add(artifactUser);
	}
}
