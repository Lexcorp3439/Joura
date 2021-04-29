package com.lexcorp.joura.analysis.point;

import java.util.Objects;
import java.util.Set;

public class Alias {
    String aliasName;
    Set<Alias> aliases;

    public Alias(String aliasName) {
        this.aliasName = aliasName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Alias point = (Alias) o;
        return aliasName.equals(point.aliasName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aliasName);
    }

    @Override
    public String toString() {
        return "Point( " + aliasName + " )";
    }
}
