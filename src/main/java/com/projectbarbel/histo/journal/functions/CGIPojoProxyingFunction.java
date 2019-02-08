package com.projectbarbel.histo.journal.functions;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class CGIPojoProxyingFunction implements BiFunction<Object, BitemporalStamp, Object> {

    @Override
    public Object apply(Object template, BitemporalStamp stamp) {
        
        Enhancer enhancer = new Enhancer();
        
        enhancer.setClassLoader(getClass().getClassLoader());
        enhancer.setSuperclass(template.getClass());

        enhancer.setCallback(new MethodInterceptor() {
        
            private BitemporalStamp sp = stamp;
            private Object target = template;

            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                if (method.getName().equals("getBitemporalStamp")) {
                    return sp;
                } else if (method.getName().equals("setBitemporalStamp")) {
                    sp = args.length > 0 ? (BitemporalStamp)args[0] : null;
                    if (template instanceof Bitemporal) { // if target is bitemporal sync bitemporal stamps
                        ((Bitemporal)template).setBitemporalStamp(sp);
                    }
                    return null;
                } else if (method.getName().equals("getTarget")) {
                    return target;
                } else if (method.getName().equals("toString")) {
                    return target.toString() + "/" + sp.getEffectiveTime().toString();
                } else {
                    return proxy.invoke(target, args);
                }
            }

        });
        
        enhancer.setInterfaces(new Class[] { Bitemporal.class, BarbelProxy.class });
        
        Object myProxy = enhancer.create();

        return myProxy;

    }

}
