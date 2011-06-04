package org.ebayopensource.nexus.reversedep.rest;

import java.util.Collection;

import javax.ws.rs.Produces;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.ebayopensource.nexus.reversedep.store.Artifact;
import org.ebayopensource.nexus.reversedep.store.Dependee;
import org.ebayopensource.nexus.reversedep.store.ReverseDependencyStore;
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

@Produces({ "application/xml", "application/json" })
@Component(role = PlexusResource.class, hint = "org.ebayopensource.nexus.reversedep.rest.DependeeGraphResource")
public class DependeeGraphResource extends AbstractNexusPlexusResource {
	@Requirement(hint = "InMemory")
	private ReverseDependencyStore dependeeStore;

	@Override
	public String getResourceUri() {
		return "/dependeeGraph";
	}

	@Override
	public Object get(Context context, Request request, Response response,
			Variant variant) throws ResourceException {
		// TODO: Figure out why this is creating NPE
		// getLogger().info("getting dependees for "+request.getResourceRef().getLastSegment());
		DependeeGraphResourceResponse res = new DependeeGraphResourceResponse();

		// TODO: would love the ability to get multiple levels in one request,
		// perhaps even the whole tree
		String artifactGav = request.getResourceRef().getLastSegment();
		String type = "xml";
		if (artifactGav.endsWith(".json")) {
			artifactGav = artifactGav.substring(0, artifactGav.length() - 5);
			type = "json";
		} else if (artifactGav.endsWith(".xml")) {
			artifactGav = artifactGav.substring(0, artifactGav.length() - 4);
		}
		Collection<Dependee> dependees = dependeeStore
				.getTransitiveDependees(new Artifact(artifactGav));
		for (Dependee dependee : dependees) {
			res.addDependee(dependee);
		}

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
		return new PathProtectionDescriptor("/dependeeGraph",
				"authcBasic,tgperms");
	}

	@Override
	public Object getPayloadInstance() {
		return null;
	}

}
