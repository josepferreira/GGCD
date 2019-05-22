package hello.representations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AviaoAtraso {
    public long atraso;
    public String aviao;
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
}
