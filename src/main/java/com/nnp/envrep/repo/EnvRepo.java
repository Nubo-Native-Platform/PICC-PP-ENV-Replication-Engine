/**
 * EnvRepo.java
 *
 * @author AC
 * @date 22-Apr-2025
 */
package com.nnp.envrep.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nnp.envrep.model.Environment;

/**
 * EnvRepo.java
 *
 * @author AC
 * @date 22-Apr-2025
 */
@Repository
public interface EnvRepo extends JpaRepository<Environment, String>{

}
