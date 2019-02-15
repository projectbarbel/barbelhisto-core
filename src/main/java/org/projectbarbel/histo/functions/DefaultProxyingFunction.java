package org.projectbarbel.histo.functions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.BiFunction;

import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;

import com.google.common.base.Defaults;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class DefaultProxyingFunction implements BiFunction<Object, BitemporalStamp, Object> {

    public static class Interceptor implements MethodInterceptor {

        private BitemporalStamp sp;
        private Object target;

        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            if (method.getName().equals("getBitemporalStamp")) {
                return sp;
            } else if (method.getName().equals("setBitemporalStamp")) {
                sp = args.length > 0 ? (BitemporalStamp) args[0] : null;
                if (target instanceof Bitemporal) { // if target is bitemporal sync bitemporal stamps
                    ((Bitemporal) target).setBitemporalStamp(sp);
                }
                return null;
            } else if (method.getName().equals("getTarget")) {
                return target;
            } else if (method.getName().equals("setTarget")) {
                this.target = args.length > 0 ? args[0] : null;
                return null;
            } else if (method.getName().equals("toString")) {
                return sp.getEffectiveTime().toString() + " | " + toString();
            } else if (method.getName().equals("equals")) {
                obj = args.length > 0 ? args[0] : null;
                if (obj instanceof BarbelProxy)
                    return target.equals(((BarbelProxy) obj).getTarget())
                            && sp.equals(((Bitemporal) obj).getBitemporalStamp());
                return target.equals(obj);
            } else if (method.getName().equals("hashCode")) {
                return hashCode();
            } else {
                return proxy.invoke(target, args);
            }
        }

        @Override
        public String toString() {
            return "Interceptor [sp=" + sp + ", target=" + target + "]";
        }

        @Override
        public int hashCode() {
            return Objects.hash(sp, target);
        }

    }

    @Override
    public Object apply(Object template, BitemporalStamp stamp) {

        Enhancer enhancer = new Enhancer();

        enhancer.setCallback(new Interceptor());
        enhancer.setInterfaces(new Class[] { Bitemporal.class, BarbelProxy.class });
        enhancer.setSuperclass(template.getClass());
        enhancer.setUseCache(false);

        Object proxy = null;
        try {
            proxy = tryCreateStraight(enhancer);
        } catch (Exception e1) {
            try {
                proxy = tryCreateComplex(template, enhancer);
            } catch (Exception e2) {
                throw new IllegalArgumentException("failed to create CGI proxy for type: " + template.getClass(), e2);
            }
        }

        ((Bitemporal) proxy).setBitemporalStamp(stamp);
        ((BarbelProxy) proxy).setTarget(template);

        return proxy;

    }

    private Object tryCreateStraight(Enhancer enhancer) {
        Object proxy = enhancer.create();
        return proxy;
    }

    private Object tryCreateComplex(Object template, Enhancer enhancer) {
        Constructor<?>[] constructors = template.getClass().getConstructors();
        if (constructors.length > 0) {
            Class<?>[] parameterTypes = constructors[0].getParameterTypes();
            ArrayList<Object> argumentList = new ArrayList<Object>();
            for (int i = 0; i < parameterTypes.length; i++) {
                argumentList.add(Defaults.defaultValue(parameterTypes[i]));

            }
            Object[] arguments = new Object[argumentList.size()];
            argumentList.toArray(arguments);
            return enhancer.create(parameterTypes, arguments);
        }
        throw new IllegalArgumentException("no constructor found for CGI proxying of type: " + template.getClass());
    }

}
