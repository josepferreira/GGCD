package hello;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import hello.resources.HelloResource;
import hello.health.TemplateHealthCheck;

public class DiretorioApplication extends Application<DiretorioConfiguration> {
    public static void main(String[] args) throws Exception {
        for(String s: args)
            System.out.println(s + "!");
        new DiretorioApplication().run(args);
    }

    @Override
    public String getName() { return "Hello"; }

    @Override
    public void initialize(Bootstrap<DiretorioConfiguration> bootstrap) { }

    @Override
    public void run(DiretorioConfiguration configuration,
                    Environment environment) {
        System.out.println("Entrei");
        environment.jersey().register(
            new HelloResource(configuration.template, configuration.defaultName)
        );
        System.out.println("PAssei");

        environment.healthChecks().register("template",
            new TemplateHealthCheck(configuration.template));
    }

}

