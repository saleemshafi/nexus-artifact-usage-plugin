package org.ebayopensource.nexus.plugins.artifactusage.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "node")
public class ArtifactUser extends Artifact {
	private List<ArtifactUser> artifactUsers = new ArrayList<ArtifactUser>();

	public ArtifactUser(String groupId, String artifactId, String version,
			String path) {
		super(groupId, artifactId, version, path);
	}

	@XmlElementWrapper(name = "data")
	@XmlElement(name = "node")
	public Collection<ArtifactUser> getArtifactUsers() {
		return this.artifactUsers;
	}

	public void addArtifactUser(ArtifactUser artifactUser) {
		this.artifactUsers.add(artifactUser);
	}
}
