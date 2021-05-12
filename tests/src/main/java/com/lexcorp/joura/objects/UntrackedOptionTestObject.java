package com.lexcorp.joura.objects;

import java.util.ArrayList;
import java.util.List;

import com.lexcorp.joura.runtime.Trackable;
import com.lexcorp.joura.runtime.options.Strategy;
import com.lexcorp.joura.runtime.options.TrackOptions;
import com.lexcorp.joura.runtime.options.Untracked;
import com.lexcorp.joura.runtime.options.test.ExpectedFields;

@TrackOptions(strategy = Strategy.ALWAYS_TRACK)
public class UntrackedOptionTestObject implements Trackable {
    private int value1 = 0;
    @Untracked
    private Integer value2 = 2;
    private List<Integer> integers = new ArrayList<>();

    @ExpectedFields(fields = {"value1", "integers"})
    public int getValue1() {
        return value1;
    }

    @ExpectedFields(fields = {})
    @Untracked
    public int testSimpleAssign() {
        value1 = 1000;
        return value1;
    }

    @ExpectedFields(fields = {"value1", "integers"})
    public void testSendFieldToArgs() {
        change(this.value2);
    }

    @ExpectedFields(fields = {})
    @Untracked
    public void testChangeMutableField() {
        this.integers.add(100);
    }

    public static List<Integer> change(Integer value) {
        value = 100;
        return null;
    }

}
