package com.psicomanager.api.services;

import com.psicomanager.api.domain.address.dto.AddressOnPatientDTO;
import com.psicomanager.api.repositories.address.model.Address;
import com.psicomanager.api.domain.patient.exception.DuplicatePatientEntryException;
import com.psicomanager.api.domain.patient.exception.PatientNotFoundException;
import com.psicomanager.api.repositories.AddressRepository;
import com.psicomanager.api.repositories.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AddressPatientService {

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private AddressRepository addressRepo;

    @Transactional
    public void saveAddressPatient(AddressOnPatientDTO dto, String patientId){
        log.info("Buscando informações do paciente de id "+ patientId);
        var patient = patientRepo.findById(patientId).orElseThrow(PatientNotFoundException::new);
        if(!patient.getAddresses().isEmpty()) {
            patient.getAddresses().forEach(address -> {
                if(address.getZipcode().equals(dto.zipcode())) throw new DuplicatePatientEntryException("ZipCode/CEP já cadastrado para esse paciente");
            });
        }
        var address = new Address(dto, patient);
        log.info("Salvando endereço no paciente de id "+ patientId);
        addressRepo.save(address);
    }
}
