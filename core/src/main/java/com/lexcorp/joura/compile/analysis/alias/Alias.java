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

    public boolean containsUnknown() {
        for (Instance instance : instances) {
            if (instance.type == Instance.Type.UNKNOWN) {
                return true;
            }
        }
        return false;
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
        return Objects.equals(name, alias.name) && Objects.equals(instances, alias.instances);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, instances);
    }

    @Override
    public String toString() {
        return instances.toString();
    }
}
