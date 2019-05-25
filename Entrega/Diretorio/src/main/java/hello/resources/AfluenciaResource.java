package hello.resources;

import com.google.common.base.Optional;
import hello.health.Definicoes;
import hello.representations.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.*;
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
import java.util.stream.Collectors;

import static org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil.convertScanToString;


@Path("/afluencia")
@Produces(MediaType.APPLICATION_JSON)
public class AfluenciaResource {
    private final String template;
    private volatile String defaultName;
    private long counter;
    private JavaSparkContext sparkContext;

    public AfluenciaResource(String template, String defaultName, JavaSparkContext jsc) {
        this.template = template;
        this.defaultName = defaultName;
        sparkContext = jsc;
    }

    @GET
    @Path("/diaDaSemana")
    public Response diaSemana() throws IOException {
        Configuration conf = HBaseConfiguration.create();
        String tableName = "trafego";

        System.setProperty("user.name", "hdfs");
        System.setProperty("HADOOP_USER_NAME", "hdfs");
        Scan scan = new Scan();
        scan.addColumn("infogerais".getBytes(),"DayOfWeek".getBytes());
        conf.set("hbase.zookeeper.quorum", Definicoes.ZKIP);
        conf.set("hbase.zookeeper.property.clientPort", Definicoes.ZKPort);
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
            return Response.ok(res).build();


        }
        catch(Exception e){
            System.out.println(e);
            return Response.status(Response.Status.NOT_FOUND).entity("Não existe informação disponível").build();
        }
    }


    @GET
    @Path("/diaDoMes")
    public Response diaMes(@QueryParam("mes") Optional<String> mesEscolhido) throws IOException {

        Configuration conf = HBaseConfiguration.create();
        String tableName = "trafego";

        System.setProperty("user.name", "hdfs");
        System.setProperty("HADOOP_USER_NAME", "hdfs");
        Scan scan = new Scan();

        if(mesEscolhido.isPresent()) {
            System.out.println("Mes escolhido: " + mesEscolhido.get());
            Filter filter = new RowFilter(CompareFilter.CompareOp.EQUAL,
                    new RegexStringComparator("\\.*::.*/"+mesEscolhido.get()+"\\/.*"));
            scan.setFilter(filter);
        }
        conf.set("hbase.zookeeper.quorum", Definicoes.ZKIP);
        conf.set("hbase.zookeeper.property.clientPort", Definicoes.ZKPort);
        conf.set(TableInputFormat.INPUT_TABLE, tableName);
        conf.set(TableInputFormat.SCAN, convertScanToString(scan));

        try {
            JavaPairRDD<ImmutableBytesWritable, Result> data =
                    sparkContext.newAPIHadoopRDD(conf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);

            List<DiaMes> res = data.values()
                    .map(a -> {
                        String aux2 = new String(a.getRow());
                        String[] key = aux2.split("::");
                        String[] date = key[1].split("/");

                        return date[2];

                    })
                    .countByValue()
                    .entrySet()
                    .stream()
                    .map(a -> new DiaMes(a.getKey(),a.getValue()))
                    .sorted()
                    .collect(Collectors.toList());
            return Response.ok(res).build();


        }
        catch(Exception e){
            System.out.println(e);
            return Response.status(Response.Status.NOT_FOUND).entity("Não existe informação disponível").build();
        }
    }

    public boolean filtraA(String a, String b){
        System.out.println(a + b );
        return a.equals(b);
    }
    @GET
    @Path("/mesDoAno")
    public Response mesAno(@QueryParam("ano") Optional<String> ano) throws IOException {

        Configuration conf = HBaseConfiguration.create();
        String tableName = "trafego";

        System.setProperty("user.name", "hdfs");
        System.setProperty("HADOOP_USER_NAME", "hdfs");
        Scan scan = new Scan();

        if(ano.isPresent()) {
            System.out.println("Ano escolhido: " + ano.get());
            Filter filter = new RowFilter(CompareFilter.CompareOp.EQUAL,
                    new RegexStringComparator("\\.*::"+ano.get()+"\\/.*/.*"));
            scan.setFilter(filter);
        }
        conf.set("hbase.zookeeper.quorum", Definicoes.ZKIP);
        conf.set("hbase.zookeeper.property.clientPort", Definicoes.ZKPort);
        conf.set(TableInputFormat.INPUT_TABLE, tableName);
        conf.set(TableInputFormat.SCAN, convertScanToString(scan));

        try {
            JavaPairRDD<ImmutableBytesWritable, Result> data =
                    sparkContext.newAPIHadoopRDD(conf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);

            List<MesAno> res = data.values()
                    .map(a -> {
                        String aux2 = new String(a.getRow());
                        String[] key = aux2.split("::");
                        String[] date = key[1].split("/");

                        return date[1];

                    })
                    .countByValue()
                    .entrySet()
                    .stream()
                    .map(a -> new MesAno(a.getKey(),a.getValue()))
                    .sorted()
                    .collect(Collectors.toList());
            return Response.ok(res).build();


        }
        catch(Exception e){
            System.out.println(e);
            return Response.status(Response.Status.NOT_FOUND).entity("Não existe informação disponível").build();
        }
    }


    @GET
    @Path("/horaMovimentada")
    public Response horaDia(@QueryParam("aeroporto") Optional<String> aeroporto) throws IOException {

        Configuration conf = HBaseConfiguration.create();
        String tableName = "trafego";

        System.setProperty("user.name", "hdfs");
        System.setProperty("HADOOP_USER_NAME", "hdfs");
        Scan scan = new Scan();
        scan.addFamily("infogerais".getBytes());

        if(!aeroporto.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Falta passar o aeroporto").build();
        }

        System.out.println("Aeroporto escolhido: " + aeroporto.get());

        SingleColumnValueFilter origem = new SingleColumnValueFilter("infogerais".getBytes(), "Origin".getBytes(), CompareFilter.CompareOp.EQUAL, aeroporto.get().getBytes());
        SingleColumnValueFilter destino = new SingleColumnValueFilter("infogerais".getBytes(), "Dest".getBytes(), CompareFilter.CompareOp.EQUAL, aeroporto.get().getBytes());
        List<Filter> lista = new ArrayList<>();
        lista.add(origem);
        lista.add(destino);
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE,lista);
        scan.setFilter(filterList);
        conf.set("hbase.zookeeper.quorum", Definicoes.ZKIP);
        conf.set("hbase.zookeeper.property.clientPort", Definicoes.ZKPort);
        conf.set(TableInputFormat.INPUT_TABLE, tableName);
        conf.set(TableInputFormat.SCAN, convertScanToString(scan));

        try {
            JavaPairRDD<ImmutableBytesWritable, Result> data =
                    sparkContext.newAPIHadoopRDD(conf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);

            List<HorasAeroporto> res = data.values()
                    //.filter(b -> { return filtraA(aeroporto.get(),new String(b.getValue("infogerais".getBytes(), "Origin".getBytes())));})
                    .map(a -> {
                        String aux;
                        String or = new String(a.getValue("infogerais".getBytes(), "Origin".getBytes()));
                        if(or.equals(aeroporto.get()))
                            aux = new String(a.getValue("infogerais".getBytes(), "DepTime".getBytes()));
                        else
                            aux = new String(a.getValue("infogerais".getBytes(), "ArrTime".getBytes()));

                        try{

                            if(!aux.equals("")){ ;
                                return aux.substring(0, aux.length() - 4);
                            }
                            else return "No Info";
                        }
                        catch(Exception e){
                            return "No Info";
                        }

                    })
                    .countByValue()
                    .entrySet()
                    .stream()
                    .map(a -> new HorasAeroporto(a.getKey(),a.getValue()))
                    .sorted()
                    .collect(Collectors.toList());
            return Response.ok(res).build();


        }
        catch(Exception e){
            System.out.println(e);
            return Response.status(Response.Status.NOT_FOUND).entity("Não existe informação disponível").build();
        }
    }

}


