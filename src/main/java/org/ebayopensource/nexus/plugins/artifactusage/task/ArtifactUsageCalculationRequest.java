package org.ebayopensource.nexus.plugins.artifactusage.task;

/**
 * Wrapper class for the arguments needed to process a repository's artifacts
 * for usage data.
 * 
 * @author Saleem Shafi
 */
public class ArtifactUsageCalculationRequest {
	private final String repositoryId;
	private final String repositoryGroupId;

	// Although the repositoryGroup stuff seems to be deprecated, it's still
	// being used throughout the core codebase, so i'm going to leave this
	// hear until i understand what the plan is.
	public ArtifactUsageCalculationRequest(String repositoryId,
			String repositoryGroupId) {
		this.repositoryId = repositoryId;
		this.repositoryGroupId = repositoryGroupId;
	}

	public String getRepositoryId() {
		return repositoryId;
	}

	public String getRepositoryGroupId() {
		return repositoryGroupId;
	}

}
