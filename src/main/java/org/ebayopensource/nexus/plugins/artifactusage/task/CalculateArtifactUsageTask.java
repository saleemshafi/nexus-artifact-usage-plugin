package org.ebayopensource.nexus.plugins.artifactusage.task;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.ebayopensource.nexus.plugins.artifactusage.task.descriptors.ArtifactUsageCalculationTaskDescriptor;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.scheduling.Scheduler;
import org.sonatype.scheduling.SchedulerTask;

/**
 * Scheduled task to build artifact usage data for all artifacts in a particular
 * repository or repository group. The ArtifactUsageEventInspector should handle
 * incremental changes to the artifact usage data, but this task is useful to 1)
 * supplement the event mechanism in case it misses something, and 2) to
 * "prime the pump" and build the initial mappings.
 * 
 * @author Saleem Shafi
 */
@Component(role = SchedulerTask.class, hint = ArtifactUsageCalculationTaskDescriptor.ID)
public class CalculateArtifactUsageTask extends
		AbstractNexusRepositoriesTask<ArtifactUsageCalculationResult> implements
		Initializable {

	public static final String CALCULATE_ARTIFACT_USAGE_ACTION = "CALCARTIFACTUSAGE";

	@Requirement
	private ArtifactUsageCalculator calculator;

	@Requirement
	private Scheduler scheduler;

	@Override
	protected ArtifactUsageCalculationResult doRun() throws Exception {
		ArtifactUsageCalculationResult result = new ArtifactUsageCalculationResult();
		ArtifactUsageCalculationRequest request = new ArtifactUsageCalculationRequest(
				getRepositoryId(), getRepositoryGroupId());
		this.calculator.calculateArtifactUsage(request);
		return result;
	}

	public void initialize() throws InitializationException {
		try {
			this.scheduler.submit(getAction(), this);
		} catch (Exception e) {
			throw new InitializationException(
					"Couldn't calculate artifact usage while initializing server",
					e);
		}
	}

	@Override
	protected String getAction() {
		return CALCULATE_ARTIFACT_USAGE_ACTION;
	}

	@Override
	protected String getMessage() {
		if (getRepositoryGroupId() != null) {
			return "Calculating artifact usage for repository group "
					+ getRepositoryGroupName();
		} else if (getRepositoryId() != null) {
			return "Calculating artifact usage for repository "
					+ getRepositoryName();
		} else {
			return "Calculating artifact usage for all registered repositories";
		}
	}

}
