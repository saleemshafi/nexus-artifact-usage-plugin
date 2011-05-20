package com.paypal.nexus;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.plexus.appevents.Event;

@Component( role = EventInspector.class, hint = "ReverseDependencyEventInspector" )
public class ReverseDependencyEventInspector extends AbstractEventInspector {

	public boolean accepts(Event<?> evt) {
		return evt instanceof RepositoryItemEventStore || evt instanceof RepositoryItemEventDelete;
	}

	public void inspect(Event<?> evt) {
		if (evt instanceof RepositoryItemEventStore) {
			onItemStore((RepositoryItemEventStore)evt);
		} else {
			onItemDelete((RepositoryItemEventDelete)evt);
		}
	}

	private void onItemDelete(RepositoryItemEventDelete evt) {
		String artifactName = evt.getItemUid().getPath();
		if (artifactName.endsWith(".pom")) {
			System.out.println(evt.getItem().getAttributes().keySet());
		}
	}

	private void onItemStore(RepositoryItemEventStore evt) {
		String artifactName = evt.getItemUid().getPath();
		if (artifactName.endsWith(".pom")) {
			System.out.println(evt.getItem().getAttributes().keySet());
		}
	}

}
