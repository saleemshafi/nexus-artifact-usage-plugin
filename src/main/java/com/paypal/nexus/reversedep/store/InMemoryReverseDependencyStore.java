package com.paypal.nexus.reversedep.store;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Collections;

public class InMemoryReverseDependencyStore implements ReverseDependencyStore {
	private Map<Artifact, Collection<Artifact>> dependeeMap = new HashMap<Artifact, Collection<Artifact>>();

	// yes, i know, this is bad, but until i figure out how Plexus works, this will do.
	private static InMemoryReverseDependencyStore instance = new InMemoryReverseDependencyStore();
	private InMemoryReverseDependencyStore() {}
	public static InMemoryReverseDependencyStore getInstance() { return instance; }
	
	public Collection<Artifact> getDependees(Artifact dependency) {
		Collection<Artifact> dependees = dependeeMap.get(dependency);
		if (dependees == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableCollection(dependees);
	}

	public void addDependee(Artifact project, Collection<Artifact> dependencies) {
		for (Artifact dependency : dependencies) {
			synchronized (this.dependeeMap) {
				Collection<Artifact> dependees = dependeeMap.get(dependency);
				if (dependees == null) {
					dependees = new HashSet<Artifact>();
					dependeeMap.put(dependency, dependees);
				}
				dependees.add(project);
			}
		}
	}

	public void removeDependee(Artifact project,
			Collection<Artifact> dependencies) {
		for (Artifact dependency : dependencies) {
			Collection<Artifact> dependees = dependeeMap.get(dependency);
			if (dependees != null) {
				dependees.remove(project);
			}
		}
	}

}
