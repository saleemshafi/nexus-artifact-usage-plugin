package org.ebayopensource.nexus.reversedep.task;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.ebayopensource.nexus.reversedep.task.descriptors.ReverseDependencyCalculationTaskDescriptor;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
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
		AbstractNexusRepositoriesTask<ReverseDependencyCalculationResult> {
	public static final String CALCULATE_REVERSE_DEPENDENCIES_ACTION = "CALCREVERSEDEPENDENCIES";

	@Requirement
	private ReverseDependencyCalculator calculator;

	@Override
	protected ReverseDependencyCalculationResult doRun() throws Exception {
		ReverseDependencyCalculationResult result = new ReverseDependencyCalculationResult();
		ReverseDependencyCalculationRequest request = new ReverseDependencyCalculationRequest(
				getRepositoryId(), getRepositoryGroupId());
		this.calculator.calculateReverseDependencies(request);
		return result;
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
