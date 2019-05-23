package hello.representations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class Atrasos {
    public float atraso;
    public String razao;
    @JsonCreator
    public Atrasos(@JsonProperty("atraso") float atraso, @JsonProperty("razao") String razao){
        this.atraso = atraso;
        this.razao = razao;
    }

    public String toString(){
        String res = "Atrasos: {";
        res += "atraso: " + atraso;
        res += "razao: " + razao;
        res += "}";

        return res;
    }
}
