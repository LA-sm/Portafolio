package com.appcrud.comunicacion;

import billsapppojos.DistribucionGasto;
import billsapppojos.Gasto;
import billsapppojos.Grupo;
import billsapppojos.Participante;
import billsapppojos.Usuario;

import java.io.Serializable;

/**
 * Clase que representa una petición enviada por el cliente al servidor.
 * Implementa Serializable para poder enviarla por un stream de objetos (ObjectOutputStream).
 */
public class Peticion implements Serializable{

	// Enumeración que define todos los tipos de operación que puede solicitar el cliente
	public enum TipoOperacion{
		LOGIN,
		CREATE_USER,
		READ_USER,
		READ_ALL_USER,
		UPDATE_USER,
		DELETE_USER,
		CREATE_GRUPO,
		READ_GRUPO,
		READ_ALL_GRUPO,
		UPDATE_GRUPO,
		DELETE_GRUPO,
		CREATE_PARTICIPANTE,
		READ_PARTICIPANTE,
		READ_ALL_PARTICIPANTE,
		UPDATE_PARTICIPANTE,
		DELETE_PARTICIPANTE,
		CREATE_GASTO,
		READ_GASTO,
		READ_ALL_GASTO,
		UPDATE_GASTO,
		DELETE_GASTO,
		CREATE_DISTRIBUCION,
		READ_DISTRIBUCION,
		READ_ALL_DISTRIBUCION,
		UPDATE_DISTRIBUCION,
		DELETE_DISTRIBUCION,
		BUSCAR_GRUPOS_POR_USUARIO,
		BUSCAR_GASTOS_POR_GRUPO,
		BUSCAR_DISTRIBUCION_POR_GASTO,
		BUSCAR_PARTICIPANTE_POR_GRUPO,
		PING,  //Comprobar el estado del servidor
	}

	// === Campos que acompañan a la petición dependiendo del tipo de operación ===
	private TipoOperacion tipoOperacion;  // Tipo de operación solicitada

	private Usuario usuario;              // Información del usuario (para crear/actualizar/login)
	private Integer idUsuario;            // ID del usuario (para operaciones que lo necesiten)
	private Integer id;                   // ID genérico (puede usarse para grupos, gastos, etc.)
	private String email;                 // Email (para operaciones de usuario)
	private String alias;                 // Alias (nombre personalizado)
	private String password_plano;        // Contraseña en texto plano (para autenticación)

	private Grupo grupo;                  // Objeto Grupo (para crear/actualizar grupos)
	private Gasto gasto;                  // Objeto Gasto (para crear/actualizar gastos)
	private DistribucionGasto distribucionGasto;  // Distribución de gasto (para insertar/modificar)
	private Participante participante;    // Participante (para agregar o actualizar miembros)

	private Integer userIdCreador;        // ID del usuario creador (para crear grupos)
	private String nombre;                // Nombre (del grupo, por ejemplo)

	// === Constructores sobrecargados para diferentes combinaciones de parámetros ===
	public Peticion(TipoOperacion tipoOperacion, Usuario usuario, Integer idUsuario) {
		this.tipoOperacion = tipoOperacion;
		this.usuario = usuario;
		this.idUsuario = idUsuario;
	}

	public Peticion(TipoOperacion tipoOperacion, Usuario usuario) {
		this.tipoOperacion = tipoOperacion;
		this.usuario = usuario;
	}
	
	public Peticion(TipoOperacion tipoOperacion, Integer id) {
		this.tipoOperacion = tipoOperacion;
		this.id = id;
	}
	
	public Peticion(TipoOperacion tipoOperacion, String email) {
		this.tipoOperacion = tipoOperacion;
		this.email = email;
	}
	
	public Peticion(TipoOperacion tipoOperacion, String email, String password_plano) {
		this.tipoOperacion = tipoOperacion;
		this.email = email;
		this.password_plano = password_plano;
	}
	
	public Peticion(TipoOperacion tipoOperacion, Grupo grupo) {
		this.tipoOperacion = tipoOperacion;
		this.grupo = grupo;
	}
	
	public Peticion(TipoOperacion tipoOperacion, Integer userIdCreador, String nombre) {
		this.tipoOperacion = tipoOperacion;
		this.userIdCreador = userIdCreador;
		this.nombre = nombre;
	}
	
	public Peticion(TipoOperacion tipoOperacion, Gasto gasto) {
		this.tipoOperacion = tipoOperacion;
		this.gasto = gasto;
	}
	
	public Peticion(TipoOperacion tipoOperacion, DistribucionGasto distribucionGasto) {
		this.tipoOperacion = tipoOperacion;
		this.distribucionGasto = distribucionGasto;
	}
	
	public Peticion(TipoOperacion tipoOperacion, Participante participante) {
		this.tipoOperacion = tipoOperacion;
		this.participante = participante;
	}

	public Peticion(TipoOperacion tipoOperacion) {
		this.tipoOperacion = tipoOperacion;
	}


	// === Métodos getters y setters para acceder/modificar los atributos de la petición ===
	public TipoOperacion getTipoOperacion() {
		return tipoOperacion;
	}

	public void setTipoOperacion(TipoOperacion tipoOperacion) {
		this.tipoOperacion = tipoOperacion;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario Usuario) {
		this.usuario = usuario;
	}

	public Integer getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword_plano() {
		return password_plano;
	}

	public void setPassword_plano(String password_plano) {
		this.password_plano = password_plano;
	}

	public Grupo getGrupo() {
		return grupo;
	}

	public void setGrupo(Grupo grupo) {
		this.grupo = grupo;
	}

	public Gasto getGasto() {
		return gasto;
	}

	public void setGasto(Gasto gasto) {
		this.gasto = gasto;
	}

	public DistribucionGasto getDistribucionGasto() {
		return distribucionGasto;
	}

	public void setDistribucionGasto(DistribucionGasto distribucionGasto) {
		this.distribucionGasto = distribucionGasto;
	}

	public Participante getParticipante() {
		return participante;
	}

	public void setParticipante(Participante participante) {
		this.participante = participante;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	
	
	
}

