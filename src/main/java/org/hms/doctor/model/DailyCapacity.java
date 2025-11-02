package org.hms.doctor.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "doctor_daily_capacity", uniqueConstraints = {@UniqueConstraint(columnNames = {"docDate","doctorId"})})
public class DailyCapacity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate docDate;
    private Long doctorId;
    private Integer bookedCount = 0;
    private Integer capacity = 20;

    @Version
    private Long version;

    // getters/setters
    public Long getId(){return id;}
    public void setId(Long id){this.id=id;}
    public LocalDate getDocDate(){return docDate;}
    public void setDocDate(LocalDate docDate){this.docDate=docDate;}
    public Long getDoctorId(){return doctorId;}
    public void setDoctorId(Long doctorId){this.doctorId=doctorId;}
    public Integer getBookedCount(){return bookedCount;}
    public void setBookedCount(Integer bookedCount){this.bookedCount=bookedCount;}
    public Integer getCapacity(){return capacity;}
    public void setCapacity(Integer capacity){this.capacity=capacity;}
    public Long getVersion(){return version;}
    public void setVersion(Long version){this.version=version;}
}
