package com.lexcorp.joura;

import java.io.File;

import com.lexcorp.joura.objects.ExtendsTestObject;
import com.lexcorp.joura.objects.IfWhileTestObject;
import com.lexcorp.joura.objects.ReferenceTestObject;
import com.lexcorp.joura.objects.SimpleTestObject;
import com.lexcorp.joura.runtime.handlers.Log4JEventHandler;
import com.lexcorp.joura.runtime.listeners.FieldChangeListener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.lexcorp.joura.compile.processors.TrackProcessor;
import com.lexcorp.joura.utils.TestSpooner;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrackingTest {

    static TestSpooner spooner;

    private static final String TEST_CLASS_1 = SimpleTestObject.class.getName();
    private static final String TEST_CLASS_2 = ExtendsTestObject.class.getName();
    private static final String TEST_CLASS_3 = ReferenceTestObject.class.getName();
    private static final String TEST_CLASS_4 = IfWhileTestObject.class.getName();
    private static final String MAIN = TestSpooner.class.getName();


    @BeforeAll
    static void setUp() throws Exception {
        spooner = new TestSpooner()
                .addSource(new File("src/main/java/" + TEST_CLASS_1.replaceAll("\\.", "/") + ".java"))
//                .addSource(new File("src/main/java/" + TEST_CLASS_2.replaceAll("\\.", "/") + ".java"))
//                .addSource(new File("src/main/java/" + TEST_CLASS_3.replaceAll("\\.", "/") + ".java"))
                .addSource(new File("src/main/java/" + TEST_CLASS_4.replaceAll("\\.", "/") + ".java"));
//                .addSource(new File("src/main/java/" + MAIN.replaceAll("\\.", "/") + ".java"));
    }

    @Test
    public void testTrack() throws Exception {
        spooner.process(TrackProcessor.class);
        spooner.print(new File("target/spooned/track"));
        assertTrue(spooner.compile());

        Class clz1 = spooner.getSpoonedClass(TEST_CLASS_1);
        Object instance1 = clz1.getDeclaredConstructors()[0].newInstance();

        FieldChangeListener listener = FieldChangeListener.getInstance();
        listener.addEventHandler(new Log4JEventHandler());
//
        clz1.getMethod("startTrack").invoke(instance1);
        clz1.getMethod("setTag", String.class).invoke(instance1, "SimpleTestObject-1");
        clz1.getMethod("testWriteFieldsWithReference").invoke(instance1);
        clz1.getMethod("stopTrack").invoke(instance1);

//        System.out.println("_______________________________________");
//        Class clz2 = spooner.getSpoonedClass(TEST_CLASS_2);
//        Object instance2 = clz2.getDeclaredConstructors()[0].newInstance();
//
//        clz2.getMethod("update").invoke(instance2);
//        clz2.getMethod("startTrack").invoke(instance2);
//        clz2.getMethod("update").invoke(instance2);
//
//        System.out.println("_______________________________________");
//        Class clz3 = spooner.getSpoonedClass(TEST_CLASS_3);
//        Object instance3 = clz3.getDeclaredConstructors()[0].newInstance();
//
//        clz3.getMethod("update").invoke(instance3);
//        clz3.getMethod("startTrack").invoke(instance3);
//        clz3.getMethod("update").invoke(instance3);
//
//        System.out.println("_______________________________________");
//        Class clz4 = spooner.getSpoonedClass(TEST_CLASS_4);
//        Object instance4 = clz4.getDeclaredConstructors()[0].newInstance();
//
//        clz4.getMethod("startTrack").invoke(instance4);

//        Assert.assertEquals(20, clz.getDeclaredField("lastResult").getInt(instance));
//        Assert.assertEquals(2, Container.CONTAINER.buff.size());
    }

}
