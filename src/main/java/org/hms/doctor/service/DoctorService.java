package org.hms.doctor.service;

import org.hms.doctor.model.Doctor;
import org.hms.doctor.repo.DoctorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DoctorService {
    private final DoctorRepository repo;
    public DoctorService(DoctorRepository repo) { this.repo = repo; }

    public Doctor create(Doctor d) { return repo.save(d); }
    public Optional<Doctor> get(Long id) { return repo.findById(id); }
    public Page<Doctor> listByDepartment(String department, Pageable pageable) {
        if (department == null) return repo.findAll(pageable);
        return repo.findByDepartmentIgnoreCase(department, pageable);
    }
    public Doctor update(Long id, Doctor update) {
        return repo.findById(id).map(existing -> {
            existing.setName(update.getName());
            existing.setEmail(update.getEmail());
            existing.setPhone(update.getPhone());
            existing.setDepartment(update.getDepartment());
            existing.setSpecialization(update.getSpecialization());
            existing.setDailyCapacity(update.getDailyCapacity());
            existing.setActive(update.getActive());
            return repo.save(existing);
        }).orElseThrow(() -> new RuntimeException("Doctor not found"));
    }
    public void deactivate(Long id) {
        repo.findById(id).ifPresent(d -> { d.setActive(false); repo.save(d); });
    }
}
