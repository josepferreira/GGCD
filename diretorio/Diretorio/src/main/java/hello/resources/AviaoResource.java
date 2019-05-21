package hello.resources;

import com.google.common.base.Optional;
import hello.health.Definicoes;
import hello.representations.AeroportoCancelados;
import hello.representations.AviaoInfo;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil.convertScanToString;


@Path("/aviao")
@Produces(MediaType.APPLICATION_JSON)
public class AviaoResource {
    private final String template;
    private volatile String defaultName;
    private long counter;
    private JavaSparkContext sparkContext;

    public AviaoResource(String template, String defaultName, JavaSparkContext jsc) {
        this.template = template;
        this.defaultName = defaultName;
        sparkContext = jsc;
    }

    @GET
    @Path("/voos")
    public List<AviaoInfo> AviaoInfo() throws IOException {

        Configuration conf = HBaseConfiguration.create();
        String tableName = "trafego";

        System.setProperty("user.name", "hdfs");
        System.setProperty("HADOOP_USER_NAME", "hdfs");
        Scan scan = new Scan();
        //scan.setRowPrefixFilter((voo.get()+"::").getBytes());
        scan.addFamily("infoaviao".getBytes());
        conf.set("hbase.zookeeper.quorum", Definicoes.ZKIP);
        conf.set("hbase.zookeeper.property.clientPort", Definicoes.ZKPort);
        conf.set(TableInputFormat.INPUT_TABLE, tableName);
        conf.set(TableInputFormat.SCAN, convertScanToString(scan));


        try {
            System.out.println("Vou tentar");
            JavaPairRDD<ImmutableBytesWritable, Result> data =
                    sparkContext.newAPIHadoopRDD(conf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);
            System.out.println("Esta feito");
            List<AviaoInfo> res = data.values().map(a -> {
                try {
                    String aux = new String(a.getValue("infoaviao".getBytes(), "TailNum".getBytes()));

                    if(!aux.equals("")){
                        return aux;
                    }
                    else return "Sem ID";
                }
                catch(Exception e){
                    return "No Info";
                }
            })
                    .countByValue()
                    .entrySet()
                    .stream()
                    .map(a -> new AviaoInfo(a.getKey(),a.getValue()))
                    .sorted()
                    .collect(Collectors.toList());
            return res;


        }
        finally {
            System.out.println("Vou terminar");

        }
    }

}
