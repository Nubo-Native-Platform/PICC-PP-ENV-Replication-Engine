package com.nnp.envrep;

import com.nnp.envrep.config.GitOpsProperties;
import com.nnp.envrep.config.OpenApiConfig;
import com.nnp.envrep.config.TemplateConfig;
import com.nnp.envrep.model.EnvBBCompSpec;
import com.nnp.envrep.model.EnvBBComponent;
import com.nnp.envrep.model.EnvReqComponent;
import com.nnp.envrep.model.EnvRequest;
import com.nnp.envrep.model.Environment;
import com.nnp.envrep.event.GitOpsEvent;
import com.nnp.envrep.repo.EnvRepo;
import com.nnp.envrep.repo.EnvReqRepo;
import com.nnp.envrep.service.HAProxyService;
import com.nnp.envrep.util.CustomFileUtil;
import freemarker.template.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvrepEngineApplicationTests {

	@Nested
	@DisplayName("Configuration & Properties Tests")
	class ConfigurationTests {

		@Test
		@DisplayName("Should initialize GitOpsProperties with safe default values")
		void shouldInitializeGitOpsPropertiesDefaults() {
			GitOpsProperties properties = new GitOpsProperties();
			assertThat(properties.getFolder()).isEqualTo("./gitops");
			assertThat(properties.getGitlabUrl()).isEqualTo("https://gitlab.example.com");
			assertThat(properties.getRepopath()).isEqualTo("/env-replication-gitops/${env}-synch.git");
			assertThat(properties.getTemplate()).isEqualTo("./template");
		}

		@Test
		@DisplayName("Should configure OpenAPI documentation bean with Apache 2.0 license")
		void shouldConfigureOpenAPI() {
			OpenApiConfig openApiConfig = new OpenApiConfig();
			OpenAPI openAPI = openApiConfig.customOpenAPI();

			assertThat(openAPI).isNotNull();
			assertThat(openAPI.getInfo().getTitle()).contains("PICC-PP-ENV-Replication-Engine");
			assertThat(openAPI.getInfo().getLicense().getName()).isEqualTo("Apache License 2.0");
			assertThat(openAPI.getInfo().getContact().getEmail()).isEqualTo("contribution@nubons.com");
		}

		@Test
		@DisplayName("Should configure FreeMarker template engine with VERSION_2_3_29")
		void shouldConfigureFreeMarker() {
			TemplateConfig templateConfig = new TemplateConfig();
			Configuration cfg = templateConfig.getTemplateCfg();

			assertThat(cfg).isNotNull();
			assertThat(cfg.getIncompatibleImprovements()).isEqualTo(Configuration.VERSION_2_3_29);
		}
	}

	@Nested
	@DisplayName("Template Processing & Utility Tests")
	class TemplateAndUtilTests {

		@Test
		@DisplayName("Should process FreeMarker template with square bracket syntax")
		void shouldProcessFreeMarkerTemplate() throws Exception {
			Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
			cfg.setInterpolationSyntax(Configuration.SQUARE_BRACKET_INTERPOLATION_SYNTAX);

			freemarker.cache.StringTemplateLoader stringLoader = new freemarker.cache.StringTemplateLoader();
			stringLoader.putTemplate("testTemplate", "app-name: [=env?lower_case]-app\nrepo: [=gitLabRepoUrlToReg]");
			cfg.setTemplateLoader(stringLoader);

			Map<String, Object> dataModel = new HashMap<>();
			dataModel.put("env", "PROD");
			dataModel.put("gitLabRepoUrlToReg", "https://gitlab.example.com/repo.git");

			String result = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("testTemplate"), dataModel);

			assertThat(result).contains("app-name: prod-app");
			assertThat(result).contains("repo: https://gitlab.example.com/repo.git");
		}

		@Test
		@DisplayName("Should write and delete files using CustomFileUtil")
		void fileUtilTest(@TempDir Path tempDir) {
			CustomFileUtil fileUtil = new CustomFileUtil();
			File targetFile = tempDir.resolve("test-file.txt").toFile();

			String content = "hello open source replication engine";
			fileUtil.writeFile(new ByteArrayInputStream(content.getBytes()), targetFile);

			assertThat(targetFile).exists();
			assertThat(targetFile.length()).isGreaterThan(0);

			fileUtil.deleteFile(tempDir.toString());
			assertThat(targetFile).doesNotExist();
		}
	}

	@Nested
	@DisplayName("HAProxy Service Unit Tests")
	class HAProxyServiceTests {

		@Mock
		private EnvReqRepo envReqRepo;

		@Mock
		private EnvRepo envRepo;

		@Mock
		private WebClient webClientHAProxy;

		@InjectMocks
		private HAProxyService haProxyService;

		@Test
		@DisplayName("Should correctly evaluate HAProxy registration for exposed components")
		void shouldEvaluateHAProxyRegistration() {
			String reqId = "REQ-1001";
			String envId = "ENV-2001";

			EnvRequest envReq = new EnvRequest();
			envReq.setReqId(reqId);
			envReq.setEnvId(envId);

			Environment env = new Environment();
			env.setEnvId(envId);
			env.setEnvCode("dev");

			EnvBBComponent bbComp = new EnvBBComponent();
			bbComp.setCompName("order-service");
			bbComp.setProxyExpose(true);

			EnvBBCompSpec portSpec = new EnvBBCompSpec();
			portSpec.setTmplSpecVarName("internal_port");
			portSpec.setSpecvalues("8080");
			bbComp.setBbCompSpec(List.of(portSpec));

			EnvReqComponent reqComp = new EnvReqComponent();
			reqComp.setBbComponent(bbComp);
			envReq.setReqComp(List.of(reqComp));

			when(envReqRepo.findById(reqId)).thenReturn(Optional.of(envReq));
			when(envRepo.findById(envId)).thenReturn(Optional.of(env));

			// Execute service method
			GitOpsEvent event = new GitOpsEvent(reqId, "TRIGGER_EVENT");
			haProxyService.registerCompHaproxy(event);

			verify(envReqRepo, times(1)).findById(reqId);
			verify(envRepo, times(1)).findById(envId);
		}
	}
}
