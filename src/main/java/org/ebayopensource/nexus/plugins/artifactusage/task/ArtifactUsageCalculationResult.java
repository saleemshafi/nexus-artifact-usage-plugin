package org.ebayopensource.nexus.plugins.artifactusage.task;

import java.util.ArrayList;
import java.util.Collection;

import org.ebayopensource.nexus.plugins.artifactusage.store.GAV;

/**
 * The result of calculating the usage of a number of artifacts.
 * 
 * @author Saleem Shafi
 */
public class ArtifactUsageCalculationResult {
	private boolean successful = true;
	private Collection<GAV> updatedArtifacts = new ArrayList<GAV>();

	public Collection<GAV> getUpdatedArtifacts() {
		return this.updatedArtifacts;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void addResult(ArtifactUsageCalculationRepositoryResult result) {
		this.updatedArtifacts.addAll(result.getUpdatedArtifacts());
		this.successful &= result.isSuccessful();
	}

}
