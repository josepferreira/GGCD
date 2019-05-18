package hello;

import hello.representations.Atrasos;
import hello.representations.DistanciaAviao;
import hello.resources.AeroportoResource;
import hello.resources.AfluenciaResource;
import hello.resources.AtrasoResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import hello.resources.HelloResource;
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
        Class[] c = new Class[2];
        c[0] = Atrasos.class;
        c[1] = DistanciaAviao.class;
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
                    new AfluenciaResource(configuration.template, configuration.defaultName)
            );
            environment.jersey().register(
                    new AeroportoResource(configuration.template, configuration.defaultName)
            );
            System.out.println("PAssei");

            environment.healthChecks().register("template",
                    new TemplateHealthCheck(configuration.template));
        }
        finally {
            System.out.println("Vou terminar asdfghjghdfsa");
//            sparkContext.close();
        }
    }

}

