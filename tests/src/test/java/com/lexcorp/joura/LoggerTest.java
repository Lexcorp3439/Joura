package com.lexcorp.joura;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.apache.log4j.Level;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lexcorp.joura.compile.processors.TrackProcessor;
import com.lexcorp.joura.handlers.EventsTestHandler;
import com.lexcorp.joura.objects.SimpleTestObject;
import com.lexcorp.joura.runtime.handlers.LogEventHandler;
import com.lexcorp.joura.runtime.listeners.FieldChangeReceiver;
import com.lexcorp.joura.utils.IOUtils;
import com.lexcorp.joura.utils.TestSpooner;

import spoon.compiler.Environment;
import spoon.support.StandardEnvironment;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoggerTest {
    private static final String FILES_PATH_PREFIX = "src/main/java/";
    static TestSpooner spooner;

    static Class<?> testClass;
    static Object instance;
    static FieldChangeReceiver listener;
    static EventsTestHandler eventsTestHandler;

    @BeforeEach
    void setUp() throws Exception {
        Environment environment = new StandardEnvironment();
        environment.setLevel(String.valueOf(Level.OFF));
        spooner = new TestSpooner(environment);

        Class<SimpleTestObject> beforeTransformClass = SimpleTestObject.class;
        this.compileAndCheckResult(beforeTransformClass);

        testClass = spooner.getSpoonedClass(beforeTransformClass.getName());
        instance = testClass.getDeclaredConstructors()[0].newInstance();
        listener = FieldChangeReceiver.getInstance();
        eventsTestHandler = new EventsTestHandler();
        listener.addEventHandler(eventsTestHandler);
        listener.addEventHandler(new LogEventHandler());
    }

    @Test
    public void testStartStopTrackWorkCorrect() throws Exception {
        invoke("setTag", "SimpleTestObject-1");
        eventsTestHandler.checkEntityReceivedCount(0);

        invoke("testAssignWithThis", 2);
        eventsTestHandler.checkEntityReceivedCount(0);

        invoke("startTrack");
        eventsTestHandler.checkEntityReceivedCount(0);

        invoke("testAssignWithThis", 2);
        eventsTestHandler.checkEntityReceivedCount(1);
        eventsTestHandler.checkLastEntityMethodName("testAssignWithThis(java.lang.Integer)");

        invoke("stopTrack");
        eventsTestHandler.checkEntityReceivedCount(0);

        invoke("testAssignWithThis", 2);
        eventsTestHandler.checkEntityReceivedCount(0);
    }

    @Test
    public void testLogEntriesExistAndCorrect() throws Exception {

    }

    private Object invoke(String methodName, Object... args)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?>[] parameterTypes = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
        return testClass.getMethod(methodName, parameterTypes).invoke(instance, args);
    }

    private void compileAndCheckResult(Class<?> objectClass) throws Exception {
        File testClass = IOUtils.getFileByClass(FILES_PATH_PREFIX, objectClass);
        spooner.addSource(testClass);
        //noinspection unchecked
        spooner.process(TrackProcessor.class);
        spooner.print(new File("target/spooned/track"));
        assertTrue(spooner.compile(), "Compile failed");
    }
}
