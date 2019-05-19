package hello.representations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VooInfo {
    public String voo;
    public String dia_semana;
    public String data;

    @JsonCreator
    public VooInfo(@JsonProperty("voo") String distancia, @JsonProperty("dia_semana") String numero_aviao,
                          @JsonProperty("data") String data){
        this.voo = distancia;
        this.dia_semana = numero_aviao;
        this.data = data;
    }

    public String toString(){
        String res = "DistanciaAviao: {";
        res += "distancia: " + voo;
        res += "; numero_aviao: " + dia_semana + "}";

        return res;
    }
}
