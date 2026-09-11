/**
 * GitOpsService.java
 *
 * @author AC
 * @date 06-May-2025
 */
package com.nnp.envrep.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nnp.envrep.argoint.api.client.ArgoIntegrationClient;
import com.nnp.envrep.config.GitOpsProperties;
import com.nnp.envrep.exception.ArgoException;
import com.nnp.envrep.exception.EnvReplEngineEx;
import com.nnp.envrep.model.EnvBBComponent;
import com.nnp.envrep.model.EnvBBCompSpec;
import com.nnp.envrep.model.EnvBBCompTemplate;
import com.nnp.envrep.model.EnvProxyConfig;
import com.nnp.envrep.model.EnvReqComponent;
import com.nnp.envrep.model.argo.ArgoGitRepoReg;
import com.nnp.envrep.repo.EnvProxyConfigRepo;
import com.nnp.envrep.util.CustomFileUtil;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * GitOpsService.java
 *
 * @author AC
 * @date 06-May-2025
 */
@Service
@Slf4j
public class GitOpsService {
	private final Configuration templateCfg;
	private final GitOpsProperties location;
    private final ArgoIntegrationClient argoClient;
    private final CustomFileUtil customFileUtil;
    private final EnvProxyConfigRepo envProxyConfigRepo;
    public GitOpsService(Configuration templateCfg,
                         GitOpsProperties location,
                         ArgoIntegrationClient argoClient,
                         CustomFileUtil customFileUtil,
                         EnvProxyConfigRepo envProxyConfigRepo) {
        this.templateCfg = templateCfg;
        this.location = location;
        this.argoClient = argoClient;
        this.customFileUtil = customFileUtil;
        this.envProxyConfigRepo = envProxyConfigRepo;


    }
	@Value("${argo.user:admin}")
	private String argoUser;

	@Value("${argo.pass:}")
	private String argoPass;
	
	@Value("${gitlab.repo.user:gitops-bot}")
	private String gitUser;

	@Value("${gitlab.repo.password:}")
	private String gitPass;

	@Value("${haproxy.base.domain:nnp.nubons.com}")
	private String baseDomain;

	/**
	 * This method is used to create root application and component level application and manifest files for argocd.
	 * separate child application and manifest file for each components.
	 * @param dataMap
	 * @param baseDir
	 * @param baseDir 
	 * @param compUrlMap 
	 * @throws IOException
	 * @throws TemplateException
	 */
	public void createArgoApp(/*EnvRequest envReq*/List<EnvReqComponent> dedicatedComponents, Map<String, String> dataMap, String baseDir, Map<String, String> compUrlMap) {		

		try {
			// create root application in argocd
			String appYaml = convertYamlToJson(createAppYmlContent(dataMap));
			boolean result = callArgoCreateApp(appYaml, dataMap.get("gitLabRepoUrlToReg")).getStatusCode().is2xxSuccessful();

			if (result) {
				int lineIndex = 0;
				for (EnvReqComponent reqComp : dedicatedComponents) {
					createYamlsForArgo(reqComp, dataMap, baseDir);
					if (reqComp.getBbComponent().isProxyExpose()) {
						haproxyCfgPopulate(reqComp, dataMap, String.valueOf(lineIndex++));
					}
				}
				
			}
		} catch (IOException | TemplateException e) {
			log.error("Exception occured in GitOpsService - " + e.getMessage());
			throw new ArgoException("Exception occured in GitOpsService", e);
		}
	}

	private void haproxyCfgPopulate(EnvReqComponent reqComp, Map<String, String> dataMap, String lineIndex) {
		EnvBBComponent bbComp = reqComp.getBbComponent();

		// 1. Resolve Environment Name and Namespace (env name is identical to namespace)
		String envName = (dataMap.get("env") != null ? dataMap.get("env") : dataMap.get("envId")).toLowerCase().trim();
		String envId = dataMap.get("envId") != null ? dataMap.get("envId") : envName;

		// 2. Resolve Component Name (preferred k8sCompName, fallback to compName)
		String compName = bbComp.getK8sCompName();
		if (compName == null || compName.isBlank()) {
			compName = bbComp.getCompName().toLowerCase().trim().replaceAll("[^a-z0-9-]", "-");
		}

		// 3. Extract specs: internal_port and optional app_name override
		int internalPort = 0;
		if (bbComp.getBbCompSpec() != null) {
			for (var spec : bbComp.getBbCompSpec()) {
				String specName = spec.getSpecName() != null ? spec.getSpecName().toLowerCase() : "";
				String tmplVar = spec.getTmplSpecVarName() != null ? spec.getTmplSpecVarName().toLowerCase() : "";

				if ("app_name".equals(specName) || "app_name".equals(tmplVar)) {
					if (spec.getSpecvalues() != null && !spec.getSpecvalues().isBlank()) {
						compName = spec.getSpecvalues().toLowerCase().trim();
					}
				} else if ("internal_port".equals(specName) || "internal_port".equals(tmplVar)) {
					try {
						internalPort = Integer.parseInt(spec.getSpecvalues().trim());
					} catch (NumberFormatException e) {
						log.error("Invalid internal_port for component {}: {}", compName, spec.getSpecvalues());
					}
				}
			}
		}

		if (internalPort <= 0) {
			log.warn("Skipping HAProxy config for component '{}' because internal_port is missing or invalid.", compName);
			return;
		}

		// 4. Construct domain name: {componentname}-{env}.{baseDomain}
		String domainName = String.format("%s-%s.%s", compName, envName, baseDomain);

		// 5. Populate EnvProxyConfig entity
		EnvProxyConfig proxyConfig = new EnvProxyConfig();
		proxyConfig.setEnvId(envId);
		proxyConfig.setCompServName(compName);
		proxyConfig.setDomainName(domainName);
		proxyConfig.setNamespace(envName);
		proxyConfig.setInternalPort(internalPort);
		proxyConfig.setParentFrontend("http_front");
		proxyConfig.setBackendType("K8S_DNS");
		proxyConfig.setLineIndex(lineIndex != null ? lineIndex : "0");
		proxyConfig.setSsl(false);

		// 6. Save into portal.env_proxy_config
		envProxyConfigRepo.saveAndFlush(proxyConfig);
		log.info("Saved HAProxy config in DB -> comp: {}, domain: {}, namespace: {}, port: {}",
				compName, domainName, envName, internalPort);
	}

	/**
	 * For each components create application and manifest files and keep it in work directory for git push.
	 * Use template to generate files. Templates will be fetched from DB based on component id.
	 * @param reqComp
	 * @param dataMap
	 * @param baseDir
	 * @throws IOException
	 * @throws TemplateException
	 * @throws TemplateNotFoundException
	 * @throws MalformedTemplateNameException
	 * @throws ParseException
	 * @throws EnvReplEngineEx
	 */
	private void createYamlsForArgo(EnvReqComponent reqComp, Map<String, String> dataMap, String baseDir) throws IOException, TemplateException {
		// create component level app
		Map<String, String> compDataMap = new HashMap<>();
		compDataMap.putAll(dataMap);
		compDataMap.put("comp_name", reqComp.getBbComponent().getK8sCompName());
		
		reqComp.getBbComponent().getBbCompSpec().forEach(bbcompSpec -> {
			if("active".equalsIgnoreCase(bbcompSpec.getCompstatus()))
				compDataMap.put(bbcompSpec.getTmplSpecVarName(), bbcompSpec.getSpecvalues());
		});
		// get the template
		for (EnvBBCompTemplate tmpl : reqComp.getBbComponent().getBbCompTempl()) {
			compDataMap.put("comp_manifest_path", tmpl.getFilePath());

			if (null == tmpl.getTemplate() || tmpl.getTemplate().isBlank()
					|| "inactive".equalsIgnoreCase(tmpl.getStatus())) {
				continue;
			}
			String fileDir = baseDir + File.separator;
			if (!"app".equalsIgnoreCase(tmpl.getTmplType())) {
				fileDir = fileDir + /* tmpl.getBbComponent().getEnvGitOpsPath() */ tmpl.getFilePath()+ File.separator;
			}
			StringTemplateLoader strTemplateLoader = new StringTemplateLoader();
			strTemplateLoader.putTemplate(tmpl.getTmplId(), tmpl.getTemplate());
			templateCfg.setTemplateLoader(strTemplateLoader);
			templateCfg.setInterpolationSyntax(Configuration.SQUARE_BRACKET_INTERPOLATION_SYNTAX);
			StringBuilder contentModel = new StringBuilder();
			contentModel.append(FreeMarkerTemplateUtils
					.processTemplateIntoString(templateCfg.getTemplate(tmpl.getTmplId()), compDataMap));
			templateCfg.unsetTemplateLoader();
			log.info(contentModel.toString());
			customFileUtil.writeFile(new ByteArrayInputStream(contentModel.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)), new File(
					fileDir +tmpl.getFileName() + "_" + tmpl.getTmplType() + ".yaml"));
		}
		
	}

	/**
	 * This method will use Feign client of argo to create
	 * 1. Token for subsequent API call based on admin user of argo
	 * 2. Repository creation in argo under settings -> Repositories
	 * 3. Create root level application.
	 * @param appYaml
	 * @return
	 */
	private ResponseEntity<String> callArgoCreateApp(String appYaml, String gitLabRepoUrlToReg) {
		String argoToken="";
		try {
			//get token from argo based on admin credential. This token will be used in subsequent call to argo.
			String argoTokenJson = argoClient.createJWTToken(getJsonBodyForSession()).getBody();
			ObjectMapper mapper = new ObjectMapper();
			Map<String, String> token;
			token = mapper.readValue(argoTokenJson, Map.class);
			argoToken = token.get("token");
			//register git repository in argo -- if already registered it will overwrite
			ArgoGitRepoReg gitRepoRegReq = new ArgoGitRepoReg();
			gitRepoRegReq.setType("git");
			gitRepoRegReq.setProject("default");
			gitRepoRegReq.setRepo(gitLabRepoUrlToReg);
			gitRepoRegReq.setGitUser(gitUser);
			gitRepoRegReq.setGitPass(gitPass);
			ResponseEntity<String> resp =  argoClient.createRepo("Bearer "+argoToken,gitRepoRegReq);
			//Now create application
			return argoClient.createApplication("Bearer "+argoToken, appYaml);
		} catch (JsonProcessingException e) {
			log.error("Error in parsing json at GitOpsService -> callArgoCreateApp()" + e.getMessage());
			//delete gitlab repo registration
			argoClient.deleteRepo("Bearer "+argoToken,gitLabRepoUrlToReg);
			throw new ArgoException("Error in parsing json at GitOpsService -> callArgoCreateApp()", e);
		}

	}

	/**
	 * @return
	 */
	private String getJsonBodyForSession() {
		String json = "{\"username\":" + "\""+argoUser +"\""+ ",\"password\":" + "\""+argoPass + "\"}";
		return json;
	}

	/**
	 * @param dataMap
	 * @throws IOException
	 * @throws TemplateException
	 * @throws TemplateNotFoundException
	 * @throws MalformedTemplateNameException
	 * @throws ParseException
	 */
	private String createAppYmlContent(Map<String, String> dataMap) throws IOException, TemplateException {
		FileTemplateLoader ftlArgoApp = new FileTemplateLoader(new File(location.getTemplate() + "/"));
		templateCfg.setTemplateLoader(ftlArgoApp);
		templateCfg.setInterpolationSyntax(Configuration.SQUARE_BRACKET_INTERPOLATION_SYNTAX);
		StringBuilder contentModel = new StringBuilder();
		contentModel.append(
				FreeMarkerTemplateUtils.processTemplateIntoString(templateCfg.getTemplate("app.yaml"), dataMap));
		templateCfg.unsetTemplateLoader();
		return contentModel.toString();
	}
	


	private String convertYamlToJson(String appYaml) {
		Yaml yaml = new Yaml();
		Map<String, Object> map = (Map<String, Object>) yaml.load(appYaml);

		JSONObject jsonObject = new JSONObject(map);
		return jsonObject.toString();
	}
	

}

