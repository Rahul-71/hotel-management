package com.telus.hotel_management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HotelManagementApplication {

    private final static Logger log = LoggerFactory.getLogger(HotelManagementApplication.class);

//    @Bean
//    public ModelMapper modelMapper() {
//        return new ModelMapper();
//    }

    public static void main(String[] args) {
        SpringApplication.run(HotelManagementApplication.class, args);
    }

//    @Bean
//    public CommandLineRunner initializeRoles(RoleRepository roleRepository) {
//        return args -> {
//            if (roleRepository.findByName(RoleType.USER).isEmpty()) {
//                roleRepository.save(new Role(RoleType.USER));
//                log.info("Created ROLE_USER");
//            }
//            if (roleRepository.findByName(RoleType.HOTEL).isEmpty()) {
//                roleRepository.save(new Role(RoleType.HOTEL));
//                log.info("Created ROLE_HOTEL");
//            }
//            if (roleRepository.findByName(RoleType.ADMIN).isEmpty()) {
//                roleRepository.save(new Role(RoleType.ADMIN));
//                log.info("Created ROLE_ADMIN");
//            }
//            log.info("Roles Initialization Completed ---");
//        };
//
//    }
}
