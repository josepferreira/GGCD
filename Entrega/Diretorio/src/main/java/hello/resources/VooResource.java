package hello.resources;

import hello.health.Definicoes;
import hello.representations.Atrasos;
import hello.representations.DiaSemana;
import hello.representations.DistanciaAviao;

import hello.representations.VooInfo;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
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
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;
import com.google.common.base.Optional;
import scala.reflect.internal.Required;

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
    public Response InfosGerais(@QueryParam("voo") Optional<String> voo, @QueryParam("date") Optional<String> date) throws IOException {

        Configuration conf = HBaseConfiguration.create();
        String tableName = "trafego";

        System.setProperty("user.name", "hdfs");
        System.setProperty("HADOOP_USER_NAME", "hdfs");
        Scan scan = new Scan();


        if(voo.isPresent() && date.isPresent()){
            System.out.println("Voo escolhido: " + voo.get());
            System.out.println("Data escolhida: " + date.get());
            scan.setRowPrefixFilter((voo.get()+"::"+date.get()).getBytes());
        }
        else if(voo.isPresent()){
            System.out.println("Voo escolhido: " + voo.get());
            scan.setRowPrefixFilter((voo.get()+"::").getBytes());
        }
        else if(date.isPresent()){
            System.out.println("Data escolhida: " + date.get());
            Filter filter = new RowFilter(CompareFilter.CompareOp.EQUAL,
                    new RegexStringComparator("\\.*::"+date.get()));
            scan.setFilter(filter);
        }

        scan.addFamily("infogerais".getBytes());
        conf.set("hbase.zookeeper.quorum", Definicoes.ZKIP);
        conf.set("hbase.zookeeper.property.clientPort", Definicoes.ZKPort);
        conf.set(TableInputFormat.INPUT_TABLE, tableName);
        conf.set(TableInputFormat.SCAN, convertScanToString(scan));

        try {
            String[] colunas = {"DayOfWeek","DepTime","ArrTime","UniqueCarrier","TailNum","AirTime","Origin","Dest","Distance"};
            String[] valores = new String[colunas.length];
            JavaPairRDD<ImmutableBytesWritable, Result> data =
                    sparkContext.newAPIHadoopRDD(conf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);

            List<VooInfo> res = data.values()
                    .map(a -> {
                        String aux2 = new String(a.getRow());
                        String[] key = aux2.split("::");
                        for(int i = 0; i < colunas.length; i++){

                            try{
                                String aux = new String(a.getValue("infogerais".getBytes(),colunas[i].getBytes()));
                                valores[i] = aux;
                            }
                            catch(Exception e){
                                valores[i] = "null";
                            }
                        }
                        VooInfo vi = new VooInfo(key[0],key[1],valores[0],valores[1],
                                valores[2],valores[3],valores[4],valores[5],
                                valores[6],valores[7],valores[8]);
                        return vi;
                    })
                    .collect();
            return Response.ok(res).build();


        }
        catch(Exception e){
            System.out.println(e);
            return Response.status(Response.Status.NOT_FOUND).entity("Não existe informação disponível").build();
        }
    }

    @GET
    @Path("/infototais")
    public Response total(@QueryParam("voo") Optional<String> voo) throws IOException {

        if (!voo.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).entity("Não existe nenhum voo").build();
        }

        Configuration conf = HBaseConfiguration.create();
        String tableName = "trafego";

        System.setProperty("user.name", "hdfs");
        System.setProperty("HADOOP_USER_NAME", "hdfs");
        Get g = new Get(voo.get().getBytes());
        conf.set("hbase.zookeeper.quorum", Definicoes.ZKIP);
        conf.set("hbase.zookeeper.property.clientPort", Definicoes.ZKPort);


        System.out.println("Conectado!");
        try (Connection connection = ConnectionFactory.createConnection(conf)) {
            System.out.println("Connection feita");
            try (Table hTable = connection.getTable(TableName.valueOf(tableName))) {

                Result a = hTable.get(g);
                String[] colunas = {"DayOfWeek", "DepTime", "ArrTime", "UniqueCarrier", "TailNum", "AirTime", "Origin", "Dest", "Distance"};
                String[] valores = new String[colunas.length];
                System.out.println(a);
                String aux2 = new String(a.getRow());
                System.out.println(aux2);
                String[] key = aux2.split("::");
                for (int i = 0; i < colunas.length; i++) {

                    try {
                        String aux = new String(a.getValue("data".getBytes(), colunas[i].getBytes()));
                        valores[i] = aux;
                    } catch (Exception e) {
                        valores[i] = "null";
                    }
                }
                VooInfo res = new VooInfo(key[0], key[1], valores[0], valores[1],
                        valores[2], valores[3], valores[4], valores[5],
                        valores[6], valores[7], valores[8]);

                return Response.ok(res).build();


            }

            catch (Exception e){
                System.out.println("Vou terminar");
                return Response.status(Response.Status.NOT_FOUND).entity("Não existe nenhum voo").build();
            }
        }
    }

}

