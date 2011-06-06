package org.ebayopensource.nexus.plugins.artifactusage.store;

import java.util.Collection;
import java.util.HashSet;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "node")
public class DependencyList {
	private Collection<Artifact> dependencies = new HashSet<Artifact>();
	private long lastCalculated;

	public DependencyList(Collection<Artifact> dependencies) {
		this.dependencies = dependencies;
		this.lastCalculated = System.currentTimeMillis();
	}

	public Collection<Artifact> getDependencies() {
		return this.dependencies;
	}

	public long getLastCalculated() {
		return this.lastCalculated;
	}
}
