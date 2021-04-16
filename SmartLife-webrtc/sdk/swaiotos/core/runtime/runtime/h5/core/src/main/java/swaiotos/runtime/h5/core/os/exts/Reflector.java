package swaiotos.runtime.h5.core.os.exts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created by qiaopu on 2018/4/26.
 */
public class Reflector {

    /**
     * The constant LOG_TAG.
     */
    public static final String LOG_TAG = "Reflector";

    /**
     * The M type.
     */
    protected Class<?> mType;
    /**
     * The M caller.
     */
    protected Object mCaller;
    /**
     * The M constructor.
     */
    protected Constructor mConstructor;
    /**
     * The M field.
     */
    protected Field mField;
    /**
     * The M method.
     */
    protected Method mMethod;

    /**
     * The type Reflected exception.
     */
    public static class ReflectedException extends Exception {

        /**
         * Instantiates a new Reflected exception.
         *
         * @param message the message
         */
        public ReflectedException(String message) {
            super(message);
        }

        /**
         * Instantiates a new Reflected exception.
         *
         * @param message the message
         * @param cause   the cause
         */
        public ReflectedException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    /**
     * On reflector.
     *
     * @param name the name
     * @return the reflector
     * @throws ReflectedException the reflected exception
     */
    public static Reflector on(@NonNull String name) throws ReflectedException {
        return on(name, true, Reflector.class.getClassLoader());
    }

    /**
     * On reflector.
     *
     * @param name       the name
     * @param initialize the initialize
     * @return the reflector
     * @throws ReflectedException the reflected exception
     */
    public static Reflector on(@NonNull String name, boolean initialize) throws ReflectedException {
        return on(name, initialize, Reflector.class.getClassLoader());
    }

    /**
     * On reflector.
     *
     * @param name       the name
     * @param initialize the initialize
     * @param loader     the loader
     * @return the reflector
     * @throws ReflectedException the reflected exception
     */
    public static Reflector on(@NonNull String name, boolean initialize, @Nullable ClassLoader loader) throws ReflectedException {
        try {
            return on(Class.forName(name, initialize, loader));
        } catch (Throwable e) {
            throw new ReflectedException("Oops!", e);
        }
    }

    /**
     * On reflector.
     *
     * @param type the type
     * @return the reflector
     */
    public static Reflector on(@NonNull Class<?> type) {
        Reflector reflector = new Reflector();
        reflector.mType = type;
        return reflector;
    }

    /**
     * With reflector.
     *
     * @param caller the caller
     * @return the reflector
     * @throws ReflectedException the reflected exception
     */
    public static Reflector with(@NonNull Object caller) throws ReflectedException {
        return on(caller.getClass()).bind(caller);
    }

    /**
     * Instantiates a new Reflector.
     */
    protected Reflector() {

    }

    /**
     * Constructor reflector.
     *
     * @param parameterTypes the parameter types
     * @return the reflector
     * @throws ReflectedException the reflected exception
     */
    public Reflector constructor(@Nullable Class<?>... parameterTypes) throws ReflectedException {
        try {
            mConstructor = mType.getDeclaredConstructor(parameterTypes);
            mConstructor.setAccessible(true);
            mField = null;
            mMethod = null;
            return this;
        } catch (Throwable e) {
            throw new ReflectedException("Oops!", e);
        }
    }

    /**
     * New instance r.
     *
     * @param <R>      the type parameter
     * @param initargs the initargs
     * @return the r
     * @throws ReflectedException the reflected exception
     */
    @SuppressWarnings("unchecked")
    public <R> R newInstance(@Nullable Object... initargs) throws ReflectedException {
        if (mConstructor == null) {
            throw new ReflectedException("Constructor was null!");
        }
        try {
            return (R) mConstructor.newInstance(initargs);
        } catch (InvocationTargetException e) {
            throw new ReflectedException("Oops!", e.getTargetException());
        } catch (Throwable e) {
            throw new ReflectedException("Oops!", e);
        }
    }

    /**
     * Checked object.
     *
     * @param caller the caller
     * @return the object
     * @throws ReflectedException the reflected exception
     */
    protected Object checked(@Nullable Object caller) throws ReflectedException {
        if (caller == null || mType.isInstance(caller)) {
            return caller;
        }
        throw new ReflectedException("Caller [" + caller + "] is not a instance of type [" + mType + "]!");
    }

    /**
     * Check.
     *
     * @param caller the caller
     * @param member the member
     * @param name   the name
     * @throws ReflectedException the reflected exception
     */
    protected void check(@Nullable Object caller, @Nullable Member member, @NonNull String name) throws ReflectedException {
        if (member == null) {
            throw new ReflectedException(name + " was null!");
        }
        if (caller == null && !Modifier.isStatic(member.getModifiers())) {
            throw new ReflectedException("Need a caller!");
        }
        checked(caller);
    }

    /**
     * Bind reflector.
     *
     * @param caller the caller
     * @return the reflector
     * @throws ReflectedException the reflected exception
     */
    public Reflector bind(@Nullable Object caller) throws ReflectedException {
        mCaller = checked(caller);
        return this;
    }

    /**
     * Unbind reflector.
     *
     * @return the reflector
     */
    public Reflector unbind() {
        mCaller = null;
        return this;
    }

    /**
     * Field reflector.
     *
     * @param name the name
     * @return the reflector
     * @throws ReflectedException the reflected exception
     */
    public Reflector field(@NonNull String name) throws ReflectedException {
        try {
            mField = findField(name);
            mField.setAccessible(true);
            mConstructor = null;
            mMethod = null;
            return this;
        } catch (Throwable e) {
            throw new ReflectedException("Oops!", e);
        }
    }

    /**
     * Find field field.
     *
     * @param name the name
     * @return the field
     * @throws NoSuchFieldException the no such field exception
     */
    protected Field findField(@NonNull String name) throws NoSuchFieldException {
        try {
            return mType.getField(name);
        } catch (NoSuchFieldException e) {
            for (Class<?> cls = mType; cls != null; cls = cls.getSuperclass()) {
                try {
                    return cls.getDeclaredField(name);
                } catch (NoSuchFieldException ex) {
                    // Ignored
                }
            }
            throw e;
        }
    }

    /**
     * Get r.
     *
     * @param <R> the type parameter
     * @return the r
     * @throws ReflectedException the reflected exception
     */
    @SuppressWarnings("unchecked")
    public <R> R get() throws ReflectedException {
        return get(mCaller);
    }

    /**
     * Get r.
     *
     * @param <R>    the type parameter
     * @param caller the caller
     * @return the r
     * @throws ReflectedException the reflected exception
     */
    @SuppressWarnings("unchecked")
    public <R> R get(@Nullable Object caller) throws ReflectedException {
        check(caller, mField, "Field");
        try {
            return (R) mField.get(caller);
        } catch (Throwable e) {
            throw new ReflectedException("Oops!", e);
        }
    }

    /**
     * Set reflector.
     *
     * @param value the value
     * @return the reflector
     * @throws ReflectedException the reflected exception
     */
    public Reflector set(@Nullable Object value) throws ReflectedException {
        return set(mCaller, value);
    }

    /**
     * Set reflector.
     *
     * @param caller the caller
     * @param value  the value
     * @return the reflector
     * @throws ReflectedException the reflected exception
     */
    public Reflector set(@Nullable Object caller, @Nullable Object value) throws ReflectedException {
        check(caller, mField, "Field");
        try {
            mField.set(caller, value);
            return this;
        } catch (Throwable e) {
            throw new ReflectedException("Oops!", e);
        }
    }

    /**
     * Method reflector.
     *
     * @param name           the name
     * @param parameterTypes the parameter types
     * @return the reflector
     * @throws ReflectedException the reflected exception
     */
    public Reflector method(@NonNull String name, @Nullable Class<?>... parameterTypes) throws ReflectedException {
        try {
            mMethod = findMethod(name, parameterTypes);
            mMethod.setAccessible(true);
            mConstructor = null;
            mField = null;
            return this;
        } catch (NoSuchMethodException e) {
            throw new ReflectedException("Oops!", e);
        }
    }

    /**
     * Find method method.
     *
     * @param name           the name
     * @param parameterTypes the parameter types
     * @return the method
     * @throws NoSuchMethodException the no such method exception
     */
    protected Method findMethod(@NonNull String name, @Nullable Class<?>... parameterTypes) throws NoSuchMethodException {
        try {
            return mType.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            for (Class<?> cls = mType; cls != null; cls = cls.getSuperclass()) {
                try {
                    return cls.getDeclaredMethod(name, parameterTypes);
                } catch (NoSuchMethodException ex) {
                    // Ignored
                }
            }
            throw e;
        }
    }

    /**
     * Call r.
     *
     * @param <R>  the type parameter
     * @param args the args
     * @return the r
     * @throws ReflectedException the reflected exception
     */
    public <R> R call(@Nullable Object... args) throws ReflectedException {
        return callByCaller(mCaller, args);
    }

    /**
     * Call by caller r.
     *
     * @param <R>    the type parameter
     * @param caller the caller
     * @param args   the args
     * @return the r
     * @throws ReflectedException the reflected exception
     */
    @SuppressWarnings("unchecked")
    public <R> R callByCaller(@Nullable Object caller, @Nullable Object... args) throws ReflectedException {
        check(caller, mMethod, "Method");
        try {
            return (R) mMethod.invoke(caller, args);
        } catch (InvocationTargetException e) {
            throw new ReflectedException("Oops!", e.getTargetException());
        } catch (Throwable e) {
            throw new ReflectedException("Oops!", e);
        }
    }

    /**
     * The type Quiet reflector.
     */
    public static class QuietReflector extends Reflector {

        /**
         * The M ignored.
         */
        protected Throwable mIgnored;

        /**
         * On quiet reflector.
         *
         * @param name the name
         * @return the quiet reflector
         */
        public static QuietReflector on(@NonNull String name) {
            return on(name, true, QuietReflector.class.getClassLoader());
        }

        /**
         * On quiet reflector.
         *
         * @param name       the name
         * @param initialize the initialize
         * @return the quiet reflector
         */
        public static QuietReflector on(@NonNull String name, boolean initialize) {
            return on(name, initialize, QuietReflector.class.getClassLoader());
        }

        /**
         * On quiet reflector.
         *
         * @param name       the name
         * @param initialize the initialize
         * @param loader     the loader
         * @return the quiet reflector
         */
        public static QuietReflector on(@NonNull String name, boolean initialize, @Nullable ClassLoader loader) {
            Class<?> cls = null;
            try {
                cls = Class.forName(name, initialize, loader);
                return on(cls, null);
            } catch (Throwable e) {
//                Log.w(LOG_TAG, "Oops!", e);
                return on(cls, e);
            }
        }

        /**
         * On quiet reflector.
         *
         * @param type the type
         * @return the quiet reflector
         */
        public static QuietReflector on(@Nullable Class<?> type) {
            return on(type, (type == null) ? new ReflectedException("Type was null!") : null);
        }

        private static QuietReflector on(@Nullable Class<?> type, @Nullable Throwable ignored) {
            QuietReflector reflector = new QuietReflector();
            reflector.mType = type;
            reflector.mIgnored = ignored;
            return reflector;
        }

        /**
         * With quiet reflector.
         *
         * @param caller the caller
         * @return the quiet reflector
         */
        public static QuietReflector with(@Nullable Object caller) {
            if (caller == null) {
                return on((Class<?>) null);
            }
            return on(caller.getClass()).bind(caller);
        }

        /**
         * Instantiates a new Quiet reflector.
         */
        protected QuietReflector() {

        }

        /**
         * Gets ignored.
         *
         * @return the ignored
         */
        public Throwable getIgnored() {
            return mIgnored;
        }

        /**
         * Skip boolean.
         *
         * @return the boolean
         */
        protected boolean skip() {
            return skipAlways() || mIgnored != null;
        }

        /**
         * Skip always boolean.
         *
         * @return the boolean
         */
        protected boolean skipAlways() {
            return mType == null;
        }

        @Override
        public QuietReflector constructor(@Nullable Class<?>... parameterTypes) {
            if (skipAlways()) {
                return this;
            }
            try {
                mIgnored = null;
                super.constructor(parameterTypes);
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return this;
        }

        @Override
        public <R> R newInstance(@Nullable Object... initargs) {
            if (skip()) {
                return null;
            }
            try {
                mIgnored = null;
                return super.newInstance(initargs);
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return null;
        }

        @Override
        public QuietReflector bind(@Nullable Object obj) {
            if (skipAlways()) {
                return this;
            }
            try {
                mIgnored = null;
                super.bind(obj);
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return this;
        }

        @Override
        public QuietReflector unbind() {
            super.unbind();
            return this;
        }

        @Override
        public QuietReflector field(@NonNull String name) {
            if (skipAlways()) {
                return this;
            }
            try {
                mIgnored = null;
                super.field(name);
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return this;
        }

        @Override
        public <R> R get() {
            if (skip()) {
                return null;
            }
            try {
                mIgnored = null;
                return super.get();
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return null;
        }

        @Override
        public <R> R get(@Nullable Object caller) {
            if (skip()) {
                return null;
            }
            try {
                mIgnored = null;
                return super.get(caller);
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return null;
        }

        @Override
        public QuietReflector set(@Nullable Object value) {
            if (skip()) {
                return this;
            }
            try {
                mIgnored = null;
                super.set(value);
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return this;
        }

        @Override
        public QuietReflector set(@Nullable Object caller, @Nullable Object value) {
            if (skip()) {
                return this;
            }
            try {
                mIgnored = null;
                super.set(caller, value);
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return this;
        }

        @Override
        public QuietReflector method(@NonNull String name, @Nullable Class<?>... parameterTypes) {
            if (skipAlways()) {
                return this;
            }
            try {
                mIgnored = null;
                super.method(name, parameterTypes);
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return this;
        }

        @Override
        public <R> R call(@Nullable Object... args) {
            if (skip()) {
                return null;
            }
            try {
                mIgnored = null;
                return super.call(args);
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return null;
        }

        @Override
        public <R> R callByCaller(@Nullable Object caller, @Nullable Object... args) {
            if (skip()) {
                return null;
            }
            try {
                mIgnored = null;
                return super.callByCaller(caller, args);
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return null;
        }
    }
}
