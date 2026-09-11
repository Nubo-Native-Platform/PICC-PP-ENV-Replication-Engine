/**
 * EnvReqCommunicationRepo.java
 *
 * @author AC
 * @date 29-Apr-2025
 */
package com.nnp.envrep.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nnp.envrep.model.EnvReqCommunication;

/**
 * EnvReqCommunicationRepo.java
 *
 * @author AC
 * @date 29-Apr-2025
 */
@Repository
public interface EnvReqCommunicationRepo extends JpaRepository<EnvReqCommunication, Long>{

}
