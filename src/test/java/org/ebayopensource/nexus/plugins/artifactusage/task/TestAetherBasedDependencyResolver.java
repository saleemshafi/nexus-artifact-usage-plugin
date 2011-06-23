package org.ebayopensource.nexus.plugins.artifactusage.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.codehaus.plexus.logging.Logger;
import org.ebayopensource.nexus.plugins.artifactusage.store.Artifact;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

public class TestAetherBasedDependencyResolver extends TestCase {
	@Test
	public void testResolveDependenciesForSimpleProject() throws Exception {
		AetherBasedDependencyResolver resolver = setupResolver();
		Artifact simpleArtifact = new Artifact("test.artifactusage",
				"simple-artifact", "1.0");
		Collection<Artifact> dependencies = resolver
				.resolveDependencies(simpleArtifact);

		Assert.assertNotNull(dependencies);
		Assert.assertEquals(2, dependencies.size());

	}

	AetherBasedDependencyResolver setupResolver() {
		RepositoryRegistry repoRegistry = Mockito
				.mock(RepositoryRegistry.class);
		List<Repository> remoteRepos = new ArrayList<Repository>();
		Repository testRemoteRepo = Mockito.mock(Repository.class);
		Mockito.when(testRemoteRepo.getLocalUrl()).thenReturn(
				new File("./src/test/remote-repo").toURI().toString());
		remoteRepos.add(testRemoteRepo);
		Mockito.when(repoRegistry.getRepositories()).thenReturn(remoteRepos);
		AetherBasedDependencyResolver resolver = new AetherBasedDependencyResolver();
		resolver.setRepositoryRegistry(repoRegistry);
		resolver.setTempDirectory(new File("./target/test-repo/"));

		Logger logger = Mockito.mock(Logger.class);
		resolver.setupLogger(logger);
		return resolver;
	}
}
