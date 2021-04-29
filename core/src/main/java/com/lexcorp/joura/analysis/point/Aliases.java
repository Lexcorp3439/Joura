package com.lexcorp.joura.analysis.point;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Aliases {
    private final HashMap<Alias, Set<Alias>> aliases;

    private final Alias thisAlias = new Alias("this");


    public Aliases() {
        this.aliases = new HashMap<>();
    }

    public void add(Alias target, Alias newAlias) {
        if (!aliases.containsKey(target)) {
            aliases.put(target, new HashSet<>());
        }
        aliases.get(target).add(newAlias);
    }

    public void addThisAlias(Alias target) {
        aliases.get(target).add(thisAlias);
    }
}
