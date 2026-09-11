package com.nnp.envrep.config;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

/**
 * JMSConfig.java
 *
 * @author AC
 * @date 21-Apr-2025
 */
@EnableJms
@Configuration
public class JMSConfig {

	@Value("${spring.activemq.broker-url:tcp://localhost:61616}")
	private String brokerUrl;

	@Value("${spring.activemq.user:artemis}")
	private String user;

	@Value("${spring.activemq.password:artemis}")
	private String password;

	public static final String REQUEST_QUEUE = "env-rep::env_replication";
	
	@Bean
	public ActiveMQConnectionFactory receiverActiveMQConnectionFactory() {
		return new ActiveMQConnectionFactory(brokerUrl, user, password);
	}
	
	@Bean
	public CachingConnectionFactory cachingConnectionFactory() {
		return new CachingConnectionFactory(receiverActiveMQConnectionFactory());
	}

	@Bean
	public JmsTemplate jmsTemplate() {
		JmsTemplate template =  new JmsTemplate(cachingConnectionFactory());
		template.setMessageConverter(messageConverter());
		return template;
	}

	@Bean
	public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(receiverActiveMQConnectionFactory());
		//factory.setMessageConverter(messageConverter());
		factory.setConcurrency("3-10");
		return factory;
	}

	@Bean
	public MessageConverter messageConverter() {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		converter.setTargetType(MessageType.TEXT);
		converter.setTypeIdPropertyName("_type");
		return converter;
	}

}
