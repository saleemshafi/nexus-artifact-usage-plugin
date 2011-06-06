package org.ebayopensource.nexus.plugins.artifactusage.rest;

import java.util.Collection;

import javax.ws.rs.Produces;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.ebayopensource.nexus.plugins.artifactusage.store.Artifact;
import org.ebayopensource.nexus.plugins.artifactusage.store.ArtifactUsageStore;
import org.ebayopensource.nexus.plugins.artifactusage.store.ArtifactUser;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import com.thoughtworks.xstream.XStream;

// TODO: Consider re-implementing this as a view provider and display full usage tree at once (no Ajax tree building)
@Produces({ "application/xml", "application/json" })
@Component(role = PlexusResource.class, hint = "org.ebayopensource.nexus.plugins.artifactusage.rest.ArtifactUsageGraphResource")
public class ArtifactUsageGraphResource extends AbstractNexusPlexusResource {
	@Requirement(hint = "InMemory")
	private ArtifactUsageStore artifactUsageStore;

	@Override
	public String getResourceUri() {
		return "/usageGraph";
	}

	@Override
	public Object get(Context context, Request request, Response response,
			Variant variant) throws ResourceException {
		if (getLogger().isDebugEnabled()) {
			getLogger().debug(
					"getting usage of "
							+ request.getResourceRef().getLastSegment());
		}
		ArtifactUsageGraphResourceResponse res = new ArtifactUsageGraphResourceResponse();

		// Figure out the requested content type for the data
		String artifactGav = request.getResourceRef().getLastSegment();
		String type = "xml";
		if (artifactGav.endsWith(".json")) {
			artifactGav = artifactGav.substring(0, artifactGav.length() - 5);
			type = "json";
		} else if (artifactGav.endsWith(".xml")) {
			artifactGav = artifactGav.substring(0, artifactGav.length() - 4);
		}

		Collection<ArtifactUser> artifactUsers = artifactUsageStore
				.getArtifactUsersTransitively(new Artifact(artifactGav));
		for (ArtifactUser user : artifactUsers) {
			res.addArtifactUser(user);
		}

		// if the client wanted JSON, setup the appropriate Representation
		if ("json".equals(type)) {
			XStream xstream = (XStream) context.getAttributes().get(
					PlexusRestletApplicationBridge.JSON_XSTREAM);
			XStreamRepresentation rep = new XStreamRepresentation(xstream, "",
					MediaType.APPLICATION_JSON);
			rep.setPayload(res);
			rep.setMediaType(MediaType.APPLICATION_JSON);
			return rep;
		} else {
			// default is XML, so no need to wrap it in a Representation
			return res;
		}
	}

	@Override
	public PathProtectionDescriptor getResourceProtection() {
		return new PathProtectionDescriptor("/usageGraph", "authcBasic,tgperms");
	}

	@Override
	public Object getPayloadInstance() {
		return null;
	}

}
