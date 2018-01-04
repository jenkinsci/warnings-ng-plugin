package hudson.plugins.analysis.util;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;

/**
 * Compatibility utilities.
 */
public final class Compatibility {

    /**
     * Util.isOverridden does not work on non-public methods, that's why this one is used here.
     */
    public static boolean isOverridden(@Nonnull Class base, @Nonnull Class derived, @Nonnull String methodName, @Nonnull Class... types) {
        try {
            return !getMethod(base, methodName, types).equals(getMethod(derived, methodName, types));
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    private static Method getMethod(@Nonnull Class clazz, @Nonnull String methodName, @Nonnull Class... types) throws NoSuchMethodException {
        Method res = null;
        try {
            res = clazz.getDeclaredMethod(methodName, types);
        } catch (NoSuchMethodException e) {
            // Method not found in clazz, let's search in superclasses
            Class superclass = clazz.getSuperclass();
            if (superclass != null) {
                res = getMethod(superclass, methodName, types);
            }
        } catch (SecurityException e) {
            throw new AssertionError(e);
        }
        if (res == null) {
            throw new NoSuchMethodException("Method " + methodName + " not found in " + clazz.getName());
        }
        return res;
    }

    private Compatibility() {}
}

