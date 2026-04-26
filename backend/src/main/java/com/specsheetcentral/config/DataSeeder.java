package com.specsheetcentral.config;

import com.specsheetcentral.model.*;
import com.specsheetcentral.repository.CategoryRepository;
import com.specsheetcentral.repository.ProductRepository;
import com.specsheetcentral.repository.ProductSpecRepository;
import com.specsheetcentral.repository.ReviewRepository;
import com.specsheetcentral.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    @Bean
    CommandLineRunner seed(CategoryRepository categories,
                           ProductRepository products,
                           ProductSpecRepository specs,
                           UserRepository users,
                           ReviewRepository reviews,
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
            p1.setImageUrl("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='400' viewBox='0 0 400 400'%3E%3Crect fill='%230068B8' width='400' height='400'/%3E%3Crect x='120' y='80' width='160' height='240' rx='8' fill='%2300979D'/%3E%3Crect x='140' y='100' width='120' height='80' rx='4' fill='%23111'/%3E%3Ccircle cx='200' cy='140' r='20' fill='%23333'/%3E%3Crect x='140' y='200' width='120' height='10' rx='2' fill='%23fff' opacity='0.3'/%3E%3Crect x='140' y='220' width='80' height='10' rx='2' fill='%23fff' opacity='0.3'/%3E%3Crect x='140' y='240' width='100' height='10' rx='2' fill='%23fff' opacity='0.3'/%3E%3Crect x='140' y='260' width='60' height='10' rx='2' fill='%23fff' opacity='0.3'/%3E%3Ccircle cx='150' cy='300' r='8' fill='%23fff' opacity='0.5'/%3E%3Ccircle cx='250' cy='300' r='8' fill='%23fff' opacity='0.5'/%3E%3C/svg%3E");
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
            p2.setImageUrl("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='400' viewBox='0 0 400 400'%3E%3Crect fill='%23C51A4A' width='400' height='400'/%3E%3Crect x='80' y='80' width='240' height='240' rx='12' fill='%231a1a2e'/%3E%3Crect x='100' y='100' width='80' height='80' rx='4' fill='%23333'/%3E%3Crect x='200' y='100' width='100' height='40' rx='4' fill='%23555'/%3E%3Crect x='200' y='160' width='100' height='40' rx='4' fill='%23555'/%3E%3Ccircle cx='140' cy='260' r='30' fill='%23222'/%3E%3Ccircle cx='140' cy='260' r='15' fill='%23444'/%3E%3Crect x='220' y='220' width='60' height='60' rx='4' fill='%23333'/%3E%3Crect x='230' y='230' width='40' height='40' rx='2' fill='%23555'/%3E%3C/svg%3E");
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
            p3.setImageUrl("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='400' viewBox='0 0 400 400'%3E%3Crect fill='%231B5E20' width='400' height='400'/%3E%3Crect x='140' y='60' width='120' height='280' rx='16' fill='%23fff'/%3E%3Crect x='155' y='80' width='90' height='120' rx='8' fill='%23e0e0e0'/%3E%3Ccircle cx='200' cy='140' r='30' fill='%234CAF50'/%3E%3Ccircle cx='200' cy='140' r='15' fill='%23fff'/%3E%3Ctext x='200' y='250' text-anchor='middle' font-family='sans-serif' font-size='14' fill='%23333'%3EDHT22%3C/text%3E%3Crect x='160' y='270' width='80' height='8' rx='2' fill='%23ccc'/%3E%3Crect x='160' y='285' width='80' height='8' rx='2' fill='%23ccc'/%3E%3Crect x='160' y='300' width='80' height='8' rx='2' fill='%23ccc'/%3E%3Crect x='170' y='320' width='60' height='10' rx='2' fill='%23999'/%3E%3C/svg%3E");
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
            p4.setImageUrl("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='400' viewBox='0 0 400 400'%3E%3Crect fill='%231a1a2e' width='400' height='400'/%3E%3Crect x='40' y='180' width='320' height='40' rx='4' fill='%23333'/%3E%3Ccircle cx='80' cy='200' r='12' fill='%23FF0000'/%3E%3Ccircle cx='140' cy='200' r='12' fill='%2300FF00'/%3E%3Ccircle cx='200' cy='200' r='12' fill='%230000FF'/%3E%3Ccircle cx='260' cy='200' r='12' fill='%23FF0000'/%3E%3Ccircle cx='320' cy='200' r='12' fill='%2300FF00'/%3E%3Ccircle cx='80' cy='200' r='6' fill='%23fff' opacity='0.5'/%3E%3Ccircle cx='140' cy='200' r='6' fill='%23fff' opacity='0.5'/%3E%3Ccircle cx='200' cy='200' r='6' fill='%23fff' opacity='0.5'/%3E%3Ccircle cx='260' cy='200' r='6' fill='%23fff' opacity='0.5'/%3E%3Ccircle cx='320' cy='200' r='6' fill='%23fff' opacity='0.5'/%3E%3Crect x='40' y='140' width='320' height='4' fill='%23555'/%3E%3Crect x='40' y='260' width='320' height='4' fill='%23555'/%3E%3Ctext x='200' y='100' text-anchor='middle' font-family='sans-serif' font-size='20' fill='%23fff'%3EWS2812B LED Strip%3C/text%3E%3C/svg%3E");
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
            p5.setImageUrl("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='400' viewBox='0 0 400 400'%3E%3Crect fill='%23263238' width='400' height='400'/%3E%3Ccircle cx='200' cy='200' r='100' fill='%23455A64'/%3E%3Ccircle cx='200' cy='200' r='70' fill='%2337474F'/%3E%3Ccircle cx='200' cy='200' r='40' fill='%22546E7A'/%3E%3Ccircle cx='200' cy='200' r='15' fill='%23263238'/%3E%3Cline x1='200' y1='130' x2='200' y2='160' stroke='%2378909C' stroke-width='4'/%3E%3Cline x1='200' y1='240' x2='200' y2='270' stroke='%2378909C' stroke-width='4'/%3E%3Cline x1='130' y1='200' x2='160' y2='200' stroke='%2378909C' stroke-width='4'/%3E%3Cline x1='240' y1='200' x2='270' y2='200' stroke='%2378909C' stroke-width='4'/%3E%3Crect x='170' y='300' width='60' height='30' rx='4' fill='%23455A64'/%3E%3Crect x='180' y='310' width='8' height='10' fill='%2378909C'/%3E%3Crect x='195' y='310' width='8' height='10' fill='%2378909C'/%3E%3Crect x='210' y='310' width='8' height='10' fill='%2378909C'/%3E%3C/svg%3E");
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
            p6.setImageUrl("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='400' viewBox='0 0 400 400'%3E%3Crect fill='%230D47A1' width='400' height='400'/%3E%3Crect x='130' y='60' width='140' height='280' rx='8' fill='%231565C0'/%3E%3Crect x='150' y='80' width='100' height='100' rx='4' fill='%230D47A1'/%3E%3Crect x='160' y='90' width='80' height='80' rx='2' fill='%231976D2'/%3E%3Ccircle cx='200' cy='130' r='20' fill='%230D47A1'/%3E%3Ccircle cx='200' cy='130' r='10' fill='%2342A5F5'/%3E%3Crect x='150' y='200' width='100' height='8' rx='2' fill='%23fff' opacity='0.3'/%3E%3Crect x='150' y='215' width='70' height='8' rx='2' fill='%23fff' opacity='0.3'/%3E%3Crect x='150' y='230' width='90' height='8' rx='2' fill='%23fff' opacity='0.3'/%3E%3Crect x='150' y='245' width='50' height='8' rx='2' fill='%23fff' opacity='0.3'/%3E%3Crect x='150' y='270' width='100' height='40' rx='4' fill='%230D47A1'/%3E%3Crect x='160' y='280' width='80' height='20' rx='2' fill='%231976D2'/%3E%3Ccircle cx='160' cy='320' r='6' fill='%2342A5F5'/%3E%3Ccircle cx='240' cy='320' r='6' fill='%2342A5F5'/%3E%3C/svg%3E");
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

                Review r1 = new Review();
                r1.setProduct(p1);
                r1.setUser(admin);
                r1.setRating(5);
                r1.setComment("Excellent board for beginners!");
                r1.setCreatedAt(LocalDateTime.now());
                reviews.save(r1);

                Review r2 = new Review();
                r2.setProduct(p1);
                r2.setUser(user);
                r2.setRating(4);
                r2.setComment("Great value, reliable performance.");
                r2.setCreatedAt(LocalDateTime.now());
                reviews.save(r2);

                Review r3 = new Review();
                r3.setProduct(p2);
                r3.setUser(admin);
                r3.setRating(4);
                r3.setComment("Powerful single-board computer.");
                r3.setCreatedAt(LocalDateTime.now());
                reviews.save(r3);

                Review r4 = new Review();
                r4.setProduct(p3);
                r4.setUser(user);
                r4.setRating(5);
                r4.setComment("Accurate and easy to use.");
                r4.setCreatedAt(LocalDateTime.now());
                reviews.save(r4);

                Review r5 = new Review();
                r5.setProduct(p6);
                r5.setUser(admin);
                r5.setRating(5);
                r5.setComment("Best ESP32 dev board for the price.");
                r5.setCreatedAt(LocalDateTime.now());
                reviews.save(r5);
            } else {
                log.info("SKIPPED user seeding: SEED_ADMIN_PASSWORD and SEED_USER_PASSWORD env vars not set");
            }
        };
    }
}
