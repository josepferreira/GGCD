package hello.resources;

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

@Path("/hello")
@Produces(MediaType.APPLICATION_JSON)
public class HelloResource {
    private final String template;
    private volatile String defaultName;
    private long counter;

    public HelloResource(String template, String defaultName) {
        this.template = template;
        this.defaultName = defaultName;
    }

    @GET
    public List<DistanciaAviao> sayHello() throws IOException {
        SparkConf config = new SparkConf().setMaster("local[*]").setAppName("Word Count");
        JavaSparkContext sparkContext = new JavaSparkContext(config);

        Configuration conf = HBaseConfiguration.create();
        String tableName = "trafego";

        System.setProperty("user.name", "hdfs");
        System.setProperty("HADOOP_USER_NAME", "hdfs");
        Scan scan = new Scan();
        scan.addFamily("infoaviao".getBytes());
        conf.set("hbase.zookeeper.quorum", "172.19.0.4");
        conf.set("hbase.zookeeper.property.clientPort", "2181");
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
                    .take(30)
                    .stream()
                    .map(a -> new DistanciaAviao(a._1,a._2))
                    .collect(Collectors.toList());

            System.out.println("CENASFXGHURYESTDFXCVJFERYGDS");

            System.out.println("Consegui");

            return res;


        }
        finally {
            System.out.println("Vou terminar");
            sparkContext.close();
            sparkContext.stop();
        }
    }

}

