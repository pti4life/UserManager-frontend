package com.innovitech.usermanager.valueobject;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Address {

    private Long id;

    private Integer postalCode;

    private String city;

    private String street;

    private Integer houseNumber;

    private User user;

}
