package org.ebayopensource.nexus.reversedep.rest;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.ebayopensource.nexus.reversedep.store.Artifact;
import org.ebayopensource.nexus.reversedep.store.ReverseDependencyStore;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Produces({ "application/xml", "application/json" })
@Consumes({ "application/xml", "application/json" })
@Component(role = PlexusResource.class, hint = "org.ebayopensource.nexus.reversedep.rest.DependeeListResource")
public class DependeeListResource extends AbstractNexusPlexusResource {
	@Requirement(hint = "InMemory")
	private ReverseDependencyStore dependeeStore;

	@Override
	public String getResourceUri() {
		return "/dependees";
	}

	@Override
	public Object get(Context context, Request request, Response response,
			Variant variant) throws ResourceException {
		if (getLogger().isDebugEnabled()) {
			getLogger().debug(
					"getting dependees for "
							+ request.getResourceRef().getLastSegment());
		}
		DependeeListResourceResponse res = new DependeeListResourceResponse();

		// TODO: would love the ability to get multiple levels in one request,
		// perhaps even the whole tree
		Collection<Artifact> dependees = dependeeStore
				.getDependees(new Artifact(request.getResourceRef()
						.getLastSegment()));
		for (Artifact dependee : dependees) {
			res.addDependee(dependee);
		}
		return res;
	}

	@Override
	public PathProtectionDescriptor getResourceProtection() {
		return new PathProtectionDescriptor("/dependees", "authcBasic,tgperms");
	}

	@Override
	public Object getPayloadInstance() {
		return null;
	}

}
