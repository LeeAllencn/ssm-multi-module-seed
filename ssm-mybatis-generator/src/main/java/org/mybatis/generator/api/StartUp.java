package org.mybatis.generator.api;

import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StartUp {

	public static void main(String[] args) throws Exception {
		String[] str = {"user"};
		for (int i = 0; i < str.length; i++) {
			List<String> warnings = new ArrayList<String>();
			File configurationFile = new File(StartUp.class.getResource("/generatorConfig_"+str[i]+".xml").toURI());
			ConfigurationParser cp = new ConfigurationParser(warnings);
			Configuration config = cp.parseConfiguration(configurationFile);
			DefaultShellCallback shellCallback = new DefaultShellCallback(true);
			MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, shellCallback, warnings);
			myBatisGenerator.generate(null);
			System.out.println(warnings);
		}
	}
	
}
