package com.shixin.po;

public enum OutboxMessageStatus {
    /**
     * 已发送
     */
    SENT,
    /**
     * 发送成功
     */
    SEND_SUCCESS,
    /**
     * 发送失败
     */
    SEND_FAILED
}
