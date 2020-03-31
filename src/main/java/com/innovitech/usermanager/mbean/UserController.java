package com.innovitech.usermanager.mbean;

import com.innovitech.usermanager.response.AddressResponse;
import com.innovitech.usermanager.response.UserResponse;
import com.innovitech.usermanager.valueobject.Address;
import com.innovitech.usermanager.valueobject.User;
import org.glassfish.jersey.client.ClientConfig;
import org.primefaces.PrimeFaces;
import org.primefaces.event.RowEditEvent;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@ManagedBean
@ViewScoped
public class UserController {

    private List<User> users = new ArrayList<>();

    private List<Address> addresses = new ArrayList<>();

    private User user = new User();

    private Address address = new Address();

    private String newPassword = "";

    private User userForNewAddress = null;

    private WebTarget webTarget = ClientBuilder.newClient(new ClientConfig())
            .target("http://localhost:8080/rest");

    private String token = null;


    @PostConstruct
    public void init() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        String token = (String) facesContext.getExternalContext().getSessionMap()
                .get("token");
        if (token != null) {
            this.token = "Beaer " + token;
        }

        Invocation.Builder invocationBuilder = webTarget.path("users").request(MediaType.APPLICATION_JSON).header("Authorization", this.token);
        Response response = invocationBuilder.get();
        int statusCode = response.getStatus();
        if (statusCode == 403) {
            try {
                facesContext.getExternalContext().redirect("index.xhtml");
                return;
            } catch (IOException e) {
                System.out.println(e);
            }
        } else if (statusCode != 200) {
            facesContext.addMessage(null, new FacesMessage("Error", "Valami hiba történt, vegye fel" +
                    "a kapcsoaltot a rendszergazdájával"));
            return;
        }
        this.users = response.readEntity(UserResponse.class).getUsers();
    }

    public void deleteUser(User user) {
        Invocation.Builder invocationBuilder = webTarget.path("users/"+user.getId()).request(MediaType.APPLICATION_JSON).header("Authorization", token);
        Response response = invocationBuilder.delete();
        if(response.getStatus() != 200) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage("Error", "Valami hiba történt, vegye fel" +
                    "a kapcsoaltot a rendszergazdájával"));
        }
        Invocation.Builder invocationBuilder2 = webTarget.path("users").request().header("Authorization", token);
        UserResponse userResponse = invocationBuilder2.get(UserResponse.class);
        this.users = userResponse.getUsers();
    }

    public void deleteAddress(Address address) {
        Invocation.Builder invocationBuilder = webTarget.path("addresses/"+address.getId()).request().header("Authorization", token);
        Response response = invocationBuilder.delete();
        if(response.getStatus() != 200) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage("Error", "Valami hiba történt, vegye fel" +
                    "a kapcsoaltot a rendszergazdájával"));
        }
        Invocation.Builder invocationBuilder2 = webTarget.path("addresses/"+address.getUser().getId()).request().header("Authorization", token);
        AddressResponse addressResponse = invocationBuilder2.get(AddressResponse.class);
        this.addresses = addressResponse.getAddresses();
    }

    public void onUserEdit(RowEditEvent event) {
        User user = (User) event.getObject();
        if (this.newPassword.length() >= 6) {
            user.setPassword(this.newPassword);
            this.newPassword = "";
        }
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Invocation.Builder invocationBuilder = webTarget.path("users").request(MediaType.APPLICATION_JSON).header("Authorization", token);
        Response response = invocationBuilder.put(Entity.entity(user, MediaType.APPLICATION_JSON));

        if (response.getStatus() != 200) {
            facesContext.addMessage(null, new FacesMessage("Error", "Valami hiba történt, vegye fel" +
                    "a kapcsoaltot a rendszergazdájával"));
        }
    }

    public void onUserAddresses(User user) {
        Invocation.Builder invocationBuilder = webTarget.path("addresses/" + user.getId()).request(MediaType.APPLICATION_JSON).header("Authorization", token);
        this.userForNewAddress = user;
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Response response = invocationBuilder.get();
        if (response.getStatus() != 200) {
            facesContext.addMessage(null, new FacesMessage("Error", "Valami hiba történt, vegye fel" +
                    "a kapcsoaltot a rendszergazdájával"));
        }

        this.addresses = response.readEntity(AddressResponse.class).getAddresses();

        PrimeFaces current = PrimeFaces.current();
        current.ajax().update("form:addresses");
        current.executeScript("PF('dlg1').show();");

    }

    public void onAddressEdit(RowEditEvent event) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Address address = (Address) event.getObject();
        Invocation.Builder invocationBuilder = webTarget.path("addresses").request(MediaType.APPLICATION_JSON).header("Authorization", token);
        Response response = invocationBuilder.put(Entity.entity(address, MediaType.APPLICATION_JSON));
        if (response.getStatus() != 200) {
            facesContext.addMessage(null, new FacesMessage("Error", "Valami hiba történt, vegye fel" +
                    "a kapcsoaltot a rendszergazdájával"));
        }
    }

    public void saveUser() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Invocation.Builder invocationBuilder = webTarget.path("users").request(MediaType.APPLICATION_JSON).header("Authorization", token);
        Response response = invocationBuilder.post(Entity.entity(this.user, MediaType.APPLICATION_JSON));
        if (response.getStatus() != 200) {
            facesContext.addMessage(null, new FacesMessage("Error", "Valami hiba történt, vegye fel" +
                    "a kapcsoaltot a rendszergazdájával"));
        }

        Invocation.Builder ib = webTarget.path("users").request(MediaType.APPLICATION_JSON).header("Authorization", token);
        UserResponse ur = ib.get(UserResponse.class);
        this.users = ur.getUsers();
        this.user = new User();
    }

    public void saveAddress() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        this.address.setUser(this.userForNewAddress);
        Invocation.Builder invocationBuilder = webTarget.path("addresses").request(MediaType.APPLICATION_JSON).header("Authorization", token);
        Response response = invocationBuilder.post(Entity.entity(this.address, MediaType.APPLICATION_JSON));
        if (response.getStatus() != 200) {
            facesContext.addMessage(null, new FacesMessage("Error", "Valami hiba történt, vegye fel" +
                    "a kapcsoaltot a rendszergazdájával"));
        }
        Invocation.Builder ib = webTarget.path("addresses/" + this.address.getUser().getId()).request(MediaType.APPLICATION_JSON).header("Authorization", token);
        AddressResponse adr = ib.get(AddressResponse.class);
        this.addresses = adr.getAddresses();
        this.address = new Address();
    }

    public void logout() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
        sessionMap.clear();
        try {
            context.getExternalContext().redirect("index.xhtml");
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}