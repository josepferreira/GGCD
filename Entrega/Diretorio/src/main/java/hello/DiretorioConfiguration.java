package hello;

import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

public class DiretorioConfiguration extends Configuration {
    @NotEmpty
    public String template;

    public String defaultName = "Stranger";
}

