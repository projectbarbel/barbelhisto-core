package com.projectbarbel.histo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.lang3.Validate;

import com.projectbarbel.histo.model.Systemclock;

public final class BarbelHistoContext {

    private final static String CONFIGFILE = System.getProperty("barbelconfig", "barbelhisto.properties");
    private final static String DOCID_GENERATOR_CLASS = "com.projectbarbel.histo.docidgen";
    private final static String VERSIONID_GENERATOR_CLASS = "com.projectbarbel.histo.versionidgen";
    private final static String DEFAULT_CREATEDBY = "com.projectbarbel.histo.createdby";
    private final static String DEFAULT_ACTIVITY = "com.projectbarbel.histo.activity";
    private final static LocalDate INFINITE = LocalDate.MAX;
    public final static BarbelHistoContext CONTEXT = builder().withProperties(properties(CONFIGFILE)).build();
    private final Properties properties;
    private final Supplier<?> versionIdGenerator;
    private final Supplier<String> documentIdGenerator;
    private final Systemclock clock;

    private BarbelHistoContext(Builder builder) {
        this.properties = builder.properties;
        this.versionIdGenerator = builder.versionIdGenerator != null ? builder.versionIdGenerator : instantiate(properties.getProperty(VERSIONID_GENERATOR_CLASS), new Object[] {});;
        this.documentIdGenerator = builder.documentIdGenerator != null ? builder.documentIdGenerator : instantiate(properties.getProperty(DOCID_GENERATOR_CLASS), new Object[] {});;
        this.clock = builder.clock != null ? builder.clock : new Systemclock();
    }
    
    public LocalDate infiniteDate() {
        return INFINITE;
    }
    public Systemclock clock() {
        return clock;
    }

    public String defaultCreatedBy() {
        return properties.getProperty(DEFAULT_CREATEDBY);
    }

    public String defaultActivity() {
        return properties.getProperty(DEFAULT_ACTIVITY);
    }
    
    @SuppressWarnings("unchecked")
    public Supplier<Serializable> versionIdGenerator() {
        return (Supplier<Serializable>)versionIdGenerator;
    }

    public Supplier<String> documentIdGenerator() {
        return documentIdGenerator;
    }
    
    public String getProperty(String key) {
        return properties.getProperty(key);
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
    
    private static Properties properties(String configFileName) {
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String appConfigPath = rootPath + configFileName;
        Properties appProps = new Properties();
        try {
            appProps.load(new FileInputStream(appConfigPath));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("the config file name could not be found: " + configFileName, e);
        } catch (IOException e) {
            throw new IllegalArgumentException("config file i/o failed for config file: " + configFileName, e);
        }
        return appProps;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        public Systemclock clock;
        private Properties properties;
        private Supplier<?> versionIdGenerator;
        private Supplier<String> documentIdGenerator;

        private Builder() {
        }

        public Builder withProperties(Properties properties) {
            this.properties = properties;
            return this;
        }

        public Builder withVersionIdGenerator(Supplier<?> versionIdGenerator) {
            this.versionIdGenerator = versionIdGenerator;
            return this;
        }

        public Builder withClock(Systemclock clock) {
            this.clock = clock;
            return this;
        }
        
        public Builder withDocumentIdGenerator(Supplier<String> documentIdGenerator) {
            this.documentIdGenerator = documentIdGenerator;
            return this;
        }
        
        public BarbelHistoContext build() {
            return new BarbelHistoContext(this);
        }
    }

}
