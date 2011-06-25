package org.ebayopensource.nexus.plugins.artifactusage.rest;

import java.util.Collection;

import org.ebayopensource.nexus.plugins.artifactusage.store.ArtifactUser;
import org.ebayopensource.nexus.plugins.artifactusage.store.GAV;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ArtifactUsageSerializer {
	public static String toJson(ArtifactUser artifact, int depthLimit) {
		String jsonText = "{";
		GAV gav = artifact.getGav();
		jsonText += "\"groupId\":\"" + gav.getGroupId() + "\",";
		jsonText += "\"artifactId\":\"" + gav.getArtifactId() + "\",";
		jsonText += "\"version\":\"" + gav.getVersion() + "\",";
		jsonText += "\"text\":\"" + gav.toString() + "\"";
		if (depthLimit > 1) {
			jsonText += ","
					+ toJson(artifact.getArtifactUsers(), depthLimit - 1);
		}
		jsonText += "}";
		return jsonText;
	}

	public static String toJson(Collection<ArtifactUser> users, int depthLimit) {
		String jsonText = "\"data\":[";
		boolean first = true;
		for (ArtifactUser artifact : users) {
			if (!first) {
				jsonText += ",";
			}
			first = false;
			jsonText += toJson(artifact, depthLimit);
		}
		jsonText += "]";
		return jsonText;
	}

	public static Element toXml(GAV gav, Document doc) {
		Element artifactElement = doc.createElement("artifact");
		artifactElement.setAttribute("groupId", gav.getGroupId());
		artifactElement.setAttribute("artifactId", gav.getArtifactId());
		artifactElement.setAttribute("version", gav.getVersion());
		return artifactElement;
	}

	public static void toXml(ArtifactUser artifact, Document doc,
			Element parentElement) {
		Element artifactElement = toXml(artifact.getGav(), doc);
		parentElement.appendChild(artifactElement);
		Element usersElement = doc.createElement("users");
		artifactElement.appendChild(usersElement);
		toXml(artifact.getArtifactUsers(), doc, usersElement);
	}

	public static void toXml(Collection<ArtifactUser> users, Document doc,
			Element parentElement) {
		for (ArtifactUser artifact : users) {
			toXml(artifact, doc, parentElement);
		}
	}

}
