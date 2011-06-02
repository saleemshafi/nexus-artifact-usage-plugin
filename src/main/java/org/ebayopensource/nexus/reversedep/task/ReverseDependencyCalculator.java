package org.ebayopensource.nexus.reversedep.task;

import java.io.IOException;

import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.item.StorageFileItem;

/**
 * Calculates the reverse dependencies for a particular artifact or an entire
 * repository. Implementations are responsible for processing the artifact,
 * determining the list of dependencies and then storing the reverse dependency
 * mappings.
 * 
 * @author Saleem Shafi
 */
public interface ReverseDependencyCalculator {
	/**
	 * Calculates the reverse dependencies for all of the artifacts in the given
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
	ReverseDependencyCalculationResult calculateReverseDependencies(
			ReverseDependencyCalculationRequest request)
			throws NoSuchRepositoryException, IllegalArgumentException;

	/**
	 * Calculates the reverse dependencies for the given artifact.
	 * 
	 * @param item
	 *            the artifact to process
	 * @throws IOException
	 *             if there is a problem reading the file
	 * @throws ArtifactDescriptorException
	 */
	void calculateReverseDependencies(StorageFileItem item) throws IOException,
			ArtifactDescriptorException;

	/**
	 * Eliminates the reverse dependencies for the given artifact.
	 * 
	 * @param item
	 *            the artifact to delete
	 * @throws IOException
	 *             if there is a problem reading the file
	 */
	void removeReverseDependencies(StorageFileItem item) throws IOException;
}
