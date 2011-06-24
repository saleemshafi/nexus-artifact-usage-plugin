package org.ebayopensource.nexus.plugins.artifactusage.store;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ArtifactUser {
	private final GAV artifact;
	private final Set<ArtifactUser> artifactUsers = new HashSet<ArtifactUser>();

	public ArtifactUser(GAV artifact) {
		this.artifact = artifact;
	}

	public Collection<ArtifactUser> getArtifactUsers() {
		return this.artifactUsers;
	}

	public void addArtifactUser(ArtifactUser artifactUser) {
		this.artifactUsers.add(artifactUser);
	}

	public void removeArtifactUser(ArtifactUser artifactUser) {
		this.artifactUsers.remove(artifactUser);
	}

	@Override
	public int hashCode() {
		return artifact.hashCode();
	}

	public GAV getGav() {
		return this.artifact;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ArtifactUser) {
			return this.artifact.equals(((ArtifactUser) obj).artifact);
		}
		return false;
	}

}
