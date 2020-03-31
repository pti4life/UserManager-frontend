package com.innovitech.usermanager.mbean;

import com.innovitech.usermanager.filter.RequestFilter;
import com.innovitech.usermanager.valueobject.User;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@ManagedBean
@RequestScoped
public class LoginController {

    private User user = new User();

    private WebTarget webTarget = ClientBuilder.newClient(new ClientConfig().register(RequestFilter.class))
            .target("http://localhost:8080/rest");

    private FacesContext facesContext = FacesContext.getCurrentInstance();


    public void login() {
        Invocation.Builder invocationBuilder = webTarget.path("login").request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.post(Entity.entity(this.user, MediaType.APPLICATION_JSON));
        int statusCode = response.getStatus();
        String entity = response.readEntity(String.class);
        if (statusCode == 400) {
            facesContext.addMessage(null, new FacesMessage("Error", entity));
            return;
        } else if (statusCode != 200) {
            facesContext.addMessage(null, new FacesMessage("Error", "Valami hiba történt, vegye fel" +
                    "a kapcsoaltot a rendszergazdájával"));
            return;
        }
        try {
            facesContext.getExternalContext().getSessionMap()
                    .put("token", entity);
            String token = (String) facesContext.getExternalContext().getSessionMap().get("token");
            facesContext.getExternalContext().redirect("user.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
