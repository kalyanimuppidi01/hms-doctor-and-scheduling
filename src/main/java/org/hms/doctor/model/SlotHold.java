package org.hms.doctor.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "doctor_slots_hold", indexes = {@Index(name = "idx_doctor_slot", columnList = "doctorId,slotStart,slotEnd")})
public class SlotHold {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long holdId;

    private Long doctorId;

    private OffsetDateTime slotStart;
    private OffsetDateTime slotEnd;

    private Long appointmentId;

    private String holdStatus; // HELD, CONFIRMED, RELEASED

    private OffsetDateTime expiresAt;

    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Version
    private Long version;

    // getters / setters
    public Long getHoldId(){return holdId;}
    public void setHoldId(Long holdId){this.holdId=holdId;}
    public Long getDoctorId(){return doctorId;}
    public void setDoctorId(Long doctorId){this.doctorId=doctorId;}
    public OffsetDateTime getSlotStart(){return slotStart;}
    public void setSlotStart(OffsetDateTime slotStart){this.slotStart=slotStart;}
    public OffsetDateTime getSlotEnd(){return slotEnd;}
    public void setSlotEnd(OffsetDateTime slotEnd){this.slotEnd=slotEnd;}
    public Long getAppointmentId(){return appointmentId;}
    public void setAppointmentId(Long appointmentId){this.appointmentId=appointmentId;}
    public String getHoldStatus(){return holdStatus;}
    public void setHoldStatus(String holdStatus){this.holdStatus=holdStatus;}
    public OffsetDateTime getExpiresAt(){return expiresAt;}
    public void setExpiresAt(OffsetDateTime expiresAt){this.expiresAt=expiresAt;}
    public OffsetDateTime getCreatedAt(){return createdAt;}
    public void setCreatedAt(OffsetDateTime createdAt){this.createdAt=createdAt;}
    public Long getVersion(){return version;}
    public void setVersion(Long version){this.version=version;}
}
