package org.ebayopensource.nexus.plugins.artifactusage.store;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * An in-memory representation of the artifact usage data. This version would
 * have to be recreated any time the server goes down and comes back up again.
 * 
 * @author Saleem Shafi
 */
@Singleton
@Named("InMemory")
public class InMemoryArtifactUsageStore implements ArtifactUsageStore {

	private Map<GAV, ArtifactUser> userMap = new ConcurrentHashMap<GAV, ArtifactUser>();
	// we need this one, too, so that we can clean up old usage data if
	// an artifact is updated
	private Map<GAV, DependencyList> dependencyMap = new HashMap<GAV, DependencyList>();
	// allow us to convert the a StorageFileItem path to an Artifact so that we
	// can coordinate the IsAlreadyCalculated logic with the actual usage
	// stuff
	private Map<String, GAV> pathMap = new HashMap<String, GAV>();

	public Collection<ArtifactUser> getArtifactUsers(GAV artifact) {
		Collection<ArtifactUser> users = getOrCreateArtifactUser(artifact)
				.getArtifactUsers();
		return users;
	}

	public void addDependencies(GAV artifact, Collection<GAV> dependencies,
			String artifactPath) {

		// remove all of the old usage data
		this.removeArtifactUser(artifact);

		// remember the dependencies so that we can clean up next time
		dependencyMap.put(artifact, new DependencyList(dependencies));

		// remember where the associated file is located so we can compare
		// file modification time next time we try to calculate
		if (artifactPath != null) {
			this.pathMap.put(artifactPath, artifact);
		}
		if (dependencies != null) {
			// go through all of the dependencies
			for (GAV dependency : dependencies) {
				ArtifactUser usage = getOrCreateArtifactUser(dependency);
				usage.addArtifactUser(getOrCreateArtifactUser(artifact));
			}
		}
	}

	private ArtifactUser getOrCreateArtifactUser(GAV artifact) {
		ArtifactUser usage = userMap.get(artifact);
		if (usage == null) {
			usage = new ArtifactUser(artifact);
			userMap.put(artifact, usage);
		}
		return usage;
	}

	public boolean isAlreadyCalculated(String path, long lastModifiedTime) {
		GAV artifact = this.pathMap.get(path);
		if (artifact == null)
			return false;
		DependencyList dependencies = this.dependencyMap.get(artifact);
		return (dependencies != null && dependencies.getLastCalculated() > lastModifiedTime);
	}

	// Removing an artifact from this map is handled as the case of adding
	// an artifact with no dependencies
	public void removeArtifact(GAV artifact) {
		this.addDependencies(artifact, null, null);
	}

	protected void removeArtifactUser(GAV user) {
		DependencyList oldDependencies = dependencyMap.get(user);
		if (oldDependencies != null
				&& oldDependencies.getDependencies() != null) {
			for (GAV dependency : oldDependencies.getDependencies()) {
				ArtifactUser dependencyUser = getOrCreateArtifactUser(dependency);
				dependencyUser
						.removeArtifactUser(getOrCreateArtifactUser(user));
			}
		}

	}

}
