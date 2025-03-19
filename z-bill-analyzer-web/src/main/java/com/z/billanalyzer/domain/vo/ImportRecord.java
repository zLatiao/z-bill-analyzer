package com.z.billanalyzer.domain.vo;

import java.time.LocalDateTime;

public record ImportRecord(Integer id, LocalDateTime importTime, String fileNames) {
}
