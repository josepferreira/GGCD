package hello.resources;

import hello.health.Definicoes;
import hello.representations.Atrasos;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil.convertScanToString;


@Path("/atraso")
@Produces(MediaType.APPLICATION_JSON)
public class AtrasoResource {
    private final String template;
    private volatile String defaultName;
    private long counter;
    private JavaSparkContext sparkContext;
    public AtrasoResource(String template, String defaultName, JavaSparkContext jsc) {
        this.template = template;
        this.defaultName = defaultName;
        sparkContext = jsc;
    }




    @GET
    @Path("/totais")
    public List<Atrasos> sayHello() throws IOException {
        System.out.println("Cheguei");


        Configuration conf = HBaseConfiguration.create();
        String tableName = "trafego";

        System.setProperty("user.name", "hdfs");
        System.setProperty("HADOOP_USER_NAME", "hdfs");
        Scan scan = new Scan();
        scan.addFamily("tipoatrasos".getBytes());
        conf.set("hbase.zookeeper.quorum", Definicoes.ZKIP);
        conf.set("hbase.zookeeper.property.clientPort", Definicoes.ZKPort);
        conf.set(TableInputFormat.INPUT_TABLE, tableName);
        conf.set(TableInputFormat.SCAN, convertScanToString(scan));

        try {
            System.out.println("Vou tentar");
            JavaPairRDD<ImmutableBytesWritable, Result> data =
                    sparkContext.newAPIHadoopRDD(conf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);
            System.out.println("Esta feito");
            List<Atrasos> res = data.values().flatMapToPair(a -> {
                ArrayList<Tuple2<String,Float>> l = new ArrayList<>();

                String aux = new String(a.getValue("tipoatrasos".getBytes(),"WeatherDelay".getBytes()));
                float la = 0;
                try{
                    la = Float.valueOf(aux);
                }
                catch(Exception e){}

                l.add(new Tuple2<String,Float>("Weather",la));

                aux = new String(a.getValue("tipoatrasos".getBytes(),"NASDelay".getBytes()));
                la = 0;
                try{
                    la = Float.valueOf(aux);
                }
                catch(Exception e){}

                l.add(new Tuple2<String,Float>("NAS",la));

                aux = new String(a.getValue("tipoatrasos".getBytes(),"CarrierDelay".getBytes()));
                la = 0;
                try{
                    la = Float.valueOf(aux);
                }
                catch(Exception e){}

                l.add(new Tuple2<String,Float>("Carrier",la));

                aux = new String(a.getValue("tipoatrasos".getBytes(),"SecurityDelay".getBytes()));
                la = 0;
                try{
                    la = Float.valueOf(aux);
                }
                catch(Exception e){}

                l.add(new Tuple2<String,Float>("Security",la));

                aux = new String(a.getValue("tipoatrasos".getBytes(),"LateAircraftDelay".getBytes()));
                la = 0;
                try{
                    la = Float.valueOf(aux);
                }
                catch(Exception e){}

                l.add(new Tuple2<String,Float>("LateAircraft",la));


                return l.iterator();

            }).reduceByKey((Float count1, Float count2) -> count1 + count2)
                    .mapToPair(a -> {
                        return new Tuple2<Float,String>(a._2,a._1);
                    })
                    .sortByKey(false)
                    .map(a -> new Atrasos(a._1,a._2))
                    .collect();


            return res;


        }
        finally {
            System.out.println("Vou terminar");
        }
    }

    @GET
    @Path("/numeros")
    public List<Atrasos> nrAtrados() throws IOException {
        System.out.println("Cheguei");


        Configuration conf = HBaseConfiguration.create();
        String tableName = "trafego";

        System.setProperty("user.name", "hdfs");
        System.setProperty("HADOOP_USER_NAME", "hdfs");
        Scan scan = new Scan();
        scan.addFamily("tipoatrasos".getBytes());
        conf.set("hbase.zookeeper.quorum", Definicoes.ZKIP);
        conf.set("hbase.zookeeper.property.clientPort", Definicoes.ZKPort);
        conf.set(TableInputFormat.INPUT_TABLE, tableName);
        conf.set(TableInputFormat.SCAN, convertScanToString(scan));

        try {
            System.out.println("Vou tentar");
            JavaPairRDD<ImmutableBytesWritable, Result> data =
                    sparkContext.newAPIHadoopRDD(conf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);
            System.out.println("Esta feito");
            List<Atrasos> res = data.values().flatMapToPair(a -> {
                ArrayList<Tuple2<String,Float>> l = new ArrayList<>();

                String aux = new String(a.getValue("tipoatrasos".getBytes(),"WeatherDelay".getBytes()));
                float la = 0;
                try{
                    la = Float.valueOf(aux) > 0 ? 1 : 0;
                }
                catch(Exception e){}

                l.add(new Tuple2<String,Float>("Weather",la));

                aux = new String(a.getValue("tipoatrasos".getBytes(),"NASDelay".getBytes()));
                la = 0;
                try{
                    la = Float.valueOf(aux) > 0 ? 1 : 0;

                }
                catch(Exception e){}

                l.add(new Tuple2<String,Float>("NAS",la));

                aux = new String(a.getValue("tipoatrasos".getBytes(),"CarrierDelay".getBytes()));
                la = 0;
                try{
                    la = Float.valueOf(aux) > 0 ? 1 : 0;
                }
                catch(Exception e){}

                l.add(new Tuple2<String,Float>("Carrier",la));

                aux = new String(a.getValue("tipoatrasos".getBytes(),"SecurityDelay".getBytes()));
                la = 0;
                try{
                    la = Float.valueOf(aux) > 0 ? 1 : 0;
                }
                catch(Exception e){}

                l.add(new Tuple2<String,Float>("Security",la));

                aux = new String(a.getValue("tipoatrasos".getBytes(),"LateAircraftDelay".getBytes()));
                la = 0;
                try{
                    la = Float.valueOf(aux) > 0 ? 1 : 0;
                }
                catch(Exception e){}

                l.add(new Tuple2<String,Float>("LateAircraft",la));


                return l.iterator();

            }).reduceByKey((Float count1, Float count2) -> count1 + count2)
                    .mapToPair(a -> {
                        return new Tuple2<Float,String>(a._2,a._1);
                    })
                    .sortByKey(false)
                    .map(a -> new Atrasos(a._1,a._2))
                    .collect();


            return res;


        }
        finally {
            System.out.println("Vou terminar");
        }
    }

}

