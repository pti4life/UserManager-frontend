package com.innovitech.usermanager.response;

import com.innovitech.usermanager.valueobject.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserResponse {

    private List<User> users;

}
