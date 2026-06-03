package com.ecommerce.sshop.request.orders;

import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    private String status;
}
