package org.ebayopensource.nexus.reversedep.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.sonatype.nexus.rest.model.NexusResponse;

@XmlRootElement(name = "dependeeresponse")
public class DependeeGraphResourceResponse extends NexusResponse implements
		Serializable {

	private static final long serialVersionUID = -4492092221347656388L;

	private List<Dependee> data = new ArrayList<Dependee>();

	@XmlElementWrapper(name = "data")
	@XmlElement(name = "node")
	public List<Dependee> getData() {
		return data;
	}

	public void addDependee(Dependee dependee) {
		this.data.add(dependee);
	}
}
