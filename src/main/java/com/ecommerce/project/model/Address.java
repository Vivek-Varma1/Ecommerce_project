package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@ToString
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min = 5,message = "Street Name must be atleast 5 Characters")
    private String street;

    @NotBlank
    @Size(min = 5,message = "Building Name must be atleast 5 Characters")
    private String buildingName;

    @NotBlank
    @Size(min = 4,message = "City Name must be atleast 4 Characters")
    private String city;

    @NotBlank
    @Size(min = 2,message = "State Name must be atleast 2 Characters")
    private String state;

    @NotBlank
    @Size(min = 2,message = "Country Name must be atleast 2 Characters")
    private String country;

    @NotBlank
    @Size(min = 6,message = "PinCode Name must be atleast 6 Characters")
    private String pinCode;

//    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Address(String street, String buildingName, String city, String state, String country, String pinCode) {
        this.street = street;
        this.buildingName = buildingName;
        this.city = city;
        this.state = state;
        this.country = country;
        this.pinCode = pinCode;
    }
}
