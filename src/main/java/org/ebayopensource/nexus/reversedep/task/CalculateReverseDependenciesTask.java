package org.ebayopensource.nexus.reversedep.task;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.ebayopensource.nexus.reversedep.task.descriptors.ReverseDependencyCalculationTaskDescriptor;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.scheduling.Scheduler;
import org.sonatype.scheduling.SchedulerTask;

/**
 * Scheduled task to build reverse dependency mapping for all artifacts in a
 * particular repository or repository group. The
 * ReverseDependencyEventInspector should handle incremental changes to the
 * reverse dependency mappings, but this task is useful to 1) supplement the
 * event mechanism in case it misses something, and 2) to "prime the pump" and
 * build the initial mappings.
 * 
 * @author Saleem Shafi
 */
@Component(role = SchedulerTask.class, hint = ReverseDependencyCalculationTaskDescriptor.ID, instantiationStrategy = "per-lookup")
public class CalculateReverseDependenciesTask extends
		AbstractNexusRepositoriesTask<ReverseDependencyCalculationResult>
		implements Initializable {

	public static final String CALCULATE_REVERSE_DEPENDENCIES_ACTION = "CALCREVERSEDEPENDENCIES";

	@Requirement
	private ReverseDependencyCalculator calculator;

	@Requirement
	private Scheduler scheduler;

	@Override
	protected ReverseDependencyCalculationResult doRun() throws Exception {
		ReverseDependencyCalculationResult result = new ReverseDependencyCalculationResult();
		ReverseDependencyCalculationRequest request = new ReverseDependencyCalculationRequest(
				getRepositoryId(), getRepositoryGroupId());
		this.calculator.calculateReverseDependencies(request);
		return result;
	}

	public void initialize() throws InitializationException {
		try {
			this.scheduler.submit(getAction(), this);
		} catch (Exception e) {
			throw new InitializationException(
					"Couldn't calculate reverse dependencies while initializing server",
					e);
		}
	}

	@Override
	protected String getAction() {
		return CALCULATE_REVERSE_DEPENDENCIES_ACTION;
	}

	@Override
	protected String getMessage() {
		if (getRepositoryGroupId() != null) {
			return "Calculating reverse dependencies for repository group "
					+ getRepositoryGroupName();
		} else if (getRepositoryId() != null) {
			return "Calculating reverse dependencies for repository "
					+ getRepositoryName();
		} else {
			return "Calculating reverse dependencies for all registered repositories";
		}
	}

}
