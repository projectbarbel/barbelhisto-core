package com.projectbarbel.histo.journal.functions;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class CGIPojoProxyingFunction<T> implements BiFunction<T, BitemporalStamp, T> {

    @Override
    public T apply(T template, BitemporalStamp stamp) {
        
        Enhancer enhancer = new Enhancer();

        enhancer.setClassLoader(getClass().getClassLoader());
        enhancer.setSuperclass(template.getClass());

        enhancer.setCallback(new MethodInterceptor() {
        
            private BitemporalStamp sp = stamp;
            private T target = template;

            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                if (method.getName().equals("getBitemporalStamp")) {
                    return sp;
                } else if (method.getName().equals("setBitemporalStamp")) {
                    sp = args.length > 0 ? (BitemporalStamp)args[0] : null;
                    return null;
                } else if (method.getName().equals("getTarget")) {
                    return target;
                } else {
                    return proxy.invoke(target, args);
                }
            }

        });
        
        enhancer.setInterfaces(new Class[] { Bitemporal.class, BarbelProxy.class });
        
        @SuppressWarnings("unchecked")
        T myProxy = (T) enhancer.create();

        return myProxy;

    }

}
