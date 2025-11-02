package org.hms.doctor.repo;

import org.hms.doctor.model.SlotHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.List;

public interface SlotHoldRepository extends JpaRepository<SlotHold, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
      select s from SlotHold s
      where s.doctorId = :doctorId
        and s.holdStatus in ('HELD','CONFIRMED')
        and not (s.slotEnd <= :slotStart or s.slotStart >= :slotEnd)
    """)
    List<SlotHold> findOverlappingForUpdate(@Param("doctorId") Long doctorId,
                                            @Param("slotStart") OffsetDateTime slotStart,
                                            @Param("slotEnd") OffsetDateTime slotEnd);

    List<SlotHold> findByDoctorIdAndHoldStatusAndExpiresAtBefore(Long doctorId, String status, OffsetDateTime before);
}
