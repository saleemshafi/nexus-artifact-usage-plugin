package org.ebayopensource.nexus.plugins.artifactusage.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import junit.framework.TestCase;

import org.ebayopensource.nexus.plugins.artifactusage.task.ArtifactUsageCalculator;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventRetrieve;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.StorageFileItem;

public class TestArtifactUsageEventInspector extends TestCase {
	public void testAcceptsCorrectEventTypes() throws Exception {
		ArtifactUsageEventInspector eventInspector = new ArtifactUsageEventInspector();
		assertTrue(eventInspector.accepts(mock(RepositoryItemEventCache.class)));
		assertTrue(eventInspector
				.accepts(mock(RepositoryItemEventDelete.class)));
		assertTrue(eventInspector.accepts(mock(RepositoryItemEventStore.class)));
		assertFalse(eventInspector
				.accepts(mock(RepositoryItemEventRetrieve.class)));
		assertFalse(eventInspector.accepts(mock(NexusStartedEvent.class)));
	}

	public void testInspectInvalidEventType() throws Exception {
		ArtifactUsageEventInspector eventInspector = new ArtifactUsageEventInspector();
		ArtifactUsageCalculator calculator = mock(ArtifactUsageCalculator.class);
		eventInspector.setArtifactUsageCalculator(calculator);
		eventInspector.inspect(mock(RepositoryItemEventRetrieve.class));
		verifyZeroInteractions(calculator);
	}

	public void testInspectNewArtifactPomFile() throws Exception {
		testCreateEventWithPomFile(RepositoryItemEventCache.class);
		testCreateEventWithPomFile(RepositoryItemEventStore.class);
	}

	public void testInspectValidEventsWithJarFile() throws Exception {
		testEventWithJarFile(RepositoryItemEventCache.class);
		testEventWithJarFile(RepositoryItemEventStore.class);
		testEventWithJarFile(RepositoryItemEventDelete.class);
	}

	protected void testCreateEventWithPomFile(
			Class<? extends RepositoryItemEvent> eventClass) throws Exception {
		ArtifactUsageEventInspector eventInspector = new ArtifactUsageEventInspector();
		ArtifactUsageCalculator calculator = mock(ArtifactUsageCalculator.class);
		eventInspector.setArtifactUsageCalculator(calculator);
		StorageFileItem item = mock(StorageFileItem.class);
		when(item.getPath()).thenReturn("some.pom");
		RepositoryItemEvent event = mock(eventClass);
		when(event.getItem()).thenReturn(item);

		eventInspector.inspect(event);

		verify(calculator, only()).calculateArtifactUsage(item);
	}

	public void testRemoteEventWithPomFile() throws Exception {
		ArtifactUsageEventInspector eventInspector = new ArtifactUsageEventInspector();
		ArtifactUsageCalculator calculator = mock(ArtifactUsageCalculator.class);
		eventInspector.setArtifactUsageCalculator(calculator);
		StorageFileItem item = mock(StorageFileItem.class);
		when(item.getPath()).thenReturn("some.pom");
		RepositoryItemEvent event = mock(RepositoryItemEventDelete.class);
		when(event.getItem()).thenReturn(item);

		eventInspector.inspect(event);

		verify(calculator, only()).removeArtifactUsage(item);
	}

	protected void testEventWithJarFile(
			Class<? extends RepositoryItemEvent> eventClass) throws Exception {
		ArtifactUsageEventInspector eventInspector = new ArtifactUsageEventInspector();
		ArtifactUsageCalculator calculator = mock(ArtifactUsageCalculator.class);
		eventInspector.setArtifactUsageCalculator(calculator);
		StorageFileItem item = mock(StorageFileItem.class);
		when(item.getPath()).thenReturn("some.jar");
		RepositoryItemEvent event = mock(eventClass);
		when(event.getItem()).thenReturn(item);

		eventInspector.inspect(event);

		verifyZeroInteractions(calculator);
	}

}
