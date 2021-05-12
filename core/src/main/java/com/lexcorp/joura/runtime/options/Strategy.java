package com.lexcorp.joura.runtime.options;

import java.lang.reflect.InvocationTargetException;

import com.lexcorp.joura.compile.analysis.strategies.AbstractStrategy;
import com.lexcorp.joura.compile.analysis.strategies.AliasStrategy;
import com.lexcorp.joura.compile.analysis.strategies.AnalysisStrategy;
import com.lexcorp.joura.compile.analysis.strategies.SimpleStrategy;

public enum Strategy {
    ALWAYS_TRACK(SimpleStrategy.class), ALIAS_ANALYSIS(AliasStrategy.class);

    Class<? extends AbstractStrategy> strategyClass;

    Strategy(Class<? extends AbstractStrategy> strategy) {
        this.strategyClass = strategy;
    }

    public AnalysisStrategy getStrategy() {
        try {
            return strategyClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return new SimpleStrategy();
    }
}
