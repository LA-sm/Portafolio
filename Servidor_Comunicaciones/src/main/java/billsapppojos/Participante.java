
package billsapppojos;

import java.io.Serializable;

public class Participante implements Serializable{
    
    private Grupo grupo;
    private String alias;
    private Integer participanteId;
    
    public Participante() {
    }

    public Participante(Integer participanteId) {
        this.participanteId = participanteId;
    }

    public Participante(Integer participanteId, Grupo grupo, String alias) {
        this.participanteId = participanteId;
        this.grupo = grupo;
        this.alias = alias;
    }

    public Integer getParticipanteId() {
        return participanteId;
    }

    public void setParticipanteId(Integer participanteId) {
        this.participanteId = participanteId;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }
    

    public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
    public String toString() {
        return "Participante{" + "participanteId=" + participanteId + ", grupo=" + grupo + ", alias=" + alias + '}';
    }
    
    
}
