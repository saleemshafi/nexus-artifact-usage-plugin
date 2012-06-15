package org.ebayopensource.nexus.plugins.artifactusage.task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.apache.maven.repository.internal.MavenServiceLocator;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.ebayopensource.nexus.plugins.artifactusage.store.GAV;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.aether.resolution.ArtifactDescriptorRequest;
import org.sonatype.aether.resolution.ArtifactDescriptorResult;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

@Component(role = DependencyResolver.class, hint = "AetherBased")
public class AetherBasedDependencyResolver extends AbstractLogEnabled implements
		DependencyResolver {

	@Requirement
	private NexusConfiguration nexusConfig;

	@Requirement
	private RepositoryRegistry repositoryRegistry;

	private RepositorySystem repoSystem;
	private MavenRepositorySystemSession session;
	private File tempDirectory;

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
				if (getLogger().isDebugEnabled()) {
					getLogger().debug(
							artifact.getArtifactId() + " depends on "
									+ dependency.getArtifact().getGroupId()
									+ ":"
									+ dependency.getArtifact().getArtifactId()
									+ ":"
									+ dependency.getArtifact().getVersion());
				}
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

	void setTempDirectory(File tempDirectory) {
		this.tempDirectory = tempDirectory;
	}

	void setRepo(RepositoryRegistry registry) {
		this.repositoryRegistry = registry;
	}

	void setupLogger(Logger logger) {
		this.setupLogger(this, logger);
	}

	private File getTempDirectory() {
		if (this.tempDirectory == null) {
			this.tempDirectory = new File(
					this.nexusConfig.getTemporaryDirectory(),
					"artifact-usage-repo");
		}
		return this.tempDirectory;
	}

	// i'm not sure we really need to synchronize this
	private MavenRepositorySystemSession getRepositorySession() {
		session = new MavenRepositorySystemSession();
		// TODO: see if there's a way to implement a local repo manager that
		// doesn't bother writing anything to disk.
		LocalRepository localRepo = new LocalRepository(this.getTempDirectory());
		this.session.setLocalRepositoryManager(this.getRepositorySystem()
				.newLocalRepositoryManager(localRepo));
		return this.session;
	}

	// i'm not sure we really need to synchronize this
	private RepositorySystem getRepositorySystem() {
		if (this.repoSystem == null) {
			MavenServiceLocator locator = new MavenServiceLocator();
			locator.addService(RepositoryConnectorFactory.class,
					FileRepositoryConnectorFactory.class);
			this.repoSystem = locator.getService(RepositorySystem.class);
		}
		return this.repoSystem;
	}

}
