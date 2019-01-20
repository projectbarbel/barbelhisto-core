package com.projectbarbel.histo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BarbelHistoOptions {

    public final static BarbelHistoOptions DEFAULT_CONFIG = new BarbelHistoOptions();
    private Map<String, String> options = new HashMap<>();
    
    public BarbelHistoOptions() {
    }

    public void addOption(String key, String value) {
        options.put(key, value);
    }

    public Optional<String> getOption(String key) {
        return Optional.ofNullable(options.get(key));
    }
    
}
