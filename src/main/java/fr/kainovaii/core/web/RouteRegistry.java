package fr.kainovaii.core.web;

import java.util.HashMap;
import java.util.Map;

public class RouteRegistry
{
    private static final Map<String, String> namedRoutes = new HashMap<>();

    public static void registerNamedRoute(String name, String path)
    {
        if (name != null && !name.isEmpty()) {
            namedRoutes.put(name, path);
        }
    }

    public static String getPath(String name) { return namedRoutes.get(name); }

    public static boolean hasRoute(String name) { return namedRoutes.containsKey(name); }

    public static Map<String, String> getAllRoutes() { return new HashMap<>(namedRoutes); }
}