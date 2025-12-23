package com.ecommerce.project.controller;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.services.AddressService;
import com.ecommerce.project.util.AuthUtil;
import jakarta.validation.Valid;
import org.hibernate.validator.constraints.ISBN;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> addAddress(@Valid @RequestBody AddressDTO addressDTO){
        AddressDTO savedAddressDTO= addressService.addAddress(addressDTO);
        return new ResponseEntity<>(savedAddressDTO, HttpStatus.CREATED);
    }
    @GetMapping("/addresses")
    public  ResponseEntity<List<AddressDTO>> getAllAddresses(){
        List<AddressDTO> addressDTOS=addressService.getAllAddresses();
        return new ResponseEntity<List<AddressDTO>>(addressDTOS,HttpStatus.FOUND);
    }
    @GetMapping("/user/addresses")
    public  ResponseEntity<List<AddressDTO>> getAllUserAddresses(){
        User user=authUtil.loggedInUser();
        List<AddressDTO> addressDTOS=addressService.getAllAddressesOfUser(user);
        return new ResponseEntity<List<AddressDTO>>(addressDTOS,HttpStatus.FOUND);
    }

    @GetMapping("/addresses/{addressId}")
    public  ResponseEntity<AddressDTO> getAllAddressById(@PathVariable Long addressId){
        AddressDTO addressDTO=addressService.getAddressById(addressId);
        return new ResponseEntity<AddressDTO>(addressDTO,HttpStatus.FOUND);
    }

    @PutMapping("/updateAddress/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long addressId,
                                                    @RequestBody AddressDTO addressDTO){
        AddressDTO updatedAddress=addressService.updateAddress(addressId,addressDTO);

        return new ResponseEntity<AddressDTO>(updatedAddress,HttpStatus.OK);
    }

    @DeleteMapping("/deleteAddress/{addressId}")
    public String deleteAddress(@PathVariable Long addressId){
        String deletedAddress=addressService.deleteAddress(addressId);

        return deletedAddress;
    }

}
