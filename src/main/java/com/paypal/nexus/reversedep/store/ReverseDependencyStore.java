package com.paypal.nexus.reversedep.store;

import java.util.Collection;

/**
 * Representation of a storage mechanism for the reverse dependencies. This is
 * essentially a mapping between projects and their dependencies, but oriented
 * from the dependency's point of view.
 * 
 * The term 'dependee' is being used as a synonym of 'reverse dependency'.
 * 
 * @author Saleem Shafi
 */
public interface ReverseDependencyStore {
	/**
	 * Returns a list of artifacts that depend on the given artifact.
	 * 
	 * @param dependency
	 *            the Artifact that is depended upon
	 * @return list of dependees
	 */
	Collection<Artifact> getDependees(Artifact dependency);

	/**
	 * Marks the first Artifact argument as a project that depends on the
	 * following Artifacts.
	 * 
	 * @param dependee
	 *            The artifact that depends on the rest
	 * @param dependencies
	 *            The artifacts that are depended upon
	 */
	void addDependee(Artifact dependee, Collection<Artifact> dependencies);

	/**
	 * Remove any mappings to this dependee from the store.
	 * 
	 * @param dependee
	 *            The artifact being removed from the repository
	 */
	void removeDependee(Artifact dependee);
}
