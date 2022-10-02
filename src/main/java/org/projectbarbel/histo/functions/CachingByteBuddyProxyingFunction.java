package org.projectbarbel.histo.functions;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.BiFunction;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;
import org.projectbarbel.histo.model.BarbelProxy;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperMethod;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;

public class CachingByteBuddyProxyingFunction implements BiFunction<Object, BitemporalStamp, Object> {

    private final ByteBuddy bytebuddy;
    public static final CachingByteBuddyProxyingFunction INSTANCE = new CachingByteBuddyProxyingFunction();
    private final Objenesis objenesis = new ObjenesisStd();

    private CachingByteBuddyProxyingFunction() {
        bytebuddy = new ByteBuddy();
    }

    public static class Interceptor {
        private BitemporalStamp sp;
        private Object target;

        public Interceptor(BitemporalStamp sp, Object target) {
            super();
            this.sp = sp;
            this.target = target;
        }

        @RuntimeType
        public Object intercept(@This Object self, @Origin Method method, @AllArguments Object[] args,
                @SuperMethod(nullIfImpossible = true) Method superMethod) throws Throwable {
            if (method.getName().equals("getBitemporalStamp")) {
                return sp;
            } else if (method.getName().equals("setBitemporalStamp")) {
                sp = (BitemporalStamp) getArgument(args);
                if (target instanceof Bitemporal) { // if target is bitemporal sync bitemporal stamps
                    ((Bitemporal) target).setBitemporalStamp(sp);
                }
                return null;
            } else if (method.getName().equals("getTarget")) {
                return target;
            } else if (method.getName().equals("setTarget")) {
                this.target = getArgument(args);
                return null;
            } else if (method.getName().equals("toString")) {
                return sp.getEffectiveTime().toString() + " | " + ftoString(sp, target);
            } else if (method.getName().equals("equals")) {
                Object other = getArgument(args);
                return fequals(this, sp, target, other);
            } else if (method.getName().equals("hashCode")) {
                return fhashCode(sp, target);
            } else {
                return method.invoke(target, args);
            }
        }
    }

    private static Object getArgument(Object[] args) {
        return args.length > 0 ? args[0] : null;
    }

    public static boolean fequals(Object thizz, BitemporalStamp sp, Object target, Object obj) {
        if (thizz == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof BarbelProxy) || !(obj instanceof Bitemporal))
            return false;
        BarbelProxy otherProxy = (BarbelProxy) obj;
        Bitemporal otherBitemporal = (Bitemporal) obj;
        return Objects.equals(sp, otherBitemporal.getBitemporalStamp())
                && Objects.equals(target, otherProxy.getTarget());
    }

    public static String ftoString(BitemporalStamp sp, Object target) {
        return "Interceptor [sp=" + sp + ", target=" + target + "]";
    }

    public static int fhashCode(BitemporalStamp sp, Object target) {
        return Objects.hash(sp, target);
    }

    @Override
    public Object apply(Object template, BitemporalStamp stamp) {

        Object proxy = null;
        Interceptor interceptor = new Interceptor(stamp, template);
        Class<?> clazz = bytebuddy.subclass(template.getClass())
                .implement(Bitemporal.class)
                .implement(BarbelProxy.class).method(ElementMatchers.any()).intercept(MethodDelegation.to(interceptor))
                .make().load(Bitemporal.class.getClassLoader()).getLoaded();

        ObjectInstantiator<?> thingyInstantiator = objenesis.getInstantiatorOf(clazz);
        proxy = thingyInstantiator.newInstance();

        return proxy;

    }

}
