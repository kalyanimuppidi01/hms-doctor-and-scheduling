package org.hms.doctor.repo;

import org.hms.doctor.model.DailyCapacity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;

public interface DailyCapacityRepository extends JpaRepository<DailyCapacity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DailyCapacity d where d.doctorId = :doctorId and d.docDate = :docDate")
    Optional<DailyCapacity> findByDoctorIdAndDocDateForUpdate(@Param("doctorId") Long doctorId, @Param("docDate") LocalDate docDate);
}
