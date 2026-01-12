package com.myproject.point.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_configs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String configKey;

    @Column(nullable = false)
    private Long configValue;

    @Builder
    public PointConfig(String configKey, Long configValue) {
        this.configKey = configKey;
        this.configValue = configValue;
    }

    public void updateValue(Long value) {
        this.configValue = value;
    }
}

