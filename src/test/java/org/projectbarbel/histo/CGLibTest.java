package org.projectbarbel.histo;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;
import org.projectbarbel.histo.model.BarbelProxy;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.pojos.ComplexFieldsPrivatePojoPartialContructor;
import org.projectbarbel.histo.pojos.ComplexFieldsPrivatePojoPartialContructorWithComplexType;
import org.projectbarbel.histo.pojos.NoPrimitivePrivatePojoPartialContructor;
import org.projectbarbel.histo.pojos.PrimitivePrivatePojo;
import org.projectbarbel.histo.pojos.PrimitivePrivatePojoPartialContructor;

import io.github.benas.randombeans.api.EnhancedRandom;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class CGLibTest {

    @SuppressWarnings("unused")
    private static Stream<Arguments> createPojos() {
        return Stream.of(Arguments.of(EnhancedRandom.random(PrimitivePrivatePojo.class)),
                Arguments.of(EnhancedRandom.random(PrimitivePrivatePojoPartialContructor.class)),
                Arguments.of(EnhancedRandom.random(NoPrimitivePrivatePojoPartialContructor.class)),
                Arguments.of(EnhancedRandom.random(ComplexFieldsPrivatePojoPartialContructorWithComplexType.class)),
                Arguments.of(EnhancedRandom.random(ComplexFieldsPrivatePojoPartialContructor.class)));
    }

    @SuppressWarnings({ "unused", "rawtypes" })
    @ParameterizedTest
    @MethodSource("createPojos")
    public void test(Object pojo) throws InstantiationException, IllegalAccessException {

        MethodInterceptor interceptor = new Interceptor();

        Enhancer enhancer = new Enhancer();
        enhancer.setInterfaces(new Class[] { Bitemporal.class, BarbelProxy.class });
        enhancer.setCallbackType(interceptor.getClass());
        enhancer.setSuperclass(pojo.getClass());
        Class<?> classForProxy = enhancer.createClass();

        Objenesis objenesis = new ObjenesisStd();
        ObjectInstantiator<?> thingyInstantiator = objenesis.getInstantiatorOf(classForProxy);

        Enhancer.registerCallbacks(classForProxy, new Callback[] { new Interceptor() });
        Object object1 = thingyInstantiator.newInstance();
        ((Bitemporal) object1).setBitemporalStamp(BitemporalStamp.createActive());

        Enhancer.registerCallbacks(classForProxy, new Callback[] { new Interceptor() });
        Object object2 = thingyInstantiator.newInstance();
        ((Bitemporal) object2).setBitemporalStamp(null);

        Enhancer.registerCallbacks(classForProxy, new Callback[] { new Interceptor() });
        Object object3 = thingyInstantiator.newInstance();
        ((BarbelProxy) object3).setTarget(pojo);

        // Factory factory = (Factory)object3;
        // Object objFactory = factory.newInstance(new Interceptor());

        Object object4 = thingyInstantiator.newInstance();
        assertNotNull(((BarbelProxy) object4).getTarget()); // the interceptor of the previous instance !!

        Class[] interfaces = classForProxy.getInterfaces();

    }

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

}
