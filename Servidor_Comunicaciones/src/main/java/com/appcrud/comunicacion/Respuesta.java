package com.appcrud.comunicacion;

import java.io.Serializable;
import java.util.List;

import billsapppojos.DistribucionGasto;
import billsapppojos.Gasto;
import billsapppojos.Grupo;
import billsapppojos.Participante;
import billsapppojos.Usuario;

public class Respuesta implements Serializable {

	// Indica si la operación fue exitosa o no
	private boolean exito;
	
	// Mensaje informativo, de error o confirmación
	private String mensaje;
	
	// Objetos individuales devueltos en la respuesta
	private Usuario usuario;
	private Grupo grupo;
	private Gasto gasto;
	
	// Listas de objetos devueltos en operaciones que retornan múltiples resultados
	private List<Usuario> usuarios;
	private List<Grupo> listaGrupos;
	private List<Gasto> listaGastos;
	private List<Participante> listaParticipantes;
	private List<DistribucionGasto> listaDistribucionGasto;
	
	// Número de registros afectados por la operación (útil para INSERT, UPDATE, DELETE)
	private Integer registrosAfectados;
	
	
	/**
	 * Constructor vacío necesario para la serialización/deserialización.
	 */
	public Respuesta() {
	}


	/**
	 * Constructor completo para inicializar todos los campos posibles de la respuesta.
	 *
	 * @param exito indica si la operación fue exitosa
	 * @param registrosAfectados número de registros modificados
	 * @param mensaje mensaje de respuesta (éxito o error)
	 * @param usuario objeto Usuario devuelto
	 * @param grupo objeto Grupo devuelto
	 * @param gasto objeto Gasto devuelto
	 * @param usuarios lista de usuarios
	 * @param listaGrupos lista de grupos
	 * @param listaGastos lista de gastos
	 * @param listaParticipantes lista de participantes
	 * @param listaDistribucionGasto lista de distribuciones de gasto
	 */
	public Respuesta(boolean exito, Integer registrosAfectados, String mensaje, Usuario usuario, Grupo grupo, Gasto gasto, List<Usuario> usuarios, 
			List<Grupo> listaGrupos, List<Gasto> listaGastos, List<Participante> listaParticipantes, List<DistribucionGasto> listaDistribucionGasto) {
		super();
		this.exito = exito;
		this.mensaje = mensaje;
		this.usuario = usuario;
		this.grupo = grupo;
		this.gasto = gasto;
		this.usuarios = usuarios;
		this.registrosAfectados = registrosAfectados;
		this.listaGrupos = listaGrupos;
		this.listaGastos = listaGastos;
		this.listaParticipantes = listaParticipantes;
		this.listaDistribucionGasto = listaDistribucionGasto;
	}


	// Getters y setters para cada campo de la clase.
	// Permiten acceder y modificar los valores de los atributos.
	public boolean isExito() {
		return exito;
	}


	public void setExito(boolean exito) {
		this.exito = exito;
	}


	public String getMensaje() {
		return mensaje;
	}


	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}


	public Usuario getUsuario() {
		return usuario;
	}


	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}


	public List<Usuario> getUsuarios() {
		return usuarios;
	}


	public void setUsuarios(List<Usuario> usuarios) {
		this.usuarios = usuarios;
	}


	public Integer getRegistrosAfectados() {
		return registrosAfectados;
	}


	public void setRegistrosAfectados(Integer registrosAfectados) {
		this.registrosAfectados = registrosAfectados;
	}


	public List<Grupo> getListaGrupos() {
		return listaGrupos;
	}


	public void setListaGrupos(List<Grupo> listaGrupos) {
		this.listaGrupos = listaGrupos;
	}


	public List<Gasto> getListaGastos() {
		return listaGastos;
	}


	public void setListaGastos(List<Gasto> listaGastos) {
		this.listaGastos = listaGastos;
	}


	public List<DistribucionGasto> getListaDistribucionGasto() {
		return listaDistribucionGasto;
	}


	public void setListaDistribucionGasto(List<DistribucionGasto> listaDistribucionGasto) {
		this.listaDistribucionGasto = listaDistribucionGasto;
	}


	public Grupo getGrupo() {
		return grupo;
	}


	public void setGrupo(Grupo grupo) {
		this.grupo = grupo;
	}


	public List<Participante> getListaParticipantes() {
		return listaParticipantes;
	}


	public void setListaParticipantes(List<Participante> listaParticipantes) {
		this.listaParticipantes = listaParticipantes;
	}


	public Gasto getGasto() {
		return gasto;
	}


	public void setGasto(Gasto gasto) {
		this.gasto = gasto;
	}
	
	
	
}
