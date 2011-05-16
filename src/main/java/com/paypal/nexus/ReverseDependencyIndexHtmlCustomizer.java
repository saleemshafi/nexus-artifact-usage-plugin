package com.paypal.nexus;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractNexusIndexHtmlCustomizer;
import org.sonatype.nexus.plugins.rest.NexusIndexHtmlCustomizer;

@Component( role = NexusIndexHtmlCustomizer.class, hint = "ReverseDependencyIndexHtmlCustomizer" )
public class ReverseDependencyIndexHtmlCustomizer extends AbstractNexusIndexHtmlCustomizer {
    @Override
    public String getPostHeadContribution( Map<String, Object> ctx )
    {
        String version = getVersionFromJarFile( "/META-INF/maven/com.paypal.nexus/reverse-dependency-plugin/pom.properties" );

        return "<script src=\"js/repoServer/nexus-plugin-reverse-dependency-plugin-all.js"
            + ( version == null ? "" : "?" + version ) + "\" type=\"text/javascript\" charset=\"utf-8\"></script>";
    }

}
