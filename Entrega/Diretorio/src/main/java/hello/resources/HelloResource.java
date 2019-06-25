package hello.resources;

import com.opencsv.CSVReader;
import hello.health.Definicoes;
import hello.representations.DistanciaAviao;

import hello.representations.VooInfo;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Array;
import scala.Tuple2;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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
    public Response sayHello() throws IOException {


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
                    .map(a -> new DistanciaAviao(a._2,a._1))
                    .sortBy(a -> a.distancia,false,2)
                    .take(30);

            System.out.println("CENASFXGHURYESTDFXCVJFERYGDS");

            System.out.println("Consegui");

            return Response.ok(res).build();


        }catch(Exception e){
            System.out.println(e);
            return Response.status(Response.Status.NOT_FOUND).entity("Não existe informação disponível").build();
        }
    }

    public Put processa(HashMap<String,String> entrada){
        HashMap<String, String> values = new HashMap<String, String>() {
            {
                put("LateAircraftDelay", "0");
                put("NASDelay", "0");
                put("WeatherDelay", "0");
                put("CarrierDelay", "0");
                put("SecurityDelay", "0");
            }
        };

        for(Map.Entry<String,String> aux : values.entrySet()){
            String valor = entrada.get(aux.getKey());
            if(valor == null || valor.equals("")){
                entrada.put(aux.getKey(),aux.getValue());
            }
        }

        String chave = entrada.get("FlightNum") + "::" + entrada.get("Year") + "/" + entrada.get("Month") + "/" + entrada.get("DayofMonth");


        HashMap<String,String> mapeam = new HashMap<String, String>(){
            {
                put("Dest","Dest");
                put("Cancelados","Cancelled");
                put("Desviados","Diverted");
                put("Aviao","TailNum");
                put("DelayChegada","ArrDelay");
                put("DelayPartida","DepDelay");
                put("Distancia","Distance");
                put("Weather","WeatherDelay");
                put("NAS","NASDelay");
                put("Carrier","CarrierDelay");
                put("Security","SecurityDelay");
                put("LateAircraft","LateAircraftDelay");
                put("DayOfWeek","DayOfWeek");
                put("DepTime","DepTime");
                put("ArrivalTime","ArrTime");
                put("UniqueCarrier","UniqueCarrier");
                put("TailNumber","TailNumber");
                put("AirTime","AirTime");
                put("Orig","Origin");
                put("Distance","Distance");
            }
        };

        entrada.remove("CancellationCode");
        entrada.remove("TaxiIn");
        entrada.remove("TaxiOut");
        entrada.remove("FlightNum");
        entrada.remove("Year");
        entrada.remove("Month");
        entrada.remove("DayofMonth");

        HashMap<String, ArrayList<String>> familias = new HashMap<String, ArrayList<String>>() {
            {
                put("Aeroportos" , new ArrayList<>(Arrays.asList("Dest","Cancelados","Desviados")));
                put("InfoAviao" , new ArrayList<>(Arrays.asList("Aviao","DelayChegada","DelayPartida","Distancia")));
                put("TipoAtrasos" ,new ArrayList<>(Arrays.asList("Weather","NAS","Carrier","Security","LateAircraft")));
                put("InfosGerais" ,new ArrayList<>(Arrays.asList("DayOfWeek","DepTime","ArrivalTime","UniqueCarrier","TailNumber","AirTime","Orig","Dest","Distance")));

            }
        };

        Put p = new Put(chave.getBytes());

        for(Map.Entry<String, ArrayList<String>> entry : familias.entrySet()){
            for(String a : entry.getValue()){
                String idC = mapeam.get(a);
                String val = entrada.get(idC);
                if(val == null){
                    val = "";
                }
                p.addColumn(entry.getKey().getBytes(),a.getBytes(),val.getBytes());
            }
        }

        return p;
    }

    public void insereValores(ArrayList<Put> puts){
        String tableName = "trafego";
        Configuration conf = HBaseConfiguration.create();

        System.setProperty("user.name", "hdfs");
        System.setProperty("HADOOP_USER_NAME", "hdfs");
        conf.set("hbase.zookeeper.quorum", Definicoes.ZKIP);
        conf.set("hbase.zookeeper.property.clientPort", Definicoes.ZKPort);


        System.out.println("Conectado!");
        try (Connection connection = ConnectionFactory.createConnection(conf)) {
            System.out.println("Connection feita");
            try (Table hTable = connection.getTable(TableName.valueOf(tableName))) {

                hTable.put(puts);
            }

            catch (Exception e){
                System.out.println("Vou terminar");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GET
    @Path("/insere")
    public Response coloca() {
        try {

            // Create an object of filereader
            // class with CSV file as a parameter.
            FileReader filereader = new FileReader("ola.csv");

            // create csvReader object passing
            // file reader as a parameter
            CSVReader csvReader = new CSVReader(filereader);
            String[] nextRecord;

            // we are going to read data line by line
            ArrayList<String> header = new ArrayList<>();
            if((nextRecord = csvReader.readNext()) != null){
                for (String cell : nextRecord) {
                    header.add(cell);
                }
            }

            System.out.println("HEADER: " + header);
            int headerSize = header.size();
            int tamanho = 0;
            ArrayList<Put> puts = new ArrayList<>();

            HashMap<String,String> aux = new HashMap<>();
            while ((nextRecord = csvReader.readNext()) != null) {
                int i = 0;
                for (String cell : nextRecord) {
                    aux.put(header.get(i++),cell);
                }

                puts.add(processa(aux));
                tamanho++;

                if(tamanho > 2000){
                    insereValores(puts);
                    puts.clear();
                    tamanho = 0;
                }
                aux.clear();
            }

            insereValores(puts);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.ok().build();
    }

}

