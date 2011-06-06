package org.ebayopensource.nexus.plugins.artifactusage.task;

import java.io.IOException;
import java.util.Collection;

import org.ebayopensource.nexus.plugins.artifactusage.store.Artifact;

public interface DependencyResolver {
	Collection<Artifact> resolveDependencies(Artifact artifact)
			throws IOException;
}
