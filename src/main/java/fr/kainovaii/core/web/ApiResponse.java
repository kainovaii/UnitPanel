package fr.kainovaii.core.web;

import spark.Response;

import java.util.Map;

public class ApiResponse
{
    protected Map<String, Object> success(Response res)
    {
        res.status(200);
        res.type("application/json");
        return Map.of("success", true);
    }

    protected Map<String, Object> success(Response res, Object data)
    {
        res.status(200);
        res.type("application/json");
        return Map.of(
                "success", true,
                "data", data
        );
    }

    protected Map<String, Object> error(Response res, String message)
    {
        res.status(400);
        res.type("application/json");
        return Map.of("success", false, "error", message);
    }
}
