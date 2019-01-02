package com.tiza.task.support.dao.jpa;

import com.tiza.task.support.dao.dto.DefaultPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Description: DefaultPolicyJpa
 * Author: DIYILIU
 * Update: 2018-07-30 09:45
 */
public interface DefaultPolicyJpa extends JpaRepository<DefaultPolicy, Long> {
}
