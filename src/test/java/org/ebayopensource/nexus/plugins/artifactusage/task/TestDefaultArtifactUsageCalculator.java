package org.ebayopensource.nexus.plugins.artifactusage.task;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.ebayopensource.nexus.plugins.artifactusage.store.GAV;
import org.mockito.Mockito;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.test.NexusTestSupport;

public class TestDefaultArtifactUsageCalculator extends NexusTestSupport {
	public void testCalculateArtifactUsageForRepository() throws Exception {
		DefaultArtifactUsageCalculator calculator = getCalculator();
		// calculator.calculateArtifactUsage(repository);
	}

	public void testCalculateArtifactUsageOfArtifact() throws Exception {
		DefaultArtifactUsageCalculator calculator = getCalculator();
		// calculator.calculateArtifactUsage(item);
	}

	DefaultArtifactUsageCalculator getCalculator() throws Exception {
		return (DefaultArtifactUsageCalculator) this
				.lookup(ArtifactUsageCalculator.class);
	}

	public void testGetGavFromSimplePom() throws Exception {
		StorageFileItem pomFileItem = getMockStorageFileItem("src/test/remote-repo/test/artifactusage/simple-artifact/1.0/simple-artifact-1.0.pom");

		GAV artifact = ((DefaultArtifactUsageCalculator) this
				.lookup(ArtifactUsageCalculator.class))
				.getArtifactForStorageItem(pomFileItem);
		assertNotNull(artifact);
		assertEquals("test.artifactusage", artifact.getGroupId());
		assertEquals("simple-artifact", artifact.getArtifactId());
		assertEquals("1.0", artifact.getVersion());
	}

	public void testGetGavFromPomWithParent() throws Exception {
		StorageFileItem pomFileItem = getMockStorageFileItem("src/test/remote-repo/test/artifactusage/artifact-with-parent/1.0/artifact-with-parent-1.0.pom");

		GAV artifact = ((DefaultArtifactUsageCalculator) this
				.lookup(ArtifactUsageCalculator.class))
				.getArtifactForStorageItem(pomFileItem);
		assertNotNull(artifact);
		assertEquals("test.artifactusage", artifact.getGroupId());
		assertEquals("artifact-with-parent", artifact.getArtifactId());
		assertEquals("1.9.1", artifact.getVersion());

	}

	public void testGetGavFromPomWithProperties() throws Exception {
		StorageFileItem pomFileItem = getMockStorageFileItem("src/test/remote-repo/test/artifactusage/artifact-with-properties/1.0/artifact-with-properties-1.0.pom");

		GAV artifact = ((DefaultArtifactUsageCalculator) this
				.lookup(ArtifactUsageCalculator.class))
				.getArtifactForStorageItem(pomFileItem);
		assertNotNull(artifact);
		assertEquals("test.artifactusage", artifact.getGroupId());
		assertEquals("artifact-with-properties", artifact.getArtifactId());
		assertEquals("${this-version}", artifact.getVersion());

	}

	StorageFileItem getMockStorageFileItem(String path) throws IOException {
		InputStream input = new FileInputStream(path);
		RepositoryItemUid itemUid = Mockito.mock(RepositoryItemUid.class);
		Mockito.when(itemUid.getPath()).thenReturn(path);

		StorageFileItem pomFileItem = Mockito.mock(StorageFileItem.class);
		Mockito.when(pomFileItem.getRepositoryItemUid()).thenReturn(itemUid);
		Mockito.when(pomFileItem.getInputStream()).thenReturn(input);
		Mockito.when(pomFileItem.getModified()).thenReturn(
				System.currentTimeMillis());
		return pomFileItem;
	}

}
