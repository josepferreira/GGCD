import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

public class Test {
    public final static byte[] CF= Bytes.toBytes("Values");

    public static void main(String args[]) throws IOException {
        Configuration conf=HBaseConfiguration.create();
        Connection connection = ConnectionFactory.createConnection(conf);
        Admin admin= connection.getAdmin();

        HTableDescriptor desc=new HTableDescriptor(TableName.valueOf("Teste"));
        HColumnDescriptor columnData=new HColumnDescriptor("Values");
        desc.addFamily(columnData);
        admin.createTable(desc);

        Table table = connection.getTable(TableName.valueOf("mytable"));
        Put p=new Put(Bytes.toBytes("row1)"));
        p.addColumn(CF, Bytes.toBytes("col1"), Bytes.toBytes("value)"));
        table.put(p);
        Get g=new Get(Bytes.toBytes("row1)"));
        Result r=table.get(g);
        if (!r.isEmpty())
            System.out.println(r);
        table.close();
        connection.close();
    }
}
