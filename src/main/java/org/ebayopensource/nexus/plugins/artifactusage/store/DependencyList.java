package org.ebayopensource.nexus.plugins.artifactusage.store;

import java.util.Collection;
import java.util.HashSet;

public class DependencyList {
	private Collection<GAV> dependencies = new HashSet<GAV>();
	private long lastCalculated;

	public DependencyList(Collection<GAV> dependencies) {
		this.dependencies = dependencies;
		this.lastCalculated = System.currentTimeMillis();
	}

	public Collection<GAV> getDependencies() {
		return this.dependencies;
	}

	public long getLastCalculated() {
		return this.lastCalculated;
	}
}
