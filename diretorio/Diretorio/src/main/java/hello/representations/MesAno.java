package hello.representations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MesAno implements Comparable {
    public String mes_do_ano;
    public long numero_voos;
    @JsonCreator
    public MesAno(@JsonProperty("mes_do_ano") String dia, @JsonProperty("numero_voos") long nvoos){
        this.mes_do_ano = dia;
        this.numero_voos = nvoos;
    }

    public int compareTo(Object o){

        if(o == null) return 0;

        MesAno dm = (MesAno) o;

        return Long.compare(dm.numero_voos, this.numero_voos);
    }

    public String toString(){
        String res = "Dias_do_Mes: {";
        res += "dia: " + mes_do_ano;
        res += "numero_voos: " + numero_voos;
        res += "}";

        return res;
    }
}