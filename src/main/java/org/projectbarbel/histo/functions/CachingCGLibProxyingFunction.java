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

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * Proxying function that uses CGLib and {@link Objenesis}.
 * 
 * @author Niklas Schlimm
 *
 */
public class CachingCGLibProxyingFunction implements BiFunction<Object, BitemporalStamp, Object> {

    private final Enhancer enhancer;
    public static final CachingCGLibProxyingFunction INSTANCE = new CachingCGLibProxyingFunction();
    private final Objenesis objenesis = new ObjenesisStd();

    private CachingCGLibProxyingFunction() {
        enhancer = new Enhancer();
        enhancer.setInterfaces(new Class[] { Bitemporal.class, BarbelProxy.class });
        enhancer.setCallbackType(Interceptor.class);
    }

    public static class Interceptor implements MethodInterceptor {

        private BitemporalStamp sp;
        private Object target;

        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            if (method.getName().equals("getBitemporalStamp")) {
                return sp;
            } else if (method.getName().equals("setBitemporalStamp")) {
                sp = (BitemporalStamp)getArgument(args);
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
                return sp.getEffectiveTime().toString() + " | " + toString();
            } else if (method.getName().equals("equals")) {
                Object other = getArgument(args);
                return equals(other);
            } else if (method.getName().equals("hashCode")) {
                return hashCode();
            } else {
                return proxy.invoke(target, args);
            }
        }

		private Object getArgument(Object[] args) {
			return args.length > 0 ? args[0] : null;
		}

        @Override
        public String toString() {
            return "Interceptor [sp=" + sp + ", target=" + target + "]";
        }

        @Override
        public int hashCode() {
            return Objects.hash(sp, target);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
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

    }

    @Override
    public Object apply(Object template, BitemporalStamp stamp) {

        enhancer.setSuperclass(template.getClass());
        Class<?> proxyClass = enhancer.createClass();
        Enhancer.registerCallbacks(proxyClass, new Callback[] { new Interceptor() });

        ObjectInstantiator<?> thingyInstantiator = objenesis.getInstantiatorOf(proxyClass);
        Object proxy = thingyInstantiator.newInstance();

        ((Bitemporal) proxy).setBitemporalStamp(stamp);
        ((BarbelProxy) proxy).setTarget(template);

        return proxy;

    }

}
