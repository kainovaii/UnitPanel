package fr.kainovaii.unitpanel.app.models;

import org.javalite.activejdbc.Model;

public class Service extends Model
{
    public Integer getId() { return getInteger("id"); }
    public String getName() { return getString("name"); }
    public String getExecStart() { return getString("execStart"); }
    public String getWorkingDirectory() { return getString("workingDirectory"); }
    public String getUnit() { return getString("unit"); }
    public String getDescription() { return getString("description"); }
    public Boolean getAutostart() { return getBoolean("autostart"); }

    public void setName(String v) { set("name", v); }
    public void setExecStart(String v) { set("execStart", v); }
    public void setWorkingDirectory(String v) { set("workingDirectory", v); }
    public void setUnit(String v) { set("unit", v); }
    public void setDescription(String v) { set("description", v); }
    public void setAutostart(Boolean v) { set("autostart", v); }
}
