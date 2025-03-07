package com.vivero.viveroApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.vivero.viveroApp")
public class ViveroAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ViveroAppApplication.class, args);
                
                System.out.println("INGRESANDO A VIVEROLITE :D");
	}

}
