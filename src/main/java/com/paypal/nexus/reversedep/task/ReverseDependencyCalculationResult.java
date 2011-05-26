package com.paypal.nexus.reversedep.task;

import java.util.ArrayList;
import java.util.Collection;

import com.paypal.nexus.reversedep.store.Artifact;

/**
 * The result of calculating the reverse dependencies for the artifacts in
 * repository.
 * 
 * @author Saleem Shafi
 */
public class ReverseDependencyCalculationResult {
	private boolean successful = true;
	private Collection<Artifact> updatedArtifacts = new ArrayList<Artifact>();

	public Collection<Artifact> getUpdatedArtifacts() {
		return this.updatedArtifacts;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void addResult(ReverseDependencyCalculationRepositoryResult result) {
		this.updatedArtifacts.addAll(result.getUpdatedArtifacts());
		this.successful &= result.isSuccessful();
	}

}
