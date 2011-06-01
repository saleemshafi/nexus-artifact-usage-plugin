package org.ebayopensource.nexus.reversedep.task.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.ScheduledTaskPropertyDescriptor;

@Component( role = ScheduledTaskDescriptor.class, hint = "CalculateReverseDependencies", description = "Recalculate Reverse Dependencies" )
public class ReverseDependencyCalculationTaskDescriptor

    extends AbstractScheduledTaskDescriptor
{
    public static final String ID = "CalculateReverseDependenciesTask";

    @Requirement( role = ScheduledTaskPropertyDescriptor.class, hint = "RepositoryOrGroup" )
    private ScheduledTaskPropertyDescriptor repositoryOrGroupId;

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Recalculate Reverse Dependencies";
    }

    public List<ScheduledTaskPropertyDescriptor> getPropertyDescriptors()
    {
        List<ScheduledTaskPropertyDescriptor> properties = new ArrayList<ScheduledTaskPropertyDescriptor>();
        properties.add( repositoryOrGroupId );

        return properties;
    }
}
