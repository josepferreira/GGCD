package hello.resources;

import hello.health.Definicoes;
import hello.representations.AeroportoCancelados;
import hello.representations.AeroportoDesviado;
import hello.representations.Atrasos;
import hello.representations.DistanciaAviao;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.util.Bytes;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil.convertScanToString;


@Path("/aeroporto")
@Produces(MediaType.APPLICATION_JSON)
public class AeroportoResource {
    private final String template;
    private volatile String defaultName;
    private long counter;
    private JavaSparkContext sparkContext;

    public AeroportoResource(String template, String defaultName, JavaSparkContext jsc) {
        this.template = template;
        this.defaultName = defaultName;
        sparkContext = jsc;
    }




    @GET
    @Path("/cancelados")
    public List<AeroportoCancelados> sayHello() throws IOException {
        System.out.println("Cheguei");

        Configuration conf = HBaseConfiguration.create();
        String tableName = "trafego";

        System.setProperty("user.name", "hdfs");
        System.setProperty("HADOOP_USER_NAME", "hdfs");
        Scan scan = new Scan();
        Filter filtro = new SingleColumnValueFilter("aeroportos".getBytes(), "Cancelled".getBytes(),
                CompareFilter.CompareOp.GREATER_OR_EQUAL, new BinaryComparator(Bytes.toBytes("1")));
        scan.setFilter(filtro);
        scan.addColumn("aeroportos".getBytes(), "Dest".getBytes());
        conf.set("hbase.zookeeper.quorum", Definicoes.ZKIP);
        conf.set("hbase.zookeeper.property.clientPort", Definicoes.ZKPort);
        conf.set(TableInputFormat.INPUT_TABLE, tableName);
        conf.set(TableInputFormat.SCAN, convertScanToString(scan));

        try {
            System.out.println("Vou tentar");
            JavaPairRDD<ImmutableBytesWritable, Result> data =
                    sparkContext.newAPIHadoopRDD(conf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);
            System.out.println("Esta feito");
            List<AeroportoCancelados> res = data.values().map(a -> {
                String aux = new String(a.getValue("aeroportos".getBytes(), "Dest".getBytes()));
                return aux;
            }).countByValue().entrySet().stream().map(a -> new AeroportoCancelados(a.getValue(),a.getKey()))
                    .sorted()
                    .collect(Collectors.toList());
            return res;


        }
        finally {
            System.out.println("Vou terminar");
        }
    }

    @GET
    @Path("/desviados")
    public List<AeroportoDesviado> diverted() throws IOException {
        System.out.println("Cheguei");

        Configuration conf = HBaseConfiguration.create();
        String tableName = "trafego";

        System.setProperty("user.name", "hdfs");
        System.setProperty("HADOOP_USER_NAME", "hdfs");
        Scan scan = new Scan();
        Filter filtro = new SingleColumnValueFilter("aeroportos".getBytes(), "Diverted".getBytes(),
                CompareFilter.CompareOp.GREATER_OR_EQUAL, new BinaryComparator(Bytes.toBytes("1")));
        scan.setFilter(filtro);
        scan.addColumn("aeroportos".getBytes(), "Dest".getBytes());
        conf.set("hbase.zookeeper.quorum", Definicoes.ZKIP);
        conf.set("hbase.zookeeper.property.clientPort", Definicoes.ZKPort);
        conf.set(TableInputFormat.INPUT_TABLE, tableName);
        conf.set(TableInputFormat.SCAN, convertScanToString(scan));

        try {
            System.out.println("Vou tentar");
            JavaPairRDD<ImmutableBytesWritable, Result> data =
                    sparkContext.newAPIHadoopRDD(conf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);
            System.out.println("Esta feito");
            List<AeroportoDesviado> res = data.values().map(a -> {
                String aux = new String(a.getValue("aeroportos".getBytes(), "Dest".getBytes()));
                return aux;
            }).countByValue().entrySet().stream().map(a -> new AeroportoDesviado(a.getValue(),a.getKey()))
                    .sorted()
                    .collect(Collectors.toList());
            return res;


        }
        finally {
            System.out.println("Vou terminar");
        }
    }

}

