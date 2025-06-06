package com.shixin.util;

public class RabbitMQMessage {
    private String exchange;
    private String routingKey;
    private Object message;

    public RabbitMQMessage(String exchange, String routingKey, Object message) {
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.message = message;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "RabbitMQMessage{" +
                "exchange='" + exchange + '\'' +
                ", routingKey='" + routingKey + '\'' +
                ", message=" + message +
                '}';
    }
}
