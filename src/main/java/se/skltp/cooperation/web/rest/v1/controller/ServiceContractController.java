package se.skltp.cooperation.web.rest.v1.controller;

import java.util.ArrayList;
import java.util.List;

import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import se.skltp.cooperation.domain.ServiceContract;
import se.skltp.cooperation.service.ServiceContractService;
import se.skltp.cooperation.web.rest.exception.ResourceNotFoundException;
import se.skltp.cooperation.web.rest.v1.dto.ServiceContractDTO;
import se.skltp.cooperation.web.rest.v1.listdto.ServiceContractListDTO;

/**
 * REST controller for managing ServiceContract.
 *
 * @author Jan Vasternas
 */
@RestController
@RequestMapping("/v1/serviceContracts")
public class ServiceContractController {

	private final Logger log = LoggerFactory
			.getLogger(ServiceContractController.class);

	private final ServiceContractService serviceContractService;
	private final DozerBeanMapper mapper;

	@Autowired
	public ServiceContractController(
			ServiceContractService serviceContractService,
			DozerBeanMapper mapper) {
		this.serviceContractService = serviceContractService;
		this.mapper = mapper;
	}

	/**
	 * GET /serviceContracts -> get all the serviceContracts. Content type: JSON
	 */
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ServiceContractDTO> getAllAsJson() {
		log.debug("REST request to get all ServiceContracts");
		
		List<ServiceContractDTO> result = new ArrayList<>();
		List<ServiceContract> serviceContracts = serviceContractService
				.findAll();
		for (ServiceContract cp : serviceContracts) {
			result.add(toDTO(cp));
		}
		return result;

	}

	/**
	 * GET /serviceContracts -> get all the serviceContracts. Content type: XML
	 */
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public ServiceContractListDTO getAllAsXml() {
		log.debug("REST request to get all ServiceContracts");

		ServiceContractListDTO result = new ServiceContractListDTO();
		result.setServiceContracts(getAllAsJson());
		return result;

	}

	/**
	 * GET /serviceContracts/:id -> get the "id" serviceContract.
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public ServiceContractDTO get(@PathVariable Long id) {
		log.debug("REST request to get ServiceContract : {}", id);

		ServiceContract cp = serviceContractService.find(id);
		if (cp == null) {
			log.debug("Connection point with id {} not found", id);
			throw new ResourceNotFoundException("Connection point with id "
					+ id + " not found");
		}
		return toDTO(cp);
	}

	private ServiceContractDTO toDTO(ServiceContract cp) {
		return mapper.map(cp, ServiceContractDTO.class);
	}

}