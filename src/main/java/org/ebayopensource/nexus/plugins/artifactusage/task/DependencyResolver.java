package org.ebayopensource.nexus.plugins.artifactusage.task;

import java.io.IOException;
import java.util.Collection;

import org.ebayopensource.nexus.plugins.artifactusage.store.GAV;

public interface DependencyResolver {
	Collection<GAV> resolveDependencies(GAV artifact) throws IOException;
}
