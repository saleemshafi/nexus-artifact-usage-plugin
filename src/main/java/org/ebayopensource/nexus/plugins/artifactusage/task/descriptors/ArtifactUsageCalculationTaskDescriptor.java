package org.ebayopensource.nexus.plugins.artifactusage.task.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepoOrGroupComboFormField;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;

@Component(role = ScheduledTaskDescriptor.class, hint = "CalculateArtifactUsage", description = "Recalculate Artifact Usage")
public class ArtifactUsageCalculationTaskDescriptor

extends AbstractScheduledTaskDescriptor {
	public static final String ID = "CalculateArtifactUsageTask";

	public String getId() {
		return ID;
	}

	public String getName() {
		return "Recalculate Artifact Usage";
	}

	@Override
	public List<FormField> formFields() {
		List<FormField> fields = new ArrayList<FormField>();
        fields.add( new RepoOrGroupComboFormField( "repositoryId", "Repository/Group", 
        		"Select the repository or repository group to assign to this task.", 
        		true ) );
		return fields;
	}	
}