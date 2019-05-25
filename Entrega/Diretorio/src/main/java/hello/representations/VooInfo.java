package hello.representations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VooInfo {
    public String voo;
    public String data;
    public String DayOfWeek;
    public String DepTime;
    public String ArrivalTime;
    public String UniqueCarrier;
    public String TailNumber;
    public String AirTime;
    public String Orig;
    public String Dest;
    public String Distance;
    @JsonCreator
    public VooInfo(@JsonProperty("voo") String voo, @JsonProperty("data") String data,
                   @JsonProperty("dia_semana") String numero_aviao,@JsonProperty("deptime") String numero_aviao2,
                   @JsonProperty("arrivaltime") String numero_aviao3,@JsonProperty("uc") String numero_aviao4,
                   @JsonProperty("tn") String numero_aviao5,@JsonProperty("at") String numero_aviao6,
                   @JsonProperty("or") String numero_aviao7,@JsonProperty("dest") String numero_aviao8,
                   @JsonProperty("dist") String numero_aviao9){
        this.voo = voo;
        this.data = data;
        this.DayOfWeek = numero_aviao;
        this.DepTime = numero_aviao2;
        this.ArrivalTime = numero_aviao3;
        this.UniqueCarrier = numero_aviao4;
        this.TailNumber = numero_aviao5;
        this.AirTime = numero_aviao6;
        this.Orig = numero_aviao7;
        this.Dest = numero_aviao8;
        this.Distance = numero_aviao9;
    }

    public String toString(){
        String res = "DistanciaAviao: {";
        res += "distancia: " + voo;
        res += "; numero_aviao: " + data + "}";

        return res;
    }
}
