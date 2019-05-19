package hello.resources;

import hello.health.Definicoes;
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
import scala.Tuple2;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil.convertScanToString;

@Path("/distancia")
@Produces(MediaType.APPLICATION_JSON)
public class HelloResource {
    private final String template;
    private volatile String defaultName;
    private long counter;
    private JavaSparkContext sparkContext;

    public HelloResource(String template, String defaultName, JavaSparkContext jsc) {
        this.template = template;
        this.defaultName = defaultName;
        sparkContext = jsc;
    }

    @GET
    public List<DistanciaAviao> sayHello() throws IOException {


        Configuration conf = HBaseConfiguration.create();
        String tableName = "trafego";

        System.setProperty("user.name", "hdfs");
        System.setProperty("HADOOP_USER_NAME", "hdfs");
        Scan scan = new Scan();
        scan.addColumn("infoaviao".getBytes(), "TailNum".getBytes());
        scan.addColumn("infoaviao".getBytes(), "Distance".getBytes());
        conf.set("hbase.zookeeper.quorum", Definicoes.ZKIP);
        conf.set("hbase.zookeeper.property.clientPort", Definicoes.ZKPort);
        conf.set(TableInputFormat.INPUT_TABLE, tableName);
        conf.set(TableInputFormat.SCAN, convertScanToString(scan));

        try {
            JavaPairRDD<ImmutableBytesWritable, Result> data =
                    sparkContext.newAPIHadoopRDD(conf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);

            List<DistanciaAviao> res = data.values().mapToPair(a -> {
                String aux = new String(a.getValue("infoaviao".getBytes(), "TailNum".getBytes()));
                String b = new String(a.getValue("infoaviao".getBytes(), "Distance".getBytes()));
                long dist = 0;
                try{
                    dist = Long.valueOf(b);
                }
                catch(Exception e){}
                return new Tuple2<String,Long>(aux, dist);
            }).reduceByKey((Long count1, Long count2) -> count1 + count2)
                    .mapToPair(a -> {
                        return new Tuple2<Long,String>(a._2,a._1);
                    })
                    .sortByKey(false)
                    .map(a -> new DistanciaAviao(a._1,a._2))
                    .take(30);

            System.out.println("CENASFXGHURYESTDFXCVJFERYGDS");

            System.out.println("Consegui");

            return res;


        }
        finally {
            System.out.println("Vou terminar");
        }
    }

}

