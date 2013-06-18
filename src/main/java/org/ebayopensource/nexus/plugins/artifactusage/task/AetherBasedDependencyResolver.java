package org.ebayopensource.nexus.plugins.artifactusage.task;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.ebayopensource.nexus.plugins.artifactusage.store.GAV;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.aether.resolution.ArtifactDescriptorRequest;
import org.sonatype.aether.resolution.ArtifactDescriptorResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.mavenbridge.NexusAether;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@Component(role = DependencyResolver.class, hint = "AetherBased")
public class AetherBasedDependencyResolver extends AbstractLoggingComponent implements
		DependencyResolver {

	@Requirement
	private RepositoryRegistry repositoryRegistry;

  @Requirement
  private NexusAether nexusAether;

	public Collection<GAV> resolveDependencies(GAV artifact) throws IOException {
		Collection<GAV> artifactDependencies = new ArrayList<GAV>();

		ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
		descriptorRequest.setArtifact(new DefaultArtifact(artifact.toString()));
		for (Repository repo : this.repositoryRegistry.getRepositories()) {
			if (repo.getLocalUrl() != null) {
				descriptorRequest.addRepository(new RemoteRepository(repo
						.getId(), "default", repo.getLocalUrl()));
			}
		}

		try {
			ArtifactDescriptorResult descriptorResult = this
					.getRepositorySystem().readArtifactDescriptor(
							this.getRepositorySession(), descriptorRequest);
			for (org.sonatype.aether.graph.Dependency dependency : descriptorResult
					.getDependencies()) {
        getLogger().debug("{} depends on {}", artifact, dependency.getArtifact());
        artifactDependencies.add(new GAV(dependency.getArtifact()
						.getGroupId(),
						dependency.getArtifact().getArtifactId(), dependency
								.getArtifact().getVersion()));
			}
		} catch (ArtifactDescriptorException e) {
			throw new IOException(e);
		}

		return artifactDependencies;
	}

	// i'm not sure we really need to synchronize this
	private RepositorySystemSession getRepositorySession() {
		return nexusAether.getDefaultRepositorySystemSession();
	}

	// i'm not sure we really need to synchronize this
	private RepositorySystem getRepositorySystem() {
    return nexusAether.getRepositorySystem();
  }

}
