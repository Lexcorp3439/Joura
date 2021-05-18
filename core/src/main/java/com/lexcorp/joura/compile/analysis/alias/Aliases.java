package com.lexcorp.joura.compile.analysis.alias;

import java.util.Collection;
import java.util.Collections;
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

    public boolean add(Alias alias, Collection<Instance> instance) {
        if (!aliases.containsKey(alias.name)) {
            aliases.put(alias.name, alias);
        } else {
            assert alias.equals(aliases.get(alias.name)) : "Aliases has the same names but not equals";
        }
        return this.checkAliasChangeAfterProcedure(alias, () -> alias.add(instance));
    }

    public boolean add(String aliasName, Collection<Instance> instance) {
        Alias alias = aliases.containsKey(aliasName)
                ? aliases.get(aliasName)
                : new Alias(aliasName);
        aliases.put(aliasName, alias);
        return this.add(alias, instance);
    }


    public boolean add(Alias alias, Instance instance) {
        return this.add(alias, Collections.singletonList(instance));
    }

    public boolean add(String aliasName, Instance instance) {
        return this.add(aliasName, Collections.singletonList(instance));
    }

    public boolean merge(Alias alias, Alias otherAlias) {
        return this.checkAliasChangeAfterProcedure(alias, () -> alias.merge(otherAlias));
    }

    public boolean merge(String aliasName, String otherAliasName) {
        Alias alias = get(aliasName);
        Alias otherAlias = get(otherAliasName);
        return this.merge(alias, otherAlias);
    }

    public boolean merge(Alias alias, String otherAliasName) {
        Alias otherAlias = get(otherAliasName);
        return this.merge(alias, otherAlias);
    }

    public boolean merge(String aliasName, Alias otherAlias) {
        Alias alias = get(aliasName);
        return this.merge(alias, otherAlias);
    }

    public boolean addThisAlias(Alias alias) {
        return this.add(alias.name, THIS);
    }

    public boolean addThisAlias(String aliasName) {
        return this.add(aliasName, THIS);
    }

    public boolean addNewObjAlias(Alias alias) {
        Instance instance = this.obj(Type.NEW);
        return this.add(alias, instance);
    }

    public boolean addNewObjAlias(String aliasName) {
        Instance instance = this.obj(Type.NEW);
        return this.add(aliasName, instance);
    }

    private boolean checkAliasChangeAfterProcedure(Alias alias, Procedure procedure) {
        int beforeCount = alias.instances.size();
        procedure.run();
        int afterCount = alias.instances.size();
        return beforeCount != afterCount;
    }

    public HashMap<String, Alias> getMap() {
        return aliases;
    }

    @Override
    public String toString() {
        return "aliases=" + aliases;
    }

    @FunctionalInterface
    public interface Procedure {
        void run();
    }
}
