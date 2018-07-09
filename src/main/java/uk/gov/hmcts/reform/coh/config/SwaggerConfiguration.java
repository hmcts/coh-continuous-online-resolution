package uk.gov.hmcts.reform.coh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@ComponentScan("uk.gov.hmcts.reform.coh.controller")
public class SwaggerConfiguration {

    private final static String apiVersion = "0.0.1";

    private static final String MODEL_REF_TYPE = "string";
    private static final String PARAMETER_TYPE = "header";

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.regex("/continuous-online-hearings(.*)"))
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Continuous Online Hearing API")
                .description("Documented API for continuous online hearing."
                        + "To use the API calls generate an Authorization JWT Tokens (user and service) which is required in the header.")
                .version(apiVersion)
                .build();
    }
}