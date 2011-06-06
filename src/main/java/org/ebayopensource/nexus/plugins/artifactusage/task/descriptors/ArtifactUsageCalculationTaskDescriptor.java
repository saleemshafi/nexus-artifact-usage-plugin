package org.ebayopensource.nexus.plugins.artifactusage.task.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.ScheduledTaskPropertyDescriptor;

@Component(role = ScheduledTaskDescriptor.class, hint = "CalculateArtifactUsage", description = "Recalculate Artifact Usage")
public class ArtifactUsageCalculationTaskDescriptor

extends AbstractScheduledTaskDescriptor {
	public static final String ID = "CalculateArtifactUsageTask";

	@Requirement(role = ScheduledTaskPropertyDescriptor.class, hint = "RepositoryOrGroup")
	private ScheduledTaskPropertyDescriptor repositoryOrGroupId;

	public String getId() {
		return ID;
	}

	public String getName() {
		return "Recalculate Artifact Usage";
	}

	public List<ScheduledTaskPropertyDescriptor> getPropertyDescriptors() {
		List<ScheduledTaskPropertyDescriptor> properties = new ArrayList<ScheduledTaskPropertyDescriptor>();
		properties.add(repositoryOrGroupId);

		return properties;
	}
}
