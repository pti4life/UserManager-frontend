package com.innovitech.usermanager.response;

import com.innovitech.usermanager.valueobject.Address;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddressResponse {

    private List<Address> addresses;

}
