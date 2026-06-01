package com.ecommerce.sshop.exception.order;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class OrderHasBeenPaidExceptionTest {
    @Test
    void messageIsPreserved() {
        OrderHasBeenPaidException ex = new OrderHasBeenPaidException("paid");
        assertEquals("paid", ex.getMessage());
    }
}
