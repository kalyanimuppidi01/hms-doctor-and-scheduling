package org.hms.doctor.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "doctors")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doctor_id")
    private Long doctorId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;

    private String phone;

    @Column(nullable = false)
    private String department;

    private String specialization;

    private Integer dailyCapacity = 20;

    private Boolean active = true;

    private OffsetDateTime createdAt = OffsetDateTime.now();

    // getters / setters ...
    // (omitted here for brevity — include standard getters and setters)
    // ... implement them in your IDE or paste from earlier assistant code
    // (I'll assume you add them; if you want I can paste them verbatim)
    public Long getDoctorId(){return doctorId;}
    public void setDoctorId(Long doctorId){this.doctorId=doctorId;}
    public String getName(){return name;}
    public void setName(String name){this.name=name;}
    public String getEmail(){return email;}
    public void setEmail(String email){this.email=email;}
    public String getPhone(){return phone;}
    public void setPhone(String phone){this.phone=phone;}
    public String getDepartment(){return department;}
    public void setDepartment(String department){this.department=department;}
    public String getSpecialization(){return specialization;}
    public void setSpecialization(String specialization){this.specialization=specialization;}
    public Integer getDailyCapacity(){return dailyCapacity;}
    public void setDailyCapacity(Integer dailyCapacity){this.dailyCapacity=dailyCapacity;}
    public Boolean getActive(){return active;}
    public void setActive(Boolean active){this.active=active;}
    public OffsetDateTime getCreatedAt(){return createdAt;}
    public void setCreatedAt(OffsetDateTime createdAt){this.createdAt=createdAt;}
}
