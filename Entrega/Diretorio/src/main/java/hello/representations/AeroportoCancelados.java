package hello.representations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AeroportoCancelados implements Comparable {
    public long numero_voos_cancelados;
    public String aeroporto;
    @JsonCreator
    public AeroportoCancelados(@JsonProperty("numero_voos_cancelados") long atraso, @JsonProperty("aeroporto") String razao){
        this.numero_voos_cancelados = atraso;
        this.aeroporto = razao;
    }

    public int compareTo(Object o){

        if(o == null) return 0;

        AeroportoCancelados ds = (AeroportoCancelados) o;



        return Long.compare(ds.numero_voos_cancelados,this.numero_voos_cancelados);
    }

    public String toString(){
        String res = "Dias_da_Semana: {";
        res += "numero_voos: " + numero_voos_cancelados;
        res += "aeroporto: " + aeroporto;
        res += "}";

        return res;
    }
}
