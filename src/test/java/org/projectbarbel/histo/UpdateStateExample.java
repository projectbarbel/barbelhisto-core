package org.projectbarbel.histo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;

public class UpdateStateExample {

    public enum UpdateState {
        UPDATEABLE(()->Validate.validState(true)), READONLY(()->Validate.validState(false));
        private Runnable action;
        private UpdateState(Runnable action) {
            this.action=action;
        }
        public <T> T set(T value) {
            action.run();
            return value;
        }
    }

    public static class ShoppingBasket1 {
        private String orderNo;
        private List<String> articleNumbers = new ArrayList<>();
        public void add(String articleNumber) {
            articleNumbers.add(articleNumber);
        }
        public String getOrderNo() {
            return orderNo;
        }
        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }
        public void order() {
            // some ordering logic and if succeeded, change state
        }
    }

    // shopping basket with state pattern
    public static class ShoppingBasket {
        private String orderNo;
        private List<String> articleNumbers = new ArrayList<>();
        private UpdateState state = UpdateState.UPDATEABLE;
        public void add(String articleNumber) {
            articleNumbers.add(state.set(articleNumber));
        }
        public String getOrderNo() {
            return orderNo;
        }
        public void setOrderNo(String orderNo) {
            this.orderNo = state.set(orderNo);
        }
        public void order() {
            // some ordering logic and if succeeded, change state
            state = UpdateState.READONLY;
        }
    }
}
