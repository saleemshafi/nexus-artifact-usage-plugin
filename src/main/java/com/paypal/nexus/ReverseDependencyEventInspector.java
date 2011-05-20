package com.paypal.nexus;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.plexus.appevents.Event;

@Component( role = EventInspector.class, hint = "ReverseDependencyEventInspector" )
public class ReverseDependencyEventInspector extends AbstractEventInspector {

	public boolean accepts(Event<?> evt) {
		return evt instanceof RepositoryItemEventStore || evt instanceof RepositoryItemEventDelete;
	}

	public void inspect(Event<?> evt) {
		try {
		if (evt instanceof RepositoryItemEventStore) {
			onItemStore((RepositoryItemEventStore)evt);
		} else {
			onItemDelete((RepositoryItemEventDelete)evt);
		}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void onItemDelete(RepositoryItemEventDelete evt) throws IOException {
		StorageItem item = evt.getItem();
		if (item instanceof StorageFileItem && item.getPath().endsWith(".pom")) {
			InputStream input = ((StorageFileItem)item).getContentLocator().getContent();
			MavenProject project = getMavenProject(input);
			if (project != null) {
			System.out.println("!!!!!!!!!!!! Got "+project.getGroupId()+":"+project.getArtifactId());
			}
		}
	}

	private void onItemStore(RepositoryItemEventStore evt) throws IOException {
		StorageItem item = evt.getItem();
		if (item instanceof StorageFileItem && item.getPath().endsWith(".pom")) {
			InputStream input = ((StorageFileItem)item).getContentLocator().getContent();
			MavenProject project = getMavenProject(input);
			if (project != null) {
				System.out.println("!!!!!!!!!!!! Got "+project.getGroupId()+":"+project.getArtifactId());
			}
		}
	}
	
	private MavenProject getMavenProject(InputStream pomFile) {
		Model model = null;
		InputStreamReader reader = null;
		MavenProject project = null;
		MavenXpp3Reader mavenreader = new MavenXpp3Reader();
		try {
		    reader = new InputStreamReader(pomFile);
		    model = mavenreader.read(reader);
		    project = new MavenProject(model);
			
		}catch(Exception ex){ex.printStackTrace();}
		return project;
	}
	
}
