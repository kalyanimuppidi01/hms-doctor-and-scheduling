package org.hms.doctor.repo;

import org.hms.doctor.model.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Page<Doctor> findByDepartmentIgnoreCase(String department, Pageable pageable);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

}
