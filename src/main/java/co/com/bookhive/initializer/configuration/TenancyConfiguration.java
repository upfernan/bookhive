package co.com.bookhive.initializer.configuration;

import org.hibernate.cfg.MultiTenancySettings;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import co.com.bookhive.circulation.infrastructure.input.interceptors.TenantInterceptor;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.tenancy.TenantConnectionProvider;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.tenancy.TenantSchemaResolver;

@Configuration
public class TenancyConfiguration implements WebMvcConfigurer {

	private static final String TENANTED_PATHS = "/api/**";

	private final TenantInterceptor tenantInterceptor;
	private final TenantConnectionProvider tenantConnectionProvider;
	private final TenantSchemaResolver tenantSchemaResolver;

	public TenancyConfiguration(
			final TenantInterceptor tenantInterceptor,
			final TenantConnectionProvider tenantConnectionProvider,
			final TenantSchemaResolver tenantSchemaResolver) {

		super();
		this.tenantInterceptor = tenantInterceptor;
		this.tenantConnectionProvider = tenantConnectionProvider;
		this.tenantSchemaResolver = tenantSchemaResolver;
	}

	@Override
	public void addInterceptors(final InterceptorRegistry registry) {
		registry.addInterceptor(tenantInterceptor).addPathPatterns(TENANTED_PATHS);
	}

	@Bean
	HibernatePropertiesCustomizer tenancyHibernateCustomizer() {

		return properties -> {
			properties.put(
					MultiTenancySettings.MULTI_TENANT_CONNECTION_PROVIDER,
					tenantConnectionProvider);
			properties.put(
					MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER,
					tenantSchemaResolver);
		};
	}

}
