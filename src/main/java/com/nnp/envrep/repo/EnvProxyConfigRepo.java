package com.nnp.envrep.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nnp.envrep.model.EnvProxyConfig;

public interface EnvProxyConfigRepo extends JpaRepository<EnvProxyConfig, String> {
}
