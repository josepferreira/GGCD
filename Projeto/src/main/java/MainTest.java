import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;

import java.io.IOException;

public class MainTest {
    public static void main(String[] args){
        Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", "172.19.0.2");
        config.set("hbase.zookeeper.property.clientPort", "2181");
        try {
            HBaseAdmin.available(config);
            System.out.println("Conectado!");
            try (Connection connection = ConnectionFactory.createConnection(config)) {
                System.out.println("Connection feita");
                try (Table t = connection.getTable(TableName.valueOf("AUX"))) {
                    System.out.println("Fazer scan");
                    Scan scan = new Scan();

                    ResultScanner scanner = t.getScanner(scan);
                    System.out.println("Fiz scan");
                    System.out.println(scanner);
                    for (Result result : scanner) {
                        System.out.println("Found row: " + result);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
