CREATE TABLE doctors (
  doctor_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  email VARCHAR(200),
  phone VARCHAR(50),
  department VARCHAR(100) NOT NULL,
  specialization VARCHAR(200),
  daily_capacity INT DEFAULT 20,
  active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE doctor_slots_hold (
  hold_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  doctor_id BIGINT NOT NULL,
  slot_start TIMESTAMP NOT NULL,
  slot_end TIMESTAMP NOT NULL,
  appointment_id BIGINT NULL,
  hold_status VARCHAR(20) NOT NULL DEFAULT 'HELD',
  expires_at TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  version BIGINT,
  CONSTRAINT fk_hold_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id)
);

CREATE TABLE doctor_daily_capacity (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  doc_date DATE NOT NULL,
  doctor_id BIGINT NOT NULL,
  booked_count INT DEFAULT 0,
  capacity INT DEFAULT 20,
  version BIGINT,
  UNIQUE (doc_date, doctor_id)
);
