package hello.representations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HorasAeroporto implements Comparable {
    public String horas;
    public long voos;
    @JsonCreator
    public HorasAeroporto(@JsonProperty("horas") String hora, @JsonProperty("ocorrencias") long nvezes){
        this.horas = hora;
        this.voos = nvezes;
    }


    public int compareTo(Object o){

        if(o == null) return 0;

        HorasAeroporto avinf = (HorasAeroporto) o;

        return Long.compare(avinf.voos,this.voos);
    }


    public String toString(){
        String res = "TotalVoos: {";
        res += "numero_aviao: " + this.horas;
        res += "; numero_voos: " + this.voos + "}";

        return res;
    }
}
