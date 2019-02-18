package org.projectbarbel.histo;

import java.util.concurrent.atomic.AtomicBoolean;

public class MySingleton {

    public final static MySingleton INSTANCE = new MySingleton();
    private boolean initialized = false;
    private AtomicBoolean initializing = new AtomicBoolean(false);
    private String complexProduct;

    public void someRuntimeMethod(String runtimeData) {
        initializeComplexProduct(runtimeData);
        // working on the complex product
        System.out.println(complexProduct);
    }

    private void initializeComplexProduct(String runtimeData) {
        if (initializing.compareAndSet(false, true)) {
            // some complex assembly only once
            complexProduct = "i am a complex product with " + runtimeData;
            System.out.println("created by " + Thread.currentThread().getName());
            initialized = true;
            this.notifyAll(); // notify waiting threads
        }
        while(!initialized) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
