package org.hms.doctor.dto;

import java.time.OffsetDateTime;

public class ReserveRequest {
    public OffsetDateTime slotStart;
    public OffsetDateTime slotEnd;
    public Long appointmentId;
    public Integer ttlMinutes;
}
