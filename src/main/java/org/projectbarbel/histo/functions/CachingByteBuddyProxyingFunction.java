package org.projectbarbel.histo.functions;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.function.BiFunction;

import org.apache.commons.beanutils.BeanUtils;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;
import org.projectbarbel.histo.model.BarbelProxy;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

public class CachingByteBuddyProxyingFunction implements BiFunction<Object, BitemporalStamp, Object> {

    private final ByteBuddy bytebuddy;
    public static final CachingByteBuddyProxyingFunction INSTANCE = new CachingByteBuddyProxyingFunction();
    private final Objenesis objenesis = new ObjenesisStd();

    private CachingByteBuddyProxyingFunction() {
        bytebuddy = new ByteBuddy();
    }

    public static class Interceptor implements Bitemporal, BarbelProxy{
        private BitemporalStamp sp;
        private Object target;
        private Object proxy;
        
        public Interceptor(BitemporalStamp sp, Object target) {
            super();
            this.sp = sp;
            this.target = target;
        }

        @Override
        public Object getTarget() {
            try {
                if(proxy!=null)
                  BeanUtils.copyProperties(target, proxy);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return target;
        }

        @Override
        public void setTarget(Object target) {
            this.target=target;
            
        }

        @Override
        public BitemporalStamp getBitemporalStamp() {
            return sp;
        }

        @Override
        public void setBitemporalStamp(BitemporalStamp stamp) {
            this.sp=stamp;
        }
        
        @Override
        public String toString() {
            return "Interceptor [sp=" + sp + ", target=" + getTarget() + "]";
        }

        @Override
        public int hashCode() {
            return Objects.hash(sp, getTarget());
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
                    && Objects.equals(getTarget(), otherProxy.getTarget());
        }

        public Object getProxy() {
            return proxy;
        }

        public void setProxy(Object proxy) {
            this.proxy = proxy;
        }

    }

    @Override
    public Object apply(Object template, BitemporalStamp stamp) {

        Object proxy = null;
        Interceptor interceptor = new Interceptor(stamp, template);
        Class<?> clazz = bytebuddy.subclass(template.getClass())
                .implement(Bitemporal.class)
                .implement(BarbelProxy.class)
                .method(ElementMatchers.isDeclaredBy(Bitemporal.class)).intercept(MethodDelegation.to(interceptor))
                .method(ElementMatchers.isDeclaredBy(BarbelProxy.class)).intercept(MethodDelegation.to(interceptor))
                .method(ElementMatchers.isEquals()).intercept(MethodDelegation.to(interceptor))
                .method(ElementMatchers.isHashCode()).intercept(MethodDelegation.to(interceptor))
                .method(ElementMatchers.isToString()).intercept(MethodDelegation.to(interceptor))
                .make().load(getClass().getClassLoader()).getLoaded();
        
        ObjectInstantiator<?> thingyInstantiator = objenesis.getInstantiatorOf(clazz);
        proxy = thingyInstantiator.newInstance();
        interceptor.setProxy(proxy);
        
        try {
            BeanUtils.copyProperties(proxy, template);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        
        return proxy;

    }

}
