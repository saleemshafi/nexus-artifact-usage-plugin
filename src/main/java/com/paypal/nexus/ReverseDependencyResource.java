package com.paypal.nexus;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.rest.groups.RepositoryGroupContentPlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
@Component( role = PlexusResource.class, hint = "com.paypal.nexus.ReverseDependencyResource" )
public class ReverseDependencyResource
    extends RepositoryGroupContentPlexusResource
{
    @Override
	public String getResourceUri() {
        return "/repo_groups/{" + GROUP_ID_KEY + "}/dependees";
	}

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
    	getLogger().info("getting dependees for "+request.getResourceRef().getIdentifier());
        ResourceStoreRequest req = getResourceStoreRequest( request );
        DependeeListResourceResponse res = new DependeeListResourceResponse();
        res.addDependee(new Dependee("group1","artifact123","1.0","jar"));
        res.addDependee(new Dependee("group1","artifact123","2.0","jar"));
        return res;
    }
    
}
