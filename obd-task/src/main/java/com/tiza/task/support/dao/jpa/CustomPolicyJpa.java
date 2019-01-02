package com.tiza.task.support.dao.jpa;

import com.tiza.task.support.dao.dto.CustomPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Description: CustomPolicyJpa
 * Author: DIYILIU
 * Update: 2018-07-30 09:45
 */
public interface CustomPolicyJpa extends JpaRepository<CustomPolicy, Long> {

}
