package com.gyl.awesome_inc.service;

import com.gyl.awesome_inc.domain.dto.CreateAddressRequest;
import com.gyl.awesome_inc.domain.dto.CreateAddressResponse;
import com.gyl.awesome_inc.domain.dto.GetAddressByCustomerIdResponse;
import com.gyl.awesome_inc.domain.dto.GetAddressResponse;
import com.gyl.awesome_inc.domain.model.Customer;
import com.gyl.awesome_inc.domain.model.ShipAddress;
import com.gyl.awesome_inc.repository.CustomerRepo;
import com.gyl.awesome_inc.repository.ShipAddressRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final CustomerRepo customerRepo;
    private final ModelMapper modelMapper;
    private final ShipAddressRepo shipAddressRepo;

    @Transactional
    public ResponseEntity<?> create(CreateAddressRequest createAddressRequest) {
        Optional<Customer> customerOptional = customerRepo.findById(createAddressRequest.getCustomerId());
        if (customerOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Customer customer = customerOptional.get();
        Set<ShipAddress> shipAddressSet = customer.getShipAddresses();
        ShipAddress saveShipAddress = saveNewShipAddress(createAddressRequest, shipAddressSet, customer);
        shipAddressSet.add(saveShipAddress);
        Set<CreateAddressResponse> createAddressResponseSet = new HashSet<>();
        for (ShipAddress address : shipAddressSet) {
            CreateAddressResponse createAddressResponse = modelMapper.map(address, CreateAddressResponse.class);
            createAddressResponse.setShipAddressId(address.getId());
            createAddressResponseSet.add(createAddressResponse);
        }

        return ResponseEntity.ok().body(createAddressResponseSet);
    }

    private ShipAddress saveNewShipAddress(CreateAddressRequest createAddressRequest, Set<ShipAddress> shipAddressSet, Customer customer) {
        setPrimaryAddress(createAddressRequest, shipAddressSet);
        ShipAddress shipAddress = createNewShipAddress(createAddressRequest, customer);

        return shipAddressRepo.save(shipAddress);
    }

    private void setPrimaryAddress(CreateAddressRequest createAddressRequest, Set<ShipAddress> shipAddressSet) {
        if (createAddressRequest.getIsPrimary().equals("Y") && !shipAddressSet.isEmpty()) {
            for (ShipAddress address : shipAddressSet) {
                address.setIsPrimary("N");
            }
        }
    }

    private ShipAddress createNewShipAddress(CreateAddressRequest createAddressRequest, Customer customer) {
        ShipAddress shipAddress = modelMapper.map(createAddressRequest, ShipAddress.class);
        shipAddress.setId(UUID.randomUUID().toString());
        shipAddress.setCustomer(customer);

        return shipAddress;
    }

    public ResponseEntity<?> get(String id) {
        Optional<ShipAddress> shipAddressOptional = shipAddressRepo.findById(id);
        if (shipAddressOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        ShipAddress shipAddress = shipAddressOptional.get();
        GetAddressResponse getAddressResponse = modelMapper.map(shipAddress, GetAddressResponse.class);

        return ResponseEntity.ok().body(getAddressResponse);
    }

    public ResponseEntity<?> getByCustomerId(String id) {
        Optional<Customer> customerOptional = customerRepo.findById(id);
        if (customerOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Set<ShipAddress> shipAddressSet = customerOptional.get().getShipAddresses();

        Set<GetAddressByCustomerIdResponse> getAddressByCustomerIdResponseSet = new HashSet<>();
        for (ShipAddress address : shipAddressSet) {
            GetAddressByCustomerIdResponse getAddressByCustomerIdResponse = modelMapper.map(address, GetAddressByCustomerIdResponse.class);
            getAddressByCustomerIdResponse.setShipAddressId(address.getId());
            getAddressByCustomerIdResponseSet.add(getAddressByCustomerIdResponse);
        }

        return ResponseEntity.ok().body(getAddressByCustomerIdResponseSet);
    }
}