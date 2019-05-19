package hello.resources;

import hello.health.Definicoes;
import hello.representations.Atrasos;
import hello.representations.DiaSemana;
import hello.representations.DistanciaAviao;

import hello.representations.VooInfo;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.*;
import com.google.common.base.Optional;
import java.util.stream.Collectors;

import static org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil.convertScanToString;


@Path("/voos")
@Produces(MediaType.APPLICATION_JSON)
public class VooResource {
    private final String template;
    private volatile String defaultName;
    private long counter;
    private JavaSparkContext sparkContext;

    public VooResource(String template, String defaultName, JavaSparkContext jsc) {
        this.template = template;
        this.defaultName = defaultName;
        sparkContext = jsc;
    }

    @GET
    @Path("/infogerais")
    public List<VooInfo> diaSemana(@QueryParam("voo") Optional<String> voo) throws IOException {

        if(!voo.isPresent()){
            return new ArrayList<>();
        }

        Configuration conf = HBaseConfiguration.create();
        String tableName = "trafego";

        System.setProperty("user.name", "hdfs");
        System.setProperty("HADOOP_USER_NAME", "hdfs");
        Scan scan = new Scan();
        scan.setRowPrefixFilter((voo.get()+"::").getBytes());
        scan.addFamily("infogerais".getBytes());
        conf.set("hbase.zookeeper.quorum", Definicoes.ZKIP);
        conf.set("hbase.zookeeper.property.clientPort", Definicoes.ZKPort);
        conf.set(TableInputFormat.INPUT_TABLE, tableName);
        conf.set(TableInputFormat.SCAN, convertScanToString(scan));

        try {
            JavaPairRDD<ImmutableBytesWritable, Result> data =
                    sparkContext.newAPIHadoopRDD(conf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);

            List<VooInfo> res = data.values()
                    .map(a -> {
                        String aux = new String(a.getValue("infogerais".getBytes(),"DayOfWeek".getBytes()));
                        String aux2 = new String(a.getRow());
                        String[] key = aux2.split("::");
                        VooInfo vi = new VooInfo(key[0],aux,key[1]);
                        return vi;
                    })
                    .collect();
            return res;


        }
        finally {
            System.out.println("Vou terminar");
            sparkContext.close();
        }
    }

}

