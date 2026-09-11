/**
 * EngineStartupComp.java
 *
 * @author AC
 * @date 21-Apr-2025
 */
package com.nnp.envrep.config;

import java.io.File;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * EngineStartupComp.java
 *
 * @author AC
 * @date 21-Apr-2025
 */
@Slf4j
@Component
public class EngineStartupComp implements CommandLineRunner {
	
	private GitOpsProperties location;
	
	public EngineStartupComp(GitOpsProperties location) {
		this.location = location;
	}
	
	@Override
	public void run(String... args) throws Exception {
		String path = location.getFolder();
		if (path != null && !path.trim().isEmpty()) {
			File file = new File(path);
			if (!file.exists()) {
				boolean isCreated = file.mkdirs();
				if (isCreated) {
					EngineStartupComp.log.info("GitOps working folder created: {}", file.getAbsolutePath());
				}
			}
		}
	}
}
