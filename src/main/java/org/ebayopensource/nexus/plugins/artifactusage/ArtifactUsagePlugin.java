package org.ebayopensource.nexus.plugins.artifactusage;

import org.eclipse.sisu.EagerSingleton;
import org.sonatype.nexus.plugin.PluginIdentity;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@EagerSingleton
public class ArtifactUsagePlugin extends PluginIdentity {

	public static final String ID_PREFIX = "artifact-usage";

	public static final String GROUP_ID = "org.ebayopensource.nexus.plugins";

	public static final String ARTIFACT_ID = ID_PREFIX + "-plugin";

	@Inject
	public ArtifactUsagePlugin() throws Exception {
		super(GROUP_ID, ARTIFACT_ID);
	}
}
