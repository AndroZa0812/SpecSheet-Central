package com.specsheetcentral.config;

import com.specsheetcentral.model.*;
import com.specsheetcentral.repository.CategoryRepository;
import com.specsheetcentral.repository.ProductRepository;
import com.specsheetcentral.repository.ProductSpecRepository;
import com.specsheetcentral.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    @Bean
    CommandLineRunner seed(CategoryRepository categories,
                           ProductRepository products,
                           ProductSpecRepository specs,
                           UserRepository users,
                           PasswordEncoder encoder) {
        return args -> {
            if (categories.count() > 0) return;

            Category microcontrollers = new Category();
            microcontrollers.setName("Microcontrollers");
            categories.save(microcontrollers);

            Category sensors = new Category();
            sensors.setName("Sensors");
            categories.save(sensors);

            Category leds = new Category();
            leds.setName("LEDs");
            categories.save(leds);

            Category motors = new Category();
            motors.setName("Motors");
            categories.save(motors);

            Product p1 = new Product();
            p1.setName("Arduino Uno R3");
            p1.setSku("ARD-UNO-R3");
            p1.setPrice(24.99);
            p1.setStockQuantity(150);
            p1.setCategory(microcontrollers);
            p1.setManufacturer("Arduino");
            products.save(p1);

            ProductSpec s1 = new ProductSpec();
            s1.setProduct(p1);
            s1.setSpecKey("Microcontroller");
            s1.setSpecValue("ATmega328P");
            specs.save(s1);

            String operatingVoltage = "Operating Voltage";

            ProductSpec s2 = new ProductSpec();
            s2.setProduct(p1);
            s2.setSpecKey(operatingVoltage);
            s2.setSpecValue("5V");
            specs.save(s2);

            ProductSpec s3 = new ProductSpec();
            s3.setProduct(p1);
            s3.setSpecKey("Digital I/O Pins");
            s3.setSpecValue("14");
            specs.save(s3);

            ProductSpec s4 = new ProductSpec();
            s4.setProduct(p1);
            s4.setSpecKey("Analog Input Pins");
            s4.setSpecValue("6");
            specs.save(s4);

            ProductSpec s5 = new ProductSpec();
            s5.setProduct(p1);
            s5.setSpecKey("Flash Memory");
            s5.setSpecValue("32 KB");
            specs.save(s5);

            Product p2 = new Product();
            p2.setName("Raspberry Pi 4 Model B");
            p2.setSku("RPI-4B-4GB");
            p2.setPrice(55.00);
            p2.setStockQuantity(85);
            p2.setCategory(microcontrollers);
            p2.setManufacturer("Raspberry Pi");
            products.save(p2);

            ProductSpec s6 = new ProductSpec();
            s6.setProduct(p2);
            s6.setSpecKey("Processor");
            s6.setSpecValue("Quad core Cortex-A72");
            specs.save(s6);

            ProductSpec s7 = new ProductSpec();
            s7.setProduct(p2);
            s7.setSpecKey("RAM");
            s7.setSpecValue("4GB LPDDR4");
            specs.save(s7);

            ProductSpec s8 = new ProductSpec();
            s8.setProduct(p2);
            s8.setSpecKey(operatingVoltage);
            s8.setSpecValue("5V");
            specs.save(s8);

            Product p3 = new Product();
            p3.setName("DHT22 Temperature Sensor");
            p3.setSku("DHT-22-MOD");
            p3.setPrice(9.95);
            p3.setStockQuantity(200);
            p3.setCategory(sensors);
            p3.setManufacturer("Aosong");
            products.save(p3);

            ProductSpec s9 = new ProductSpec();
            s9.setProduct(p3);
            s9.setSpecKey("Temperature Range");
            s9.setSpecValue("-40 to 80 C");
            specs.save(s9);

            ProductSpec s10 = new ProductSpec();
            s10.setProduct(p3);
            s10.setSpecKey("Humidity Range");
            s10.setSpecValue("0-100% RH");
            specs.save(s10);

            ProductSpec s11 = new ProductSpec();
            s11.setProduct(p3);
            s11.setSpecKey("Accuracy");
            s11.setSpecValue("+/-0.5 C");
            specs.save(s11);

            Product p4 = new Product();
            p4.setName("WS2812B LED Strip");
            p4.setSku("LED-WS2812B-1M");
            p4.setPrice(15.99);
            p4.setStockQuantity(120);
            p4.setCategory(leds);
            p4.setManufacturer("Worldsemi");
            products.save(p4);

            ProductSpec s12 = new ProductSpec();
            s12.setProduct(p4);
            s12.setSpecKey("LEDs per meter");
            s12.setSpecValue("60");
            specs.save(s12);

            ProductSpec s13 = new ProductSpec();
            s13.setProduct(p4);
            s13.setSpecKey(operatingVoltage);
            s13.setSpecValue("5V");
            specs.save(s13);

            ProductSpec s14 = new ProductSpec();
            s14.setProduct(p4);
            s14.setSpecKey("Color");
            s14.setSpecValue("RGB");
            specs.save(s14);

            Product p5 = new Product();
            p5.setName("28BYJ-48 Stepper Motor");
            p5.setSku("MTR-28BYJ-48");
            p5.setPrice(4.99);
            p5.setStockQuantity(300);
            p5.setCategory(motors);
            p5.setManufacturer("Generic");
            products.save(p5);

            ProductSpec s15 = new ProductSpec();
            s15.setProduct(p5);
            s15.setSpecKey("Steps per Revolution");
            s15.setSpecValue("2048");
            specs.save(s15);

            ProductSpec s16 = new ProductSpec();
            s16.setProduct(p5);
            s16.setSpecKey(operatingVoltage);
            s16.setSpecValue("5V DC");
            specs.save(s16);

            ProductSpec s17 = new ProductSpec();
            s17.setProduct(p5);
            s17.setSpecKey("Torque");
            s17.setSpecValue("34.3 mN.m");
            specs.save(s17);

            Product p6 = new Product();
            p6.setName("ESP32 Dev Board");
            p6.setSku("ESP32-DEV-38P");
            p6.setPrice(12.50);
            p6.setStockQuantity(3);
            p6.setCategory(microcontrollers);
            p6.setManufacturer("Espressif");
            products.save(p6);

            ProductSpec s18 = new ProductSpec();
            s18.setProduct(p6);
            s18.setSpecKey("Microcontroller");
            s18.setSpecValue("ESP32-D0WDQ6");
            specs.save(s18);

            ProductSpec s19 = new ProductSpec();
            s19.setProduct(p6);
            s19.setSpecKey(operatingVoltage);
            s19.setSpecValue("3.3V");
            specs.save(s19);

            ProductSpec s20 = new ProductSpec();
            s20.setProduct(p6);
            s20.setSpecKey("WiFi");
            s20.setSpecValue("802.11 b/g/n");
            specs.save(s20);

            ProductSpec s21 = new ProductSpec();
            s21.setProduct(p6);
            s21.setSpecKey("Flash Memory");
            s21.setSpecValue("4 MB");
            specs.save(s21);

            String adminPassword = System.getenv("SEED_ADMIN_PASSWORD");
            String userPassword = System.getenv("SEED_USER_PASSWORD");

            if (adminPassword != null && !adminPassword.isBlank()
                    && userPassword != null && !userPassword.isBlank()) {
                User admin = new User();
                admin.setEmail("admin@specsheet.com");
                admin.setPasswordHash(encoder.encode(adminPassword));
                admin.setRole(User.Role.ADMIN);
                users.save(admin);

                User user = new User();
                user.setEmail("user@specsheet.com");
                user.setPasswordHash(encoder.encode(userPassword));
                user.setRole(User.Role.USER);
                users.save(user);
            } else {
                log.info("SKIPPED user seeding: SEED_ADMIN_PASSWORD and SEED_USER_PASSWORD env vars not set");
            }
        };
    }
}
