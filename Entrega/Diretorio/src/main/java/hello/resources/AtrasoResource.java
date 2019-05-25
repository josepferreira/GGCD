package hello.resources;

import hello.health.Definicoes;
import hello.representations.Atrasos;
import hello.representations.AviaoAtraso;
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
import javax.ws.rs.core.Response;
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
    public Response sayHello() throws IOException {
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
                    .map(a -> new Atrasos(a._2,a._1))
                    .sortBy(atrasos -> atrasos.atraso,false,2)
                    .collect();


            return Response.ok(res).build();


        }
        catch(Exception e){
            System.out.println(e);
            return Response.status(Response.Status.NOT_FOUND).entity("Não existe informação disponível").build();
        }
    }

    @GET
    @Path("/atrasoPartida")
    public Response atrasoPartida () throws IOException {

        Configuration conf = HBaseConfiguration.create();
        String tableName = "trafego";
        System.setProperty("user.name", "hdfs");
        System.setProperty("HADOOP_USER_NAME", "hdfs");
        Scan scan = new Scan();
        scan.addFamily("infoaviao".getBytes());
        conf.set("hbase.zookeeper.quorum", Definicoes.ZKIP);
        conf.set("hbase.zookeeper.property.clientPort", Definicoes.ZKPort);
        conf.set(TableInputFormat.INPUT_TABLE, tableName);
        conf.set(TableInputFormat.SCAN, convertScanToString(scan));

        try{
            JavaPairRDD<ImmutableBytesWritable, Result> data =
                    sparkContext.newAPIHadoopRDD(conf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);

            List<AviaoAtraso> res = data.values()
                    .mapToPair(a -> {

                        String aviao = new String(a.getValue("infoaviao".getBytes(), "TailNum".getBytes()));
                        String atr = new String(a.getValue("infoaviao".getBytes(), "DepDelay".getBytes()));
                        long atraso = 0;
                        try {
                            float f = Float.valueOf(atr);
                            if (f<0)
                                f = f*(-1);
                            atraso = (long) f;
                        }catch (Exception e){
                            System.out.println(e);

                        }

                        return new Tuple2<String,Long>(aviao, atraso);
                    }).reduceByKey((Long count1, Long count2) -> count1 + count2)
                    .sortByKey(false)
                    .collectAsMap()
                    .entrySet()
                    .stream()
                    .map(a -> new AviaoAtraso(a.getValue(),a.getKey()))
                    .sorted().limit(10)
                    .collect(Collectors.toList());



            return  Response.ok(res).build();

        }catch(Exception e){
            System.out.println(e);
            return Response.status(Response.Status.NOT_FOUND).entity("Não existe informação disponível").build();
        }


    }
    @GET
    @Path("/atrasoChegada")
    public Response atrasoChegada () throws IOException {

        Configuration conf = HBaseConfiguration.create();
        String tableName = "trafego";
        System.setProperty("user.name", "hdfs");
        System.setProperty("HADOOP_USER_NAME", "hdfs");
        Scan scan = new Scan();
        scan.addFamily("infoaviao".getBytes());
        conf.set("hbase.zookeeper.quorum", Definicoes.ZKIP);
        conf.set("hbase.zookeeper.property.clientPort", Definicoes.ZKPort);
        conf.set(TableInputFormat.INPUT_TABLE, tableName);
        conf.set(TableInputFormat.SCAN, convertScanToString(scan));

        try{
            JavaPairRDD<ImmutableBytesWritable, Result> data =
                    sparkContext.newAPIHadoopRDD(conf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);

            List<AviaoAtraso> res = data.values()
                    .mapToPair(a -> {

                        String aviao = new String(a.getValue("infoaviao".getBytes(), "TailNum".getBytes()));
                        String atr = new String(a.getValue("infoaviao".getBytes(), "ArrTime".getBytes()));
                        long atraso = 0;
                        try {
                            atraso = Long.valueOf(atr);
                        }catch (Exception e){
                        }

                        return new Tuple2<String,Long>(aviao, atraso);
                    }).reduceByKey((Long count1, Long count2) -> count1 + count2)
                    .sortByKey(false)
                    .collectAsMap()
                    .entrySet()
                    .stream()
                    .map(a -> new AviaoAtraso(a.getValue(),a.getKey()))
                    .sorted().limit(10)
                    .collect(Collectors.toList());

            return  Response.ok(res).build();

        }catch(Exception e){
            System.out.println(e);
            return Response.status(Response.Status.NOT_FOUND).entity("Não existe informação disponível").build();
        }

    }
    @GET
    @Path("/numeros")
    public Response nrAtrados() throws IOException {
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
                    .map(a -> new Atrasos(a._2,a._1))
                    .sortBy(a -> a.atraso,false,2)
                    .collect();


            return Response.ok(res).build();


        }
        catch(Exception e){
            System.out.println(e);
            return Response.status(Response.Status.NOT_FOUND).entity("Não existe informação disponível").build();
        }
    }

}

