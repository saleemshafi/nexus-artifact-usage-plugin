package org.ebayopensource.nexus.plugins.artifactusage.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.ebayopensource.nexus.plugins.artifactusage.task.ArtifactUsageCalculator;
import org.slf4j.Logger;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.StorageFileItem;

import junit.framework.TestCase;

public class TestArtifactUsageEventInspector extends TestCase {

	public void testCacheEventWithPomFile() throws Exception {
		final ArtifactUsageCalculator calculator = mock(ArtifactUsageCalculator.class);
		final ArtifactUsageEventInspector eventInspector = new ArtifactUsageEventInspector(
				mock(Logger.class), calculator);
		final RepositoryItemEventCache event = mock(RepositoryItemEventCache.class);
		final StorageFileItem item = mockEvent(event, "some.pom");

		eventInspector.onItemCache(event);

		verify(calculator, only()).calculateArtifactUsage(item);
	}

	public void testStoreEventWithPomFile() throws Exception {
		final ArtifactUsageCalculator calculator = mock(ArtifactUsageCalculator.class);
		final ArtifactUsageEventInspector eventInspector = new ArtifactUsageEventInspector(
				mock(Logger.class), calculator);
		final RepositoryItemEventStore event = mock(RepositoryItemEventStore.class);
		final StorageFileItem item = mockEvent(event, "some.pom");

		eventInspector.onItemStore(event);

		verify(calculator, only()).calculateArtifactUsage(item);
	}

	public void testDeleteEventWithPomFile() throws Exception {
		final ArtifactUsageCalculator calculator = mock(ArtifactUsageCalculator.class);
		final ArtifactUsageEventInspector eventInspector = new ArtifactUsageEventInspector(
				mock(Logger.class), calculator);
		final RepositoryItemEventDelete event = mock(RepositoryItemEventDelete.class);
		final StorageFileItem item = mockEvent(event, "some.pom");

		eventInspector.onItemDelete(event);

		verify(calculator, only()).removeArtifactUsage(item);
	}

	public void testCacheEventWithJarFile() throws Exception {
		final ArtifactUsageCalculator calculator = mock(ArtifactUsageCalculator.class);
		final ArtifactUsageEventInspector eventInspector = new ArtifactUsageEventInspector(
				mock(Logger.class), calculator);
		final RepositoryItemEventCache event = mock(RepositoryItemEventCache.class);
		mockEvent(event, "some.jar");

		eventInspector.onItemCache(event);

		verifyZeroInteractions(calculator);
	}

	public void testStoreEventWithJarFile() throws Exception {
		final ArtifactUsageCalculator calculator = mock(ArtifactUsageCalculator.class);
		final ArtifactUsageEventInspector eventInspector = new ArtifactUsageEventInspector(
				mock(Logger.class), calculator);
		final RepositoryItemEventStore event = mock(RepositoryItemEventStore.class);
		mockEvent(event, "some.jar");

		eventInspector.onItemStore(event);

		verifyZeroInteractions(calculator);
	}

	public void testDeleteEventWithJarFile() throws Exception {
		final ArtifactUsageCalculator calculator = mock(ArtifactUsageCalculator.class);
		final ArtifactUsageEventInspector eventInspector = new ArtifactUsageEventInspector(
				mock(Logger.class), calculator);
		final RepositoryItemEventDelete event = mock(RepositoryItemEventDelete.class);
		mockEvent(event, "some.jar");

		eventInspector.onItemDelete(event);

		verifyZeroInteractions(calculator);
	}

	protected StorageFileItem mockEvent(final RepositoryItemEvent event, final String filename) {
		final StorageFileItem item = mock(StorageFileItem.class);
		when(item.getPath()).thenReturn(filename);
		when(event.getItem()).thenReturn(item);
		return item;
	}
}
