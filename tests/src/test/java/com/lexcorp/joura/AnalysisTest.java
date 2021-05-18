package com.lexcorp.joura;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lexcorp.joura.compile.processors.TrackProcessor;
import com.lexcorp.joura.objects.AlwaysTrackOptionTestObject;
import com.lexcorp.joura.objects.ExtendsTestObject;
import com.lexcorp.joura.objects.IfWhileTestObject;
import com.lexcorp.joura.objects.MethodInvocationTestObject;
import com.lexcorp.joura.objects.ReferenceTestObject;
import com.lexcorp.joura.objects.ReturnStatementsTestObject;
import com.lexcorp.joura.objects.SimpleTestObject;
import com.lexcorp.joura.objects.UntrackedOptionTestObject;
import com.lexcorp.joura.utils.IOUtils;
import com.lexcorp.joura.utils.TestSpooner;

import spoon.compiler.Environment;
import spoon.support.StandardEnvironment;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnalysisTest {
    private static final String FILES_PATH_PREFIX = "src/main/java/";
    static TestSpooner spooner;

    @BeforeEach
    void setUp() throws Exception {
        Environment environment = new StandardEnvironment();
        environment.setLevel(String.valueOf(Level.OFF));
        spooner = new TestSpooner(environment);
    }

    @Test
    public void testAlwaysTrackOption() throws Exception {
        this.compileAndCheckResult(AlwaysTrackOptionTestObject.class);
    }

    @Test
    public void testUntrackedOption() throws Exception {
        this.compileAndCheckResult(UntrackedOptionTestObject.class);
    }

    @Test
    public void testSimpleOperationsWithFields() throws Exception {
        this.compileAndCheckResult(SimpleTestObject.class);
    }

    @Test
    public void testIfWhileAnalysis() throws Exception {
        this.compileAndCheckResult(IfWhileTestObject.class);
    }

    @Test
    public void testOperationsWithReference() throws Exception {
        this.compileAndCheckResult(ReferenceTestObject.class);
    }

    @Test
    public void testOperationsWithExtendsClass() throws Exception {
        this.compileAndCheckResult(List.of(
                ExtendsTestObject.class,
                SimpleTestObject.class
        ));
    }

    @Test
    public void testMethodsInvocationsHandlingTestClass() throws Exception {
        this.compileAndCheckResult(MethodInvocationTestObject.class);
    }

    @Test
    public void testReturnStatementsTestClass() throws Exception {
        this.compileAndCheckResult(ReturnStatementsTestObject.class);
    }

    private void compileAndCheckResult(Class<?> objectClass) throws Exception {
        File testClass = IOUtils.getFileByClass(FILES_PATH_PREFIX, objectClass);
        spooner.addSource(testClass);
        //noinspection unchecked
        spooner.process(TrackProcessor.class);
        spooner.print(new File("target/spooned/track"));
        assertTrue(spooner.compile());
    }

    private void compileAndCheckResult(List<Class<?>> objectsClasses) throws Exception {
        for (Class<?> testClass : objectsClasses) {
            File testClassFile = IOUtils.getFileByClass(FILES_PATH_PREFIX, testClass);
            spooner.addSource(testClassFile);
        }
        //noinspection unchecked
        spooner.process(TrackProcessor.class);
        spooner.print(new File("target/spooned/track"));
        assertTrue(spooner.compile());
    }

}
