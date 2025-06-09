package com.example.foodstore.service;

import com.example.foodstore.entity.Branch;

import java.util.List;
import java.util.Optional;

public interface BranchService {
    List<Branch> getAllBranches();
    Optional<Branch> getBranchById(Long id);
    Branch saveBranch(Branch branch);
    Branch updateBranch(Long id, Branch branch);
    void deleteBranch(Long id);
}
