package fr.kainovaii.core.security;

import io.pebbletemplates.pebble.extension.AbstractExtension;
import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityExtension extends AbstractExtension
{
    @Override
    public Map<String, Function> getFunctions()
    {
        Map<String, Function> functions = new HashMap<>();

        functions.put("hasRole", new Function()
        {
            @Override
            public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber)
            {
                return "false";
            }

            @Override
            public List<String> getArgumentNames() {
                return List.of("role");
            }
        });

        return functions;
    }
}
