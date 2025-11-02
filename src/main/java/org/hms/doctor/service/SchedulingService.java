package org.hms.doctor.service;

import org.hms.doctor.dto.AvailabilityRequest;
import org.hms.doctor.dto.ConfirmRequest;
import org.hms.doctor.dto.ReserveRequest;
import org.hms.doctor.dto.ReserveResponse;
import org.hms.doctor.model.DailyCapacity;
import org.hms.doctor.model.SlotHold;
import org.hms.doctor.repo.DailyCapacityRepository;
import org.hms.doctor.repo.SlotHoldRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.Optional;

@Service
public class SchedulingService {

    private final SlotHoldRepository holdRepo;
    private final DailyCapacityRepository capacityRepo;

    private final int SLOT_MINUTES = 30;
    private final Duration LEAD_TIME = Duration.ofHours(2);

    public SchedulingService(SlotHoldRepository holdRepo, DailyCapacityRepository capacityRepo) {
        this.holdRepo = holdRepo;
        this.capacityRepo = capacityRepo;
    }

    public boolean checkAlignment(OffsetDateTime start, OffsetDateTime end) {
        Duration dur = Duration.between(start, end);
        if (dur.toMinutes() % SLOT_MINUTES != 0) return false;
        return (start.getMinute() % SLOT_MINUTES) == 0;
    }

    public boolean isWithinLeadTime(OffsetDateTime start) {
        return start.isAfter(OffsetDateTime.now(ZoneOffset.UTC).plus(LEAD_TIME));
    }

    @Transactional
    public boolean isAvailable(Long doctorId, AvailabilityRequest req) {
        if (req.slotStart == null || req.slotEnd == null) throw new IllegalArgumentException("slotStart/slotEnd required");
        if (!checkAlignment(req.slotStart, req.slotEnd)) return false;
        if (!isWithinLeadTime(req.slotStart)) return false;
        List<SlotHold> overlaps = holdRepo.findOverlappingForUpdate(doctorId, req.slotStart, req.slotEnd);
        return overlaps.isEmpty();
    }

    @Transactional
    public ReserveResponse reserve(Long doctorId, ReserveRequest req) {
        if (req.slotStart == null || req.slotEnd == null) throw new IllegalArgumentException("slotStart/slotEnd required");
        if (!checkAlignment(req.slotStart, req.slotEnd)) throw new IllegalArgumentException("slot not aligned to grid");
        if (!isWithinLeadTime(req.slotStart)) throw new IllegalArgumentException("slot too soon");

        // lock overlapping holds
        List<SlotHold> overlaps = holdRepo.findOverlappingForUpdate(doctorId, req.slotStart, req.slotEnd);
        if (!overlaps.isEmpty()) throw new IllegalStateException("slot not available");

        // check capacity for day
        LocalDate day = req.slotStart.toLocalDate();
        Optional<DailyCapacity> dcOpt = capacityRepo.findByDoctorIdAndDocDateForUpdate(doctorId, day);
        DailyCapacity dc = dcOpt.orElseGet(() -> {
            DailyCapacity n = new DailyCapacity();
            n.setDoctorId(doctorId);
            n.setDocDate(day);
            n.setCapacity(20);
            n.setBookedCount(0);
            return n;
        });
        if (dc.getBookedCount() >= dc.getCapacity()) throw new IllegalStateException("daily capacity reached");

        // create hold
        SlotHold hold = new SlotHold();
        hold.setDoctorId(doctorId);
        hold.setSlotStart(req.slotStart);
        hold.setSlotEnd(req.slotEnd);
        hold.setHoldStatus("HELD");
        int ttl = (req.ttlMinutes == null) ? 10 : req.ttlMinutes;
        hold.setExpiresAt(OffsetDateTime.now().plusMinutes(ttl));
        SlotHold saved = holdRepo.save(hold);

        return new ReserveResponse(saved.getHoldId(), saved.getExpiresAt());
    }

    @Transactional
    public SlotHold confirm(Long doctorId, Long holdId, ConfirmRequest req) {
        SlotHold hold = holdRepo.findById(holdId).orElseThrow(() -> new IllegalArgumentException("hold not found"));
        if (!hold.getDoctorId().equals(doctorId)) throw new IllegalArgumentException("doctor mismatch");
        if (!"HELD".equals(hold.getHoldStatus())) {
            if ("CONFIRMED".equals(hold.getHoldStatus())) {
                if (hold.getAppointmentId()!=null && hold.getAppointmentId().equals(req.appointmentId)) return hold;
                throw new IllegalStateException("hold already confirmed for another appointment");
            }
            throw new IllegalStateException("hold not in held state");
        }
        // capacity check & increment
        LocalDate day = hold.getSlotStart().toLocalDate();
        DailyCapacity dc = capacityRepo.findByDoctorIdAndDocDateForUpdate(doctorId, day)
                .orElseGet(() -> {
                    DailyCapacity n = new DailyCapacity();
                    n.setDoctorId(doctorId);
                    n.setDocDate(day);
                    n.setCapacity(20);
                    n.setBookedCount(0);
                    return n;
                });
        if (dc.getBookedCount() >= dc.getCapacity()) throw new IllegalStateException("daily capacity reached at confirm");
        dc.setBookedCount(dc.getBookedCount()+1);
        capacityRepo.save(dc);

        hold.setHoldStatus("CONFIRMED");
        hold.setAppointmentId(req.appointmentId);
        SlotHold saved = holdRepo.save(hold);
        return saved;
    }

    @Transactional
    public void release(Long doctorId, Long holdId) {
        SlotHold hold = holdRepo.findById(holdId).orElseThrow(() -> new IllegalArgumentException("hold not found"));
        if (!hold.getDoctorId().equals(doctorId)) throw new IllegalArgumentException("doctor mismatch");
        if ("CONFIRMED".equals(hold.getHoldStatus())) {
            LocalDate day = hold.getSlotStart().toLocalDate();
            DailyCapacity dc = capacityRepo.findByDoctorIdAndDocDateForUpdate(doctorId, day)
                    .orElse(null);
            if (dc!=null && dc.getBookedCount()>0) {
                dc.setBookedCount(dc.getBookedCount()-1);
                capacityRepo.save(dc);
            }
        }
        hold.setHoldStatus("RELEASED");
        holdRepo.save(hold);
    }
}
