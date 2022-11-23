package com.example.taykotoproject.repository;

import com.example.taykotoproject.model.Deal;
import com.example.taykotoproject.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {


    List<Deal> findAllByCustomerId(Long id);
    List<Deal> getDealByStatus(String status);
    Optional<Deal> findByVehicle(Vehicle vehicle);
}
