package org.ebayopensource.nexus.plugins.artifactusage.view;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractNexusIndexHtmlCustomizer;
import org.sonatype.nexus.plugins.rest.NexusIndexHtmlCustomizer;

/**
 * Defines the JavaScript file that adds the UI components for the Artifact
 * Usage functionality.
 * 
 * @author Saleem Shafi
 */
@Component(role = NexusIndexHtmlCustomizer.class, hint = "ArtifactUsageIndexHtmlCustomizer")
public class ArtifactUsageIndexHtmlCustomizer extends
		AbstractNexusIndexHtmlCustomizer {

	@Override
	public String getPostHeadContribution(Map<String, Object> ctx) {
		String version = getVersionFromJarFile("/META-INF/maven/org.ebayopensource.nexus.plugins/artifact-usage-plugin/pom.properties");

		return "<script src=\"js/repoServer/nexus-artifact-usage-plugin-all.js"
				+ (version == null ? "" : "?" + version)
				+ "\" type=\"text/javascript\" charset=\"utf-8\"></script>";
	}

}
