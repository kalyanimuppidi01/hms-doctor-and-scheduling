package org.hms.doctor.dto;

import java.time.OffsetDateTime;

public class AvailabilityRequest {
    public OffsetDateTime slotStart;
    public OffsetDateTime slotEnd;
    public Long patientId;
    public Long appointmentId;
}
