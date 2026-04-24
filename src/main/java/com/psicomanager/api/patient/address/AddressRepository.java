package com.psicomanager.api.patient.address;

import com.psicomanager.api.patient.address.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, String> {
}
