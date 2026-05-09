package com.tractorbhai.repository;

import com.tractorbhai.model.RentalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tractorbhai.model.User;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface RentalEntryRepository extends JpaRepository<RentalEntry, Long> {
    List<RentalEntry> findByUser(User user);
    List<RentalEntry> findByEntryDateBetweenAndUser(LocalDateTime start, LocalDateTime end, User user);
}
