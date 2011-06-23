package org.ebayopensource.nexus.plugins.artifactusage.store;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestInMemoryArtifactUsageStore extends TestCase {
	private InMemoryArtifactUsageStore store;

	@Before
	public void setUp() throws Exception {
		store = new InMemoryArtifactUsageStore();
		Artifact levelA_artifact1 = new Artifact("a:one:1.0"); // used by 2
		Artifact levelA_artifact2 = new Artifact("a:two:1.0"); // used by 1
		Artifact levelB_artifact1 = new Artifact("b:one:1.0"); // used by 2
		Artifact levelB_artifact2 = new Artifact("b:two:1.0"); // used by 1
		Artifact levelC_artifact1 = new Artifact("c:one:1.0"); // unused
		Artifact levelC_artifact2 = new Artifact("c:two:1.0"); // unused

		store.addDependencies(levelA_artifact1, Collections.EMPTY_LIST); // uses
																			// none
		store.addDependencies(levelA_artifact2, Collections.EMPTY_LIST);
		store.addDependencies(levelB_artifact1, // uses 1, single level
				Arrays.asList(new Artifact[] { levelA_artifact1 }));
		store.addDependencies(
				levelB_artifact2, // uses two, single level
				Arrays.asList(new Artifact[] { levelA_artifact1,
						levelA_artifact2 }));
		store.addDependencies(levelC_artifact1, // uses one, multiple levels
				Arrays.asList(new Artifact[] { levelB_artifact1 }));
		store.addDependencies(
				levelC_artifact2, // uses two, multiple levels
				Arrays.asList(new Artifact[] { levelB_artifact1,
						levelB_artifact2 }));
	}

	@Test
	public void testArtifactUsersForUnknownArtifact() throws Exception {
		Collection<Artifact> users = store.getArtifactUsers(new Artifact(
				"unknown:unknown:1.0"));
		Assert.assertNotNull(users);
		Assert.assertEquals(0, users.size());
	}

	@Test
	public void testArtifactUsersForUnusedArtifact() throws Exception {
		Collection<Artifact> users = store.getArtifactUsers(new Artifact(
				"c:one:1.0"));
		Assert.assertNotNull(users);
		Assert.assertEquals(0, users.size());
	}

	@Test
	public void testArtifactUsersForSingleResult() throws Exception {
		Collection<Artifact> users = store.getArtifactUsers(new Artifact(
				"b:two:1.0"));
		Assert.assertNotNull(users);
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(users.contains(new Artifact("c:two:1.0")));
	}

	@Test
	public void testArtifactUsersForMultipleResults() throws Exception {
		Collection<Artifact> users = store.getArtifactUsers(new Artifact(
				"a:one:1.0"));
		Assert.assertNotNull(users);
		Assert.assertEquals(2, users.size());
		Assert.assertTrue(users.contains(new Artifact("b:one:1.0")));
		Assert.assertTrue(users.contains(new Artifact("b:two:1.0")));
	}

	@Test
	public void testArtifactUsersForNestedResults() throws Exception {
		Collection<Artifact> users = store.getArtifactUsers(new Artifact(
				"a:two:1.0"));
		Assert.assertNotNull(users);
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(users.contains(new Artifact("b:two:1.0")));
	}

	@Test
	public void testTransitiveArtifactUsersForUnusedArtifact() throws Exception {
		Collection<ArtifactUser> users = store
				.getArtifactUsersTransitively(new Artifact("c:one:1.0"));
		Assert.assertNotNull(users);
		Assert.assertEquals(0, users.size());
	}

	@Test
	public void testTransitiveArtifactUsersForSingleLevelArtifact()
			throws Exception {
		Collection<ArtifactUser> users = store
				.getArtifactUsersTransitively(new Artifact("b:two:1.0"));
		Assert.assertNotNull(users);
		Assert.assertEquals(1, users.size());
		ArtifactUser loneUser = users.iterator().next();
		Assert.assertTrue(loneUser.getArtifactUsers().isEmpty());
	}

	@Test
	public void testTransitiveArtifactUsersForNestedResults() throws Exception {
		Collection<ArtifactUser> users = store
				.getArtifactUsersTransitively(new Artifact("a:one:1.0"));
		Assert.assertNotNull(users);
		Assert.assertFalse(users.isEmpty());
		ArtifactUser user = users.iterator().next();
		Assert.assertFalse(user.getArtifactUsers().isEmpty());
	}

	@Test
	public void testRemoveLoneArtifact() throws Exception {
		Artifact originalDep = new Artifact("b:two:1.0");
		Artifact loneUser = new Artifact("c:two:1.0");

		Collection<Artifact> users = store.getArtifactUsers(originalDep);
		Assert.assertNotNull(users);
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(users.contains(loneUser));
		store.removeArtifact(loneUser);
		users = store.getArtifactUsers(originalDep);
		Assert.assertNotNull(users);
		Assert.assertEquals(0, users.size());
	}

	@Test
	public void testRemoveOneOfMultipeArtifacts() throws Exception {
		Artifact originalDep = new Artifact("a:one:1.0");
		Artifact oneUser = new Artifact("b:two:1.0");
		Artifact otherUser = new Artifact("b:one:1.0");

		Collection<Artifact> users = store.getArtifactUsers(originalDep);
		Assert.assertNotNull(users);
		Assert.assertEquals(2, users.size());
		Assert.assertTrue(users.contains(oneUser));
		Assert.assertTrue(users.contains(otherUser));

		store.removeArtifact(oneUser);
		users = store.getArtifactUsers(originalDep);
		Assert.assertNotNull(users);
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(users.contains(otherUser));
	}

	@Test
	public void testIsAlreadyCalculateForOldArtifact() throws Exception {
		long modificationTime = System.currentTimeMillis() - 10;
		Artifact pathArtifact = new Artifact("old", "artifact", "1.0", "/path");
		store.addDependencies(pathArtifact,
				Arrays.asList(new Artifact[] { new Artifact("dep:dep:1.0") }));
		Assert.assertTrue(store.isAlreadyCalculated(pathArtifact.getPath(),
				modificationTime));
	}

	public void testIsAlreadyCalculatedAfterChange() throws Exception {
		Artifact pathArtifact = new Artifact("old", "artifact", "1.0", "/path");
		store.addDependencies(pathArtifact,
				Arrays.asList(new Artifact[] { new Artifact("dep:dep:1.0") }));
		long modificationTime = System.currentTimeMillis() + 10;
		Assert.assertFalse(store.isAlreadyCalculated(pathArtifact.getPath(),
				modificationTime));
	}
}
