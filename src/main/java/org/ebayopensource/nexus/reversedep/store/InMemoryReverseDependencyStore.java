package org.ebayopensource.nexus.reversedep.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.ebayopensource.nexus.reversedep.rest.Dependee;

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
	private Map<Artifact, DependencyList> dependencyMap = new HashMap<Artifact, DependencyList>();
	// allow us to convert the a StorageFileItem path to an Artifact so that we
	// can coordinate the IsAlreadyCalculated logic with the actual dependee
	// stuff
	private Map<String, Artifact> pathMap = new HashMap<String, Artifact>();

	public Collection<Artifact> getDependees(Artifact dependency) {
		Collection<Artifact> dependees = dependeeMap.get(dependency);
		if (dependees == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableCollection(dependees);
	}

	public Collection<Dependee> getTransitiveDependees(Artifact dependency) {
		Collection<Dependee> dependees = new ArrayList<Dependee>();
		for (Artifact artifact : this.getDependees(dependency)) {
			Dependee dependee = new Dependee(artifact.getGroupId(),
					artifact.getArtifactId(), artifact.getVersion(),
					artifact.getPath());
			for (Dependee transitiveDependee : getTransitiveDependees(artifact)) {
				dependee.addDependee(transitiveDependee);
			}
			dependees.add(dependee);
		}
		return dependees;
	}

	public void addDependee(Artifact dependee, Collection<Artifact> dependencies) {
		// remove all of the old dependee mappings
		DependencyList oldDependencies = dependencyMap.get(dependee);
		if (oldDependencies != null
				&& oldDependencies.getDependencies() != null) {
			this.removeDependee(dependee, oldDependencies.getDependencies());
		}
		// remember the dependencies so that we can clean up next time
		DependencyList newDependencies = new DependencyList(dependencies);
		dependencyMap.put(dependee, newDependencies);

		// remember where the associated file is located so we can compare
		// file modification time next time we try to calculate
		if (dependee.getPath() != null) {
			this.pathMap.put(dependee.getPath(), dependee);
		}
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

	public boolean isAlreadyCalculated(String path, long lastModifiedTime) {
		Artifact artifact = this.pathMap.get(path);
		if (artifact == null)
			return false;
		DependencyList dependencies = this.dependencyMap.get(artifact);
		return (dependencies != null && dependencies.getLastCalculated() > lastModifiedTime);
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
