package org.hms.doctor.controller;

import org.hms.doctor.dto.*;
import org.hms.doctor.model.SlotHold;
import org.hms.doctor.service.SchedulingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/doctors")
public class SchedulingController {
    private final SchedulingService svc;
    public SchedulingController(SchedulingService svc) { this.svc = svc; }

    @PostMapping("/{id}/availability")
    public ResponseEntity<?> availability(@PathVariable Long id, @RequestBody AvailabilityRequest req, @RequestHeader("X-User-Role") String role) {
        if (!role.equals("admin") && !role.equals("reception")) return ResponseEntity.status(403).build();
        try {
            boolean ok = svc.isAvailable(id, req);
            return ResponseEntity.ok(java.util.Map.of("available", ok));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<?> reserve(@PathVariable Long id, @RequestBody ReserveRequest req, @RequestHeader("X-User-Role") String role) {
        if (!role.equals("admin") && !role.equals("reception")) return ResponseEntity.status(403).build();
        try {
            if (req.ttlMinutes==null) req.ttlMinutes = 10;
            var resp = svc.reserve(id, req);
            return ResponseEntity.status(201).body(resp);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/reserve/{holdId}/confirm")
    public ResponseEntity<?> confirm(@PathVariable Long id, @PathVariable Long holdId, @RequestBody ConfirmRequest req, @RequestHeader("X-User-Role") String role) {
        if (!role.equals("admin")) return ResponseEntity.status(403).build();
        try {
            SlotHold h = svc.confirm(id, holdId, req);
            return ResponseEntity.ok(h);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/reserve/{holdId}/release")
    public ResponseEntity<?> release(@PathVariable Long id, @PathVariable Long holdId, @RequestHeader("X-User-Role") String role) {
        if (!role.equals("admin") && !role.equals("reception")) return ResponseEntity.status(403).build();
        try {
            svc.release(id, holdId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }
}
