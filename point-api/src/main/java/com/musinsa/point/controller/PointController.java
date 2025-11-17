package com.musinsa.point.controller;

import com.musinsa.point.dto.request.AccumulateCancelRequest;
import com.musinsa.point.dto.request.AccumulateRequest;
import com.musinsa.point.dto.request.UseCancelRequest;
import com.musinsa.point.dto.request.UseRequest;
import com.musinsa.point.dto.response.BalanceResponse;
import com.musinsa.point.dto.response.PointHistoryResponse;
import com.musinsa.point.dto.response.PointResponse;
import com.musinsa.point.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "포인트 API", description = "무료 포인트 시스템 API")
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @Operation(summary = "포인트 적립", description = "사용자에게 포인트를 적립합니다.")
    @PostMapping("/accumulate")
    public ResponseEntity<PointResponse> accumulate(@Valid @RequestBody AccumulateRequest request) {
        PointResponse response = pointService.accumulate(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "포인트 적립 취소", description = "적립된 포인트를 취소합니다. (일부 사용된 경우 불가)")
    @PostMapping("/accumulate/cancel")
    public ResponseEntity<Void> accumulateCancel(@Valid @RequestBody AccumulateCancelRequest request) {
        pointService.accumulateCancel(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "포인트 사용", description = "주문 시 포인트를 사용합니다. 수기 지급 포인트 우선, 만료일 짧은 순서로 사용됩니다.")
    @PostMapping("/use")
    public ResponseEntity<PointResponse> use(@Valid @RequestBody UseRequest request) {
        PointResponse response = pointService.use(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "포인트 사용 취소", description = "사용한 포인트를 취소합니다. 전체 또는 일부 취소 가능하며, 만료된 포인트는 신규 적립 처리됩니다.")
    @PostMapping("/use/cancel")
    public ResponseEntity<Void> useCancel(@Valid @RequestBody UseCancelRequest request) {
        pointService.useCancel(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "포인트 잔액 조회", description = "사용자의 포인트 잔액을 조회합니다.")
    @GetMapping("/balance/{userId}")
    public ResponseEntity<BalanceResponse> getBalance(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long userId) {
        BalanceResponse response = pointService.getBalance(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "포인트 이력 조회", description = "사용자의 포인트 적립/사용 이력을 조회합니다.")
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<PointHistoryResponse>> getHistory(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long userId) {
        List<PointHistoryResponse> response = pointService.getHistory(userId);
        return ResponseEntity.ok(response);
    }
}

