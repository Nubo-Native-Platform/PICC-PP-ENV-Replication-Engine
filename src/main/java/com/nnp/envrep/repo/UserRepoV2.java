package com.nnp.envrep.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nnp.envrep.model.UserV2;

public interface UserRepoV2 extends JpaRepository<UserV2, String> {

    @Query("SELECT u FROM UserV2 u WHERE u.userStatus = :userStatus")
    List<UserV2> getUsersByStatus(@Param("userStatus") String userStatus);

    List<UserV2> findByEnvId(String envId);

    List<UserV2> findByEnvIdAndUserStatus(String envId, String userStatus);

	UserV2 findByUserId(String logedInUserId);

	List<UserV2> findByEnvIdAndUserId(String envId, String userId);

	List<UserV2> findByEnvIdAndUserStatusAndUserTypeNot(String envId, String string, String string2);



//	@Modifying
//	@Query("DELETE  FROM UserConfig u WHERE u.userId =:userId and u.envId =:envId")
//	void deleteUserByEnv(@Param("userId")String userId, @Param("envId")String envId);
}
