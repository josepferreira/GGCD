package hello.representations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AeroportoDesviado implements Comparable {
    public long numero_voos_desviados;
    public String aeroporto;
    @JsonCreator
    public AeroportoDesviado(@JsonProperty("numero_voos_desviados") long atraso, @JsonProperty("aeroporto") String razao){
        this.numero_voos_desviados = atraso;
        this.aeroporto = razao;
    }

    public int compareTo(Object o){

        if(o == null) return 0;

        AeroportoDesviado ds = (AeroportoDesviado) o;



        return Long.compare(ds.numero_voos_desviados,this.numero_voos_desviados);
    }

    public String toString(){
        String res = "Dias_da_Semana: {";
        res += "numero_voos: " + numero_voos_desviados;
        res += "aeroporto: " + aeroporto;
        res += "}";

        return res;
    }
}
