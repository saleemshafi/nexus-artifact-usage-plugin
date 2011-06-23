package org.ebayopensource.nexus.plugins.artifactusage.event;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.ebayopensource.nexus.plugins.artifactusage.task.ArtifactUsageCalculator;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.plexus.appevents.Event;

/**
 * Event handler that updates the artifact usage data whenever an artifact is
 * added or deleted from the repository.
 * 
 * @author Saleem Shafi
 * 
 */
@Component(role = EventInspector.class, hint = "ArtifactEventInspector")
public class ArtifactUsageEventInspector extends AbstractEventInspector {

	@Requirement
	private ArtifactUsageCalculator calculator;

	// Only handle cases where a repository is added, updated or deleted
	public boolean accepts(Event<?> evt) {
		return evt instanceof RepositoryItemEventStore
				|| evt instanceof RepositoryItemEventDelete
				|| evt instanceof RepositoryItemEventCache;
	}

	public void inspect(Event<?> evt) {
		try {
			// add or update
			if (evt instanceof RepositoryItemEventStore) {
				onItemStore((RepositoryItemEventStore) evt);
				// downloaded and cached
			} else if (evt instanceof RepositoryItemEventCache) {
				onItemCache((RepositoryItemEventCache) evt);
				// delete
			} else if (evt instanceof RepositoryItemEventDelete) {
				onItemDelete((RepositoryItemEventDelete) evt);
			}
			// just log the errors
		} catch (IOException e) {
			getLogger()
					.error("Error processing artifact usage during event", e);
		} catch (ArtifactDescriptorException e) {
			getLogger()
					.error("Error processing artifact usage during event", e);
		}
	}

	private void onItemDelete(RepositoryItemEventDelete evt) throws IOException {
		// we only care about POM files
		StorageItem item = evt.getItem();
		if (item instanceof StorageFileItem && item.getPath().endsWith(".pom")) {
			calculator.removeArtifactUsage((StorageFileItem) item);
		}
	}

	private void onItemCache(RepositoryItemEventCache evt) throws IOException,
			ArtifactDescriptorException {
		StorageItem item = evt.getItem();
		if (item instanceof StorageFileItem && item.getPath().endsWith(".pom")) {
			calculator.calculateArtifactUsage((StorageFileItem) item);
		}
	}

	private void onItemStore(RepositoryItemEventStore evt) throws IOException,
			ArtifactDescriptorException {
		// we only care about POM files
		StorageItem item = evt.getItem();
		if (item instanceof StorageFileItem && item.getPath().endsWith(".pom")) {
			calculator.calculateArtifactUsage((StorageFileItem) item);
		}
	}

	void setArtifactUsageCalculator(ArtifactUsageCalculator calculator) {
		this.calculator = calculator;
	}
}
