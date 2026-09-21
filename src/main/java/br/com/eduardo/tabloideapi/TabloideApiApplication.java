package br.com.eduardo.tabloideapi;

import br.com.eduardo.tabloideapi.config.TabloideProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(TabloideProperties.class)
public class TabloideApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TabloideApiApplication.class, args);
	}

}
