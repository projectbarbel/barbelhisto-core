package com.projectbarbel.histo;

public class BarbelHistoContext {

    private final BarbelHistoOptions activeOptions;
    private final BarbelHistoFactory activeFactory;
    
    private BarbelHistoContext(BarbelHistoOptions options, BarbelHistoFactory factory) {
        this.activeFactory = factory;
        this.activeOptions = options;
    }

    public BarbelHistoOptions options() {
        return activeOptions;
    }

    public BarbelHistoFactory factory() {
        return activeFactory;
    }

    public static BarbelHistoContext createDefault() {
        BarbelHistoOptions options = BarbelHistoOptions.builder().withDefaultValues().build();
        BarbelHistoFactory factory = BarbelHistoFactory.create(options);
        return new BarbelHistoContext(options, factory);
    }

    public static BarbelHistoContext of(BarbelHistoOptions options, BarbelHistoFactory factory) {
        return new BarbelHistoContext(options, factory);
    }
    
    public static BarbelHistoContext of(BarbelHistoOptions options) {
        return new BarbelHistoContext(options, BarbelHistoFactory.create(options));
    }
    
}
