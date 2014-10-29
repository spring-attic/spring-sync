/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.sync.diffsync;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.dialect.H2Dialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(basePackages="org.springframework.sync")
public class EmbeddedDataSourceConfig {

	@Bean
	public DataSource dataSource() {
		return new EmbeddedDatabaseBuilder()
			.setType(EmbeddedDatabaseType.H2)
			.addScript("classpath:/org/springframework/sync/testdb.sql").build();
	}

	@Bean
	public Map<String, Object> jpaProperties() {
			Map<String, Object> props = new HashMap<String, Object>();
			props.put("hibernate.dialect", H2Dialect.class.getName());
			return props;
	}

	@Bean
	public JpaVendorAdapter jpaVendorAdapter() {
			HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
			hibernateJpaVendorAdapter.setShowSql(false);
			hibernateJpaVendorAdapter.setGenerateDdl(true);
			hibernateJpaVendorAdapter.setDatabase(Database.H2);
			return hibernateJpaVendorAdapter;
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
			return new JpaTransactionManager( entityManagerFactory().getObject() );
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
			LocalContainerEntityManagerFactoryBean lef = new LocalContainerEntityManagerFactoryBean();
			lef.setDataSource(dataSource());
			lef.setJpaPropertyMap(this.jpaProperties());
			lef.setJpaVendorAdapter(this.jpaVendorAdapter());
			lef.setPackagesToScan("org.springframework.sync");
			return lef;
	}

}
