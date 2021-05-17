package com.lexcorp.joura.logger;


import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Marker implements Serializable {
    Set<Marker> parents;
    String name;

    public static Marker getMarker(String name) {
        return new Marker(name);
    }

    public Marker(String name) {
        this.name = name;
        this.parents = new HashSet<>();
    }

    public Marker addParents(Marker... markers) {
        parents.addAll(Arrays.asList(markers));
        return this;
    }

    public String getName() {
        return name;
    }

    public Marker[] getParents() {
        return parents.toArray(new Marker[0]);
    }

    public boolean hasParents() {
        return parents.size() > 0;
    }

    public boolean isInstanceOf(Marker m) {
        return parents.contains(m);
    }

    public boolean remove(Marker marker) {
        return parents.remove(marker);
    }

    public Marker setParents(Marker... markers) {
        parents = new HashSet<>(Arrays.asList(markers));
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Marker marker = (Marker) o;
        return Objects.equals(parents, marker.parents) && Objects.equals(name, marker.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parents, name);
    }
}
