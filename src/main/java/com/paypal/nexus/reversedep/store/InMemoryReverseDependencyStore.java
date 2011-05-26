package com.paypal.nexus.reversedep.store;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * An in-memory representation of the reverse dependency mappings. This version
 * would have to be recreated any time the server goes down and comes back up
 * again.
 * 
 * @author Saleem Shafi
 */
@Component(role = ReverseDependencyStore.class, hint = "InMemory")
public class InMemoryReverseDependencyStore extends AbstractLogEnabled
		implements ReverseDependencyStore {

	private Map<Artifact, Collection<Artifact>> dependeeMap = new HashMap<Artifact, Collection<Artifact>>();
	// we need this one, too, so that we can clean up old dependee settings if
	// an artifact is updated
	private Map<Artifact, Collection<Artifact>> dependencyMap = new HashMap<Artifact, Collection<Artifact>>();

	public Collection<Artifact> getDependees(Artifact dependency) {
		Collection<Artifact> dependees = dependeeMap.get(dependency);
		if (dependees == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableCollection(dependees);
	}

	public void addDependee(Artifact dependee, Collection<Artifact> dependencies) {
		// remove all of the old dependee mappings
		Collection<Artifact> oldDependencies = dependencyMap.get(dependee);
		if (oldDependencies != null) {
			this.removeDependee(dependee, oldDependencies);
		}
		// remember the dependencies so that we can clean up next time
		dependencyMap.put(dependee, dependencies);
		if (dependencies != null) {
			// go through all of the dependencies
			synchronized (this.dependeeMap) {
				for (Artifact dependency : dependencies) {
					Collection<Artifact> dependees = dependeeMap
							.get(dependency);
					if (dependees == null) {
						dependees = new HashSet<Artifact>();
						dependeeMap.put(dependency, dependees);
					}
					// mark this as a dependee of the dependency
					dependees.add(dependee);
				}
			}
		}
	}

	// Removing a dependee from this map is handled as the case of adding
	// a dependee with no dependencies
	public void removeDependee(Artifact dependee) {
		this.addDependee(dependee, null);
	}

	protected void removeDependee(Artifact dependee,
			Collection<Artifact> dependencies) {
		for (Artifact dependency : dependencies) {
			Collection<Artifact> dependees = dependeeMap.get(dependency);
			if (dependees != null) {
				dependees.remove(dependee);
			}
		}
	}

}
