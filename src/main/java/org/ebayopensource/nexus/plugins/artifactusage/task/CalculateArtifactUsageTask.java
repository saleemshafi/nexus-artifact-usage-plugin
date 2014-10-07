package org.ebayopensource.nexus.plugins.artifactusage.task;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.ebayopensource.nexus.plugins.artifactusage.task.descriptors.ArtifactUsageCalculationTaskDescriptor;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.scheduling.Scheduler;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Scheduled task to build artifact usage data for all artifacts in a particular
 * repository or repository group. The ArtifactUsageEventInspector should handle
 * incremental changes to the artifact usage data, but this task is useful to 1)
 * supplement the event mechanism in case it misses something, and 2) to
 * "prime the pump" and build the initial mappings.
 * 
 * @author Saleem Shafi
 */
@Singleton
@Named(ArtifactUsageCalculationTaskDescriptor.ID)
public class CalculateArtifactUsageTask extends
		AbstractNexusRepositoriesTask<ArtifactUsageCalculationResult> implements
		Initializable {

	public static final String CALCULATE_ARTIFACT_USAGE_ACTION = "CALCARTIFACTUSAGE";

	private final ArtifactUsageCalculator calculator;

	private final Scheduler scheduler;

	@Inject
	public CalculateArtifactUsageTask(ArtifactUsageCalculator calculator, Scheduler scheduler) {
		this.calculator = calculator;
		this.scheduler = scheduler;
	}

	@Override
	protected ArtifactUsageCalculationResult doRun() throws Exception {
		ArtifactUsageCalculationResult result = new ArtifactUsageCalculationResult();
		ArtifactUsageCalculationRequest request = new ArtifactUsageCalculationRequest(
				getRepositoryId(), getRepositoryId());
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
		if (getRepositoryId() != null) {
			return "Calculating artifact usage for repository group "
					+ getRepositoryName();
		} else if (getRepositoryId() != null) {
			return "Calculating artifact usage for repository "
					+ getRepositoryName();
		} else {
			return "Calculating artifact usage for all registered repositories";
		}
	}

	@Override
	protected String getRepositoryFieldId() {
		return ArtifactUsageCalculationTaskDescriptor.REPO_OR_GROUP_FIELD_ID;
	}

}
