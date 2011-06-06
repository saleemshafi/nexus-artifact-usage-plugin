package org.ebayopensource.nexus.plugins.artifactusage.task;

import java.util.ArrayList;
import java.util.Collection;

import org.ebayopensource.nexus.plugins.artifactusage.store.Artifact;

/**
 * The results of calculating the artifact usage of a particular repository.
 * 
 * @author Saleem Shafi
 */
public class ArtifactUsageCalculationRepositoryResult {
	private Collection<Artifact> updatedArtifacts = new ArrayList<Artifact>();
	private String repositoryId;
	private boolean successful;

	public ArtifactUsageCalculationRepositoryResult(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	public String getRepositoryId() {
		return repositoryId;
	}

	public void addUpdatedArtifact(Artifact artifact) {
		this.updatedArtifacts.add(artifact);
	}

	public Collection<Artifact> getUpdatedArtifacts() {
		return this.updatedArtifacts;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}
}
