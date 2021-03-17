package com.lexcorp.joura;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import com.lexcorp.joura.processors.TrackProcessor;
import com.lexcorp.joura.utils.TestSpooner;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrackingTest {

    static TestSpooner spooner;

    private static final String TEST_CLASS_1 = TestObject1.class.getName();
    private static final String TEST_CLASS_2 = TestObject2.class.getName();

    @BeforeAll
    static void setUp() throws Exception {
        spooner = new TestSpooner()
                .addSource(new File("src/main/java/" + TEST_CLASS_1.replaceAll("\\.", "/") + ".java"))
                .addSource(new File("src/main/java/" + TEST_CLASS_2.replaceAll("\\.", "/") + ".java"));
    }

    @Test
    public void testTrack() throws Exception {
        spooner.process(TrackProcessor.class);
        spooner.print(new File("target/spooned/track"));
        assertTrue(spooner.compile());

        Class clz1 = spooner.getSpoonedClass(TEST_CLASS_1);
        Object instance1 = clz1.getDeclaredConstructors()[0].newInstance();

        clz1.getMethod("update").invoke(instance1);
        clz1.getMethod("startTrack").invoke(instance1);
        clz1.getMethod("update").invoke(instance1);
        clz1.getMethod("stopTrack").invoke(instance1);

        System.out.println("_______________________________________");
        Class clz2 = spooner.getSpoonedClass(TEST_CLASS_2);
        Object instance2 = clz2.getDeclaredConstructors()[0].newInstance();

        clz2.getMethod("update").invoke(instance2);
        clz2.getMethod("startTrack").invoke(instance2);
        clz2.getMethod("update").invoke(instance2);

//        Assert.assertEquals(20, clz.getDeclaredField("lastResult").getInt(instance));
//        Assert.assertEquals(2, Container.CONTAINER.buff.size());
    }

}
