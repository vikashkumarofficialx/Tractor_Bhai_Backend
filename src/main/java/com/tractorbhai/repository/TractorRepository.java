package com.tractorbhai.repository;

import com.tractorbhai.model.Tractor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tractorbhai.model.User;
import java.util.List;

@Repository
public interface TractorRepository extends JpaRepository<Tractor, Long> {
    List<Tractor> findByUser(User user);
}
