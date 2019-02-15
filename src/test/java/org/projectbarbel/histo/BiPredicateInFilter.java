package org.projectbarbel.histo;

import java.util.Set;
import java.util.stream.Collectors;

public class BiPredicateInFilter {

    private static Set<String> envKeys = System.getenv().keySet();
    public static class Predicates {
        private String pattern;
        public boolean containsPattern(String string) {
            return string.contains(pattern);
        }
        public Predicates(String pattern) {
            this.pattern = pattern;
        }
    }
    public static Set<String> someStreamProcessing() {
        Predicates predicates = new Predicates("SSH");
        return envKeys.stream().filter(predicates::containsPattern).collect(Collectors.toSet());
    }
    public static void main(String[] args) {
        Set<String> envset = someStreamProcessing();
        System.out.println(envset);
    }
}
