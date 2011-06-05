package org.ebayopensource.nexus.reversedep.task;

import java.io.IOException;
import java.util.Collection;

import org.ebayopensource.nexus.reversedep.store.Artifact;

public interface DependencyResolver {
	Collection<Artifact> resolveDependencies(Artifact artifact)
			throws IOException;
}
