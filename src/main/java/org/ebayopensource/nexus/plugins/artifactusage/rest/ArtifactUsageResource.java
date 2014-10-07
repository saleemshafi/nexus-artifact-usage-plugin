package org.ebayopensource.nexus.plugins.artifactusage.rest;

import java.util.Collection;

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
@Named("org.ebayopensource.nexus.plugins.artifactusage.rest.ArtifactUsageResource")
public class ArtifactUsageResource extends AbstractNexusPlexusResource {

	private final ArtifactUsageStore artifactUsageStore;

	@Inject
	public ArtifactUsageResource(@Named("InMemory") ArtifactUsageStore artifactUsageStore) {
		this.artifactUsageStore = artifactUsageStore;
	}

	@Override
	public String getResourceUri() {
		return "/usage";
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
		// limiting depth of the data to n levels so that we don't stall out
		String jsonText = "{" + ArtifactUsageSerializer.toJson(artifactList, 5)
				+ "}";

		return new StringRepresentation(jsonText, MediaType.APPLICATION_JSON);
	}

	@Override
	public PathProtectionDescriptor getResourceProtection() {
		return new PathProtectionDescriptor("/usage", "authcBasic,tgperms");
	}

	@Override
	public Object getPayloadInstance() {
		return null;
	}
}
