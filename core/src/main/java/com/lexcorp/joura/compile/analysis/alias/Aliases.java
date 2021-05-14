package com.lexcorp.joura.compile.analysis.alias;

import java.util.Collection;
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

    public boolean contains(String name) {
        return aliases.containsKey(name);
    }

    public boolean isThisOrUnknownAlias(String variableName) {
        return this.get(variableName).containsThis() || this.get(variableName).containsUnknown();
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

    public void add(String aliasName, Collection<Instance> instance) {
        if (!aliases.containsKey(aliasName)) {
            aliases.put(aliasName, new Alias(aliasName));
        }
        aliases.get(aliasName).add(instance);
    }

    public void add(Alias alias, Collection<Instance> instance) {
        if (!aliases.containsKey(alias.name)) {
            aliases.put(alias.name, alias);
        }
        aliases.get(alias.name).add(instance);
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

    public void addWithMerge(String aliasName, String otherAliasName) {
        Alias alias = get(aliasName);
        Alias otherAlias = get(otherAliasName);
        alias.merge(otherAlias);
    }

    public void addWithMerge(String aliasName, Alias otherAlias) {
        Alias alias = get(aliasName);
        alias.merge(otherAlias);
    }

    public HashMap<String, Alias> getMap() {
        return aliases;
    }

    @Override
    public String toString() {
        return "aliases=" + aliases;
    }
}
