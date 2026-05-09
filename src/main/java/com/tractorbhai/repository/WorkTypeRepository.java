package com.tractorbhai.repository;

import com.tractorbhai.model.WorkType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tractorbhai.model.User;
import java.util.List;

@Repository
public interface WorkTypeRepository extends JpaRepository<WorkType, Long> {
    List<WorkType> findByUser(User user);
}
