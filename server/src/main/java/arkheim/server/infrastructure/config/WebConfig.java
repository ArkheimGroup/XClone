package arkheim.server.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures upload for server's uploaded media access
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**") // this line makes spring look for a file everytime user sends request to /uploads endpoint instead of looking for uploads configurator.
                .addResourceLocations( // Looks for the requested file inside these paths (relative to where jar file is running from)
                        "file:uploads/",
                        "file:../uploads/"
                );
    }
}

