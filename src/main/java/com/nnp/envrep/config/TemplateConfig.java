/**
 * TemplateConfig.java
 *
 * @author AC
 * @date 22-Apr-2025
 */
package com.nnp.envrep.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nnp.envrep.util.CustomFileUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * TemplateConfig.java
 *
 * @author AC
 * @date 22-Apr-2025
 */
@Configuration
@Slf4j
public class TemplateConfig {
	
	@Bean(name = "templateCfg")
	public freemarker.template.Configuration getTemplateCfg() {
		freemarker.template.Configuration templateCfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_29);
		return templateCfg;
	}
	
	@Bean(name = "customFileUtil")
	public CustomFileUtil getCustomFileUtil() {
		return new CustomFileUtil();
	}

}
