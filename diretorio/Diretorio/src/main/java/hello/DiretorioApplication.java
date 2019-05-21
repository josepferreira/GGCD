package hello;

import hello.representations.*;
import hello.resources.*;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import hello.health.TemplateHealthCheck;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

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
        SparkConf config = new SparkConf().setMaster("local[*]").setAppName("APPGGCD");
        Class[] c = new Class[6];
        c[0] = Atrasos.class;
        c[1] = DistanciaAviao.class;
        c[2] = VooInfo.class;
        c[3] = AeroportoCancelados.class;
        c[4] = AeroportoDesviado.class;
        c[5] = AviaoInfo.class;


        config.registerKryoClasses(c);
        JavaSparkContext sparkContext = new JavaSparkContext(config);
        try {
            environment.jersey().register(
                    new HelloResource(configuration.template, configuration.defaultName, sparkContext)
            );

            environment.jersey().register(
                    new AtrasoResource(configuration.template, configuration.defaultName, sparkContext)
            );
            environment.jersey().register(
                    new AfluenciaResource(configuration.template, configuration.defaultName, sparkContext)
            );
            environment.jersey().register(
                    new AeroportoResource(configuration.template, configuration.defaultName, sparkContext)
            );
            environment.jersey().register(
                    new VooResource(configuration.template, configuration.defaultName, sparkContext)
            );
            environment.jersey().register(
                    new AviaoResource(configuration.template, configuration.defaultName, sparkContext)
            );
            System.out.println("PAssei");

            environment.healthChecks().register("template",
                    new TemplateHealthCheck(configuration.template));
        }
        finally {
            System.out.println("Vou terminar asdfghjghdfsa");
        }
    }

}

