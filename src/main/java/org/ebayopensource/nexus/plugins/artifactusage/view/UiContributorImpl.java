package org.ebayopensource.nexus.plugins.artifactusage.view;

import org.ebayopensource.nexus.plugins.artifactusage.ArtifactUsagePlugin;
import org.sonatype.nexus.plugins.ui.contribution.UiContributorSupport;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
public class UiContributorImpl extends UiContributorSupport {

    @Inject
    public UiContributorImpl(final ArtifactUsagePlugin owner) {
        super(owner);
    }

}
