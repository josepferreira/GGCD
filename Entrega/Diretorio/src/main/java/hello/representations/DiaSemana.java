package hello.representations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class DiaSemana implements Comparable {
    public long numero_voos;
    public String dia;
    @JsonCreator
    public DiaSemana(@JsonProperty("numero_voos") long atraso, @JsonProperty("dia") String razao){
        this.numero_voos = atraso;
        this.dia = razao;
    }

    public int compareTo(Object o){

        if(o == null) return 0;

        DiaSemana ds = (DiaSemana) o;



        return Long.compare(ds.numero_voos,this.numero_voos);
    }

    public String toString(){
        String res = "Dias_da_Semana: {";
        res += "numero_voos: " + numero_voos;
        res += "dia: " + dia;
        res += "}";

        return res;
    }
}