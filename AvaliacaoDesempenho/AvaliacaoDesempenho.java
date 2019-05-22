import java.io.*;
import java.util.*;
import java.net.*;
import org.json.*;

class AvaliaDesempenho implements Runnable{
    public ArrayList<Long> medicoes = new ArrayList<>();
    private int numeroMedicoes;
    private String caminho;
    URL url;

    public AvaliaDesempenho(String d, int nm){
        caminho = "http://" + d;
        numeroMedicoes = nm;
        try{
            url = new URL(caminho);
        }catch(Exception e){
            System.out.println("Excecao na formacao de url");
        }
    }

    public void clear(){
        medicoes.clear();
    }

    public void run() {
        for(int i = 0; i < numeroMedicoes; i++){
            try{
                long timeInicial = System.currentTimeMillis();
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setInstanceFollowRedirects(false);
                con.setUseCaches(false);

                con.getResponseMessage();
                long time = System.currentTimeMillis() - timeInicial;
                medicoes.add(time);
            }
            catch(Exception e){
                System.out.println("Excepcao");
                i--;
            }
        }
    }
}

public class AvaliacaoDesempenho{
    public static void main(String[] args) throws Exception{
        JSONObject jo = new JSONObject();
        ArrayList<AvaliaDesempenho> clientes = new ArrayList<>();
        int nClientes = 10;
        int nTestes = 10;
        for(int i = 0; i < nClientes; i++){
            ArrayList<Thread> threads = new ArrayList<>();
            AvaliaDesempenho ad = new AvaliaDesempenho(args[0], nTestes);
            
            clientes.add(ad);
            for(AvaliaDesempenho adp: clientes){
                Thread t = new Thread(adp);
                threads.add(t);
            }

            for(Thread ta: threads){
                ta.start();
            }

            for(Thread ta: threads){
                ta.join();
            }
            
            ArrayList<Long> medicoesT = new ArrayList<>();
            for(AvaliaDesempenho c: clientes){
                medicoesT.addAll(c.medicoes);
                c.clear();
            }

            // System.out.println(medicoesT);
            jo.put(""+(i+1),medicoesT); 
	        System.out.println("Terminei: " + i);           
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]));
        writer.write(jo.toString());
     
        writer.close();

    }
}
