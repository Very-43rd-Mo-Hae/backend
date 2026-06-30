package com.very.relink.appointment.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentJpaRepository extends JpaRepository<AppointmentJpaEntity, Long> {
}
