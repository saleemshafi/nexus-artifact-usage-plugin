package org.ebayopensource.nexus.plugins.artifactusage.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * An in-memory representation of the artifact usage data. This version would
 * have to be recreated any time the server goes down and comes back up again.
 * 
 * @author Saleem Shafi
 */
@Component(role = ArtifactUsageStore.class, hint = "InMemory")
public class InMemoryArtifactUsageStore extends AbstractLogEnabled implements
		ArtifactUsageStore {

	private Map<Artifact, Collection<Artifact>> userMap = new HashMap<Artifact, Collection<Artifact>>();
	// we need this one, too, so that we can clean up old usage data if
	// an artifact is updated
	private Map<Artifact, DependencyList> dependencyMap = new HashMap<Artifact, DependencyList>();
	// allow us to convert the a StorageFileItem path to an Artifact so that we
	// can coordinate the IsAlreadyCalculated logic with the actual usage
	// stuff
	private Map<String, Artifact> pathMap = new HashMap<String, Artifact>();

	public Collection<Artifact> getArtifactUsers(Artifact artifact) {
		Collection<Artifact> users = userMap.get(artifact);
		if (users == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableCollection(users);
	}

	public Collection<ArtifactUser> getArtifactUsersTransitively(
			Artifact artifact) {
		Collection<ArtifactUser> artifactUsers = new ArrayList<ArtifactUser>();
		for (Artifact artifactUser : this.getArtifactUsers(artifact)) {
			ArtifactUser user = new ArtifactUser(artifactUser.getGroupId(),
					artifactUser.getArtifactId(), artifactUser.getVersion(),
					artifactUser.getPath());
			for (ArtifactUser transitiveUser : getArtifactUsersTransitively(artifact)) {
				user.addArtifactUser(transitiveUser);
			}
			artifactUsers.add(user);
		}
		return artifactUsers;
	}

	public void addDependencies(Artifact artifact,
			Collection<Artifact> dependencies) {
		// remove all of the old usage data
		DependencyList oldDependencies = dependencyMap.get(artifact);
		if (oldDependencies != null
				&& oldDependencies.getDependencies() != null) {
			this.removeArtifactUser(artifact, oldDependencies.getDependencies());
		}
		// remember the dependencies so that we can clean up next time
		DependencyList newDependencies = new DependencyList(dependencies);
		dependencyMap.put(artifact, newDependencies);

		// remember where the associated file is located so we can compare
		// file modification time next time we try to calculate
		if (artifact.getPath() != null) {
			this.pathMap.put(artifact.getPath(), artifact);
		}
		if (dependencies != null) {
			// go through all of the dependencies
			synchronized (this.userMap) {
				for (Artifact dependency : dependencies) {
					Collection<Artifact> users = userMap.get(dependency);
					if (users == null) {
						users = new HashSet<Artifact>();
						userMap.put(dependency, users);
					}
					// mark this as a user of the artifact
					users.add(artifact);
				}
			}
		}
	}

	public boolean isAlreadyCalculated(String path, long lastModifiedTime) {
		Artifact artifact = this.pathMap.get(path);
		if (artifact == null)
			return false;
		DependencyList dependencies = this.dependencyMap.get(artifact);
		return (dependencies != null && dependencies.getLastCalculated() > lastModifiedTime);
	}

	// Removing an artifact from this map is handled as the case of adding
	// an artifact with no dependencies
	public void removeArtifact(Artifact artifact) {
		this.addDependencies(artifact, null);
	}

	protected void removeArtifactUser(Artifact user,
			Collection<Artifact> artifacts) {
		for (Artifact artifact : artifacts) {
			Collection<Artifact> artifactUsers = userMap.get(artifact);
			if (artifactUsers != null) {
				artifactUsers.remove(user);
			}
		}
	}

}
