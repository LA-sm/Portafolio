
package billsapppojos;

import java.io.Serializable;

public class ExceptionBillsapp  extends Exception implements Serializable{
    
    private String mensajeErrorUsuario;
    private String mensajeErrorAdministrador;
    private String sentenciaSql;
    private Integer codigoError;

    public ExceptionBillsapp() {
    }

    public ExceptionBillsapp(String mensajeErrorUsuario, String mensajeErrorAdministrador, String sentenciaSql, Integer codigoError) {
        this.mensajeErrorUsuario = mensajeErrorUsuario;
        this.mensajeErrorAdministrador = mensajeErrorAdministrador;
        this.sentenciaSql = sentenciaSql;
        this.codigoError = codigoError;
    }

    public String getMensajeErrorUsuario() {
        return mensajeErrorUsuario;
    }

    public void setMensajeErrorUsuario(String mensajeErrorUsuario) {
        this.mensajeErrorUsuario = mensajeErrorUsuario;
    }

    public String getMensajeErrorAdministrador() {
        return mensajeErrorAdministrador;
    }

    public void setMensajeErrorAdministrador(String mensajeErrorAdministrador) {
        this.mensajeErrorAdministrador = mensajeErrorAdministrador;
    }

    public String getSentenciaSql() {
        return sentenciaSql;
    }

    public void setSentenciaSql(String sentenciaSql) {
        this.sentenciaSql = sentenciaSql;
    }

    public Integer getCodigoError() {
        return codigoError;
    }

    public void setCodigoError(Integer codigoError) {
        this.codigoError = codigoError;
    }

    @Override
    public String toString() {
        return "ExceptionBillsapp{" + "mensajeErrorUsuario=" + mensajeErrorUsuario + ", mensajeErrorAdministrador=" + mensajeErrorAdministrador + ", sentenciaSql=" + sentenciaSql + ", codigoError=" + codigoError + '}';
    } 
    
}
