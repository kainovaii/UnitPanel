package fr.kainovaii.unitpanel.app.models;

import org.javalite.activejdbc.Model;

import java.time.LocalDateTime;

public class ApiToken extends Model
{
    public Long getId() { return getLong("id"); }

    public String getToken() { return getString("token"); }
    public void setToken(String token) { set("token", token); }

    public Long getUserId() { return getLong("user_id"); }
    public void setUserId(Long userId) { set("user_id", userId); }

    public LocalDateTime getCreatedAt()
    {
        Object createdAt = get("created_at");
        if (createdAt instanceof java.sql.Timestamp) return ((java.sql.Timestamp) createdAt).toLocalDateTime();
        else if (createdAt instanceof LocalDateTime) return (LocalDateTime) createdAt;
        return null;
    }
    public void setCreatedAt(LocalDateTime createdAt) { set("created_at", createdAt); }

    public LocalDateTime getExpiresAt()
    {
        Object expiresAt = get("expires_at");
        if (expiresAt instanceof java.sql.Timestamp) return ((java.sql.Timestamp) expiresAt).toLocalDateTime();
        else if (expiresAt instanceof LocalDateTime) return (LocalDateTime) expiresAt;
        return null;
    }
    public void setExpiresAt(LocalDateTime expiresAt) { set("expires_at", expiresAt); }

    public boolean isExpired()
    {
        LocalDateTime expiryDate = getExpiresAt();
        if (expiryDate == null) return true;
        return LocalDateTime.now().isAfter(expiryDate);
    }
}