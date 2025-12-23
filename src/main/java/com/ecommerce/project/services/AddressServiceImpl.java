package com.ecommerce.project.services;

import com.ecommerce.project.customExceptionHandler.ApiException;
import com.ecommerce.project.customExceptionHandler.ResourseNorFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class AddressServiceImpl implements AddressService {
    @Autowired
    private AuthUtil authUtil;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AddressRepository addressRepository;

    @Override
    public AddressDTO addAddress(AddressDTO addressDTO) {
        User loggedInUser=authUtil.loggedInUser();

        Address address=modelMapper.map(addressDTO,Address.class);

//        Address newAddress=new Address(addressDTO.getStreet(),addressDTO.getBuildingName(),
//                addressDTO.getCity(),addressDTO.getState(),addressDTO.getCountry(),addressDTO.getPincode());

        List<Address> addressList=loggedInUser.getAddresses();
        addressList.add(address);
        loggedInUser.setAddresses(addressList);

        address.setUser(loggedInUser);
        Address savedAddress=addressRepository.save(address);

        return modelMapper.map(address,AddressDTO.class);


    }

    @Override
    public List<AddressDTO> getAllAddresses() {
        List<Address> addressList=addressRepository.findAll();
        if (addressList.isEmpty())
            throw new ApiException("THere Are no  Addresses ....!");

        List<AddressDTO> addressDTOS=addressList.stream().map(address -> {
            AddressDTO addressDTO = modelMapper.map(address, AddressDTO.class);
            return addressDTO;
        }).toList();
        return addressDTOS;
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address=addressRepository.findById(addressId)
                .orElseThrow(()->new ResourseNorFoundException("Address","AddressId",addressId));
        return modelMapper.map(address,AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAllAddressesOfUser(User user) {
        List<Address> addressList=user.getAddresses();

        List<AddressDTO> addressDTOS=addressList.stream().map(address -> {
            AddressDTO addressDTO=modelMapper.map(address,AddressDTO.class);
            return addressDTO;
        }).toList();

        return addressDTOS;
    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        Address addressFromDb=addressRepository.findById(addressId)
                .orElseThrow(()->new ResourseNorFoundException("Address","AddressId",addressId));
        addressFromDb.setCity(addressDTO.getCity());
        addressFromDb.setCountry(addressDTO.getCountry());
        addressFromDb.setState(addressDTO.getState());
        addressFromDb.setStreet(addressDTO.getStreet());
        addressFromDb.setBuildingName(addressDTO.getBuildingName());
        addressFromDb.setPinCode(addressDTO.getPincode());

        Address updatedAddress=addressRepository.save(addressFromDb);

        User user=addressFromDb.getUser();
        user.getAddresses().removeIf(address->address.getAddressId().equals(addressId));
        user.getAddresses().add(updatedAddress);
        userRepository.save(user);

        return modelMapper.map(updatedAddress,AddressDTO.class);
    }

    @Override
    public String deleteAddress(Long addressId) {
        Address deletedAddress=addressRepository.findById(addressId)
                .orElseThrow(()-> new ResourseNorFoundException("Address","AddressId",addressId));
        addressRepository.delete(deletedAddress);

        return "Address deleted successfully with address id: "+addressId;
    }
}
