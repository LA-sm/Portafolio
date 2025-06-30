
package billsapppojos;

import java.io.Serializable;
import java.util.Date;

public class Grupo implements Serializable{
    
    private String nombre;
    private Date fechaCreacion;
    private Usuario usuarioCreador;
    private Integer grupoId;

    public Grupo() {
        
    }
    
    public Grupo(Integer grupoId) {
        this.grupoId = grupoId;
    }

    public Grupo(String nombre, Date fechaCreacion, Usuario usuarioCreador, Integer grupoId) {
        this.nombre = nombre;
        this.fechaCreacion = fechaCreacion;
        this.usuarioCreador = usuarioCreador;
        this.grupoId = grupoId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Usuario getUsuarioCreador() {
        return usuarioCreador;
    }

    public void setUsuarioCreador(Usuario usuarioCreador) {
        this.usuarioCreador = usuarioCreador;
    }

    public Integer getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(Integer grupoId) {
        this.grupoId = grupoId;
    }

    @Override
    public String toString() {
        return "Grupo{" + "nombre=" + nombre + ", fechaCreacion=" + fechaCreacion + ", usuarioCreador=" + usuarioCreador + ", grupoId=" + grupoId + '}';
    }
    
    
}
