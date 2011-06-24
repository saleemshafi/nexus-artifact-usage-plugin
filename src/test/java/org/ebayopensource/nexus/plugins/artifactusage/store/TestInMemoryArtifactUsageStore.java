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
		GAV levelA_artifact1 = new GAV("a:one:1.0"); // used by 2
		GAV levelA_artifact2 = new GAV("a:two:1.0"); // used by 1
		GAV levelB_artifact1 = new GAV("b:one:1.0"); // used by 2
		GAV levelB_artifact2 = new GAV("b:two:1.0"); // used by 1
		GAV levelC_artifact1 = new GAV("c:one:1.0"); // unused
		GAV levelC_artifact2 = new GAV("c:two:1.0"); // unused

		store.addDependencies(levelA_artifact1, Collections.EMPTY_LIST, null); // uses
																				// none
		store.addDependencies(levelA_artifact2, Collections.EMPTY_LIST, null);
		store.addDependencies(levelB_artifact1, // uses 1, single level
				Arrays.asList(new GAV[] { levelA_artifact1 }), null);
		store.addDependencies(
				levelB_artifact2, // uses two, single level
				Arrays.asList(new GAV[] { levelA_artifact1, levelA_artifact2 }),
				null);
		store.addDependencies(levelC_artifact1, // uses one, multiple levels
				Arrays.asList(new GAV[] { levelB_artifact1 }), null);
		store.addDependencies(
				levelC_artifact2, // uses two, multiple levels
				Arrays.asList(new GAV[] { levelB_artifact1, levelB_artifact2 }),
				null);
	}

	@Test
	public void testArtifactUsersForUnknownArtifact() throws Exception {
		Collection<ArtifactUser> users = store.getArtifactUsers(new GAV(
				"unknown:unknown:1.0"));
		Assert.assertNotNull(users);
		Assert.assertEquals(0, users.size());
	}

	@Test
	public void testArtifactUsersForUnusedArtifact() throws Exception {
		Collection<ArtifactUser> users = store.getArtifactUsers(new GAV(
				"c:one:1.0"));
		Assert.assertNotNull(users);
		Assert.assertEquals(0, users.size());
	}

	@Test
	public void testArtifactUsersForSingleResult() throws Exception {
		Collection<ArtifactUser> users = store.getArtifactUsers(new GAV(
				"b:two:1.0"));
		Assert.assertNotNull(users);
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(users
				.contains(new ArtifactUser(new GAV("c:two:1.0"))));
	}

	@Test
	public void testArtifactUsersForMultipleResults() throws Exception {
		Collection<ArtifactUser> users = store.getArtifactUsers(new GAV(
				"a:one:1.0"));
		Assert.assertNotNull(users);
		Assert.assertEquals(2, users.size());
		Assert.assertTrue(users
				.contains(new ArtifactUser(new GAV("b:one:1.0"))));
		Assert.assertTrue(users
				.contains(new ArtifactUser(new GAV("b:two:1.0"))));
	}

	@Test
	public void testArtifactUsersForNestedResults() throws Exception {
		Collection<ArtifactUser> users = store.getArtifactUsers(new GAV(
				"a:two:1.0"));
		Assert.assertNotNull(users);
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(users
				.contains(new ArtifactUser(new GAV("b:two:1.0"))));
	}

	@Test
	public void testTransitiveArtifactUsersForUnusedArtifact() throws Exception {
		Collection<ArtifactUser> users = store.getArtifactUsers(new GAV(
				"c:one:1.0"));
		Assert.assertNotNull(users);
		Assert.assertEquals(0, users.size());
	}

	@Test
	public void testTransitiveArtifactUsersForSingleLevelArtifact()
			throws Exception {
		Collection<ArtifactUser> users = store.getArtifactUsers(new GAV(
				"b:two:1.0"));
		Assert.assertNotNull(users);
		Assert.assertEquals(1, users.size());
		ArtifactUser loneUser = users.iterator().next();
		Assert.assertTrue(loneUser.getArtifactUsers().isEmpty());
	}

	@Test
	public void testTransitiveArtifactUsersForNestedResults() throws Exception {
		Collection<ArtifactUser> users = store.getArtifactUsers(new GAV(
				"a:one:1.0"));
		Assert.assertNotNull(users);
		Assert.assertFalse(users.isEmpty());
		ArtifactUser user = users.iterator().next();
		Assert.assertFalse(user.getArtifactUsers().isEmpty());
	}

	@Test
	public void testRemoveLoneArtifact() throws Exception {
		GAV originalDep = new GAV("b:two:1.0");
		GAV loneUser = new GAV("c:two:1.0");

		Collection<ArtifactUser> users = store.getArtifactUsers(originalDep);
		Assert.assertNotNull(users);
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(users.contains(new ArtifactUser(loneUser)));
		store.removeArtifact(loneUser);
		users = store.getArtifactUsers(originalDep);
		Assert.assertNotNull(users);
		Assert.assertEquals(0, users.size());
	}

	@Test
	public void testRemoveOneOfMultipeArtifacts() throws Exception {
		GAV originalDep = new GAV("a:one:1.0");
		GAV oneUser = new GAV("b:two:1.0");
		GAV otherUser = new GAV("b:one:1.0");

		Collection<ArtifactUser> users = store.getArtifactUsers(originalDep);
		Assert.assertNotNull(users);
		Assert.assertEquals(2, users.size());
		Assert.assertTrue(users.contains(new ArtifactUser(oneUser)));
		Assert.assertTrue(users.contains(new ArtifactUser(otherUser)));

		store.removeArtifact(oneUser);
		users = store.getArtifactUsers(originalDep);
		Assert.assertNotNull(users);
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(users.contains(new ArtifactUser(otherUser)));
	}

	@Test
	public void testIsAlreadyCalculateForOldArtifact() throws Exception {
		long modificationTime = System.currentTimeMillis() - 10;
		GAV pathArtifact = new GAV("old", "artifact", "1.0");
		store.addDependencies(pathArtifact,
				Arrays.asList(new GAV[] { new GAV("dep:dep:1.0") }), "/path");
		Assert.assertTrue(store.isAlreadyCalculated("/path", modificationTime));
	}

	public void testIsAlreadyCalculatedAfterChange() throws Exception {
		GAV pathArtifact = new GAV("old", "artifact", "1.0");
		store.addDependencies(pathArtifact,
				Arrays.asList(new GAV[] { new GAV("dep:dep:1.0") }), "/path");
		long modificationTime = System.currentTimeMillis() + 10;
		Assert.assertFalse(store.isAlreadyCalculated("/path", modificationTime));
	}
}
