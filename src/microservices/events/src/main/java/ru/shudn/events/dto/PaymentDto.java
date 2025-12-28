package ru.shudn.events.dto;

import java.math.BigDecimal;


public record PaymentDto(String id, String userId, BigDecimal amount, String status) {

}
