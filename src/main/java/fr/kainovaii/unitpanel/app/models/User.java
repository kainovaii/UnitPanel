package fr.kainovaii.unitpanel.app.models;

import org.javalite.activejdbc.Model;

public class User extends Model
{
    public String getUsername() {
        return getString("username");
    }
    public void setUsername(String username) {
        set("username", username);
    }

    public String getEmail() {
        return getString("email");
    }
    public void setEmail(String email) { set("email", email); }

    public String getPassword() {
        return getString("password");
    }
    public void setPassword(String password) {
        set("password", password);
    }

    public String getRole() {
        return getString("role");
    }
    public void setRole(String role) {
        set("role", role);
    }
}