package com.shixin.po;

public enum OrderStatus {
    /**
     * 订单创建
     */
    CREATED,
    /**
     * 商家接单
     */
    RESTAURANT_ACCEPTED,
    /**
     * 骑手接单
     */
    DELIVERY_ACCEPTED,
    /**
     * 订单失败
     */
    FAILED
}
