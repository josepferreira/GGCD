package hello;

import hello.health.Definicoes;
import hello.representations.*;
import hello.resources.*;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import hello.health.TemplateHealthCheck;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.IOException;

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

    private void criaTabela(){

        String tableName = "trafego";
        Configuration conf = HBaseConfiguration.create();

        System.setProperty("user.name", "hdfs");
        System.setProperty("HADOOP_USER_NAME", "hdfs");
        conf.set("hbase.zookeeper.quorum", Definicoes.ZKIP);
        conf.set("hbase.zookeeper.property.clientPort", Definicoes.ZKPort);

        HTableDescriptor htable = new HTableDescriptor(TableName.valueOf(tableName));
        htable.addFamily( new HColumnDescriptor("Aeroportos"));
        htable.addFamily( new HColumnDescriptor("InfoAviao"));
        htable.addFamily( new HColumnDescriptor("TipoAtrasos"));
        htable.addFamily( new HColumnDescriptor("InfosGerais"));

        System.out.println("Vou tentar criar o admin");

        try (Connection connection = ConnectionFactory.createConnection(conf)) {
            Admin hbase_admin = connection.getAdmin();
            System.out.println( "Creating Table..." );
            hbase_admin.createTable( htable );
            System.out.println("Done!");
            hbase_admin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

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

        criaTabela();

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

