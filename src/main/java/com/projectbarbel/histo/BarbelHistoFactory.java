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

    private static Map<String, Object> beans = new ConcurrentHashMap<String, Object>();
    private static Map<String, BiFunction<BarbelHistoOptions, Object[], Object>> factories = new ConcurrentHashMap<>();
    static {
        initialize();
    }
    
    public enum HistoType {
        DAO, SERVICE, COPIER, IDGENERATOR, UPDATER;
    }

    public static synchronized void initialize() {
        factories.clear();
        beans.clear();
        factories.computeIfAbsent(HistoType.DAO.name(), (k) -> (options, args) -> instantiate(options.getDaoClassName(), args));
        factories.computeIfAbsent(HistoType.SERVICE.name(), (k) -> (options, args) -> instantiate(options.getServiceClassName(), args));
        factories.computeIfAbsent(HistoType.COPIER.name(), (k) -> (options, args) -> instantiate(options.getPojoCopierClassName(), args));
        factories.computeIfAbsent(HistoType.IDGENERATOR.name(), (k) -> 
                (options, args) -> instantiate(options.getIdGeneratorClassName(), args));
        factories.computeIfAbsent(HistoType.UPDATER.name(), (k) -> (options, args) -> instantiate(options.getUpdaterClassName(), args));
    }
    
    @SuppressWarnings("unchecked")
    protected static <O> O instantiate(String classname, Object... constructorArgs) {
        Validate.notEmpty(classname, "classname must not be empty");
        try {
            return (O)ConstructorUtils.invokeConstructor(Class.forName(classname), constructorArgs);
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
            throw new RuntimeException(
                    "The class " + classname + " could not be instintiated. Cannot find constructor method with arg(s): "
                            + Arrays.stream(constructorArgs).map((arg)-> arg.getClass().getName()).collect(Collectors.joining(", ")),
                    e);
        } catch (SecurityException e) {
            throw new RuntimeException("The class " + classname + " could not be instintiated. Security error. "
                    + constructorArgs.toString(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "The class " + classname + " could not be instintiated. Passed illegal arguments to constructor. "
                            + Arrays.stream(constructorArgs).map((arg)-> arg.getClass().getName()).collect(Collectors.joining(", ")),
                    e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(
                    "The class " + classname + " could not be instintiated. An exception was thrown in target class. "
                            + Arrays.stream(constructorArgs).map((arg)-> arg.getClass().getName()).collect(Collectors.joining(", ")),
                    e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T instanceOf(HistoType type) {
        return (T)instanceOf(type, new Object[0]);
    }

    @SuppressWarnings("unchecked")
    public static <T> T instanceOf(HistoType type, Object... constructorArgs) {
        Validate.noNullElements(Arrays.asList(type, BarbelHistoOptions.ACTIVE_CONFIG));
        return (T)beans.computeIfAbsent(type.name(), (k) -> factories.get(type.name()).apply(BarbelHistoOptions.ACTIVE_CONFIG, constructorArgs));
    }

    @SuppressWarnings("unchecked")
    public static <T> T instanceOf(String customHistoType) {
        return (T)instanceOf(customHistoType, new Object[0]);
    }

    @SuppressWarnings("unchecked")
    public static <T> T instanceOf(String customHistoType, Object... constructorArgs) {
        Validate.noNullElements(Arrays.asList(customHistoType, constructorArgs, BarbelHistoOptions.ACTIVE_CONFIG));
        return (T)beans.computeIfAbsent(customHistoType, (k) -> factories.get(customHistoType).apply(BarbelHistoOptions.ACTIVE_CONFIG, constructorArgs));
    }
    
}