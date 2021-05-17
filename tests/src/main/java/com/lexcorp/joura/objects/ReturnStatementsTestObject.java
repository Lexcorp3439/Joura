package com.lexcorp.joura.objects;

import com.lexcorp.joura.runtime.Trackable;
import com.lexcorp.joura.runtime.options.test.ExpectedFields;

public class ReturnStatementsTestObject implements Trackable {
    private ReturnStatementsTestObject reference;
    private int value1 = 6;
    private int value2 = 0;
    private int value3 = 0;

    @ExpectedFields(fields = {"value2"})
    public int testSomeReturnStatements() {
        for (int i = 0; i < this.value1; i++) {
            if (this.value2 > 100) {
                return this.value3;
            }
            this.value2 += 5;
        }

        if (value3 > 20) {
            return 1000;
        }
        return value1;
    }
}
