package com.trivialware.hotelms.Configuration;

import com.trivialware.hotelms.Entities.Hotel;
import com.trivialware.hotelms.Models.Hotel.HotelDTOSimple;
import com.trivialware.hotelms.Models.Hotel.HotelDTOSimpleAdmin;
import com.trivialware.hotelms.Models.Mapper.ListToListCountConverter;
import com.trivialware.hotelms.Repositories.UserRepository;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    private final UserRepository userRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STANDARD);
        modelMapper.typeMap(Hotel.class, HotelDTOSimple.class).addMappings(new PropertyMap<>() {
            @Override
            protected void configure() {
                using(new ListToListCountConverter<>()).map(source.getRooms(), destination.getNumberRooms());
                using(new ListToListCountConverter<>()).map(source.getServices(), destination.getNumberServices());
            }
        });
        modelMapper.typeMap(Hotel.class, HotelDTOSimpleAdmin.class).addMappings(new PropertyMap<>() {
            @Override
            protected void configure() {
                using(new ListToListCountConverter<>()).map(source.getRooms(), destination.getNumberRooms());
                using(new ListToListCountConverter<>()).map(source.getServices(), destination.getNumberServices());
            }
        });

        return modelMapper;
    }

    //Swagger Configuration
    @Bean
    public GroupedOpenApi apiGroup() {
        return GroupedOpenApi.builder().group("API").pathsToMatch("/**").build();
    }

    @Bean
    public OpenAPI apiInfo() {
        String securitySchemeName = "Authorization";
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        securitySchemeName,
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .bearerFormat("JWT")
                                                .scheme("bearer")
                                )
                )
                .info(
                        new Info().
                                title("HotelMS REST Api").
                                description("REST API for Hotel Management System").
                                version("1.0").
                                license(
                                        new License().
                                                name("GNU Affero General Public License v3.0 or later").
                                                identifier("AGPL-3.0-or-later").
                                                url("https://www.gnu.org/licenses/agpl-3.0.txt")
                                )
                );
    }
}
