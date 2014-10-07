package org.ebayopensource.nexus.plugins.artifactusage.event;

import org.ebayopensource.nexus.plugins.artifactusage.task.ArtifactUsageCalculator;
import org.slf4j.Logger;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

import com.google.common.eventbus.Subscribe;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Event handler that updates the artifact usage data whenever an artifact is
 * added or deleted from the repository.
 * 
 * @author Saleem Shafi
 */
@Singleton
@Named("ArtifactEventInspector")
public class ArtifactUsageEventInspector implements EventSubscriber {

	private final Logger logger;

	private final ArtifactUsageCalculator calculator;

	@Inject
	public ArtifactUsageEventInspector(final Logger logger, final ArtifactUsageCalculator calculator) {
		this.logger = logger;
		this.calculator = calculator;
	}

	@Subscribe
	public void onItemDelete(final RepositoryItemEventDelete evt) {
		try {
			// we only care about POM files
			final StorageItem item = evt.getItem();
			if (item instanceof StorageFileItem && item.getPath().endsWith(".pom")) {
				calculator.removeArtifactUsage((StorageFileItem) item);
			}
		} catch (final IOException e) {
			logger.error("Error processing artifact usage during event", e);
		}
	}

	@Subscribe
	public void onItemCache(final RepositoryItemEventCache evt) {
		try {
			final StorageItem item = evt.getItem();
			if (item instanceof StorageFileItem && item.getPath().endsWith(".pom")) {
				calculator.calculateArtifactUsage((StorageFileItem) item);
			}
		} catch (final IOException e) {
			logger.error("Error processing artifact usage during event", e);
		} catch (final ArtifactDescriptorException e) {
			logger.error("Error processing artifact usage during event", e);
		}
	}

	@Subscribe
	public void onItemStore(final RepositoryItemEventStore evt) {
		try {
			// we only care about POM files
			final StorageItem item = evt.getItem();
			if (item instanceof StorageFileItem && item.getPath().endsWith(".pom")) {
				calculator.calculateArtifactUsage((StorageFileItem) item);
			}
		} catch (final IOException e) {
			logger.error("Error processing artifact usage during event", e);
		} catch (final ArtifactDescriptorException e) {
			logger.error("Error processing artifact usage during event", e);
		}
	}

}
