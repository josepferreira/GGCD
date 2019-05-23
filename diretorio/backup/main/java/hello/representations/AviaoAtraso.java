package hello.representations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AviaoAtraso {
    public long atraso;
    public String aviao;

    public long getAtraso() {
        return atraso;
    }

    @JsonCreator
    public AviaoAtraso(@JsonProperty("atraso") long atraso, @JsonProperty("aviao") String aviao){
        this.aviao = aviao;
        this.atraso = atraso;
    }

    public String toString(){
        String res = "Atrasos: {";
        res += "atraso: " + atraso;
        res += "razao: " + aviao;
        res += "}";

        return res;
    }
    public int compareTo(Object o){

        if(o == null) return 0;

        AviaoAtraso dm = (AviaoAtraso) o;
        return Long.compare(dm.atraso, this.atraso);
    }
}
