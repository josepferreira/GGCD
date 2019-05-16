import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

public class MyTest {
    public final static byte[] CF1= Bytes.toBytes("ColFamily1");
    public final static byte[] CF2= Bytes.toBytes("ColFamily2");

    public static void main(String args[]) throws IOException {
        System.out.println("Cheguei");
        Configuration conf= HBaseConfiguration.create();
        Connection connection = ConnectionFactory.createConnection(conf);
        Admin admin= connection.getAdmin();

        System.out.println("ASDFSA");

        if(!admin.tableExists(TableName.valueOf("MyTestTable"))){
            System.out.println("Nao existe!");
            HTableDescriptor desc=new HTableDescriptor(TableName.valueOf("MyTestTable"));
            HColumnDescriptor columnData1=new HColumnDescriptor("ColFamily1");
            HColumnDescriptor columnData2=new HColumnDescriptor("ColFamily2");
            desc.addFamily(columnData1);
            desc.addFamily(columnData2);
            admin.createTable(desc);

        }
        else{
            System.out.println("Existe!");
        }

        System.out.println("Passei");

        Table table = connection.getTable(TableName.valueOf("MyTestTable"));
        Put p=new Put(Bytes.toBytes("row1)"));
        p.addColumn(CF1, Bytes.toBytes("col1"), Bytes.toBytes("c11val1)"));
        table.put(p);
        System.out.println("1st put");

        p=new Put(Bytes.toBytes("row1)"));
        p.addColumn(CF2, Bytes.toBytes("col2"), Bytes.toBytes("c12val1)"));
        table.put(p);
        System.out.println("2nd put");

        p=new Put(Bytes.toBytes("row1)"));
        p.addColumn(CF2, Bytes.toBytes("col2"), Bytes.toBytes("c22val1)"));
        table.put(p);
        System.out.println("3rd put");

        p=new Put(Bytes.toBytes("row2)"));
        p.addColumn(CF1, Bytes.toBytes("col1"), Bytes.toBytes("c11val2)"));
        table.put(p);
        System.out.println("4th put");


        System.out.println("------SCAN--------");
        Scan s = new Scan();
        ResultScanner rs = table.getScanner(s);

        while(rs.iterator().hasNext()){
            Result aux = rs.iterator().next();
            System.out.println(aux);
        }

       /* Get g=new Get(Bytes.toBytes("row1)"));
        Result r=table.get(g);
        if (!r.isEmpty())
            System.out.println(r);
        table.close();
        connection.close();*/
    }
}
