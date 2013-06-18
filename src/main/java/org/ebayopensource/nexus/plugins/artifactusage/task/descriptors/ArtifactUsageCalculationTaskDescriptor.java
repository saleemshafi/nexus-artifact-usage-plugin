package org.ebayopensource.nexus.plugins.artifactusage.task.descriptors;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepoOrGroupComboFormField;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;

import java.util.ArrayList;
import java.util.List;

@Component(role = ScheduledTaskDescriptor.class, hint = "CalculateArtifactUsage", description = "Recalculate Artifact Usage")
public class ArtifactUsageCalculationTaskDescriptor

extends AbstractScheduledTaskDescriptor {
	public static final String ID = "CalculateArtifactUsageTask";
  public static final String REPO_OR_GROUP_FIELD_ID = "repositoryId";

	public String getId() {
		return ID;
	}

	public String getName() {
		return "Recalculate Artifact Usage";
	}

	@Override
	public List<FormField> formFields() {
		List<FormField> fields = new ArrayList<FormField>();
        fields.add( new RepoOrGroupComboFormField( REPO_OR_GROUP_FIELD_ID,
                FormField.MANDATORY ) );
		return fields;
	}	
}