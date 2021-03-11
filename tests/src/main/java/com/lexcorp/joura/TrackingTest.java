package com.lexcorp.joura;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import com.lexcorp.joura.processors.TrackProcessor;
import com.lexcorp.joura.processors.TrackProcessor1;
import com.lexcorp.joura.templates.TrackableTemplate;
import com.lexcorp.joura.utils.TestSpooner;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrackingTest {

    static TestSpooner spooner;

    private static final String TEST_CLASS = TestObject.class.getName();

    @BeforeAll
    static void setUp() throws Exception {
        spooner = new TestSpooner()
                .addSource(new File("src/main/java/" + TEST_CLASS.replaceAll("\\.", "/") + ".java"));
    }

    @Test
    public void testTrack() throws Exception {
        spooner.process(TrackProcessor1.class);
        spooner.print(new File("target/spooned/track"));
        assertTrue(spooner.compile());

        Class clz = spooner.getSpoonedClass(TEST_CLASS);
        Object instance = clz.getDeclaredConstructors()[0].newInstance();

        clz.getMethod("update").invoke(instance);
        clz.getMethod("startTrack").invoke(instance);
        clz.getMethod("update").invoke(instance);
        clz.getMethod("stopTrack").invoke(instance);

//        Assert.assertEquals(20, clz.getDeclaredField("lastResult").getInt(instance));
//        Assert.assertEquals(2, Container.CONTAINER.buff.size());
    }

}
