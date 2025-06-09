package com.example.foodstore.serviceImpl;

import com.example.foodstore.entity.Branch;
import com.example.foodstore.repository.BranchRepository;
import com.example.foodstore.service.BranchService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BranchServiceImpl implements BranchService {

    @Autowired
    private BranchRepository branchRepository;

    @Override
    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }

    @Override
    public Optional<Branch> getBranchById(Long id) {
        return branchRepository.findById(id);
    }

    @Override
    public Branch saveBranch(Branch branch) {
        return branchRepository.save(branch);
    }

    @Override
    public Branch updateBranch(Long id, Branch branch) {
        Branch existingBranch = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy ID branch: " + id));

        existingBranch.setName(branch.getName());
        existingBranch.setAddress(branch.getAddress());
        existingBranch.setLatitude(branch.getLatitude());
        existingBranch.setLongitude(branch.getLongitude());
        existingBranch.setPhone(branch.getPhone());
        existingBranch.setOpeningHours(branch.getOpeningHours());
        existingBranch.setStatus(branch.getStatus());

        return branchRepository.save(existingBranch);
    }

    @Override
    public void deleteBranch(Long id) {
        if (!branchRepository.existsById(id)) {
            throw new EntityNotFoundException("Branch không tìm thấy ID: " + id);
        }
        branchRepository.deleteById(id);
    }
}
