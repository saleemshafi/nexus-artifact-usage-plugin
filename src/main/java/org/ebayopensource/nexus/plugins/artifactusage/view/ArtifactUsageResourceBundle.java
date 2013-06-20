package org.ebayopensource.nexus.plugins.artifactusage.view;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.DefaultStaticResource;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.StaticResource;

/**
 * This class maps the URL reference to static resources to the physical
 * location of the corresponding resource.
 * 
 * @author Saleem Shafi
 */
@Component(role = NexusResourceBundle.class, hint = "pluginConsole")
public class ArtifactUsageResourceBundle extends AbstractNexusResourceBundle {
	@Override
	public List<StaticResource> getContributedResouces() {
		List<StaticResource> result = new ArrayList<StaticResource>();

		result.add(new DefaultStaticResource(
            getClass().getResource("/static/js/nexus-artifact-usage-plugin-all.js"),
            "/js/repoServer/nexus-artifact-usage-plugin-all.js",
            "application/x-javascript"));
    result.add(new DefaultStaticResource(
            getClass().getResource("/static/icons/jar-jar.png"),
            "/icons/repoServer/jar-jar.png",
            "image/png"));

		return result;
	}
}
