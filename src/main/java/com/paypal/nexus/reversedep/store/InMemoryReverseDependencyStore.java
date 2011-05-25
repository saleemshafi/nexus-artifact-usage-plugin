package com.paypal.nexus.reversedep.store;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;

@Component( role = ReverseDependencyStore.class, hint = "InMemory" )
public class InMemoryReverseDependencyStore extends AbstractLogEnabled implements ReverseDependencyStore {
	private Map<Artifact, Collection<Artifact>> dependeeMap = new HashMap<Artifact, Collection<Artifact>>();
	
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
