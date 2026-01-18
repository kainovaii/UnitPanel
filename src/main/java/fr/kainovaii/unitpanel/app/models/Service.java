package fr.kainovaii.unitpanel.app.models;

import org.javalite.activejdbc.Model;

import java.util.Arrays;
import java.util.List;

public class Service extends Model
{
    
    public Integer getId() { return getInteger("id"); }

    public String getName() { return getString("name"); }
    public void setName(String name) { set("name", name); }

    public String getExecStart() { return getString("execStart"); }
    public void setExecStart(String execStart) { set("execStart", execStart); }

    public String getWorkingDirectory() { return getString("workingDirectory"); }
    public void setWorkingDirectory(String workingDirectory) { set("workingDirectory", workingDirectory); }

    public String getUnit() { return getString("unit"); }
    public void setUnit(String unit) { set("unit", unit); }

    public String getDescription() { return getString("description"); }
    public void setDescription(String description) { set("description", description); }

    public Boolean getAutostart() { return getBoolean("autostart"); }
    public void setAutostart(Boolean autostart) { set("autostart", autostart); }

    public List<String> getUsers()
    {
        String users = getString("users");
        return Arrays.asList(users.replace("[", "").replace("]", "").trim().split(",\\s*"));
    }

    public void setUsers(String users) { set("users", users); }
}
