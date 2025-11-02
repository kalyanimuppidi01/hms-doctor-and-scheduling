package org.hms.doctor.dto;

import java.time.OffsetDateTime;

public class ReserveResponse {
    public Long holdId;
    public OffsetDateTime expiresAt;
    public ReserveResponse(Long holdId, OffsetDateTime expiresAt) {
        this.holdId = holdId; this.expiresAt = expiresAt;
    }
}
