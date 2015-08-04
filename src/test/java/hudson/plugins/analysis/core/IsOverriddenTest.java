package hudson.plugins.analysis.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;

import hudson.model.BuildListener;

import hudson.plugins.analysis.core.HealthAwarePublisher;

import static org.junit.Assert.*;

/**
 * Test for {@link HealthAwarePublisher.isOverridden} method.
 */
public class IsOverriddenTest {

    /**
     * Test that a method is found by isOverriden even when it is inherited from an intermediate class.
     */
    @Test
    public void isOverridenTest() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method method = HealthAwarePublisher.class.getDeclaredMethod("isOverridden", Class.class, Class.class, String.class, Class[].class);
        method.setAccessible(true);
        Object o = method.invoke(getMockPublisher(), Base.class, Derived.class, "method", new Class[0]);
        assertTrue((Boolean) o);

        o = method.invoke(getMockPublisher(), Base.class, Intermediate.class, "method", new Class[0]);
        assertTrue((Boolean) o);

        o = method.invoke(getMockPublisher(), Base.class, Base.class, "method", new Class[0]);
        assertFalse((Boolean) o);
    }

    /**
     * Negative test.
     * Trying to check for a method which does not exist in the hierarchy,
     */
    @Test(expected = NoSuchMethodException.class)
    public void isOverriddenNegativeTest() throws Throwable {
        Method method = HealthAwarePublisher.class.getDeclaredMethod("isOverridden", Class.class, Class.class, String.class, Class[].class);
        method.setAccessible(true);
        try {
            method.invoke(getMockPublisher(), Base.class, Derived.class, "method2", new Class[0]);
        } catch (InvocationTargetException e) {
            throw e.getTargetException().getCause();
        }
    }

    private HealthAwarePublisher getMockPublisher() {
        return new HealthAwarePublisher("") {
            @Override
            public MatrixAggregator createAggregator(MatrixBuild build, Launcher launcher, BuildListener listener) {
                return null;
            }
        };
    }

    public abstract class Base {
        protected abstract void method();
    }
    public abstract class Intermediate extends Base {
        protected void method() {}
    }
    public class Derived extends Intermediate {}

}

