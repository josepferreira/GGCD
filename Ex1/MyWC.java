import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Scanner;

public class MyWC{
    public static void main(String[] args) throws Exception{
        if(args.length != 1){
            System.out.println("1 argumento!");
            return;
        }
        
        long timeI = System.currentTimeMillis();
        FileReader fileReader = new FileReader(args[0]);
        HashMap<String,Integer> palavras = new HashMap<>();
        BufferedReader in = new BufferedReader(fileReader);
        String word;
        String s = in.readLine();
        while(s != null){
            Scanner sw = new Scanner(s);  // scan through the line
            while (sw.hasNext()) {
               word = sw.next(); // take a token off the line
               Integer a = palavras.get(word);
               if(a == null){
                   a = 0;
               }
               a++;
               palavras.put(word,a);
            }
            sw.close();                  //  end loop over words
          s = in.readLine();
        }
        in.close();
        System.out.println(palavras);
        long timeF = System.currentTimeMillis();
        System.out.println("Demorou: " + (timeF-timeI));
 
    }
}