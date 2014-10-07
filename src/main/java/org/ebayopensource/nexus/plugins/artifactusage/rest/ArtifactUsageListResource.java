package org.ebayopensource.nexus.plugins.artifactusage.rest;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ebayopensource.nexus.plugins.artifactusage.store.ArtifactUsageStore;
import org.ebayopensource.nexus.plugins.artifactusage.store.ArtifactUser;
import org.ebayopensource.nexus.plugins.artifactusage.store.GAV;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

@Singleton
@Named("org.ebayopensource.nexus.plugins.artifactusage.rest.ArtifactUsageListResource")
public class ArtifactUsageListResource extends AbstractNexusPlexusResource {

	private final ArtifactUsageStore artifactUsageStore;

	@Inject
	public ArtifactUsageListResource(@Named("InMemory") ArtifactUsageStore artifactUsageStore) {
		this.artifactUsageStore = artifactUsageStore;
	}

	@Override
	public String getResourceUri() {
		return "/usageList";
	}

	@Override
	public Object get(Context context, Request request, Response response,
			Variant variant) throws ResourceException {
		if (getLogger().isDebugEnabled()) {
			getLogger().debug(
					"getting usage of "
							+ request.getResourceRef().getLastSegment());
		}
		Collection<ArtifactUser> artifactList = artifactUsageStore
				.getArtifactUsers(new GAV(request.getResourceRef()
						.getLastSegment()));

		Set<GAV> artifacts = new TreeSet<GAV>();
		collectArtifacts(artifacts, artifactList);
		String jsonText = "{" + ArtifactUsageSerializer.toJson(artifacts) + "}";

		return new StringRepresentation(jsonText, MediaType.APPLICATION_JSON);
	}

	protected void collectArtifacts(Set<GAV> artifacts,
			Collection<ArtifactUser> users) {
		for (ArtifactUser user : users) {
			GAV userGav = user.getGav();
			if (artifacts.add(userGav)) {
				collectArtifacts(artifacts, user.getArtifactUsers());
			}
		}
	}

	@Override
	public PathProtectionDescriptor getResourceProtection() {
		return new PathProtectionDescriptor("/usageList", "authcBasic,tgperms");
	}

	@Override
	public Object getPayloadInstance() {
		return null;
	}
}
