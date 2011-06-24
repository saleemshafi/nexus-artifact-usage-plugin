package org.ebayopensource.nexus.plugins.artifactusage.store;

import java.util.Collection;

/**
 * Representation of a storage mechanism for artifact usage data. This is
 * essentially a mapping between projects and their dependencies, but oriented
 * from the dependency's point of view.
 * 
 * @author Saleem Shafi
 */
public interface ArtifactUsageStore {
	/**
	 * Returns a list of artifacts that use the given artifact, and each
	 * artifact returned also has a list of its users, etc., until it hits the
	 * leaf nodes.
	 * 
	 * @param artifact
	 *            the Artifact that is being used
	 * @return list of artifacts that use the given artifact, e.g. as a
	 *         dependency, filled out transitively
	 */
	Collection<ArtifactUser> getArtifactUsers(GAV dependency);

	/**
	 * Marks the first Artifact argument as a project that depends on the
	 * following Artifacts.
	 * 
	 * @param artifact
	 *            The artifact that depends on the rest
	 * @param dependencies
	 *            The artifacts that are depended upon
	 */
	void addDependencies(GAV artifact, Collection<GAV> dependencies,
			String artifactPath);

	/**
	 * Remove any mappings to this artifact from the store.
	 * 
	 * @param artifact
	 *            The artifact being removed from the repository
	 */
	void removeArtifact(GAV artifact);

	/**
	 * Returns whether or not the artifact at the given path has already been
	 * processed for usage since that last time the artifact was modified.
	 * 
	 * @param path
	 *            location of the artifact for identification purposes
	 * @param lastModifiedTime
	 *            the time in milliseconds that the file was last modified
	 * @return true if the last time we calculated the usage for the given
	 *         artifact was after the lastModifiedTime.
	 */
	boolean isAlreadyCalculated(String path, long lastModifiedTime);

}
