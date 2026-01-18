package fr.kainovaii.unitpanel.app.repository;

import fr.kainovaii.core.database.DB;
import fr.kainovaii.unitpanel.app.models.ApiToken;
import org.javalite.activejdbc.LazyList;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class ApiTokenRepository
{
    public ApiToken createToken(Long userId, int validityDays)
    {
        return DB.withConnection(() ->
        {
            LocalDateTime expires = LocalDateTime.now().plusDays(validityDays);
            ApiToken token = new ApiToken();
            token.setToken(UUID.randomUUID().toString());
            token.setUserId(userId);
            token.setExpiresAt(expires);
            token.saveIt();
            return token;
        });
    }

    public ApiToken findByToken(String tokenValue) { return ApiToken.findFirst("token = ?", tokenValue); }

    public List<ApiToken> findByUserId(long userId) { return ApiToken.where("user_id = ?", userId); }

    public void deleteExpiredTokens() { ApiToken.delete("expires_at < datetime('now')"); }

    public void revokeAllUserTokens(Long userId) { ApiToken.delete("user_id = ?", userId); }

    public void revokeToken(String tokenValue)
    {
        ApiToken token = findByToken(tokenValue);
        if (token != null) {
            token.delete();
        }
    }
}