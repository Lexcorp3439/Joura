package com.lexcorp.joura.compile.analysis.alias;

import java.util.HashMap;

import static com.lexcorp.joura.compile.analysis.alias.Instance.THIS;
import static com.lexcorp.joura.compile.analysis.alias.Instance.Type;

public class Aliases {
    private final HashMap<String, Alias> aliases;

    public Instance self() {
        return THIS;
    }

    public Instance obj(Type type) {
        return new Instance(type);
    }

    public Aliases() {
        this.aliases = new HashMap<>();
    }

    public Alias alias(String name) {
        return aliases.get(name);
    }

    public Alias get(String aliasName) {
        if (!aliases.containsKey(aliasName)) {
            aliases.put(aliasName, new Alias(aliasName));
        }
        return aliases.get(aliasName);
    }

    public void add(Alias alias, Instance instance) {
        if (!aliases.containsKey(alias.name)) {
            aliases.put(alias.name, alias);
        }
        aliases.get(alias.name).add(instance);
    }

    public void add(String aliasName, Instance instance) {
        Alias alias = new Alias(aliasName);
        this.add(alias, instance);
    }

    public void addThisAlias(Alias alias) {
        this.add(alias.name, THIS);
    }

    public void addThisAlias(String aliasName) {
        this.add(aliasName, THIS);
    }

    public void addNewObjAlias(Alias alias) {
        Instance instance = this.obj(Type.NEW);
        this.add(alias, instance);
    }

    public void addNewObjAlias(String aliasName) {
        Instance instance = this.obj(Type.NEW);
        this.add(aliasName, instance);
    }

}
