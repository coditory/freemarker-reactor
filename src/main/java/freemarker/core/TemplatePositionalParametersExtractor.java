package freemarker.core;

import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class TemplatePositionalParametersExtractor {
    private static final Field POSITIONAL_ARGS_FIELD;

    static {
        try {
            POSITIONAL_ARGS_FIELD = UnifiedCall.class.getDeclaredField("positionalArgs");
            POSITIONAL_ARGS_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Could not create TemplatePositionalParametersExtractor", e);
        }
    }

    public static List<TemplateModel> extractPositionalParams(Environment env) throws TemplateException {
        DirectiveCallPlace directiveCallPlace = env.getCurrentDirectiveCallPlace();
        return directiveCallPlace instanceof UnifiedCall
                ? extract(env, (UnifiedCall) directiveCallPlace)
                : List.of();
    }

    @SuppressWarnings({"deprecation", "unchecked"})
    private static List<TemplateModel> extract(Environment env, UnifiedCall call) throws TemplateException {
        List<Expression> expressions;
        try {
            expressions = (List<Expression>) POSITIONAL_ARGS_FIELD.get(call);
        } catch (Exception e) {
            throw new IllegalStateException("Could not extract positional arguments from call", e);
        }
        if (expressions == null || expressions.isEmpty()) {
            return List.of();
        }
        List<TemplateModel> results = new ArrayList<>();
        for (Expression expression : expressions) {
            TemplateModel result = expression.eval(env);
            results.add(result);
        }
        return unmodifiableList(results);
    }
}
