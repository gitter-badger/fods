package org.jsoftware.fods.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jsoftware.fods.DefaultSelectorFactory;
import org.jsoftware.fods.client.ext.Configuration;
import org.jsoftware.fods.client.ext.Configuration.DatabaseConfiguration;
import org.jsoftware.fods.client.ext.ConfigurationFactory;
import org.jsoftware.fods.client.ext.ConnectionCreator;
import org.jsoftware.fods.client.ext.ConnectionCreatorFactory;
import org.jsoftware.fods.client.ext.LoggerFactory;
import org.jsoftware.fods.client.ext.SelectorFactory;
import org.jsoftware.fods.impl.utils.PropertiesUtil;
import org.jsoftware.fods.log.DefaultLoggerFactory;

/**
 * Factory of {@link Configuration} based on {@link Properties}.
 * @author szalik
 */
public class PropertiesBasedConfigurationFactory implements ConfigurationFactory {
	private Properties properties;
	
	public PropertiesBasedConfigurationFactory() {
		properties = new Properties();
	}
	
	public void setProperties(Properties properties) {
		Properties props = new Properties();
		try {
			props.load(getClass().getResourceAsStream("/defaultConfigurationBase.properties"));
		} catch (IOException e) {
			throw new RuntimeException("Can not load default configuration values.", e);
		}
		for(String k : properties.stringPropertyNames()) {
			props.setProperty(k, properties.getProperty(k));
		}
		this.properties = props;
	}
	
	public Configuration getConfiguration() {
		Properties main = new Properties();
		Map<String,Properties> map = new HashMap<String, Properties>();
		for(Object k : properties.keySet()) {
			String key = k.toString();
			if (key.contains(".")) {
				String[] va  = key.split("\\.", 2);
				Properties p = map.get(va[0]);
				if (p == null) {
					p = new Properties();
				}
				p.put(va[1], properties.getProperty(key));
				map.put(va[0], p);
			} else {
				main.put(key, properties.getProperty(key));
			}
		}
		
		DefaultConfiguration configuration = new DefaultConfiguration(main);
		PropertiesUtil mainPU = new PropertiesUtil(main);
		LoggerFactory lf = mainPU.load("loggerFactory", DefaultLoggerFactory.class);
		configuration.setLogger(lf.getLogger(main));
	
		DatabaseConfiguration[] dbsco = new DatabaseConfiguration[map.size()];
		int i = 0;
		for(String dbname : map.keySet()) {
			DatabaseConfigurationImpl dbc = new DatabaseConfigurationImpl(dbname);
			dbc.props = map.get(dbname);
			PropertiesUtil pu = new PropertiesUtil(dbc.props, dbname);
			dbc.testSQL = pu.getProperty("testSQL", mainPU.getProperty("testSQL"));
			ConnectionCreatorFactory fac = pu.load("connectionCreatorFactory", null);
			dbc.connectionCreator = fac.getConnectionCreator(dbname, configuration.getLogger(), dbc.props);
			dbsco[i++] = dbc;
		}
		configuration.setDatabases(dbsco);
		
		SelectorFactory sf = mainPU.load("selectorFactory", DefaultSelectorFactory.class);
		configuration.setSelector(sf.getSelector(configuration));
		return configuration;
	}	
	
	 
}


class DatabaseConfigurationImpl implements DatabaseConfiguration {
	ConnectionCreator connectionCreator;
	Properties props;
	String testSQL;
	private String name;
	
	public DatabaseConfigurationImpl(String name) {
		this.name = name;
	}

	public ConnectionCreator getConnectionCreator() {
		return connectionCreator;
	}

	public Properties getConnectionProperties() {
		return props;
	}

	public String getDatabaseName() {
		return name;
	}

	public String getTestSql() {
		return testSQL;
	}
	
}