package com.ecommerce.sshop.model.carts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class CartItemModelTest {
    @Test
    void setTotalPrice_CoversBothBranches() {
        CartItem item = new CartItem();
        item.setQuantity(3);
        item.setUnitPrice(new BigDecimal("10"));
        item.setTotalPrice();
        assertEquals(new BigDecimal("30"), item.getTotalPrice());

        item.setUnitPrice(null);
        item.setTotalPrice();
        assertEquals(BigDecimal.ZERO, item.getTotalPrice());
    }
}
