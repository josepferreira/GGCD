package hello.representations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AviaoInfo implements Comparable {
    public String TailNumber;
    public long numero_voos;
    @JsonCreator
    public AviaoInfo(@JsonProperty("TailNumber") String aviao, @JsonProperty("numero_voos") long nvoos){
        this.numero_voos = nvoos;
        this.TailNumber = aviao;
    }


    public int compareTo(Object o){

        if(o == null) return 0;

        AviaoInfo avinf = (AviaoInfo) o;

        return Long.compare(avinf.numero_voos,this.numero_voos);
    }


    public String toString(){
        String res = "TotalVoos: {";
        res += "numero_aviao: " + this.TailNumber;
        res += "; numero_voos: " + this.numero_voos + "}";

        return res;
    }
}
