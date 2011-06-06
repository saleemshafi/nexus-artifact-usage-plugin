package org.ebayopensource.nexus.reversedep.task;

import java.io.IOException;

import org.apache.maven.index.artifact.Gav;
import org.apache.maven.index.artifact.GavCalculator;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.ebayopensource.nexus.reversedep.store.Artifact;
import org.ebayopensource.nexus.reversedep.store.ReverseDependencyStore;
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
	private Walker walker;

	@Requirement(hint = "maven2")
	private ContentClass contentClass;

	@Requirement(hint = "InMemory")
	private ReverseDependencyStore dependencyStore;

	@Requirement(hint = "AetherBased")
	private DependencyResolver dependencyResolver;

	@Requirement
	private NexusConfiguration nexusConfig;

	@Requirement
	private RepositoryRegistry repositoryRegistry;

	public ReverseDependencyCalculationResult calculateReverseDependencies(
			ReverseDependencyCalculationRequest request)
			throws NoSuchRepositoryException, IllegalArgumentException {
		ReverseDependencyCalculationResult result = new ReverseDependencyCalculationResult();

		if (request.getRepositoryId() != null) {
			Repository repository = this.repositoryRegistry
					.getRepository(request.getRepositoryId());

			if (MavenRepository.class.isAssignableFrom(repository.getClass())
					&& repository.getRepositoryContentClass().isCompatible(
							contentClass)) {
				result.addResult(calculateReverseDependencies((MavenRepository) repository));
			} else {
				throw new IllegalArgumentException("The repository with ID="
						+ repository.getId() + " is not MavenRepository!");
			}
		} else {
			for (Repository repository : this.repositoryRegistry
					.getRepositories()) {
				// skip and repository that aren't Maven repositories
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
				repository.getGavCalculator());
		ctxMain.getProcessors().add(reverseDependencyCalculationProcessor);

		walker.walk(ctxMain);

		if (ctxMain.getStopCause() != null) {
			result.setSuccessful(false);
		}

		return result;
	}

	public void calculateReverseDependencies(StorageFileItem item)
			throws IOException {
		if (getLogger().isDebugEnabled()) {
			getLogger().debug(
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
		Artifact artifact = getArtifactForStorageItem(item);
		if (artifact != null) {
			dependencyStore.addDependee(artifact,
					dependencyResolver.resolveDependencies(artifact));
		}
	}

	public void removeReverseDependencies(StorageFileItem item)
			throws IOException {
		if (getLogger().isDebugEnabled()) {
			getLogger().debug(
					"Removing reverse dependencies for "
							+ item.getRepositoryItemUid().getPath());
		}

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
		private final GavCalculator gavCalculator;

		public ReverseDependencyCalculationWalkerProcessor(
				GavCalculator gavCalculator) {
			this.gavCalculator = gavCalculator;
		}

		@Override
		public void processItem(WalkerContext context, StorageItem item)
				throws Exception {
			// just process the POM files
			if (item instanceof StorageFileItem
					&& !item.getRepositoryItemUid().getPath().endsWith(".pom")) {
				return;
			}
			Gav gav = this.gavCalculator.pathToGav(item.getPath());

			// make sure we've really got a POM file
			if (gav != null && !gav.isHash() && !gav.isSignature()
					&& gav.getExtension().equals("pom")) {
				// and then calculate the reverse dependencies
				calculateReverseDependencies((StorageFileItem) item);
			}
		}
	}
}
