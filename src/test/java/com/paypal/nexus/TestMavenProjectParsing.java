package com.paypal.nexus;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.factory.DefaultArtifactFactory;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.handler.manager.DefaultArtifactHandlerManager;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.ReflectionUtils;
import org.junit.Assert;
import org.junit.Test;

public class TestMavenProjectParsing {
	@Test
	public void testMavenProjectDependencies() throws Exception {
		// convert to a Maven project
		InputStream input = new FileInputStream("pom.xml");
		MavenProject project = null;//ReverseDependencyEventInspector.getMavenProject(input);
		Assert.assertNotNull(project);
		ArtifactFactory factory = new DefaultArtifactFactory();
		ArtifactHandlerManager handlerMgr = new DefaultArtifactHandlerManager();
		ReflectionUtils.setVariableValueInObject(factory, "artifactHandlerManager", handlerMgr);
		ReflectionUtils.setVariableValueInObject(handlerMgr, "artifactHandlers", new HashMap());
		project.setDependencyArtifacts(project.createArtifacts(factory, null, null));
		Set<Artifact> dependencies = project.getDependencyArtifacts();
		Assert.assertNotNull(dependencies);
		for (Artifact dependency : dependencies) {
			System.out.println(project.getArtifactId()+" depends on "+dependency.getId());
		}
	}
}
