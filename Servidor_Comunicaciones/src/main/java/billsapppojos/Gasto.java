
package billsapppojos;

import java.io.Serializable;
import java.util.Date;

public class Gasto implements Serializable{
    
    private Date fecha;
    private String descripcion;
    private Double importeTotal;
    private Participante participantePagador;
    private Integer gastoId;

    public Gasto() {
        
    }

    public Gasto(Integer gastoId) {
        this.gastoId = gastoId;
    }

    public Gasto(Date fecha, String descripcion, Double importeTotal, Participante participantePagador, Integer gastoId) {
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.importeTotal = importeTotal;
        this.participantePagador = participantePagador;
        this.gastoId = gastoId;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getImporteTotal() {
        return importeTotal;
    }

    public void setImporteTotal(Double importeTotal) {
        this.importeTotal = importeTotal;
    }

    public Participante getParticipantePagador() {
        return participantePagador;
    }

    public void setParticipantePagador(Participante participantePagador) {
        this.participantePagador = participantePagador;
    }

    public Integer getGastoId() {
        return gastoId;
    }

    public void setGastoId(Integer gastoId) {
        this.gastoId = gastoId;
    }

    @Override
    public String toString() {
        return "Gasto{" + "fecha=" + fecha + ", descripcion=" + descripcion + ", importeTotal=" + importeTotal + ", participantePagador=" + participantePagador + ", gastoId=" + gastoId + '}';
    }
    
    
}
