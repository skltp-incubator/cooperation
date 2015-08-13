package se.skltp.cooperation.web.rest.v1.dto;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.HashSet;
import java.util.Set;

/**
 * A ConnectionPoint Data Transfer Object with associations
 *
 * @author Peter Merikan
 */
@JacksonXmlRootElement(localName = "connectionPoint")
public class ConnectionPointDTO extends ConnectionPointBaseDTO {


	@JsonManagedReference
	@JacksonXmlElementWrapper(localName = "serviceProductions")
	@JacksonXmlProperty(localName = "serviceProduction")
	private Set<ServiceProductionDTO> serviceProductions = new HashSet<>();
	@JsonManagedReference
	@JacksonXmlElementWrapper(localName = "cooperations")
	@JacksonXmlProperty(localName = "cooperation")
	private Set<CooperationDTO> cooperations = new HashSet<>();

	public Set<ServiceProductionDTO> getServiceProductions() {
		return serviceProductions;
	}

	public void setServiceProductions(Set<ServiceProductionDTO> serviceProductions) {
		this.serviceProductions = serviceProductions;
	}

	public Set<CooperationDTO> getCooperations() {
		return cooperations;
	}

	public void setCooperations(Set<CooperationDTO> cooperations) {
		this.cooperations = cooperations;
	}
}
