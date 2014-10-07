package org.ebayopensource.nexus.plugins.artifactusage.task;

import org.ebayopensource.nexus.plugins.artifactusage.store.ArtifactUsageStore;
import org.ebayopensource.nexus.plugins.artifactusage.store.GAV;
import org.ebayopensource.nexus.plugins.artifactusage.utils.ExceptionUtils;
import org.slf4j.Logger;
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
import org.sonatype.nexus.proxy.walker.WalkerException;
import org.sonatype.nexus.rest.artifact.PomArtifactManager;
import org.sonatype.nexus.rest.model.ArtifactCoordinate;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Main implementation of a ArtifactUsageCalculator. Largely based on the
 * DefaultSnapshotRemover.
 * 
 * @author Saleem Shafi
 */
@Singleton
@Named
public class DefaultArtifactUsageCalculator implements ArtifactUsageCalculator {

	private final Logger logger;

	private final Walker walker;

	private final ContentClass contentClass;

	private final ArtifactUsageStore artifactUsageStore;

	private final DependencyResolver dependencyResolver;

	private final NexusConfiguration nexusConfig;

	private final RepositoryRegistry repositoryRegistry;

	@Inject
	public DefaultArtifactUsageCalculator(final Logger logger, final Walker walker, @Named("maven2")
	final ContentClass contentClass, @Named("InMemory")
	final ArtifactUsageStore artifactUsageStore, @Named("AetherBased")
	final DependencyResolver dependencyResolver, final NexusConfiguration nexusConfig,
	final RepositoryRegistry repositoryRegistry) {
		this.logger = logger;
		this.walker = walker;
		this.contentClass = contentClass;
		this.artifactUsageStore = artifactUsageStore;
		this.dependencyResolver = dependencyResolver;
		this.nexusConfig = nexusConfig;
		this.repositoryRegistry = repositoryRegistry;
	}

	@Override
	public ArtifactUsageCalculationResult calculateArtifactUsage(final ArtifactUsageCalculationRequest request)
			throws NoSuchRepositoryException, IllegalArgumentException {
		final ArtifactUsageCalculationResult result = new ArtifactUsageCalculationResult();

		if (request.getRepositoryId() != null) {
			final Repository repository = this.repositoryRegistry.getRepository(request.getRepositoryId());
			if (logger.isInfoEnabled()) {
				logger.info("Running ArtifactUsageCalculation only on " + repository.getId());
			}
			if (MavenRepository.class.isAssignableFrom(repository.getClass())
					&& repository.getRepositoryContentClass().isCompatible(contentClass)) {
				result.addResult(calculateArtifactUsage((MavenRepository) repository));
			} else {
				final String notAMavenRepository = "The repository with ID=" + repository.getId()
						+ " is not a MavenRepository!";
				logger.error(notAMavenRepository);
				throw new IllegalArgumentException(notAMavenRepository);
			}
		} else {
			logger.info("Running ArtifactUsageCalculation on all repositories");
			for (final Repository repository : this.repositoryRegistry.getRepositories()) {
				// skip repositories that aren't Maven repositories
				if (MavenRepository.class.isAssignableFrom(repository.getClass())
						&& repository.getRepositoryContentClass().isCompatible(contentClass)) {
					result.addResult(calculateArtifactUsage((MavenRepository) repository));
				} else {
					if (logger.isInfoEnabled()) {
						logger.info(repository.getId()
								+ " was skipped because it is not a maven repositories");
					}
				}
			}
		}

		return result;
	}

	/**
	 * Process a specific Maven repository
	 * @param repository
	 * @return
	 */
	protected ArtifactUsageCalculationRepositoryResult calculateArtifactUsage(final MavenRepository repository) {
		if (logger.isInfoEnabled()) {
			logger.info("Beginning calculating artifact usage on " + repository.getId());
		}
		final ArtifactUsageCalculationRepositoryResult result = new ArtifactUsageCalculationRepositoryResult(
				repository.getId());

		final DefaultWalkerContext ctxMain = new DefaultWalkerContext(repository, new ResourceStoreRequest(
				"/"), new DottedStoreWalkerFilter());

		ctxMain.getProcessors().add(
				new ArtifactUsageCalculationWalkerProcessor(repository.getGavCalculator()));

		try {
			walker.walk(ctxMain);
		} catch (final WalkerException we) {
			// the fact that walking through one repo ended badly does not mean
			// we want to stop the whole analysis !
			logger.error("The artifact usage calculation did not end up successfully for this repository : "
					+ repository.getId(), we);
		}

		if (ctxMain.getStopCause() != null) {
			result.setSuccessful(false);
		}
		if (logger.isInfoEnabled()) {
			logger.info("Ending calculating artifact usage on " + repository.getId());
		}
		return result;
	}

	@Override
	public void calculateArtifactUsage(final StorageFileItem item) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Calculating usage of " + item.getRepositoryItemUid().getPath());
		}

		// don't bother if the file hasn't changed since
		// the last time it was processed
		if (this.artifactUsageStore.isAlreadyCalculated(item.getRepositoryItemUid().getPath(),
				item.getModified())) {
			return;
		}

		// convert to a Maven project
		final GAV artifact = getArtifactForStorageItem(item);
		if (artifact != null) {
			artifactUsageStore.addDependencies(artifact, dependencyResolver.resolveDependencies(artifact),
					item.getRepositoryItemUid().getPath());
		}
	}

	@Override
	public void removeArtifactUsage(final StorageFileItem item) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Removing artifact usage data for " + item.getRepositoryItemUid().getPath());
		}

		final GAV artifact = getArtifactForStorageItem(item);
		if (artifact != null) {
			artifactUsageStore.removeArtifact(artifact);
		}
	}

	/**
	 * Converts a POM file into an Artifact that holds the GAV data for the pom.
	 * @param item StorageFileItem representing a POM file
	 * @return an Artifact containing the GAV information in the file
	 * @throws IOException
	 */
	GAV getArtifactForStorageItem(final StorageFileItem item) throws IOException {
		try {
			final PomArtifactManager mgr = new PomArtifactManager(this.nexusConfig.getTemporaryDirectory());
			mgr.storeTempPomFile(item.getInputStream());
			final ArtifactCoordinate ac = mgr.getArtifactCoordinateFromTempPomFile();
			return new GAV(ac.getGroupId(), ac.getArtifactId(), ac.getVersion());
		} catch (final Exception e) {
			logger.warn("Error processing POM file for artifact usage data.", ExceptionUtils.getRootCause(e));
			return null;
		}

	}

	private class ArtifactUsageCalculationWalkerProcessor extends AbstractWalkerProcessor {
		private final GavCalculator gavCalculator;

		public ArtifactUsageCalculationWalkerProcessor(final GavCalculator gavCalculator) {
			this.gavCalculator = gavCalculator;
		}

		@Override
		public void processItem(final WalkerContext context, final StorageItem item) throws Exception {
			// just process the POM files
			if (item instanceof StorageFileItem && !item.getRepositoryItemUid().getPath().endsWith(".pom")) {
				return;
			}
			try {
				final Gav gav = this.gavCalculator.pathToGav(item.getPath());

				// make sure we've really got a POM file
				if (gav != null && !gav.isHash() && !gav.isSignature() && gav.getExtension().equals("pom")) {
					// and then calculate the artifact usage
					calculateArtifactUsage((StorageFileItem) item);
				}
			} catch (final Exception e) {
				logger.warn("Error processing POM file for artifact usage data: " + item.getPath(),
						ExceptionUtils.getRootCause(e));
			}
		}
	}
}
