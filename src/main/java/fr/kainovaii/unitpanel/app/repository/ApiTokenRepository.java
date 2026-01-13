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
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ApiToken createToken(Long userId, int validityDays)
    {
        return DB.withConnection(() ->
        {
            ApiToken token = new ApiToken();
            LocalDateTime expires = LocalDateTime.now().plusDays(validityDays);

            token.set("token", UUID.randomUUID().toString());
            token.set("user_id", userId);
            token.set("expires_at", expires);
            token.saveIt();
            return token;
        });
    }

    public ApiToken findByToken(String tokenValue) { return ApiToken.findFirst("token = ?", tokenValue); }

    public List<ApiToken> findByUserId(long userId) { return ApiToken.where("user_id = ?", userId); }

    public void deleteExpiredTokens() { ApiToken.delete("expires_at < datetime('now')"); }

    public void revokeToken(String tokenValue)
    {
        ApiToken token = findByToken(tokenValue);
        if (token != null) {
            token.delete();
        }
    }

    public void revokeAllUserTokens(Long userId) { ApiToken.delete("user_id = ?", userId); }
}