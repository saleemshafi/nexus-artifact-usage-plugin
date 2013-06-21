package org.ebayopensource.nexus.plugins.artifactusage.task;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.ebayopensource.nexus.plugins.artifactusage.store.ArtifactUsageStore;
import org.ebayopensource.nexus.plugins.artifactusage.store.GAV;
import org.ebayopensource.nexus.plugins.artifactusage.utils.ExceptionUtils;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
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
import org.sonatype.nexus.proxy.walker.*;
import org.sonatype.nexus.rest.artifact.PomArtifactManager;
import org.sonatype.nexus.rest.model.ArtifactCoordinate;

/**
 * Main implementation of a ArtifactUsageCalculator. Largely based on the
 * DefaultSnapshotRemover.
 * 
 * @author Saleem Shafi
 */
@Component(role = ArtifactUsageCalculator.class)
public class DefaultArtifactUsageCalculator extends AbstractLoggingComponent
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
      if (getLogger().isInfoEnabled()) {
        getLogger().info("Running ArtifactUsageCalculation only on " + repository.getId());
      }
			if (MavenRepository.class.isAssignableFrom(repository.getClass())
					&& repository.getRepositoryContentClass().isCompatible(contentClass)) {
				result.addResult(calculateArtifactUsage((MavenRepository) repository));
			} else {
        String notAMavenRepository = "The repository with ID=" + repository.getId() + " is not a MavenRepository!";
        getLogger().error(notAMavenRepository);
				throw new IllegalArgumentException(notAMavenRepository);
			}
		} else {
      getLogger().info("Running ArtifactUsageCalculation on all repositories");
			for (Repository repository : this.repositoryRegistry
					.getRepositories()) {
				// skip repositories that aren't Maven repositories
				if (MavenRepository.class.isAssignableFrom(repository.getClass())
						&& repository.getRepositoryContentClass().isCompatible(contentClass)) {
					result.addResult(calculateArtifactUsage((MavenRepository) repository));
				} else {
          if (getLogger().isInfoEnabled()) {
            getLogger().info(repository.getId() + " was skipped because it is not a maven repositories");
          }
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
    if (getLogger().isInfoEnabled()) {
      getLogger().info("Beginning calculating artifact usage on " + repository.getId());
    }
		ArtifactUsageCalculationRepositoryResult result = new ArtifactUsageCalculationRepositoryResult(
				repository.getId());

		DefaultWalkerContext ctxMain = new DefaultWalkerContext(repository,
				new ResourceStoreRequest("/"), new DottedStoreWalkerFilter());

		ctxMain.getProcessors().add(
				new ArtifactUsageCalculationWalkerProcessor(repository
						.getGavCalculator()));

    try {
		  walker.walk(ctxMain);
    } catch (WalkerException we) {
      // the fact that walking through one repo ended badly does not mean we want to stop the whole analysis !
      getLogger().error("The artifact usage calculation did not end up successfully for this repository : "
              + repository.getId(), we);
    }

		if (ctxMain.getStopCause() != null) {
			result.setSuccessful(false);
		}
    if (getLogger().isInfoEnabled()) {
      getLogger().info("Ending calculating artifact usage on " + repository.getId());
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
			getLogger().warn(
              "Error processing POM file for artifact usage data.", ExceptionUtils.getRootCause(e));
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
				getLogger().warn(
                "Error processing POM file for artifact usage data: " + item.getPath(), ExceptionUtils.getRootCause(e));
			} 
		}
	}
}
