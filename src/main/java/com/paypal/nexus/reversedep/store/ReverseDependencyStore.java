package com.paypal.nexus.reversedep.store;

import java.util.Collection;

public interface ReverseDependencyStore {
	Collection<Artifact> getDependees(Artifact dependency);
	
	void addDependee(Artifact project, Collection<Artifact> dependencies);
	
	void removeDependee(Artifact project, Collection<Artifact> dependencies);
}
