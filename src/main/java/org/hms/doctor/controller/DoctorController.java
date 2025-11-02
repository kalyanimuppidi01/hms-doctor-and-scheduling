package org.hms.doctor.controller;

import org.hms.doctor.model.Doctor;
import org.hms.doctor.service.DoctorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/doctors")
public class DoctorController {
    private final DoctorService service;
    public DoctorController(DoctorService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<Doctor> create(@RequestBody Doctor d, @RequestHeader("X-User-Role") String role) {
        if (!"admin".equals(role) && !"reception".equals(role)) return ResponseEntity.status(403).build();
        Doctor saved = service.create(d);
        return ResponseEntity.created(java.net.URI.create("/v1/doctors/"+saved.getDoctorId())).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> get(@PathVariable Long id) {
        return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Doctor> update(@PathVariable Long id, @RequestBody Doctor d, @RequestHeader("X-User-Role") String role) {
        if (!"admin".equals(role) && !"doctor".equals(role)) return ResponseEntity.status(403).build();
        try {
            Doctor upd = service.update(id, d);
            return ResponseEntity.ok(upd);
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader("X-User-Role") String role) {
        if (!"admin".equals(role)) return ResponseEntity.status(403).build();
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public Page<Doctor> list(@RequestParam(required = false) String department,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size) {
        return service.listByDepartment(department, PageRequest.of(page, size));
    }
}
