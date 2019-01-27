package com.projectbarbel.histo;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.ConstructorUtils;

public final class BarbelHistoFactory {

    public enum DefaultHistoType implements HistoType {
        DAO, SERVICE, COPIER, IDGENERATOR, UPDATER, UPDATEPOLICY;
    }
    
    public interface HistoType {String name();};
    
    private final static Map<String, BiFunction<BarbelHistoOptions, Object[], Object>> defaultfactories = defaultfactories();
    private final BarbelHistoOptions options;
    private final ConcurrentHashMap<String, Object> beans = new ConcurrentHashMap<String, Object>();
    private final Map<String, BiFunction<BarbelHistoOptions, Object[], Object>> factories;

    private BarbelHistoFactory(BarbelHistoOptions options,
            Map<String, BiFunction<BarbelHistoOptions, Object[], Object>> factories) {
        this.factories = factories;
        this.options = options;
    }

    public static BarbelHistoFactory create(BarbelHistoOptions options) {
        return new BarbelHistoFactory(options, defaultfactories);
    }

    public static BarbelHistoFactory create(BarbelHistoOptions options,
            Map<String, BiFunction<BarbelHistoOptions, Object[], Object>> factories) {
        return new BarbelHistoFactory(options, factories);
    }

    private static synchronized Map<String, BiFunction<BarbelHistoOptions, Object[], Object>> defaultfactories() {
        ConcurrentHashMap<String, BiFunction<BarbelHistoOptions, Object[], Object>> factories = new ConcurrentHashMap<>();
        factories.computeIfAbsent(DefaultHistoType.DAO.name(),
                (k) -> (options, args) -> BarbelHistoFactory.instantiate(options.getDaoClassName(), args));
        factories.computeIfAbsent(DefaultHistoType.SERVICE.name(),
                (k) -> (options, args) -> BarbelHistoFactory.instantiate(options.getServiceClassName(), args));
        factories.computeIfAbsent(DefaultHistoType.COPIER.name(),
                (k) -> (options, args) -> BarbelHistoFactory.instantiate(options.getPojoCopierClassName(), args));
        factories.computeIfAbsent(DefaultHistoType.IDGENERATOR.name(),
                (k) -> (options, args) -> BarbelHistoFactory.instantiate(options.getIdGeneratorClassName(), args));
        factories.computeIfAbsent(DefaultHistoType.UPDATEPOLICY.name(),
                (k) -> (options, args) -> BarbelHistoFactory.instantiate(options.getUpdatePolicyClassName(), args));
        factories.computeIfAbsent(DefaultHistoType.UPDATER.name(),
                (k) -> (options, args) -> BarbelHistoFactory.instantiate(options.getUpdaterClassName(), args));
        return factories;
    }

    @SuppressWarnings("unchecked")
    protected static <O> O instantiate(String classname, Object... constructorArgs) {
        Validate.notEmpty(classname, "classname must not be empty");
        try {
            return (O) ConstructorUtils.invokeConstructor(Class.forName(classname), constructorArgs);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("The class " + classname + " cannot be found.", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("The class " + classname
                    + " could not be instintiated. Check that it has a public default constructor without any arguments.",
                    e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("The class " + classname + " could not be instintiated. Check access rights.",
                    e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("The class " + classname
                    + " could not be instintiated. Cannot find constructor method with arg(s): "
                    + Arrays.stream(constructorArgs).map((arg) -> arg.getClass().getName())
                            .collect(Collectors.joining(", ")),
                    e);
        } catch (SecurityException e) {
            throw new RuntimeException("The class " + classname + " could not be instintiated. Security error. "
                    + constructorArgs.toString(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "The class " + classname + " could not be instintiated. Passed illegal arguments to constructor. "
                            + Arrays.stream(constructorArgs).map((arg) -> arg.getClass().getName())
                                    .collect(Collectors.joining(", ")),
                    e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(
                    "The class " + classname + " could not be instintiated. An exception was thrown in target class. "
                            + Arrays.stream(constructorArgs).map((arg) -> arg.getClass().getName())
                                    .collect(Collectors.joining(", ")),
                    e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T instanceOf(DefaultHistoType type) {
        return (T) instanceOf(type, new Object[0]);
    }

    @SuppressWarnings("unchecked")
    public <T> T instanceOf(String customHistoType, Object... constructorArgs) {
        Validate.noNullElements(Arrays.asList(customHistoType, constructorArgs, options));
        T bean = (T) beans.computeIfAbsent(customHistoType,
                (k) -> factories.get(customHistoType).apply(options, constructorArgs));
        return bean;
    }
    
    public <T> T instanceOf(HistoType type, Object... constructorArgs) {
        return instanceOf(type.name(), constructorArgs);
    }

    @SuppressWarnings("unchecked")
    public <T> T instanceOf(String customHistoType) {
        return (T) instanceOf(customHistoType, new Object[0]);
    }

    public static BarbelHistoFactory withDefaultValues() {
        return BarbelHistoFactory.create(BarbelHistoOptions.withDefaultValues());
    }

    public BarbelHistoOptions options() {
        return options;
    }

    public Map<String, BiFunction<BarbelHistoOptions, Object[], Object>> factories() {
        return factories.keySet().stream().collect(Collectors.toMap((k) -> k, (k) -> factories.get(k)));
    }

}