package com.lexcorp.joura.compile.analysis.alias;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Alias {
    String name;
    Set<Instance> instances;

    public Alias(String aliasName) {
        this.name = aliasName;
        this.instances = new HashSet<>();
    }

    public void add(Instance instance) {
        this.instances.add(instance);
    }

    public void add(Collection<Instance> instances) {
        this.instances.addAll(instances);
    }

    public void merge(Alias otherAlias) {
        this.instances.addAll(otherAlias.instances);
    }

    public boolean containsThis() {
        return instances.contains(Instance.THIS);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Alias alias = (Alias) o;
        return name.equals(alias.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Point( " + name + " )";
    }
}
