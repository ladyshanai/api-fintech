package com.fintech.api.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "client")
public class ClientEntity {

    private static final int RECENT_DAYS_THRESHOLD = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name_or_company_name")
    private String lastNameOrCompanyName;

    @Column(name = "document_number")
    private String documentNumber;

    private String address;
    @Column(name = "phone_number")
    private String phoneNumber;

    private String email;

    @Column(name  = "user_type")
    private String userType;

    private boolean active;

    @Column(name = "outstanding_balance")
    private BigDecimal outstandingBalance;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "modification_date")
    private LocalDateTime modificationDate;

    public boolean isRecentUser() {
        if (registrationDate == null) {
            return false;
        }

        return !registrationDate.isBefore(
                LocalDateTime.now().minusDays(RECENT_DAYS_THRESHOLD)
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastNameOrCompanyName() {
        return lastNameOrCompanyName;
    }

    public void setLastNameOrCompanyName(String lastNameOrCompanyName) {
        this.lastNameOrCompanyName = lastNameOrCompanyName;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(BigDecimal outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(LocalDateTime modificationDate) {
        this.modificationDate = modificationDate;
    }
}
