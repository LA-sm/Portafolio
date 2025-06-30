
package billsapppojos;

import java.io.Serializable;

public class DistribucionGasto implements Serializable {
    
    private Double cantidad;
    private Participante participante;
    private Gasto gasto;
    private Integer DistribucionId;

    public DistribucionGasto() {
    }

    public DistribucionGasto(Integer DistribucionId) {
        this.DistribucionId = DistribucionId;
    }

    public DistribucionGasto(Double cantidad, Participante participante, Gasto gasto, Integer DistribucionId) {
        this.cantidad = cantidad;
        this.participante = participante;
        this.gasto = gasto;
        this.DistribucionId = DistribucionId;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public Participante getParticipante() {
        return participante;
    }

    public void setParticipante(Participante participante) {
        this.participante = participante;
    }

    public Gasto getGasto() {
        return gasto;
    }

    public void setGasto(Gasto gasto) {
        this.gasto = gasto;
    }

    public Integer getDistribucionId() {
        return DistribucionId;
    }

    public void setDistribucionId(Integer DistribucionId) {
        this.DistribucionId = DistribucionId;
    }

    @Override
    public String toString() {
        return "DistribucionGasto{" + "cantidad=" + cantidad + ", participante=" + participante + ", gasto=" + gasto + ", DistribucionId=" + DistribucionId + '}';
    }
    
    
}
