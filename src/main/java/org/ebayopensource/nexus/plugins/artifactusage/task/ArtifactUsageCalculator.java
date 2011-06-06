package org.ebayopensource.nexus.plugins.artifactusage.task;

import java.io.IOException;

import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.item.StorageFileItem;

/**
 * Calculates the usage of a particular artifact or of the artifacts in an
 * entire repository. Implementations are responsible for processing the
 * artifact, determining the list of dependencies and then storing the usage
 * data.
 * 
 * @author Saleem Shafi
 */
public interface ArtifactUsageCalculator {
	/**
	 * Calculates the artifact usage for all of the artifacts in the given
	 * repository or repository group.
	 * 
	 * @param request
	 *            defines the repository or repository group to process
	 * @return list of artifacts that were processed
	 * @throws NoSuchRepositoryException
	 *             if the specified repository does not exist
	 * @throws IllegalArgumentException
	 *             if the specified repository is not a Maven repository
	 */
	ArtifactUsageCalculationResult calculateArtifactUsage(
			ArtifactUsageCalculationRequest request)
			throws NoSuchRepositoryException, IllegalArgumentException;

	/**
	 * Calculates the usage of the given artifact.
	 * 
	 * @param item
	 *            the artifact to process
	 * @throws IOException
	 *             if there is a problem reading the file
	 * @throws ArtifactDescriptorException
	 */
	void calculateArtifactUsage(StorageFileItem item) throws IOException,
			ArtifactDescriptorException;

	/**
	 * Eliminates the artifact usage data for the given artifact.
	 * 
	 * @param item
	 *            the artifact to delete
	 * @throws IOException
	 *             if there is a problem reading the file
	 */
	void removeArtifactUsage(StorageFileItem item) throws IOException;
}
