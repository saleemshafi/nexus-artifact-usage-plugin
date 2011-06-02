package org.ebayopensource.nexus.reversedep.event;

import java.io.IOException;

import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.ebayopensource.nexus.reversedep.task.ReverseDependencyCalculator;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.plexus.appevents.Event;

/**
 * Event handler that updates the reverse dependency mappings whenever an
 * artifact is added or deleted from the repository.
 * 
 * @author Saleem Shafi
 * 
 */
@Component(role = EventInspector.class, hint = "ReverseDependencyEventInspector")
public class ReverseDependencyEventInspector extends AbstractEventInspector {

	@Requirement
	private ReverseDependencyCalculator calculator;

	// Only handle cases where a repository is added, updated or deleted
	public boolean accepts(Event<?> evt) {
		return evt instanceof RepositoryItemEventStore
				|| evt instanceof RepositoryItemEventDelete;
	}

	public void inspect(Event<?> evt) {
		try {
			// add or update
			if (evt instanceof RepositoryItemEventStore) {
				onItemStore((RepositoryItemEventStore) evt);
				// delete
			} else if (evt instanceof RepositoryItemEventDelete) {
				onItemDelete((RepositoryItemEventDelete) evt);
			}
			// just log the errors
		} catch (IOException e) {
			getLogger().error(
					"Error processing reverse dependencies during event", e);
		} catch (ComponentLookupException e) {
			getLogger().error(
					"Error processing reverse dependencies during event", e);
		} catch (PlexusContainerException e) {
			getLogger().error(
					"Error processing reverse dependencies during event", e);
		} catch (ArtifactDescriptorException e) {
			getLogger().error(
					"Error processing reverse dependencies during event", e);
		}
	}

	private void onItemDelete(RepositoryItemEventDelete evt) throws IOException {
		// we only care about POM files
		StorageItem item = evt.getItem();
		if (item instanceof StorageFileItem && item.getPath().endsWith(".pom")) {
			calculator.removeReverseDependencies((StorageFileItem) item);
		}
	}

	private void onItemStore(RepositoryItemEventStore evt) throws IOException,
			ComponentLookupException, PlexusContainerException,
			ArtifactDescriptorException {
		// we only care about POM files
		StorageItem item = evt.getItem();
		if (item instanceof StorageFileItem && item.getPath().endsWith(".pom")) {
			calculator.calculateReverseDependencies((StorageFileItem) item);
		}
	}
}
