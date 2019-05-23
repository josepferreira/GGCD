package hello.representations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DiaMes implements Comparable {
    public String dia_do_mes;
    public long numero_voos;
    @JsonCreator
    public DiaMes(@JsonProperty("dia_do_mes") String dia, @JsonProperty("numero_voos") long nvoos){
        this.dia_do_mes = dia;
        this.numero_voos = nvoos;
    }

    public int compareTo(Object o){

        if(o == null) return 0;

        DiaMes dm = (DiaMes) o;

        return Long.compare(dm.numero_voos, this.numero_voos);
    }

    public String toString(){
        String res = "Dias_do_Mes: {";
        res += "dia: " + dia_do_mes;
        res += "numero_voos: " + numero_voos;
        res += "}";

        return res;
    }
}