package hello.representations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DistanciaAviao {
    public long distancia;
    public String numero_aviao;

    @JsonCreator
    public DistanciaAviao(@JsonProperty("distancia") long distancia, @JsonProperty("numero_aviao") String numero_aviao){
        this.distancia = distancia;
        this.numero_aviao = numero_aviao;
    }

    public String toString(){
        String res = "DistanciaAviao: {";
        res += "distancia: " + distancia;
        res += "; numero_aviao: " + numero_aviao + "}";

        return res;
    }
}
