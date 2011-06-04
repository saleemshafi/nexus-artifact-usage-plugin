package org.ebayopensource.nexus.reversedep.task;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.maven.index.artifact.Gav;
import org.apache.maven.repository.internal.DefaultServiceLocator;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.ebayopensource.nexus.reversedep.store.Artifact;
import org.ebayopensource.nexus.reversedep.store.ReverseDependencyStore;
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
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.DottedStoreWalkerFilter;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.rest.artifact.PomArtifactManager;
import org.sonatype.nexus.rest.model.ArtifactCoordinate;

/**
 * Main implementation of a ReverseDependencyCalculator. Largely based on the
 * DefaultSnapshotRemover.
 * 
 * @author Saleem Shafi
 */
@Component(role = ReverseDependencyCalculator.class)
public class DefaultReverseDependencyCalculator extends AbstractLogEnabled
		implements ReverseDependencyCalculator {

	@Requirement
	private RepositoryRegistry repositoryRegistry;

	@Requirement
	private Walker walker;

	@Requirement(hint = "maven2")
	private ContentClass contentClass;

	@Requirement(hint = "InMemory")
	private ReverseDependencyStore dependencyStore;

	@Requirement
	private NexusConfiguration nexusConfig;

	public ReverseDependencyCalculationResult calculateReverseDependencies(
			ReverseDependencyCalculationRequest request)
			throws NoSuchRepositoryException, IllegalArgumentException {
		ReverseDependencyCalculationResult result = new ReverseDependencyCalculationResult();

		if (request.getRepositoryId() != null) {
			Repository repository = getRepositoryRegistry().getRepository(
					request.getRepositoryId());

			if (MavenRepository.class.isAssignableFrom(repository.getClass())
					&& repository.getRepositoryContentClass().isCompatible(
							contentClass)) {
				result.addResult(calculateReverseDependencies((MavenRepository) repository));
			} else {
				throw new IllegalArgumentException("The repository with ID="
						+ repository.getId() + " is not MavenRepository!");
			}
		} else {
			for (Repository repository : getRepositoryRegistry()
					.getRepositories()) {
				// only from maven repositories, stay silent for others and
				// simply skip
				if (MavenRepository.class.isAssignableFrom(repository
						.getClass())
						&& repository.getRepositoryContentClass().isCompatible(
								contentClass)) {
					result.addResult(calculateReverseDependencies((MavenRepository) repository));
				}
			}
		}

		return result;
	}

	protected RepositoryRegistry getRepositoryRegistry() {
		return repositoryRegistry;
	}

	/**
	 * Process a specific Maven repository
	 * 
	 * @param repository
	 * @return
	 */
	protected ReverseDependencyCalculationRepositoryResult calculateReverseDependencies(
			MavenRepository repository) {
		ReverseDependencyCalculationRepositoryResult result = new ReverseDependencyCalculationRepositoryResult(
				repository.getId());

		DefaultWalkerContext ctxMain = new DefaultWalkerContext(repository,
				new ResourceStoreRequest("/"), new DottedStoreWalkerFilter());

		ReverseDependencyCalculationWalkerProcessor reverseDependencyCalculationProcessor = new ReverseDependencyCalculationWalkerProcessor(
				repository);
		ctxMain.getProcessors().add(reverseDependencyCalculationProcessor);

		walker.walk(ctxMain);

		if (ctxMain.getStopCause() != null) {
			result.setSuccessful(false);
		}

		return result;
	}

	public void calculateReverseDependencies(StorageFileItem item)
			throws IOException, ArtifactDescriptorException {
		if (getLogger().isInfoEnabled()) {
			getLogger().info(
					"Calculating reverse dependencies for "
							+ item.getRepositoryItemUid().getPath());
		}

		// don't bother if the file hasn't changed since
		// the last time it was processed
		if (this.dependencyStore.isAlreadyCalculated(item
				.getRepositoryItemUid().getPath(), item.getModified())) {
			return;
		}

		// convert to a Maven project
		InputStream input = item.getContentLocator().getContent();
		Collection<Artifact> artifactDependencies = new ArrayList<Artifact>();
		Artifact artifact = getArtifactForStorageItem(item);
		if (artifact != null) {
			DefaultServiceLocator locator = new DefaultServiceLocator();
			locator.addService(RepositoryConnectorFactory.class,
					FileRepositoryConnectorFactory.class);

			RepositorySystem system = locator
					.getService(RepositorySystem.class);

			MavenRepositorySystemSession session = new MavenRepositorySystemSession();

			// TODO: see if there's a way to implement a local repo manager that
			// doesn't bother writing anything to disk.
			LocalRepository localRepo = new LocalRepository("target/local-repo");
			session.setLocalRepositoryManager(system
					.newLocalRepositoryManager(localRepo));

			ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
			descriptorRequest.setArtifact(new DefaultArtifact(artifact
					.toString()));
			for (Repository repo : getRepositoryRegistry().getRepositories()) {
				if (repo.getLocalUrl() != null) {
					descriptorRequest.addRepository(new RemoteRepository(repo
							.getId(),
					// TODO: figure out what options there are for the 'type'
					// param
							"default", repo.getLocalUrl()));
				}
			}

			ArtifactDescriptorResult descriptorResult = system
					.readArtifactDescriptor(session, descriptorRequest);

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
				artifactDependencies.add(new Artifact(dependency.getArtifact()
						.getGroupId(),
						dependency.getArtifact().getArtifactId(), dependency
								.getArtifact().getVersion()));
			}

			dependencyStore.addDependee(artifact, artifactDependencies);
		}
	}

	public void removeReverseDependencies(StorageFileItem item)
			throws IOException {
		if (getLogger().isInfoEnabled()) {
			getLogger().info(
					"Removing reverse dependencies for "
							+ item.getRepositoryItemUid().getPath());
		}

		InputStream input = item.getContentLocator().getContent();
		Artifact artifact = getArtifactForStorageItem(item);
		if (artifact != null) {
			dependencyStore.removeDependee(artifact);
		}
	}

	/**
	 * Converts a POM file into an Artifact that holds the GAV data for the pom.
	 * 
	 * @param item
	 *            StorageFileItem representing a POM file
	 * @return an Artifact containing the GAV information in the file
	 * @throws IOException
	 */
	Artifact getArtifactForStorageItem(StorageFileItem item) throws IOException {
		try {
			PomArtifactManager mgr = new PomArtifactManager(
					this.nexusConfig.getTemporaryDirectory());
			mgr.storeTempPomFile(item.getInputStream());
			ArtifactCoordinate ac = mgr.getArtifactCoordinateFromTempPomFile();
			return new Artifact(ac.getGroupId(), ac.getArtifactId(),
					ac.getVersion(), item.getRepositoryItemUid().getPath());
		} catch (Exception e) {
			getLogger()
					.error("Error processing POM file for reverse dependency information.",
							e);
			return null;
		}

	}

	private class ReverseDependencyCalculationWalkerProcessor extends
			AbstractWalkerProcessor {
		private final MavenRepository repository;

		public ReverseDependencyCalculationWalkerProcessor(
				MavenRepository repository) {
			this.repository = repository;
		}

		@Override
		public void processItem(WalkerContext context, StorageItem item)
				throws Exception {
			// just process the POM files
			if (item instanceof StorageFileItem
					&& !item.getRepositoryItemUid().getPath().endsWith(".pom")) {
				return;
			}
			Gav gav = this.repository.getGavCalculator().pathToGav(
					item.getPath());

			if (gav != null) {
				// make sure we've really got a POM file
				if (!gav.isHash() && !gav.isSignature()
						&& gav.getExtension().equals("pom")) {
					// and then calculate the reverse dependencies
					calculateReverseDependencies((StorageFileItem) item);
				}
			}
		}
	}
}
