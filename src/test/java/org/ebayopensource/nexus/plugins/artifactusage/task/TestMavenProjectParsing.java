package org.ebayopensource.nexus.plugins.artifactusage.task;

import java.io.FileInputStream;
import java.io.InputStream;

import org.ebayopensource.nexus.plugins.artifactusage.store.Artifact;
import org.mockito.Mockito;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;

public class TestMavenProjectParsing extends AbstractNexusTestCase {
	public void testGetGavFromSimplePom() throws Exception {
		// convert to a Maven project
		InputStream input = new FileInputStream(
				"src/test/test-resources/simple-pom.xml");
		RepositoryItemUid itemUid = Mockito.mock(RepositoryItemUid.class);
		Mockito.when(itemUid.getPath()).thenReturn(
				"src/test/test-resources/simple-pom.xml");

		StorageFileItem pomFileItem = Mockito.mock(StorageFileItem.class);
		Mockito.when(pomFileItem.getRepositoryItemUid()).thenReturn(itemUid);
		Mockito.when(pomFileItem.getInputStream()).thenReturn(input);

		Artifact artifact = ((DefaultArtifactUsageCalculator) this
				.lookup(ArtifactUsageCalculator.class))
				.getArtifactForStorageItem(pomFileItem);
		assertNotNull(artifact);
		assertEquals("org.ebayopensource.nexus.plugins", artifact.getGroupId());
		assertEquals("artifact-usage-plugin", artifact.getArtifactId());
		assertEquals("1.0-SNAPSHOT", artifact.getVersion());
	}

	public void testGetGavFromPomWithParent() throws Exception {
		// convert to a Maven project
		InputStream input = new FileInputStream(
				"src/test/test-resources/pom-with-parent.xml");
		RepositoryItemUid itemUid = Mockito.mock(RepositoryItemUid.class);
		Mockito.when(itemUid.getPath()).thenReturn(
				"src/test/test-resources/pom-with-parent.xml");

		StorageFileItem pomFileItem = Mockito.mock(StorageFileItem.class);
		Mockito.when(pomFileItem.getRepositoryItemUid()).thenReturn(itemUid);
		Mockito.when(pomFileItem.getInputStream()).thenReturn(input);

		Artifact artifact = ((DefaultArtifactUsageCalculator) this
				.lookup(ArtifactUsageCalculator.class))
				.getArtifactForStorageItem(pomFileItem);
		assertNotNull(artifact);
		assertEquals("org.sonatype.nexus", artifact.getGroupId());
		assertEquals("nexus-rest-api", artifact.getArtifactId());
		assertEquals("1.9.1", artifact.getVersion());

	}

	public void testGetGavFromPomWithProperties() throws Exception {
		// convert to a Maven project
		InputStream input = new FileInputStream(
				"src/test/test-resources/pom-with-properties.xml");
		RepositoryItemUid itemUid = Mockito.mock(RepositoryItemUid.class);
		Mockito.when(itemUid.getPath()).thenReturn(
				"src/test/test-resources/pom-with-properties.xml");

		StorageFileItem pomFileItem = Mockito.mock(StorageFileItem.class);
		Mockito.when(pomFileItem.getRepositoryItemUid()).thenReturn(itemUid);
		Mockito.when(pomFileItem.getInputStream()).thenReturn(input);

		Artifact artifact = ((DefaultArtifactUsageCalculator) this
				.lookup(ArtifactUsageCalculator.class))
				.getArtifactForStorageItem(pomFileItem);
		assertNotNull(artifact);
		assertEquals("org.ebayopensource.nexus.plugins", artifact.getGroupId());
		assertEquals("artifact-usage-plugin", artifact.getArtifactId());
		assertEquals("${this-version}", artifact.getVersion());

	}
}
