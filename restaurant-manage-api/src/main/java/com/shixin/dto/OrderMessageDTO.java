package com.shixin.dto;

import java.math.BigDecimal;

public class OrderMessageDTO {
    private Long id;

    private Long accountId;

    private String address;

    private Long restaurantId;

    private OrderStatus status;

    private BigDecimal price;

    private boolean confirm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isConfirm() {
        return confirm;
    }

    public void setConfirm(boolean confirm) {
        this.confirm = confirm;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "OrderMessageDTO{" +
                "id=" + id +
                ", accountId=" + accountId +
                ", address='" + address + '\'' +
                ", restaurantId=" + restaurantId +
                ", status=" + status +
                ", price=" + price +
                ", confirm=" + confirm +
                '}';
    }
}
