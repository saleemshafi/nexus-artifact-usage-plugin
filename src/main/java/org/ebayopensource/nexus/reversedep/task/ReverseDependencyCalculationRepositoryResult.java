package org.ebayopensource.nexus.reversedep.task;

import java.util.ArrayList;
import java.util.Collection;

import org.ebayopensource.nexus.reversedep.store.Artifact;


/**
 * The results of calculating the reverse dependencies for a particular repository.
 * 
 * Note: If the repositoryGroup stuff officially goes away, this object can replace
 * the ReverseDependencyCalculationResult class, since we'll only ever be dealing
 * with one repo at a time.
 * 
 * @author Saleem Shafi
 */
public class ReverseDependencyCalculationRepositoryResult {
	private Collection<Artifact> updatedArtifacts = new ArrayList<Artifact>();
	private String repositoryId;
	private boolean successful;
	
	public ReverseDependencyCalculationRepositoryResult(String repositoryId) {
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
