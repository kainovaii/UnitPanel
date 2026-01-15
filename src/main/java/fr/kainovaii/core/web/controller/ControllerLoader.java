package fr.kainovaii.core.web.controller;

import fr.kainovaii.core.security.HasRole;
import fr.kainovaii.core.security.RoleChecker;
import fr.kainovaii.core.web.methods.DELETE;
import fr.kainovaii.core.web.methods.GET;
import fr.kainovaii.core.web.methods.POST;
import org.reflections.Reflections;
import spark.Route;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class ControllerLoader
{
    public static void loadControllers()
    {
        Reflections reflections = new Reflections("fr.kainovaii.unitpanel.app.controllers");
        Set<Class<?>> controllerClasses = reflections.getTypesAnnotatedWith(Controller.class);

        List<Object> controllers = controllerClasses.stream()
        .map(cls -> {
            try {
                return cls.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                System.err.println("Impossible d'instancier le controller : " + cls.getName());
                e.printStackTrace();
                return null;
            }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
        registerRoutes(controllers);
    }

    private static void registerRoutes(List<Object> controllers)
    {
        for (Object controller : controllers) {
            for (Method method : controller.getClass().getDeclaredMethods())
            {
                if (method.isAnnotationPresent(GET.class))
                {
                    GET getAnnotation = method.getAnnotation(GET.class);
                    String path = getAnnotation.value();
                    if (method.isAnnotationPresent(HasRole.class)) {
                        HasRole roleAnnotation = method.getAnnotation(HasRole.class);
                        RoleChecker.registerPathWithRole(path, roleAnnotation.value());
                    }
                    get(path, createRoute(controller, method));
                }
                if (method.isAnnotationPresent(POST.class))
                {
                    POST postAnnotation = method.getAnnotation(POST.class);
                    String path = postAnnotation.value();
                    if (method.isAnnotationPresent(HasRole.class)) {
                        HasRole roleAnnotation = method.getAnnotation(HasRole.class);
                        RoleChecker.registerPathWithRole(path, roleAnnotation.value());
                    }
                    post(path, createRoute(controller, method));
                }
                if (method.isAnnotationPresent(DELETE.class))
                {
                    DELETE deleteAnnotation = method.getAnnotation(DELETE.class);
                    String path = deleteAnnotation.value();
                    if (method.isAnnotationPresent(HasRole.class)) {
                        HasRole roleAnnotation = method.getAnnotation(HasRole.class);
                        RoleChecker.registerPathWithRole(path, roleAnnotation.value());
                    }
                    delete(path, createRoute(controller, method));
                }
            }
        }
    }

    private static Route createRoute(Object controller, Method method)
    {
        return (req, res) -> {
            try {
                method.setAccessible(true);
                return method.invoke(controller, req, res);
            } catch (Exception e) {
                throw new RuntimeException("Erreur dans la route " + method.getName(), e);
            }
        };
    }
}