package com.example.taykotoproject.service;

import com.example.taykotoproject.model.Deal;
import com.example.taykotoproject.model.Vehicle;

import java.util.List;
import java.util.Optional;

public interface DealService {
    List<Deal> getAll();
    List<Deal> getDealByStatus(String status);

    void save(Deal deal);

    void delete (Long id);

    Optional<Deal> findById(Long id);

    Optional<Deal> findByVehicle(Vehicle vehicle);

    Deal getOne(Long id);

    List<Deal> getAllById(Long id);
}
