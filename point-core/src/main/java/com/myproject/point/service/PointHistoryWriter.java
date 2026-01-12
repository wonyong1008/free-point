package com.myproject.point.service;

import com.myproject.point.domain.entity.PointHistory;
import com.myproject.point.domain.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PointHistoryWriter {

    private final PointHistoryRepository pointHistoryRepository;

    public void record(Long userId,
                       PointHistory.PointHistoryType type,
                       String pointKey,
                       String orderNumber,
                       Long amount,
                       Long balance,
                       LocalDateTime createdAt) {
        PointHistory history = PointHistory.builder()
                .userId(userId)
                .type(type)
                .pointKey(pointKey)
                .orderNumber(orderNumber)
                .amount(amount)
                .balance(balance)
                .createdAt(createdAt)
                .build();
        pointHistoryRepository.save(history);
    }
}
