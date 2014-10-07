package org.ebayopensource.nexus.plugins.artifactusage.rest;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ebayopensource.nexus.plugins.artifactusage.store.ArtifactUsageStore;
import org.ebayopensource.nexus.plugins.artifactusage.store.ArtifactUser;
import org.ebayopensource.nexus.plugins.artifactusage.store.GAV;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

// TODO: Consider re-implementing this as a view provider and display full usage tree at once (no Ajax tree building)
@Produces({ "application/xml", "application/json" })
@Singleton
@Named("org.ebayopensource.nexus.plugins.artifactusage.rest.ArtifactUsageGraphResource")
public class ArtifactUsageGraphResource extends AbstractNexusPlexusResource {

	private final ArtifactUsageStore artifactUsageStore;

    @Inject
    public ArtifactUsageGraphResource(@Named("InMemory") ArtifactUsageStore artifactUsageStore) {
        this.artifactUsageStore = artifactUsageStore;
    }

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
		// Figure out the requested content type for the data
		String artifactGav = request.getResourceRef().getLastSegment();
		String type = "xml";
		if (artifactGav.endsWith(".json")) {
			artifactGav = artifactGav.substring(0, artifactGav.length() - 5);
			type = "json";
		} else if (artifactGav.endsWith(".xml")) {
			artifactGav = artifactGav.substring(0, artifactGav.length() - 4);
		}

		GAV gav = new GAV(artifactGav);
		Collection<ArtifactUser> artifactUsers = artifactUsageStore
				.getArtifactUsers(gav);

		// if the client wanted JSON, setup the appropriate Representation
		if ("json".equals(type)) {
			String jsonText = "{"
					+ ArtifactUsageSerializer.toJson(artifactUsers, 3) + "}";
			return new StringRepresentation(jsonText,
					MediaType.APPLICATION_JSON);
		} else {
			Document doc;
			try {
				doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
						.newDocument();
				Element rootElement = ArtifactUsageSerializer.toXml(gav, doc);
				doc.appendChild(rootElement);
				// default is XML, so no need to wrap it in a Representation
				ArtifactUsageSerializer.toXml(artifactUsers, doc, rootElement);
				return new DomRepresentation(MediaType.APPLICATION_XML, doc);
			} catch (ParserConfigurationException e) {
				throw new ResourceException(e);
			}
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
