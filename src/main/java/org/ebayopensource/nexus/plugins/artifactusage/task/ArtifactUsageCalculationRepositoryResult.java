package org.ebayopensource.nexus.plugins.artifactusage.task;

import java.util.ArrayList;
import java.util.Collection;

import org.ebayopensource.nexus.plugins.artifactusage.store.GAV;

/**
 * The results of calculating the artifact usage of a particular repository.
 * 
 * @author Saleem Shafi
 */
public class ArtifactUsageCalculationRepositoryResult {
	private Collection<GAV> updatedArtifacts = new ArrayList<GAV>();
	private String repositoryId;
	private boolean successful;

	public ArtifactUsageCalculationRepositoryResult(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	public String getRepositoryId() {
		return repositoryId;
	}

	public void addUpdatedArtifact(GAV artifact) {
		this.updatedArtifacts.add(artifact);
	}

	public Collection<GAV> getUpdatedArtifacts() {
		return this.updatedArtifacts;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}
}
