package uk.gov.hmcts.reform.coh.config; 
 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.context.annotation.Configuration; 
import org.springframework.web.servlet.config.annotation.InterceptorRegistry; 
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter; 
import uk.gov.hmcts.reform.coh.handlers.IdamHeaderInterceptor;

@Configuration 
public class WebMvcConfig extends WebMvcConfigurerAdapter { 
 
    @Autowired 
    IdamHeaderInterceptor idamHeaderInterceptor; 

    @Override
    public void addInterceptors(InterceptorRegistry registry) { 
        registry.addInterceptor(idamHeaderInterceptor); 
    } 
} 