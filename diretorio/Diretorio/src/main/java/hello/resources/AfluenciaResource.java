package hello.resources;

import hello.representations.Atrasos;
import hello.representations.DiaSemana;
import hello.representations.DistanciaAviao;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.jcodings.util.Hash;
import scala.Tuple2;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil.convertScanToString;


@Path("/afluencia")
@Produces(MediaType.APPLICATION_JSON)
public class AfluenciaResource {
    private final String template;
    private volatile String defaultName;
    private long counter;

    public AfluenciaResource(String template, String defaultName) {
        this.template = template;
        this.defaultName = defaultName;
    }

    @GET
    @Path("/diaDaSemana")
    public List<DiaSemana> diaSemana() throws IOException {
        SparkConf config = new SparkConf().setMaster("local[*]").setAppName("DiaSemana");
        JavaSparkContext sparkContext = new JavaSparkContext(config);

        Configuration conf = HBaseConfiguration.create();
        String tableName = "trafego";

        System.setProperty("user.name", "hdfs");
        System.setProperty("HADOOP_USER_NAME", "hdfs");
        Scan scan = new Scan();
        scan.addColumn("infogerais".getBytes(),"DayOfWeek".getBytes());
        conf.set("hbase.zookeeper.quorum", "172.19.0.4");
        conf.set("hbase.zookeeper.property.clientPort", "2181");
        conf.set(TableInputFormat.INPUT_TABLE, tableName);
        conf.set(TableInputFormat.SCAN, convertScanToString(scan));

        try {
            JavaPairRDD<ImmutableBytesWritable, Result> data =
                    sparkContext.newAPIHadoopRDD(conf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);

            List<DiaSemana> res = data.values()
                    .map(a -> {
                        String aux = new String(a.getValue("infogerais".getBytes(),"DayOfWeek".getBytes()));
                        return aux;
                    })
                    .countByValue()
                    .entrySet()
                    .stream()
                    .map(a -> new DiaSemana(a.getValue(),a.getKey()))
                    .sorted()
                    .collect(Collectors.toList());
            return res;


        }
        finally {
            System.out.println("Vou terminar");
            sparkContext.close();
        }
    }

}

