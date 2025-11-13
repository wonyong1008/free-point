package com.musinsa.point.service;

import com.musinsa.point.domain.entity.PointConfig;
import com.musinsa.point.domain.repository.PointConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointConfigService {

    private final PointConfigRepository pointConfigRepository;

    private static final String MAX_ACCUMULATE_AMOUNT_KEY = "MAX_ACCUMULATE_AMOUNT";
    private static final String MAX_USER_BALANCE_KEY = "MAX_USER_BALANCE";
    private static final Long DEFAULT_MAX_ACCUMULATE_AMOUNT = 100_000L;
    private static final Long DEFAULT_MAX_USER_BALANCE = 1_000_000L;

    public Long getMaxAccumulateAmount() {
        return pointConfigRepository.findByConfigKey(MAX_ACCUMULATE_AMOUNT_KEY)
                .map(PointConfig::getConfigValue)
                .orElse(DEFAULT_MAX_ACCUMULATE_AMOUNT);
    }

    public Long getMaxUserBalance() {
        return pointConfigRepository.findByConfigKey(MAX_USER_BALANCE_KEY)
                .map(PointConfig::getConfigValue)
                .orElse(DEFAULT_MAX_USER_BALANCE);
    }

    @Transactional
    public void setMaxAccumulateAmount(Long amount) {
        PointConfig config = pointConfigRepository.findByConfigKey(MAX_ACCUMULATE_AMOUNT_KEY)
                .orElse(PointConfig.builder()
                        .configKey(MAX_ACCUMULATE_AMOUNT_KEY)
                        .configValue(amount)
                        .build());
        config.updateValue(amount);
        pointConfigRepository.save(config);
    }

    @Transactional
    public void setMaxUserBalance(Long balance) {
        PointConfig config = pointConfigRepository.findByConfigKey(MAX_USER_BALANCE_KEY)
                .orElse(PointConfig.builder()
                        .configKey(MAX_USER_BALANCE_KEY)
                        .configValue(balance)
                        .build());
        config.updateValue(balance);
        pointConfigRepository.save(config);
    }
}

