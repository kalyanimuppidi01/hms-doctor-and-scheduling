package org.hms.doctor.config;

import org.hms.doctor.model.Doctor;
import org.hms.doctor.repo.DoctorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

@Component
public class DataLoader implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final DoctorRepository repo;
    private final JdbcTemplate jdbc;

    public DataLoader(DoctorRepository repo, JdbcTemplate jdbc) {
        this.repo = repo;
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            ClassPathResource resource = new ClassPathResource("seed/hms_doctors.csv");
            if (!resource.exists()) {
                resource = new ClassPathResource("seed/doctors.csv");
            }
            if (!resource.exists()) {
                log.info("Seed file not found: seed/hms_doctors.csv or seed/doctors.csv — skipping seed load");
                return;
            }

            List<Doctor> toSave = new ArrayList<>();
            int total = 0, skipped = 0, added = 0;
            long maxIdSeen = 0L;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                String line;
                boolean first = true;
                while ((line = br.readLine()) != null) {
                    // skip empty lines
                    if (line == null || line.trim().isEmpty()) continue;

                    // skip header if present (header likely contains 'name' and 'email' column names)
                    if (first && (line.toLowerCase().contains("name") && line.toLowerCase().contains("email"))) {
                        first = false;
                        continue;
                    }
                    first = false;
                    total++;

                    // simple CSV split; if your CSV has quoted commas, replace with a CSV parser
                    String[] cols = line.split(",");

                    if (cols.length < 5) {
                        log.warn("Skipping malformed seed line (cols < 5): {}", line);
                        skipped++;
                        continue;
                    }

                    // Detect whether first column is an ID
                    boolean firstIsId = false;
                    Long csvId = null;
                    String maybeId = cols[0].trim();
                    try {
                        csvId = Long.parseLong(maybeId);
                        firstIsId = true;
                    } catch (NumberFormatException ignored) {
                        firstIsId = false;
                    }

                    String name, email, phone, department, specialization, createdAtStr = null;
                    if (firstIsId) {
                        // expected columns: id,name,email,phone,department,specialization,created_at (or similar)
                        // we accept at least 6 cols (id + 5)
                        if (cols.length < 6) {
                            log.warn("Skipping malformed seed line (expected >=6 cols when id present): {}", line);
                            skipped++;
                            continue;
                        }
                        name = cols[1].trim();
                        email = cols[2].trim();
                        phone = cols[3].trim();
                        department = cols[4].trim();
                        specialization = cols.length > 5 ? cols[5].trim() : null;
                        if (cols.length > 6) createdAtStr = cols[6].trim();
                    } else {
                        // expected columns: name,email,phone,department,specialization,created_at (or at least 5 cols)
                        name = cols[0].trim();
                        email = cols[1].trim();
                        phone = cols[2].trim();
                        department = cols[3].trim();
                        specialization = cols.length > 4 ? cols[4].trim() : null;
                        if (cols.length > 5) createdAtStr = cols[5].trim();
                    }

                    // dedupe by email or phone
                    boolean existsByEmail = email != null && !email.isBlank() && repo.existsByEmail(email);
                    boolean existsByPhone = phone != null && !phone.isBlank() && repo.existsByPhone(phone);
                    if (existsByEmail || existsByPhone) {
                        skipped++;
                        continue;
                    }

                    Doctor d = new Doctor();
                    if (csvId != null) {
                        d.setDoctorId(csvId);
                        if (csvId > maxIdSeen) maxIdSeen = csvId;
                    }
                    d.setName(name);
                    d.setEmail(email == null || email.isBlank() ? null : email);
                    d.setPhone(phone == null || phone.isBlank() ? null : phone);
                    d.setDepartment(department == null || department.isBlank() ? null : department);
                    d.setSpecialization(specialization == null || specialization.isBlank() ? null : specialization);
                    // parse createdAt if present (attempt multiple formats), else set now
                    OffsetDateTime createdAt = OffsetDateTime.now();
                    if (createdAtStr != null && !createdAtStr.isBlank()) {
                        try {
                            // try ISO format first
                            createdAt = OffsetDateTime.parse(createdAtStr);
                        } catch (Exception ex1) {
                            try {
                                // try a common datetime pattern (yyyy-MM-dd HH:mm:ss)
                                createdAt = OffsetDateTime.parse(createdAtStr.replace(" ", "T") + "Z");
                            } catch (Exception ex2) {
                                // fallback to now
                                createdAt = OffsetDateTime.now();
                            }
                        }
                    }
                    d.setCreatedAt(createdAt);
                    d.setActive(true);

                    toSave.add(d);

                    if (toSave.size() >= 500) {
                        saveBatch(toSave);
                        added += toSave.size();
                        toSave.clear();
                    }
                }
            }

            if (!toSave.isEmpty()) {
                saveBatch(toSave);
                added += toSave.size();
            }

            // if we saw explicit ids, update table AUTO_INCREMENT to maxIdSeen + 1
            if (maxIdSeen > 0) {
                long next = maxIdSeen + 1;
                try {
                    jdbc.execute("ALTER TABLE doctors AUTO_INCREMENT = " + next);
                    log.info("Set doctors AUTO_INCREMENT to {}", next);
                } catch (Exception e) {
                    log.warn("Failed to set AUTO_INCREMENT to {}: {}", next, e.getMessage());
                }
            } else {
                // ensure AUTO_INCREMENT doesn't collide (take max from DB)
                try {
                    OptionalLong maybeMax = repo.findAll().stream().mapToLong(Doctor::getDoctorId).max();
                    if (maybeMax.isPresent()) {
                        long next = maybeMax.getAsLong() + 1;
                        jdbc.execute("ALTER TABLE doctors AUTO_INCREMENT = " + next);
                        log.info("Set doctors AUTO_INCREMENT to {}", next);
                    }
                } catch (Exception e) {
                    log.warn("Failed to set AUTO_INCREMENT from max id: {}", e.getMessage());
                }
            }

            log.info("Doctor seed load finished. Total rows read: {}, added: {}, skipped (duplicates/malformed): {}",
                    total, added, skipped);

        } catch (Exception e) {
            log.error("Failed to load doctor seed data — continuing startup (error logged)", e);
        }
    }

    @Transactional
    protected void saveBatch(List<Doctor> list) {
        repo.saveAll(list);
    }
}
