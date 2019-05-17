package dataproc.codelab;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.stream.Collectors;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import org.apache.hadoop.hbase.util.Bytes;



public class WordCount {
  public static String convert(Result r){
    for(Map.Entry<byte[], NavigableMap<byte[],byte[]>> a: r.getNoVersionMap().entrySet()){

        System.out.println(new String(a.getKey()));

        for(Map.Entry<byte[],byte[]> b: a.getValue().entrySet()){
          System.out.println(new String(b.getKey()));
          System.out.println(new String(b.getValue()));
          System.out.println("---");
        }
      System.out.println("------");
    }
    return "a";
  }
  public static void main(String[] args) {
    if (args.length != 2) {
      throw new IllegalArgumentException("Exactly 2 arguments are required: <inputUri> <outputUri>");
    }
    String inputPath = args[0];
    String outputPath = args[1];
    long timei = System.currentTimeMillis();
    SparkConf config = new SparkConf().setMaster("local[*]").setAppName("Word Count");
    JavaSparkContext sparkContext = new JavaSparkContext(config);

    Configuration conf = HBaseConfiguration.create();
    String tableName = "AUX";

    System.setProperty("user.name", "hdfs");
    System.setProperty("HADOOP_USER_NAME", "hdfs");
//    conf.set("hbase.master", "localhost:60000")
//    conf.setInt("timeout", 120000)
    conf.set("hbase.zookeeper.quorum", "172.19.0.3");
    conf.set("hbase.zookeeper.property.clientPort", "2181");
//    conf.set("zookeeper.znode.parent", "/hbase-unsecure")
    conf.set(TableInputFormat.INPUT_TABLE, tableName);

//    SparkContext javaSparkContext = new SparkContext(conf);
//    javaSparkContext.hadoopConfiguration().set("spark.hbase.host", "172.19.0.4");
//    javaSparkContext.hadoopConfiguration().set("spark.hbase.port", "2181");

//    SQLContext sqlContext = new SQLContext(sparkContext);
    try {
      System.out.println("Tentar");
      JavaPairRDD<ImmutableBytesWritable, Result> data =
              sparkContext.newAPIHadoopRDD(conf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);

      System.out.println(data.count());
      System.out.println("Vamos tenteettwafs");
      System.out.println(new String(data.keys().map(a -> a.get()).first()));
      System.out.println("asdfghhjgfdhgsafsdffghkfhdsa");
      System.out.println(data.values().map(a -> new String(a.getValue("data".getBytes(),"CRSArrTime".getBytes()))).collect());
      System.out.println("MERDAAAAAA");
      System.out.println(data.values().map(a -> a.toString()).collect());
      data.values().map(a -> convert(a)).collect();
      System.out.println("CENASFXGHURYESTDFXCVJFERYGDS");

      sparkContext.stop();
      System.out.println("Consegui");

//      String catalog = "{\n" +
//              "\t\"table\":{\"namespace\":\"default\", \"name\":\"AUX\"},\n" +
//              "    \"rowkey\":\"key\",\n" +
//              "    \"columns\":{\n" +
//              "\t    \"rowkey\":{\"cf\":\"rowkey\", \"col\":\"key\", \"type\":\"string\"},\n" +
//              "\t    \"data:CRSArrTime\":{\"cf\":\"data\", \"col\":\"CRSArrTime\", \"type\":\"string\"}\n" +
//              "    }\n" +
//              "}";
//
//      Map<String, String> optionsMap = new HashMap<>();
//
//      String htc = HBaseTableCatalog.tableCatalog();
//
//      optionsMap.put(htc, catalog);
// optionsMap.put(HBaseRelation.MIN_STAMP(), "123");
// optionsMap.put(HBaseRelation.MAX_STAMP(), "456");
      System.out.println("Antes de ler");
//      DataFrame dataset = sqlContext.read().options(optionsMap).format("org.apache.spark.sql.execution.datasources.hbase").load();

//      System.out.println("Count: " + dataset.count());
//      System.out.println("List: " + dataset.collect());
      JavaRDD<String> lines = sparkContext.textFile(inputPath);
      JavaRDD<String> words = lines.flatMap(
              (String line) -> Arrays.asList(line.split(" ")).iterator()
      );
      JavaPairRDD<String, Integer> wordCounts = words.mapToPair(
              (String word) -> new Tuple2<>(word, 1)
      ).reduceByKey(
              (Integer count1, Integer count2) -> count1 + count2
      );
//      wordCounts.saveAsTextFile(outputPath);
      long timef = System.currentTimeMillis();
      System.out.println("Demorou: " + (timef - timei));
      for (Tuple2<String, Integer> a : wordCounts.collect())
        System.out.println(a);
    }
    finally {
      System.out.println("Vou terminar");
      try {
        Thread.sleep(30000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      sparkContext.close();
    }
  }
}