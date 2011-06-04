package org.ebayopensource.nexus.reversedep.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;


@XmlType(name = "node")
public class Dependee extends Artifact {
	private List<Dependee> dependees = new ArrayList<Dependee>();

	public Dependee(String groupId, String artifactId, String version,
			String path) {
		super(groupId, artifactId, version, path);
	}

	@XmlElementWrapper(name = "data")
	@XmlElement(name = "node")
	public Collection<Dependee> getDependees() {
		return this.dependees;
	}

	public void addDependee(Dependee dependee) {
		this.dependees.add(dependee);
	}
}
