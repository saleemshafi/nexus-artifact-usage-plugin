package org.ebayopensource.nexus.plugins.artifactusage.task;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.ebayopensource.nexus.plugins.artifactusage.store.ArtifactUsageStore;
import org.ebayopensource.nexus.plugins.artifactusage.store.GAV;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.maven.gav.GavCalculator;
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
 * Main implementation of a ArtifactUsageCalculator. Largely based on the
 * DefaultSnapshotRemover.
 * 
 * @author Saleem Shafi
 */
@Component(role = ArtifactUsageCalculator.class)
public class DefaultArtifactUsageCalculator extends AbstractLogEnabled
		implements ArtifactUsageCalculator {

	@Requirement
	private Walker walker;

	@Requirement(hint = "maven2")
	private ContentClass contentClass;

	@Requirement(hint = "InMemory")
	private ArtifactUsageStore artifactUsageStore;

	@Requirement(hint = "AetherBased")
	private DependencyResolver dependencyResolver;

	@Requirement
	private NexusConfiguration nexusConfig;

	@Requirement
	private RepositoryRegistry repositoryRegistry;

	public ArtifactUsageCalculationResult calculateArtifactUsage(
			ArtifactUsageCalculationRequest request)
			throws NoSuchRepositoryException, IllegalArgumentException {
		ArtifactUsageCalculationResult result = new ArtifactUsageCalculationResult();

		if (request.getRepositoryId() != null) {
			Repository repository = this.repositoryRegistry
					.getRepository(request.getRepositoryId());

			if (MavenRepository.class.isAssignableFrom(repository.getClass())
					&& repository.getRepositoryContentClass().isCompatible(
							contentClass)) {
				result.addResult(calculateArtifactUsage((MavenRepository) repository));
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
					result.addResult(calculateArtifactUsage((MavenRepository) repository));
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
	protected ArtifactUsageCalculationRepositoryResult calculateArtifactUsage(
			MavenRepository repository) {
		ArtifactUsageCalculationRepositoryResult result = new ArtifactUsageCalculationRepositoryResult(
				repository.getId());

		DefaultWalkerContext ctxMain = new DefaultWalkerContext(repository,
				new ResourceStoreRequest("/"), new DottedStoreWalkerFilter());

		ctxMain.getProcessors().add(
				new ArtifactUsageCalculationWalkerProcessor(repository
						.getGavCalculator()));

		walker.walk(ctxMain);

		if (ctxMain.getStopCause() != null) {
			result.setSuccessful(false);
		}

		return result;
	}

	public void calculateArtifactUsage(StorageFileItem item) throws IOException {
		if (getLogger().isDebugEnabled()) {
			getLogger().debug(
					"Calculating usage of "
							+ item.getRepositoryItemUid().getPath());
		}

		// don't bother if the file hasn't changed since
		// the last time it was processed
		if (this.artifactUsageStore.isAlreadyCalculated(item
				.getRepositoryItemUid().getPath(), item.getModified())) {
			return;
		}

		// convert to a Maven project
		GAV artifact = getArtifactForStorageItem(item);
		if (artifact != null) {
			artifactUsageStore.addDependencies(artifact, dependencyResolver
					.resolveDependencies(artifact), item.getRepositoryItemUid()
					.getPath());
		}
	}

	public void removeArtifactUsage(StorageFileItem item) throws IOException {
		if (getLogger().isDebugEnabled()) {
			getLogger().debug(
					"Removing artifact usage data for "
							+ item.getRepositoryItemUid().getPath());
		}

		GAV artifact = getArtifactForStorageItem(item);
		if (artifact != null) {
			artifactUsageStore.removeArtifact(artifact);
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
	GAV getArtifactForStorageItem(StorageFileItem item) throws IOException {
		try {
			PomArtifactManager mgr = new PomArtifactManager(
					this.nexusConfig.getTemporaryDirectory());
			mgr.storeTempPomFile(item.getInputStream());
			ArtifactCoordinate ac = mgr.getArtifactCoordinateFromTempPomFile();
			return new GAV(ac.getGroupId(), ac.getArtifactId(), ac.getVersion());
		} catch (Exception e) {
			getLogger().error(
					"Error processing POM file for artifact usage data.", e);
			return null;
		}

	}

	private class ArtifactUsageCalculationWalkerProcessor extends
			AbstractWalkerProcessor {
		private final GavCalculator gavCalculator;

		public ArtifactUsageCalculationWalkerProcessor(
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
			try {
				Gav gav = this.gavCalculator.pathToGav(item.getPath());
	
				// make sure we've really got a POM file
				if (gav != null && !gav.isHash() && !gav.isSignature()
						&& gav.getExtension().equals("pom")) {
					// and then calculate the artifact usage
					calculateArtifactUsage((StorageFileItem) item);
				}
			}
			catch (Exception e) {
				getLogger().error(
					"Error processing POM file for artifact usage data: " + item.getPath(), e);
			} 
		}
	}
}
