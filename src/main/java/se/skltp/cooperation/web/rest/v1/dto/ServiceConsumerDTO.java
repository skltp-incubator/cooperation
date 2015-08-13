package se.skltp.cooperation.web.rest.v1.dto;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.HashSet;
import java.util.Set;

/**
 * A ServiceConsumer Data Transfer Object with associations
 *
 * @author Peter Merikan
 */
@JacksonXmlRootElement(localName = "serviceConsumer")
public class ServiceConsumerDTO extends ServiceConsumerBaseDTO {

	@JsonManagedReference
	@JacksonXmlElementWrapper(localName = "cooperations")
	@JacksonXmlProperty(localName = "cooperation")
	private Set<CooperationDTO> cooperations = new HashSet<>();

	public Set<CooperationDTO> getCooperations() {
		return cooperations;
	}

	public void setCooperations(Set<CooperationDTO> cooperations) {
		this.cooperations = cooperations;
	}

}
