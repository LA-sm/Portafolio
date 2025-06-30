package com.appcrud.comunicacion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;

import org.mindrot.jbcrypt.BCrypt;

import billsapppojos.DistribucionGasto;
import billsapppojos.ExceptionBillsapp;
import billsapppojos.Gasto;
import billsapppojos.Grupo;
import billsapppojos.Participante;
import billsapppojos.Usuario;

public class CAD {

	
	private Connection conexion;

	/**
	 * Constructor vacío de la clase BillsappCad
	 * 
	 * @throws ExceptionBillsapp Se lanzará esta excepción cuando se produzcan
	 *                           fallos al cargar el jdbc de la base de datos.
	 */
	public CAD() throws ExceptionBillsapp {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			String cadenaConexionBD = "jdbc:oracle:thin:@192.168.43.200:1521:test";
			String usuarioBD = "billsapp";
			String contrasenaBD = "kk";
			this.conexion = DriverManager.getConnection(cadenaConexionBD, usuarioBD, contrasenaBD);
		} catch (ClassNotFoundException ex) {
			ExceptionBillsapp e = new ExceptionBillsapp();
			e.setMensajeErrorUsuario("Error general del sistema. Consulte con  el administrador");
			e.setMensajeErrorAdministrador(ex.getMessage());
			throw e;
		} catch (SQLException ex) {
			ExceptionBillsapp e = new ExceptionBillsapp();
			e.setMensajeErrorUsuario("Error general del sistema. Consulte con  el administrador");
			e.setMensajeErrorAdministrador(ex.getMessage());
			throw e;
		}
	}
	
	
	/**
	 * Cierra la conexión a la base de datos si está abierta.
	 * 
	 * @throws ExceptionBillsapp Si ocurre un error al intentar cerrar la conexión, 
	 *                           se lanza esta excepción con detalles del error.
	 */
	public void cerrarConexion() throws ExceptionBillsapp {
	    if (conexion != null) {
	        try {
	            conexion.close();
	        } catch (SQLException ex) {
	            ExceptionBillsapp e = new ExceptionBillsapp();
	            e.setMensajeErrorUsuario("Error al cerrar la conexión. Consulte con el administrador");
	            e.setMensajeErrorAdministrador(ex.getMessage());
	            throw e;
	        }
	    }
	}


	/**
	 * Método que recupera todos los grupos creados por un usuario específico.
	 * 
	 * @param idCreador Identificador del usuario creador de los grupos.
	 * @return Una lista de objetos Grupo correspondientes a los grupos creados por ese usuario.
	 * @throws ExceptionBillsapp Se lanza cuando ocurre un error de acceso a la base de datos.
	 *         Incluye información del código de error, el SQL ejecutado, el mensaje para el administrador
	 *         y un mensaje genérico para el usuario final.
	 */
	public ArrayList<Grupo> gruposPorUsuario(Integer idCreador) throws ExceptionBillsapp {

		// Lista donde se almacenarán los grupos recuperados
		ArrayList<Grupo> listaGrupos = new ArrayList<>();

		// Variables temporales para construir los objetos Grupo y Usuario
		Grupo grupo;
		Usuario usuario;

		// Consulta SQL para obtener los grupos creados por un usuario específico,
		// incluyendo datos del creador desde la tabla USUARIO
		String dql = "select * from GRUPO g, USUARIO u where g.USER_ID_CREADOR = u.USER_ID and USER_ID_CREADOR =" + idCreador;

		try {
			// Creación de un Statement para ejecutar la consulta
			Statement sentencia = conexion.createStatement();

			// Ejecución de la consulta y obtención del resultado
			ResultSet resultado = sentencia.executeQuery(dql);

			// Iteración sobre los resultados
			while (resultado.next()) {
				grupo = new Grupo(); // Instancia de Grupo

				// Asignación del ID del grupo, comprobando si es nulo
				int grupoId = resultado.getInt("GRUPO_ID");
				if (resultado.wasNull()) {
					grupo.setGrupoId(null);
				} else {
					grupo.setGrupoId(grupoId);
				}

				// Conversión de la fecha de creación a tipo java.util.Date
				grupo.setFechaCreacion(
					resultado.getDate("FECHA_CREACION") != null
						? new java.util.Date(resultado.getDate("FECHA_CREACION").getTime())
						: null
				);

				// Asignación del nombre del grupo
				grupo.setNombre(resultado.getString("NOMBRE"));

				// Construcción del objeto Usuario (creador del grupo)
				usuario = new Usuario();
				int userId = resultado.getInt("USER_ID_CREADOR");
				if (resultado.wasNull()) {
					usuario.setUserId(null);
				} else {
					usuario.setUserId(userId);
				}
				usuario.setAlias(resultado.getString("ALIAS"));
				usuario.setEmail(resultado.getString("EMAIL"));
				usuario.setPassword(resultado.getString("PASSWORD"));
				usuario.setTelefono(resultado.getString("TELEFONO"));

				// Asociar el usuario creador al grupo
				grupo.setUsuarioCreador(usuario);

				// Añadir el grupo completo a la lista de resultados
				listaGrupos.add(grupo);
			}

			// Cierre de recursos JDBC
			resultado.close();
			sentencia.close();

		} catch (SQLException ex) {
			// En caso de error, se lanza una excepción personalizada con detalles
			ExceptionBillsapp e = new ExceptionBillsapp();
			e.setCodigoError(ex.getErrorCode()); // Código de error SQL
			e.setSentenciaSql(dql);              // Sentencia SQL que causó el error
			e.setMensajeErrorAdministrador(ex.getMessage()); // Mensaje técnico
			e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR"); // Mensaje amigable
			throw e;
		}


		// Devuelve la lista de grupos creados por el usuario
		return listaGrupos;
	}

	
	/**
	 * Método que recupera todos los gastos asociados a un grupo específico.
	 * Para ello, se obtienen los gastos en los que el participante pagador
	 * pertenece al grupo indicado.
	 * 
	 * @param idGrupo Identificador del grupo del cual se quieren recuperar los gastos.
	 * @return Una lista de objetos Gasto correspondientes a los gastos del grupo.
	 * @throws ExceptionBillsapp Se lanza cuando ocurre un error de acceso a la base de datos,
	 *         incluyendo detalles como el código de error, la sentencia SQL ejecutada,
	 *         y mensajes para el administrador y para el usuario final.
	 */
	public ArrayList<Gasto> gastosPorGrupo(Integer idGrupo) throws ExceptionBillsapp {

		// Lista donde se almacenarán los gastos recuperados
		ArrayList<Gasto> listaGasto = new ArrayList<>();

		// Variables temporales para construir los objetos
		Gasto gasto;
		Participante participante;
		Grupo grupo;

		// Consulta SQL que une las tablas GASTO y PARTICIPANTE
		// para obtener los gastos en los que el participante pagador
		// pertenece al grupo indicado por idGrupo
		String dql = "select * from gasto g, participante p where g.participante_id_pagador = p.participante_id and p.grupo_id = " + idGrupo;

		try {
			// Creación del statement para ejecutar la consulta
			Statement sentencia = conexion.createStatement();

			// Ejecución de la consulta y obtención de resultados
			ResultSet resultado = sentencia.executeQuery(dql);

			// Procesamiento de cada fila del resultado
			while (resultado.next()) {
				gasto = new Gasto(); // Instancia de gasto

				// Asignación del ID del gasto, comprobando si es nulo
				int idGasto = resultado.getInt("GASTO_ID");
				if (resultado.wasNull()) {
					gasto.setGastoId(null);
				} else {
					gasto.setGastoId(idGasto);
				}

				// Conversión de la fecha del gasto a java.util.Date
				gasto.setFecha(
					resultado.getDate("FECHA") != null
						? new java.util.Date(resultado.getDate("FECHA").getTime())
						: null
				);

				// Descripción del gasto
				gasto.setDescripcion(resultado.getString("DESCRIPCION"));

				// Importe total del gasto
				Double importe = resultado.getDouble("IMPORTE_TOTAL");
				if (resultado.wasNull()) {
					gasto.setImporteTotal(null);
				} else {
					gasto.setImporteTotal(importe);
				}

				// Construcción del objeto Participante (pagador)
				participante = new Participante();
				int participanteId = resultado.getInt("PARTICIPANTE_ID_PAGADOR");
				if (resultado.wasNull()) {
					participante.setParticipanteId(null);
				} else {
					participante.setParticipanteId(participanteId);
				}

				// Construcción del objeto Grupo al que pertenece el participante
				grupo = new Grupo();
				int grupoId = resultado.getInt("GRUPO_ID");
				if (resultado.wasNull()) {
					grupo.setGrupoId(null);
				} else {
					grupo.setGrupoId(grupoId);
				}

				// Asignación del grupo al participante
				participante.setGrupo(grupo);

				// Alias del participante pagador
				participante.setAlias(resultado.getString("ALIAS"));

				// Asignación del participante al gasto
				gasto.setParticipantePagador(participante);

				// Agregar el gasto a la lista de resultados
				listaGasto.add(gasto);
			}

			// Cierre de recursos JDBC
			resultado.close();
			sentencia.close();

		} catch (SQLException ex) {
			// En caso de error, se lanza una excepción personalizada con detalles
			ExceptionBillsapp e = new ExceptionBillsapp();
			e.setCodigoError(ex.getErrorCode());                  // Código de error SQL
			e.setSentenciaSql(dql);                               // Sentencia SQL ejecutada
			e.setMensajeErrorAdministrador(ex.getMessage());      // Mensaje técnico para el administrador
			e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR"); // Mensaje amigable
			throw e;
		}

		// Devolver la lista de gastos del grupo
		return listaGasto;
	}

	
	
	/**
	 * Método que recupera todos los participantes asociados a un grupo específico.
	 * Para ello, se realiza una consulta que une las tablas PARTICIPANTE y GRUPO,
	 * filtrando por el identificador del grupo recibido como parámetro.
	 * 
	 * @param grupoId Identificador del grupo cuyos participantes se desean recuperar.
	 * @return Una lista de objetos Participante pertenecientes al grupo indicado.
	 * @throws ExceptionBillsapp Se lanza cuando ocurre un error en el acceso a la base de datos,
	 *         proporcionando detalles técnicos (código de error, sentencia SQL)
	 *         y mensajes amigables para el usuario.
	 */
	public ArrayList<Participante> participantePorGrupo(Integer grupoId) throws ExceptionBillsapp {

		// Lista que almacenará los participantes recuperados
		ArrayList<Participante> listaParticipantes = new ArrayList<>();

		// Variables auxiliares para construir objetos durante la iteración
		Participante participante;
		Grupo grupo;
		Usuario usuario;

		// Consulta SQL que une PARTICIPANTE y GRUPO mediante el campo GRUPO_ID
		// y filtra por el identificador de grupo recibido
		String dql = "select * from PARTICIPANTE p, GRUPO g where p.GRUPO_ID = g.GRUPO_ID and p.GRUPO_ID =" + grupoId;

		try {
			// Creación del statement para ejecutar la consulta
			Statement sentencia = conexion.createStatement();

			// Ejecución de la sentencia y almacenamiento de resultados
			ResultSet resultado = sentencia.executeQuery(dql);

			// Procesamiento de cada fila del resultado
			while (resultado.next()) {
				// Creación del objeto Participante
				participante = new Participante();

				// Asignación del ID del participante, controlando valores nulos
				int idParticipante = resultado.getInt("PARTICIPANTE_ID");
				if (resultado.wasNull()) {
					participante.setParticipanteId(null);
				} else {
					participante.setParticipanteId(idParticipante);
				}

				// Construcción del objeto Grupo al que pertenece el participante
				grupo = new Grupo();
				int idGrupo = resultado.getInt("GRUPO_ID");
				if (resultado.wasNull()) {
					grupo.setGrupoId(null);
				} else {
					grupo.setGrupoId(idGrupo);
				}

				// Conversión de la fecha de creación a java.util.Date si existe
				grupo.setFechaCreacion(
					resultado.getDate("FECHA_CREACION") != null
						? new java.util.Date(resultado.getDate("FECHA_CREACION").getTime())
						: null
				);

				// Asignación del nombre del grupo
				grupo.setNombre(resultado.getString("NOMBRE"));

				// Asignación del usuario creador del grupo (solo con su ID)
				int userCreador = resultado.getInt("USER_ID_CREADOR");
				if (resultado.wasNull()) {
					grupo.setUsuarioCreador(null);
				} else {
					grupo.setUsuarioCreador(new Usuario(userCreador));
				}

				// Asignación del grupo al participante
				participante.setGrupo(grupo);

				// Asignación del alias del participante
				participante.setAlias(resultado.getString("ALIAS"));

				// Agregar el participante a la lista
				listaParticipantes.add(participante);
			}

			// Cierre de recursos JDBC
			resultado.close();
			sentencia.close();

		} catch (SQLException ex) {
			// En caso de error, se encapsula la excepción SQL en ExceptionBillsapp
			ExceptionBillsapp e = new ExceptionBillsapp();
			e.setCodigoError(ex.getErrorCode());                  // Código del error SQL
			e.setSentenciaSql(dql);                               // Sentencia ejecutada
			e.setMensajeErrorAdministrador(ex.getMessage());      // Mensaje técnico
			e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR"); // Mensaje genérico
			throw e;
		}

		// Devolución de la lista de participantes del grupo
		return listaParticipantes;
	}

	
	/**
	 * Método que recupera la distribución de un gasto específico.
	 * Para ello, se realiza una consulta que une las tablas DISTRIBUCION_GASTO, GASTO y PARTICIPANTE,
	 * filtrando por el identificador del gasto recibido.
	 * 
	 * @param idGasto Identificador del gasto del cual se quiere obtener la distribución.
	 * @return Una lista de objetos DistribucionGasto que representan cómo se distribuye el gasto entre participantes.
	 * @throws ExceptionBillsapp Se lanza cuando ocurre un error en el acceso a la base de datos,
	 *         proporcionando detalles técnicos (código de error, sentencia SQL)
	 *         y mensajes amigables para el usuario.
	 */
	public ArrayList<DistribucionGasto> distribucionPorGasto(Integer idGasto) throws ExceptionBillsapp {
	    
	    // Lista para almacenar las distribuciones recuperadas
	    ArrayList<DistribucionGasto> listaDistribucion = new ArrayList<>();
	    
	    // Variables auxiliares para crear objetos mientras se procesa el resultado
	    DistribucionGasto distribucuinG;
	    Participante participanteDist;
	    Grupo grupo;
	    Gasto gasto;
	    Participante participantePag;
	    
	    // Consulta SQL que une DISTRIBUCION_GASTO, GASTO y PARTICIPANTE
	    // para obtener la distribución de un gasto especificado por idGasto
	    String dql = "select * from DISTRIBUCION_GASTO d, GASTO g, PARTICIPANTE p " +
	                 "where d.GASTO_ID = g.GASTO_ID AND d.PARTICIPANTE_ID = p.PARTICIPANTE_ID AND d.GASTO_ID = " + idGasto;
	    
	    try {
	        // Creación del statement para ejecutar la consulta
	        Statement sentencia = conexion.createStatement();
	        
	        // Ejecución de la consulta y obtención de resultados
	        ResultSet resultado = sentencia.executeQuery(dql);
	        
	        // Procesamiento de cada fila del resultado
	        while (resultado.next()) {
	            
	            // Construcción del objeto Gasto
	            gasto = new Gasto();
	            int gastoId = resultado.getInt("GASTO_ID");
	            if (resultado.wasNull()) {
	                gasto.setGastoId(null);
	            } else {
	                gasto.setGastoId(gastoId);
	            }
	            gasto.setFecha(resultado.getDate("FECHA") != null
	                          ? new java.util.Date(resultado.getDate("FECHA").getTime())
	                          : null);
	            gasto.setDescripcion(resultado.getString("DESCRIPCION"));
	            Double importe = resultado.getDouble("IMPORTE_TOTAL");
	            if (resultado.wasNull()) {
	                gasto.setImporteTotal(null);
	            } else {
	                gasto.setImporteTotal(importe);
	            }
	            
	            // Construcción del participante pagador del gasto
	            participantePag = new Participante();
	            int participantePagador = resultado.getInt("PARTICIPANTE_ID_PAGADOR");
	            if (resultado.wasNull()) {
	                participantePag.setParticipanteId(null);
	            } else {
	                participantePag.setParticipanteId(participantePagador);
	            }
	            gasto.setParticipantePagador(participantePag);
	            
	            // Creación del objeto DistribucionGasto y asignación del gasto
	            distribucuinG = new DistribucionGasto();
	            distribucuinG.setGasto(gasto);
	            
	            // Construcción del participante asociado a la distribución
	            participanteDist = new Participante();
	            int participanteId = resultado.getInt("PARTICIPANTE_ID");
	            if (resultado.wasNull()) {
	                participanteDist.setParticipanteId(null);
	            } else {
	                participanteDist.setParticipanteId(participanteId);
	            }
	            
	            // Construcción del grupo al que pertenece el participante
	            grupo = new Grupo();
	            int grupoId = resultado.getInt("GRUPO_ID");
	            if (resultado.wasNull()) {
	                grupo.setGrupoId(null);
	            } else {
	                grupo.setGrupoId(grupoId);
	            }
	            participanteDist.setGrupo(grupo);
	            
	            // Asignación del alias del participante
	            participanteDist.setAlias(resultado.getString("ALIAS"));
	            
	            // Asignación del participante a la distribución
	            distribucuinG.setParticipante(participanteDist);
	            
	            // Asignación del identificador de la distribución
	            int idDistribucion = resultado.getInt("DISTRIBUCION_ID");
	            if (resultado.wasNull()) {
	                distribucuinG.setDistribucionId(null);
	            } else {
	                distribucuinG.setDistribucionId(idDistribucion);
	            }
	            
	            // Asignación de la cantidad debida en la distribución
	            Double cantDebida = resultado.getDouble("CANTIDAD_DEBIDA");
	            if (resultado.wasNull()) {
	                distribucuinG.setCantidad(null);
	            } else {
	                distribucuinG.setCantidad(cantDebida);
	            }
	            
	            // Agregar la distribución a la lista
	            listaDistribucion.add(distribucuinG);
	        }
	        
	        // Cierre de recursos JDBC
	        resultado.close();
	        sentencia.close();
	        
	    } catch (SQLException ex) {
	        // En caso de error, encapsulamos la excepción SQL en ExceptionBillsapp
	        ExceptionBillsapp e = new ExceptionBillsapp();
	        e.setCodigoError(ex.getErrorCode());                  // Código de error SQL
	        e.setSentenciaSql(dql);                               // Sentencia SQL ejecutada
	        e.setMensajeErrorAdministrador(ex.getMessage());     // Mensaje técnico para el administrador
	        e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR"); // Mensaje amigable para el usuario
	        throw e;
	    }
	    
	    // Retorna la lista con la distribución de gastos para el gasto especificado
	    return listaDistribucion;
	}

	
	/**
	 * Verifica si existe un usuario con las credenciales dadas (email y contraseña en texto plano).
	 * Para ello, obtiene el hash almacenado de la contraseña asociado al email y compara
	 * la contraseña proporcionada utilizando la función BCrypt.
	 * 
	 * @param email El correo electrónico del usuario que intenta iniciar sesión.
	 * @param passwordPlano La contraseña en texto plano proporcionada por el usuario.
	 * @return true si las credenciales son correctas y coinciden con el hash almacenado;
	 *         false si no existe el usuario o la contraseña no coincide.
	 */
	public boolean login(String email, String passwordPlano) throws ExceptionBillsapp {
		
		// Consulta SQL para obtener el hash de la contraseña del usuario con el email dado
        String sql = "select PASSWORD from USUARIO where EMAIL=?";
		
	    try {
	        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
	            ps.setString(1, email);
	            ResultSet rs = ps.executeQuery();
	            
	            if (rs.next()) {
	                String storedHash = rs.getString("PASSWORD");
	                // Comparar la contraseña en texto plano con el hash almacenado usando BCrypt
	                return BCrypt.checkpw(passwordPlano, storedHash);
	            }
	        }
	        // Si no se encontró el usuario con ese email, retorna false
	        return false;
	    } catch (SQLException ex) {
	    	// En caso de error, encapsulamos la excepción SQL en ExceptionBillsapp
	        ExceptionBillsapp e = new ExceptionBillsapp();
	        e.setCodigoError(ex.getErrorCode());                  // Código de error SQL
	        e.setSentenciaSql(sql);                               // Sentencia SQL ejecutada
	        e.setMensajeErrorAdministrador(ex.getMessage());     // Mensaje técnico para el administrador
	        e.setMensajeErrorUsuario("ERROR EN LOGIN"); // Mensaje amigable para el usuario
	        return false;
	    }
	}

	
	/**
	 * Método que implementa la inserción de un nuevo usuario
	 * 
	 * @param usuario Objeto de la clase Usuario que almacena los nuevos valores del
	 *                usuario a insertar. El identificador de usuario se asignará de
	 *                forma autoincremental
	 * @return Cantidad de registros usuario insertados. Valores posibles: 1 (se ha
	 *         insertado un usuario)
	 * @throws ExceptionBillsapp Se lanzará esta excepción cuando se produzca una
	 *                           violación de las constraint de la base de datos o
	 *                           por fallos internos en la ejecución del método o de
	 *                           la base de datos.
	 */
	public Integer insertarUsuario(Usuario usuario) throws ExceptionBillsapp {

		String hashed = BCrypt.hashpw(usuario.getPassword(), BCrypt.gensalt());
		String dml = "insert into USUARIO(USER_ID, ALIAS, EMAIL, PASSWORD, TELEFONO) values(USUARIO_SEQ.NEXTVAL, ?, ?, ?, ?)";
		Integer registrosAfectados = 0;
		try {

			PreparedStatement sentenciaPreparada = conexion.prepareStatement(dml);

			sentenciaPreparada.setString(1, usuario.getAlias());
			sentenciaPreparada.setString(2, usuario.getEmail());
			sentenciaPreparada.setString(3, hashed);
			sentenciaPreparada.setString(4, usuario.getTelefono());
			registrosAfectados = sentenciaPreparada.executeUpdate();

			sentenciaPreparada.close();

		} catch (SQLException ex) {
			ExceptionBillsapp e = new ExceptionBillsapp();
			e.setCodigoError(ex.getErrorCode());
			e.setSentenciaSql(dml);
			e.setMensajeErrorAdministrador(ex.getMessage());
			switch (ex.getErrorCode()) {
			case 1400:
				e.setMensajeErrorUsuario("ALIAS, EMAIL, CONTRASEÑA Y TELÉFONO SON CAMPOS OBLIGATORIOS");
				break;
			case 2290:
				e.setMensajeErrorUsuario(
						"ERROR EN EL FORMATO DEL EMAIL O DEL ALIAS. EL ALIAS DEBE COMENZAR CON UNA LETRA, VERIFÍQUELO");
				break;
			case 1:
				e.setMensajeErrorUsuario(
						"EL ALIAS, EMAIL O EL TELÉFONO DEL USUARIO YA EXISTEN. ESTOS CAMPOS DEBEN SER ÚNICOS");
				break;
			default:
				e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");
			}
			throw e;
		}

		return registrosAfectados;
	}
	
	/**
	 * Método que implementa la eliminación de un usuario
	 * 
	 * @param usuarioId Identificador del usuario que se quiere eliminar.
	 * @return Cantidad de registros usuario eliminados. Valores posibles: 0 (no se
	 *         ha eliminado ningún usuario) o 1 (se ha eliminado un usuario)
	 * @throws ExceptionBillsapp Se lanzará esta excepción cuando se produzca una
	 *                           violación de las constraint de la base de datos o
	 *                           por fallos internos en la ejecución del método o de
	 *                           la base de datos.
	 */
	public Integer eliminarUsuario(Integer usuarioId) throws ExceptionBillsapp {

		String dml = "Delete from USUARIO where USER_ID = " + usuarioId;
		int registrosAfectados;
		try {
			Statement sentencia = conexion.createStatement();

			registrosAfectados = sentencia.executeUpdate(dml);

			sentencia.close();

		} catch (SQLException ex) {
			ExceptionBillsapp e = new ExceptionBillsapp();
			e.setCodigoError(ex.getErrorCode());
			e.setSentenciaSql(dml);
			e.setMensajeErrorAdministrador(ex.getMessage());
			switch (ex.getErrorCode()) {
			case 2292:
				e.setMensajeErrorUsuario(
						"NO SE PUEDE ELIMINAR ESTE USUARIO PORQUE TIENE GRUPOS O PARTICIPANTES ASOCIADOS");
				break;
			default:
				e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");
			}
			throw e;
		}

		return registrosAfectados;
	}

	/**
	 * Método que implementa la actualización de un usuario
	 * 
	 * @param usuarioId Identificador del usuario que se quiere modificar
	 * @param usuario   Objeto de la clase Usuario que almacena los nuevos valores a
	 *                  asignar al usuario. No actualiza el identificador del
	 *                  usuario
	 * @return Cantidad de registros usuario actualizados. Valores posibles: 0 (no
	 *         se ha actualizado ningún usuario) o 1 (se ha actualizado un usuario)
	 * @throws ExceptionBillsapp Se lanzará esta excepción cuando se produzca una
	 *                           violación de las constraint de la base de datos o
	 *                           por fallos internos en la ejecución del método o de
	 *                           la base de datos.
	 */
	public Integer actualizarUsuario(Integer usuarioId, Usuario usuario) throws ExceptionBillsapp {

		String dml = "update USUARIO set ALIAS=?, TELEFONO=? where USER_ID=?";
		Integer registrosAfectados = 0;
		try {

			PreparedStatement sentenciaPreparada = conexion.prepareStatement(dml);

			sentenciaPreparada.setString(1, usuario.getAlias());
			sentenciaPreparada.setString(2, usuario.getTelefono());
			sentenciaPreparada.setObject(3, usuarioId, Types.INTEGER);
			registrosAfectados = sentenciaPreparada.executeUpdate();

			sentenciaPreparada.close();

		} catch (SQLException ex) {
			ExceptionBillsapp e = new ExceptionBillsapp();
			e.setCodigoError(ex.getErrorCode());
			e.setSentenciaSql(dml);
			e.setMensajeErrorAdministrador(ex.getMessage());
			switch (ex.getErrorCode()) {
			case 1407:
				e.setMensajeErrorUsuario("ALIAS, EMAIL, CONTRASEÑA Y TELÉFONO SON CAMPOS OBLIGATORIOS");
				break;
			case 2290:
				e.setMensajeErrorUsuario(
						"ERROR EN EL FORMATO DEL EMAIL O DEL ALIAS. EL ALIAS DEBE COMENZAR CON UNA LETRA, VREIFÍQUELO");
				break;
			case 1:
				e.setMensajeErrorUsuario(
						"EL ALIAS, EMAIL O EL TELÉFONO DEL USUARIO YA EXISTEN. ESTOS CAMPOS DEBEN SER ÚNICOS");
				break;
			default:
				e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");
			}
			throw e;
		}

		return registrosAfectados;
	}

	/**
	 * Método que implementa la lectura de un usuario
	 * 
	 * @param usuarioId Identificador del usuario que se quiere leer
	 * @return Objeto de la clase Usuario que se quiere leer
	 * @throws ExceptionBillsapp Se lanzará esta excepción cuando se produzca una
	 *                           violación de las constraint de la base de datos o
	 *                           por fallos internos en la ejecución del método o de
	 *                           la base de datos.
	 */
	public Usuario leerUsuario(String email) throws ExceptionBillsapp {

		Usuario usuario = new Usuario();
		String dql = "select * from USUARIO where EMAIL= '" + email + "'";
		try {
			Statement sentencia = conexion.createStatement();
			ResultSet resultado = sentencia.executeQuery(dql);

			if (resultado.next()) {
				int userId = resultado.getInt("USER_ID");
				if (resultado.wasNull()) {
					usuario.setUserId(null);
				} else {
					usuario.setUserId(userId);
				}

				usuario.setAlias(resultado.getString("ALIAS"));
				usuario.setEmail(resultado.getString("EMAIL"));
				usuario.setPassword(resultado.getString("PASSWORD"));
				usuario.setTelefono(resultado.getString("TELEFONO"));
			}

			resultado.close();
			sentencia.close();

		} catch (SQLException ex) {
			ExceptionBillsapp e = new ExceptionBillsapp();
			e.setCodigoError(ex.getErrorCode());
			e.setSentenciaSql(dql);
			e.setMensajeErrorAdministrador(ex.getMessage());
			e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");

			throw e;
		}

		return usuario;
	}

	/**
	 * Método que implementa la lectura de todos los usuarios
	 * 
	 * @return ArrayList de tipo Usuario con todos los usuarios a leer
	 * @throws ExceptionBillsapp Se lanzará esta excepción cuando se produzca una
	 *                           violación de las constraint de la base de datos o
	 *                           por fallos internos en la ejecución del método o de
	 *                           la base de datos.
	 */
	public ArrayList<Usuario> leerUsuarios() throws ExceptionBillsapp {
		ArrayList<Usuario> listaUsuarios = new ArrayList();
		Usuario usuario;
		String dql = "select * from USUARIO";
		try {
			Statement sentencia = conexion.createStatement();
			ResultSet resultado = sentencia.executeQuery(dql);

			while (resultado.next()) {
				usuario = new Usuario();
				int userId = resultado.getInt("USER_ID");
				if (resultado.wasNull()) {
					usuario.setUserId(null);
				} else {
					usuario.setUserId(userId);
				}
				usuario.setAlias(resultado.getString("ALIAS"));
				usuario.setEmail(resultado.getString("EMAIL"));
				usuario.setPassword(resultado.getString("PASSWORD"));
				usuario.setTelefono(resultado.getString("TELEFONO"));

				listaUsuarios.add(usuario);
			}

			resultado.close();
			sentencia.close();

		} catch (SQLException ex) {
			ExceptionBillsapp e = new ExceptionBillsapp();
			e.setCodigoError(ex.getErrorCode());
			e.setSentenciaSql(dql);
			e.setMensajeErrorAdministrador(ex.getMessage());
			e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");

			throw e;
		}

		return listaUsuarios;
	}

	/**
	 * Método que implementa la inserción de un nuevo grupo
	 * 
	 * @param grupo Objeto de la clase Grupo que almacena los nuevos valores del
	 *              grupo a insertar. El identificador de grupo se asignará de forma
	 *              autoincremental
	 * @return Cantidad de registros grupo insertados. Valores posibles: 1 (se ha
	 *         insertado un grupo)
	 * @throws ExceptionBillsapp Se lanzará esta excepción cuando se produzca una
	 *                           violación de las constraint de la base de datos o
	 *                           por fallos internos en la ejecución del método o de
	 *                           la base de datos.
	 */
	public Integer insertarGrupo(Grupo grupo) throws ExceptionBillsapp {

		String dml = "insert into GRUPO(GRUPO_ID, FECHA_CREACION, NOMBRE, USER_ID_CREADOR) values(GRUPO_SEQ.NEXTVAL, ?, ?, ?)";
		Integer registrosAfectados = 0;
		try {

			PreparedStatement sentenciaPreparada = conexion.prepareStatement(dml);

			java.sql.Date fechaSql = new java.sql.Date(grupo.getFechaCreacion().getTime());

			sentenciaPreparada.setDate(1, fechaSql);
			sentenciaPreparada.setString(2, grupo.getNombre());
			sentenciaPreparada.setObject(3, grupo.getUsuarioCreador().getUserId(), Types.INTEGER);
			registrosAfectados = sentenciaPreparada.executeUpdate();

			sentenciaPreparada.close();

		} catch (SQLException ex) {
			ExceptionBillsapp e = new ExceptionBillsapp();
			e.setCodigoError(ex.getErrorCode());
			e.setSentenciaSql(dml);
			e.setMensajeErrorAdministrador(ex.getMessage());
			switch (ex.getErrorCode()) {
			case 1400:
				e.setMensajeErrorUsuario(
						"FECHA DE CREACION, NOMBRE Y EL USUARIO CREADOR DEL GRUPO SON CAMPOS OBLIGATORIOS");
				break;
			case 2290:
				e.setMensajeErrorUsuario("EL NOMBRE DEBE COMENZAR CON UNA LETRA, VERIFÍQUELO");
				break;
			case 2291:
				e.setMensajeErrorUsuario("EL USUARIO INSERTADO NO EXISTE");
				break;
			case 1:
				e.setMensajeErrorUsuario("UN USUARIO NO PUEDE CREAR DOS GRUPOS CON EL MISMO NOMBRE");
				break;
			default:
				e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");
			}
			throw e;
		}

		return registrosAfectados;
	}

	/**
	 * Método que implementa la eliminación de un grupo
	 * 
	 * @param grupoId Identificador del grupo que se quiere eliminar.
	 * @return Cantidad de registros grupo eliminados. Valores posibles: 0 (no se ha
	 *         eliminado ningún grupo) o 1 (se ha eliminado un grupo)
	 * @throws ExceptionBillsapp Se lanzará esta excepción cuando se produzca una
	 *                           violación de las constraint de la base de datos o
	 *                           por fallos internos en la ejecución del método o de
	 *                           la base de datos.
	 */
	public Integer eliminarGrupo(Integer grupoId) throws ExceptionBillsapp {

		String dml = "Delete from GRUPO where GRUPO_ID = " + grupoId;
		int registrosAfectados;
		try {
			Statement sentencia = conexion.createStatement();

			registrosAfectados = sentencia.executeUpdate(dml);

			sentencia.close();

		} catch (SQLException ex) {
			ExceptionBillsapp e = new ExceptionBillsapp();
			e.setCodigoError(ex.getErrorCode());
			e.setSentenciaSql(dml);
			e.setMensajeErrorAdministrador(ex.getMessage());
			switch (ex.getErrorCode()) {
			case 2292:
				e.setMensajeErrorUsuario("NO SE PUEDE ELIMINAR ESTE GRUPO PORQUE TIENE PATICIPANTES ASOCIADOS");
				break;
			default:
				e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");
			}
			throw e;
		}

		return registrosAfectados;
	}

	/**
	 * Método que implementa la actualización de un grupo
	 * 
	 * @param grupoId Identificador del grupo que se quiere modificar
	 * @param grupo   Objeto de la clase Grupo que almacena los nuevos valores a
	 *                asignar al grupo. No actualiza el identificador del grupo
	 * @return Cantidad de registros grupo actualizados. Valores posibles: 0 (no se
	 *         ha actualizado ningún grupo) o 1 (se ha actualizado un grupo)
	 * @throws ExceptionBillsapp Se lanzará esta excepción cuando se produzca una
	 *                           violación de las constraint de la base de datos o
	 *                           por fallos internos en la ejecución del método o de
	 *                           la base de datos.
	 */
	public Integer actualizarGrupo(Integer grupoId, Grupo grupo) throws ExceptionBillsapp {

		String dml = "update GRUPO set FECHA_CREACION=?, NOMBRE=?, USER_ID_CREADOR=? where GRUPO_ID=?";
		Integer registrosAfectados = 0;
		try {

			PreparedStatement sentenciaPreparada = conexion.prepareStatement(dml);

			java.sql.Date fechaSql = new java.sql.Date(grupo.getFechaCreacion().getTime());

			sentenciaPreparada.setDate(1, fechaSql);
			sentenciaPreparada.setString(2, grupo.getNombre());
			sentenciaPreparada.setObject(3, grupo.getUsuarioCreador().getUserId(), Types.INTEGER);
			sentenciaPreparada.setObject(4, grupoId, Types.INTEGER);
			registrosAfectados = sentenciaPreparada.executeUpdate();

			sentenciaPreparada.close();

		} catch (SQLException ex) {
			ExceptionBillsapp e = new ExceptionBillsapp();
			e.setCodigoError(ex.getErrorCode());
			e.setSentenciaSql(dml);
			e.setMensajeErrorAdministrador(ex.getMessage());
			switch (ex.getErrorCode()) {
			case 1407:
				e.setMensajeErrorUsuario(
						"FECHA DE CREACIÓN, NOMBRE Y EL USUARIO CREADOR DEL GRUPO SON CAMPOS OBLIGATORIOS");
				break;
			case 2291:
				e.setMensajeErrorUsuario("EL USUARIO INTRODUCIDO NO EXISTE");
				break;
			case 2290:
				e.setMensajeErrorUsuario("EL NOMBRE DEL GRUPO DEBE COMENZAR CON UNA LETRA");
				break;
			case 1:
				e.setMensajeErrorUsuario("UN USUARIO NO PUEDE CREAR DOS GRUPOS CON EL MISMO NOMBRE");
				break;
			default:
				e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");
			}
			throw e;
		}

		return registrosAfectados;
	}

	/**
	 * Método que implementa la lectura de un grupo
	 * 
	 * @param grupoId Identificador del grupo que se quiere leer
	 * @return Objeto de la clase Grupo que se quiere leer
	 * @throws ExceptionBillsapp Se lanzará esta excepción cuando se produzca una
	 *                           violación de las constraint de la base de datos o
	 *                           por fallos internos en la ejecución del método o de
	 *                           la base de datos.
	 */
	public Grupo leerGrupo(Integer userIdCreador, String nombre) throws ExceptionBillsapp {

		Grupo grupo = new Grupo();
		Usuario usuario = new Usuario();
		String dql = "select * from GRUPO g, USUARIO u where g.USER_ID_CREADOR = u.USER_ID AND USER_ID_CREADOR=" + userIdCreador + " AND NOMBRE=" + "'" +  nombre + "'";
		try {
			Statement sentencia = conexion.createStatement();
			ResultSet resultado = sentencia.executeQuery(dql);
			
			if (resultado.next()) {
				int idGrupo = resultado.getInt("GRUPO_ID");
				if (resultado.wasNull()) {
					grupo.setGrupoId(null);
				} else {
					grupo.setGrupoId(idGrupo);
				}

				grupo.setFechaCreacion(resultado.getDate("FECHA_CREACION") != null ? new java.util.Date(resultado.getDate("FECHA_CREACION").getTime()) : null);
				grupo.setNombre(resultado.getString("NOMBRE"));
				
				int userId = resultado.getInt("USER_ID");
				if (resultado.wasNull()) {
					usuario.setUserId(null);
				} else {
					usuario.setUserId(userId);
				}
				usuario.setAlias(resultado.getString("ALIAS"));
				usuario.setEmail(resultado.getString("EMAIL"));
				usuario.setPassword(resultado.getString("PASSWORD"));
				usuario.setTelefono(resultado.getString("TELEFONO"));
				grupo.setUsuarioCreador(usuario);
			}

			resultado.close();
			sentencia.close();

		} catch (SQLException ex) {
			ExceptionBillsapp e = new ExceptionBillsapp();
			e.setCodigoError(ex.getErrorCode());
			e.setSentenciaSql(dql);
			e.setMensajeErrorAdministrador(ex.getMessage());
			e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");

			throw e;
		}

		return grupo;
	}

	/**
	 * Método que implementa la lectura de todos los grupo
	 * 
	 * @return ArrayList de tipo Grupo con todos los grupos a leer
	 * @throws ExceptionBillsapp Se lanzará esta excepción cuando se produzca una
	 *                           violación de las constraint de la base de datos o
	 *                           por fallos internos en la ejecución del método o de
	 *                           la base de datos.
	 */
	public ArrayList<Grupo> leerGrupos() throws ExceptionBillsapp {

		ArrayList<Grupo> listaGrupos = new ArrayList();
		Grupo grupo;
		Usuario usuario;
		String dql = "select * from USUARIO u, GRUPO g where u.USER_ID = G.USER_ID_CREADOR";
		try {
			Statement sentencia = conexion.createStatement();
			ResultSet resultado = sentencia.executeQuery(dql);

			while (resultado.next()) {
				grupo = new Grupo();
				int grupoId = resultado.getInt("GRUPO_ID");
				if (resultado.wasNull()) {
					grupo.setGrupoId(null);
				} else {
					grupo.setGrupoId(grupoId);
				}
				grupo.setFechaCreacion(resultado.getDate("FECHA_CREACION"));
				grupo.setNombre(resultado.getString("NOMBRE"));

				usuario = new Usuario();
				int userId = resultado.getInt("USER_ID");
				if (resultado.wasNull()) {
					usuario.setUserId(null);
				} else {
					usuario.setUserId(userId);
				}
				usuario.setAlias(resultado.getString("ALIAS"));
				usuario.setEmail(resultado.getString("EMAIL"));
				usuario.setPassword(resultado.getString("PASSWORD"));
				usuario.setTelefono(resultado.getString("TELEFONO"));
				grupo.setUsuarioCreador(usuario);

				listaGrupos.add(grupo);
			}

			resultado.close();
			sentencia.close();

		} catch (SQLException ex) {
			ExceptionBillsapp e = new ExceptionBillsapp();
			e.setCodigoError(ex.getErrorCode());
			e.setSentenciaSql(dql);
			e.setMensajeErrorAdministrador(ex.getMessage());
			e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");

			throw e;
		}

		return listaGrupos;
	}
	

	/**
	 * Método que inserta un nuevo participante en la base de datos.
	 * 
	 * @param participante Objeto de la clase Participante que contiene los datos del participante a insertar.
	 *                     El identificador PARTICIPANTE_ID se asignará automáticamente usando la secuencia PARTICIPANTE_SEQ.
	 * @return Número de registros afectados por la inserción. Valor esperado: 1 si la inserción fue exitosa.
	 * @throws ExceptionBillsapp Se lanza esta excepción en caso de violaciones de constraints de la base de datos
	 *         o errores internos durante la ejecución, proporcionando mensajes detallados para usuario y administrador.
	 */
	public Integer insertarParticipante(Participante participante) throws ExceptionBillsapp {

	    String dml = "insert into PARTICIPANTE(PARTICIPANTE_ID, ALIAS, GRUPO_ID) values(PARTICIPANTE_SEQ.NEXTVAL, ?, ?)";
	    Integer registrosAfectados = 0;
	    try {

	        PreparedStatement sentenciaPreparada = conexion.prepareStatement(dml);

	        sentenciaPreparada.setString(1, participante.getAlias());
	        sentenciaPreparada.setObject(2, participante.getGrupo().getGrupoId(), Types.INTEGER);
	        registrosAfectados = sentenciaPreparada.executeUpdate();

	        sentenciaPreparada.close();

	    } catch (SQLException ex) {
	        ExceptionBillsapp e = new ExceptionBillsapp();
	        e.setCodigoError(ex.getErrorCode());
	        e.setSentenciaSql(dml);
	        e.setMensajeErrorAdministrador(ex.getMessage());
	        switch (ex.getErrorCode()) {
	            case 1400:
	                e.setMensajeErrorUsuario("EL ID DEL USUARIO Y EL ALIAS SON CAMPOS OBLIGATORIOS");
	                break;
	            case 6512:
	                e.setMensajeErrorUsuario("NO PUEDEN HABER DOS PARTICIPANTES CON EL MISMO ALIAS EN UN MISMO GRUPO");
	                break;
	            case 2291:
	                e.setMensajeErrorUsuario("EL GRUPO INSERTADO NO EXISTE");
	                break;
	            case 2290:
	                e.setMensajeErrorUsuario("EL ALIAS DEL PARTICIPANTE DEBE COMENZAR CON UNA LETRA");
	                break;
	            default:
	                e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");
	        }
	        throw e;
	    }

	    return registrosAfectados;
	}


	/**
	 * Elimina un participante de la base de datos dado su identificador.
	 * 
	 * @param participanteId Identificador del participante que se desea eliminar.
	 * @return Número de registros afectados por la eliminación. Valor esperado: 1 si la eliminación fue exitosa.
	 * @throws ExceptionBillsapp Se lanza esta excepción si existen restricciones que impiden la eliminación,
	 *         como gastos o distribuciones asociadas al participante, o por otros errores de la base de datos.
	 */
	public Integer eliminarParticipante(Integer participanteId) throws ExceptionBillsapp {

	    String dml = "Delete from PARTICIPANTE where PARTICIPANTE_ID = " + participanteId;
	    int registrosAfectados;
	    try {
	        Statement sentencia = conexion.createStatement();

	        registrosAfectados = sentencia.executeUpdate(dml);

	        sentencia.close();

	    } catch (SQLException ex) {
	        ExceptionBillsapp e = new ExceptionBillsapp();
	        e.setCodigoError(ex.getErrorCode());
	        e.setSentenciaSql(dml);
	        e.setMensajeErrorAdministrador(ex.getMessage());
	        switch (ex.getErrorCode()) {
	            case 2292:
	                e.setMensajeErrorUsuario("NO SE PUEDE ELIMINAR ESTE PARTICIPANTE PORQUE TIENE GASTOS O DISTRIBUCIONES DE GASTOS ASOCIADOS");
	                break;
	            default:
	                e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");
	        }
	        throw e;
	    }

	    return registrosAfectados;
	}


	/**
	 * Actualiza los datos de un participante existente en la base de datos.
	 * 
	 * @param participanteId Identificador del participante que se desea actualizar.
	 * @param participante Objeto Participante que contiene los nuevos datos (alias y grupo).
	 * @return Número de registros afectados por la actualización. Valor esperado: 1 si la actualización fue exitosa.
	 * @throws ExceptionBillsapp Se lanza esta excepción si ocurren errores relacionados con la integridad de datos,
	 *         como campos obligatorios faltantes, existencia del grupo o alias duplicados, o cualquier otro error de base de datos.
	 */
	public Integer actualizarParticipante(Integer participanteId, Participante participante) throws ExceptionBillsapp {

	    String dml = "update PARTICIPANTE set ALIAS=?, GRUPO_ID=? where PARTICIPANTE_ID=?";
	    Integer registrosAfectados = 0;
	    try {

	        PreparedStatement sentenciaPreparada = conexion.prepareStatement(dml);

	        sentenciaPreparada.setString(1, participante.getAlias());
	        sentenciaPreparada.setObject(2, participante.getGrupo().getGrupoId(), Types.INTEGER);
	        sentenciaPreparada.setObject(3, participanteId, Types.INTEGER);
	        registrosAfectados = sentenciaPreparada.executeUpdate();

	        sentenciaPreparada.close();

	    } catch (SQLException ex) {
	        ExceptionBillsapp e = new ExceptionBillsapp();
	        e.setCodigoError(ex.getErrorCode());
	        e.setSentenciaSql(dml);
	        e.setMensajeErrorAdministrador(ex.getMessage());
	        switch (ex.getErrorCode()) {
	            case 1407:
	                e.setMensajeErrorUsuario("EL ID DEL GRUPO Y EL ALIAS SON CAMPOS OBLIGATORIOS");
	                break;
	            case 2291:
	                e.setMensajeErrorUsuario("EL GRUPO INTRODUCIDO NO EXISTE");
	                break;
	            case 2290:
	                e.setMensajeErrorUsuario("EL ALIAS DEBE COMENZAR CON UNA LETRA");
	                break;
	            case 1:
	                e.setMensajeErrorUsuario("NO PUEDEN HABER DOS PARTICIPANTES CON EL MISMO ALIAS EN UN MISMO GRUPO");
	                break;
	            default:
	                e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");
	        }
	        throw e;
	    }

	    return registrosAfectados;
	}


	/**
	 * Recupera un participante específico de la base de datos dado su identificador.
	 * Además, incluye la información del grupo al que pertenece el participante.
	 * 
	 * @param participanteId Identificador del participante a recuperar.
	 * @return Objeto Participante con sus datos y el grupo asociado.
	 *         Si no se encuentra el participante, devuelve un objeto Participante con campos nulos.
	 * @throws ExceptionBillsapp Se lanza cuando ocurre un error en el acceso a la base de datos,
	 *         incluyendo detalles técnicos y mensajes amigables para el usuario.
	 */
	public Participante leerParticipante(Integer participanteId) throws ExceptionBillsapp {

	    Participante participante = new Participante();
	    Grupo grupo = new Grupo();
	    Usuario usuario = new Usuario();
	    String dql = "select * from PARTICIPANTE p, GRUPO g where p.GRUPO_ID = g.GRUPO_ID AND PARTICIPANTE_ID=" + participanteId;

	    try {
	        Statement sentencia = conexion.createStatement();
	        ResultSet resultado = sentencia.executeQuery(dql);

	        if (resultado.next()) {

	            int idParticipante = resultado.getInt("PARTICIPANTE_ID");
	            if (resultado.wasNull()) {
	                participante.setParticipanteId(null);
	            } else {
	                participante.setParticipanteId(idParticipante);
	            }

	            int idGrupo = resultado.getInt("GRUPO_ID");
	            if (resultado.wasNull()) {
	                grupo.setGrupoId(null);
	            } else {
	                grupo.setGrupoId(idGrupo);
	            }

	            grupo.setFechaCreacion(resultado.getDate("FECHA_CREACION"));
	            grupo.setNombre(resultado.getString("NOMBRE"));
	            int userCreador = resultado.getInt("USER_ID_CREADOR");
	            if (resultado.wasNull()) {
	                grupo.setUsuarioCreador(null);
	            } else {
	                grupo.setUsuarioCreador(new Usuario(userCreador));
	            }

	            participante.setGrupo(grupo);
	            participante.setAlias(resultado.getString("ALIAS"));
	        }

	        resultado.close();
	        sentencia.close();

	    } catch (SQLException ex) {
	        ExceptionBillsapp e = new ExceptionBillsapp();
	        e.setCodigoError(ex.getErrorCode());
	        e.setSentenciaSql(dql);
	        e.setMensajeErrorAdministrador(ex.getMessage());
	        e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");

	        throw e;
	    }

	    return participante;
	}
	

	/**
	 * Recupera todos los participantes existentes en la base de datos.
	 * Para cada participante se obtiene también el grupo al que pertenece.
	 * 
	 * @return Lista de objetos Participante con sus datos y grupos asociados.
	 * @throws ExceptionBillsapp Se lanza cuando ocurre un error en el acceso a la base de datos,
	 *         incluyendo detalles técnicos y mensajes amigables para el usuario.
	 */
	public ArrayList<Participante> leerParticipantes() throws ExceptionBillsapp {

	    ArrayList<Participante> listaParticipantes = new ArrayList<>();
	    Participante participante;
	    Grupo grupo;
	    Usuario usuario;
	    String dql = "select * from PARTICIPANTE p, GRUPO g where p.GRUPO_ID = g.GRUPO_ID";

	    try {
	        Statement sentencia = conexion.createStatement();
	        ResultSet resultado = sentencia.executeQuery(dql);

	        while (resultado.next()) {
	            participante = new Participante();

	            int idParticipante = resultado.getInt("PARTICIPANTE_ID");
	            if (resultado.wasNull()) {
	                participante.setParticipanteId(null);
	            } else {
	                participante.setParticipanteId(idParticipante);
	            }

	            grupo = new Grupo();
	            int idGrupo = resultado.getInt("GRUPO_ID");
	            if (resultado.wasNull()) {
	                grupo.setGrupoId(null);
	            } else {
	                grupo.setGrupoId(idGrupo);
	            }

	            grupo.setFechaCreacion(resultado.getDate("FECHA_CREACION"));
	            grupo.setNombre(resultado.getString("NOMBRE"));
	            int userCreador = resultado.getInt("USER_ID_CREADOR");
	            if (resultado.wasNull()) {
	                grupo.setUsuarioCreador(null);
	            } else {
	                grupo.setUsuarioCreador(new Usuario(userCreador));
	            }

	            participante.setGrupo(grupo);
	            participante.setAlias(resultado.getString("ALIAS"));

	            listaParticipantes.add(participante);
	        }

	        resultado.close();
	        sentencia.close();

	    } catch (SQLException ex) {
	        ExceptionBillsapp e = new ExceptionBillsapp();
	        e.setCodigoError(ex.getErrorCode());
	        e.setSentenciaSql(dql);
	        e.setMensajeErrorAdministrador(ex.getMessage());
	        e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");

	        throw e;
	    }

	    return listaParticipantes;
	}


	
	/**
	 * Inserta un nuevo gasto en la base de datos.
	 * 
	 * @param gasto Objeto Gasto con los datos a insertar (fecha, descripción, importe total, participante pagador).
	 * @return Número de registros afectados (normalmente 1 si la inserción fue exitosa).
	 * @throws ExceptionBillsapp Se lanza en caso de error SQL, con código, sentencia y mensajes específicos.
	 */
	public int insertarGasto(Gasto gasto) throws ExceptionBillsapp {
	    String dml = "insert into GASTO(GASTO_ID, FECHA, DESCRIPCION, IMPORTE_TOTAL, PARTICIPANTE_ID_PAGADOR) values(GASTO_SEQ.NEXTVAL, ?, ?, ?, ?)";
	    Integer registrosAfectados = 0;
	    try {
	        PreparedStatement sentenciaPreparada = conexion.prepareStatement(dml);

	        java.sql.Date fechaSql = new java.sql.Date(gasto.getFecha().getTime());

	        sentenciaPreparada.setDate(1, fechaSql);
	        sentenciaPreparada.setString(2, gasto.getDescripcion());
	        sentenciaPreparada.setObject(3, gasto.getImporteTotal(), Types.DOUBLE);
	        sentenciaPreparada.setObject(4, gasto.getParticipantePagador().getParticipanteId(), Types.INTEGER);

	        registrosAfectados = sentenciaPreparada.executeUpdate();

	        sentenciaPreparada.close();

	    } catch (SQLException ex) {
	        ExceptionBillsapp e = new ExceptionBillsapp();
	        e.setCodigoError(ex.getErrorCode());
	        e.setSentenciaSql(dml);
	        e.setMensajeErrorAdministrador(ex.getMessage());
	        switch (ex.getErrorCode()) {
	            case 1400:
	                e.setMensajeErrorUsuario("TODOS LOS CAMPOS SON OBLIGATORIOS");
	                break;
	            case 2291:
	                e.setMensajeErrorUsuario("EL PARTICIPANTE INSERTADO NO EXISTE");
	                break;
	            case 2290:
	                e.setMensajeErrorUsuario("LA DESCRIPCIÓN DEL GASTO DEBE COMENZAR CON UNA LETRA Y EL IMPORTE TOTAL DEBE SER MAYOR QUE 0");
	                break;
	            default:
	                e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");
	        }
	        throw e;
	    }

	    return registrosAfectados;
	}

	/**
	 * Elimina un gasto existente de la base de datos dado su identificador.
	 * 
	 * @param gastoId Identificador del gasto a eliminar.
	 * @return Número de registros afectados (normalmente 1 si la eliminación fue exitosa).
	 * @throws ExceptionBillsapp Se lanza en caso de error SQL, con código, sentencia y mensajes específicos.
	 */
	public Integer eliminarGasto(Integer gastoId) throws ExceptionBillsapp {

	    String dml = "Delete from GASTO where GASTO_ID = " + gastoId;
	    int registrosAfectados;
	    try {
	        Statement sentencia = conexion.createStatement();

	        registrosAfectados = sentencia.executeUpdate(dml);

	        sentencia.close();

	    } catch (SQLException ex) {
	        ExceptionBillsapp e = new ExceptionBillsapp();
	        e.setCodigoError(ex.getErrorCode());
	        e.setSentenciaSql(dml);
	        e.setMensajeErrorAdministrador(ex.getMessage());
	        switch (ex.getErrorCode()) {
	            case 2292:
	                e.setMensajeErrorUsuario("NO SE PUEDE ELIMINAR ESTE GASTO PORQUE TIENE DISTRIBUCIONES ASOCIADAS");
	                break;
	            default:
	                e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");
	        }
	        throw e;
	    }

	    return registrosAfectados;
	}

	/**
	 * Actualiza los datos de un gasto existente en la base de datos.
	 * 
	 * @param gastoId Identificador del gasto a actualizar.
	 * @param gasto Objeto Gasto con los nuevos datos (fecha, descripción, importe total, participante pagador).
	 * @return Número de registros afectados (normalmente 1 si la actualización fue exitosa).
	 * @throws ExceptionBillsapp Se lanza en caso de error SQL, con código, sentencia y mensajes específicos.
	 */
	public Integer actualizarGasto(Integer gastoId, Gasto gasto) throws ExceptionBillsapp {

	    String dml = "update GASTO set FECHA=?, DESCRIPCION=?, IMPORTE_TOTAL=?, PARTICIPANTE_ID_PAGADOR=? where GASTO_ID=?";
	    Integer registrosAfectados = 0;
	    try {

	        PreparedStatement sentenciaPreparada = conexion.prepareStatement(dml);

	        java.sql.Date fechaSql = new java.sql.Date(gasto.getFecha().getTime());

	        sentenciaPreparada.setDate(1, fechaSql);
	        sentenciaPreparada.setString(2, gasto.getDescripcion());
	        sentenciaPreparada.setObject(3, gasto.getImporteTotal(), Types.DOUBLE);
	        sentenciaPreparada.setObject(4, gasto.getParticipantePagador().getParticipanteId(), Types.INTEGER);
	        sentenciaPreparada.setObject(5, gastoId, Types.INTEGER);

	        registrosAfectados = sentenciaPreparada.executeUpdate();

	        sentenciaPreparada.close();

	    } catch (SQLException ex) {
	        ExceptionBillsapp e = new ExceptionBillsapp();
	        e.setCodigoError(ex.getErrorCode());
	        e.setSentenciaSql(dml);
	        e.setMensajeErrorAdministrador(ex.getMessage());
	        switch (ex.getErrorCode()) {
	            case 1:
	                e.setMensajeErrorUsuario("No puede haber dos gastos con la misma descripción en un grupo");
	                break;
	            case 1407:
	                e.setMensajeErrorUsuario("Fecha, descripción, importe total y participante pagador son campos obligatorios");
	                break;
	            case 2291:
	                e.setMensajeErrorUsuario("El participante introducido no existe");
	                break;
	            case 2290:
	                e.setMensajeErrorUsuario("La descripción del gasto debe comenzar con una letra y el importe total debe ser mayor que 0");
	                break;
	            default:
	                e.setMensajeErrorUsuario("Error general del sistema. Consulte con el administrador");
	        }
	        throw e;
	    }

	    return registrosAfectados;
	}


	/**
	 * Lee un gasto de la base de datos filtrando por fecha y descripción.
	 * 
	 * @param fecha Fecha del gasto a buscar.
	 * @param descrip Descripción exacta del gasto a buscar.
	 * @return Objeto Gasto con los datos encontrados o con valores nulos si no existe.
	 * @throws ExceptionBillsapp En caso de error SQL, devuelve detalles y mensajes adecuados.
	 */
	public Gasto leerGasto(Date fecha, String descrip) throws ExceptionBillsapp {

	    java.sql.Date fechaSql = new java.sql.Date(fecha.getTime());
	    Gasto gasto = new Gasto();
	    Participante participante = new Participante();
	    String dql = "select * from GASTO g, PARTICIPANTE p where g.PARTICIPANTE_ID_PAGADOR = p.PARTICIPANTE_ID AND g.FECHA = ? AND g.DESCRIPCION = ?";

	    try (PreparedStatement sentencia = conexion.prepareStatement(dql)) {
	        sentencia.setDate(1, fechaSql);
	        sentencia.setString(2, descrip);

	        try (ResultSet resultado = sentencia.executeQuery()) {
	            if (resultado.next()) {  // Solo si hay resultado, se llena el objeto

	                int idGasto = resultado.getInt("GASTO_ID");
	                gasto.setGastoId(resultado.wasNull() ? null : idGasto);

	                gasto.setFecha(resultado.getDate("FECHA") != null ? new java.util.Date(resultado.getDate("FECHA").getTime()) : null);
	                gasto.setDescripcion(resultado.getString("DESCRIPCION"));

	                Double importe = resultado.getDouble("IMPORTE_TOTAL");
	                gasto.setImporteTotal(resultado.wasNull() ? null : importe);

	                participante = new Participante();
	                int participanteId = resultado.getInt("PARTICIPANTE_ID_PAGADOR");
	                participante.setParticipanteId(resultado.wasNull() ? null : participanteId);

	                Grupo grupo = new Grupo();
	                int grupoId = resultado.getInt("GRUPO_ID");
	                grupo.setGrupoId(resultado.wasNull() ? null : grupoId);
	                participante.setGrupo(grupo);

	                participante.setAlias(resultado.getString("ALIAS"));

	                gasto.setParticipantePagador(participante);
	            }
	        }

	    } catch (SQLException ex) {
	        ExceptionBillsapp e = new ExceptionBillsapp();
	        e.setCodigoError(ex.getErrorCode());
	        e.setSentenciaSql(dql);
	        e.setMensajeErrorAdministrador(ex.getMessage());
	        e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");
	        throw e;
	    }

	    return gasto;
	}

	/**
	 * Lee todos los gastos de la base de datos junto con su participante pagador.
	 * 
	 * @return Lista con todos los objetos Gasto encontrados.
	 * @throws ExceptionBillsapp En caso de error SQL, devuelve detalles y mensajes adecuados.
	 */
	public ArrayList<Gasto> leerGastos() throws ExceptionBillsapp {

	    ArrayList<Gasto> listaGasto = new ArrayList<>();
	    String dql = "select * from GASTO g, PARTICIPANTE p where g.PARTICIPANTE_ID_PAGADOR = p.PARTICIPANTE_ID";

	    try (Statement sentencia = conexion.createStatement();
	         ResultSet resultado = sentencia.executeQuery(dql)) {

	        while (resultado.next()) {
	            Gasto gasto = new Gasto();

	            int idGasto = resultado.getInt("GASTO_ID");
	            gasto.setGastoId(resultado.wasNull() ? null : idGasto);

	            gasto.setFecha(resultado.getDate("FECHA"));
	            gasto.setDescripcion(resultado.getString("DESCRIPCION"));

	            Double importe = resultado.getDouble("IMPORTE_TOTAL");
	            gasto.setImporteTotal(resultado.wasNull() ? null : importe);

	            Participante participante = new Participante();
	            int participanteId = resultado.getInt("PARTICIPANTE_ID_PAGADOR");
	            participante.setParticipanteId(resultado.wasNull() ? null : participanteId);

	            Grupo grupo = new Grupo();
	            int grupoId = resultado.getInt("USER_ID");  // OJO: revisar que USER_ID sea el campo correcto para grupoId
	            grupo.setGrupoId(resultado.wasNull() ? null : grupoId);
	            participante.setGrupo(grupo);

	            participante.setAlias(resultado.getString("ALIAS"));

	            gasto.setParticipantePagador(participante);

	            listaGasto.add(gasto);
	        }

	    } catch (SQLException ex) {
	        ExceptionBillsapp e = new ExceptionBillsapp();
	        e.setCodigoError(ex.getErrorCode());
	        e.setSentenciaSql(dql);
	        e.setMensajeErrorAdministrador(ex.getMessage());
	        e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");
	        throw e;
	    }

	    return listaGasto;
	}


	/**
	 * Inserta una nueva distribución de gasto en la base de datos.
	 * 
	 * @param distribucionGasto Objeto DistribucionGasto con los datos a insertar.
	 * @return Número de registros afectados (debería ser 1 si la inserción fue exitosa).
	 * @throws ExceptionBillsapp Excepción personalizada con mensajes de error específicos.
	 */
	public int insertarDistribucion(DistribucionGasto distribucionGasto) throws ExceptionBillsapp {
	    String dml = "insert into DISTRIBUCION_GASTO(DISTRIBUCION_ID, CANTIDAD_DEBIDA, PARTICIPANTE_ID, GASTO_ID) values(DISTRIBUCION_GASTO_SEQ.NEXTVAL, ?, ?, ?)";
	    Integer registrosAfectados = 0;
	    try {
	        PreparedStatement sentenciaPreparada = conexion.prepareStatement(dml);

	        sentenciaPreparada.setObject(1, distribucionGasto.getCantidad(), Types.DOUBLE);
	        sentenciaPreparada.setObject(2, distribucionGasto.getParticipante().getParticipanteId(), Types.INTEGER);
	        sentenciaPreparada.setObject(3, distribucionGasto.getGasto().getGastoId(), Types.INTEGER);

	        registrosAfectados = sentenciaPreparada.executeUpdate();


	    } catch (SQLException ex) {
	        ExceptionBillsapp e = new ExceptionBillsapp();
	        e.setCodigoError(ex.getErrorCode());
	        e.setSentenciaSql(dml);
	        e.setMensajeErrorAdministrador(ex.getMessage());
	        switch (ex.getErrorCode()) {
	            case 1400:
	                e.setMensajeErrorUsuario("TODOS LOS CAMPOS SON OBLIGATORIOS");
	                break;
	            case 2291:
	                e.setMensajeErrorUsuario("EL PARTICIPANTE O EL GASTO INSERTADO NO EXISTE");
	                break;
	            case 2290:
	                e.setMensajeErrorUsuario("LA CANTIDAD DEBIDA DEBE SER MAYOR QUE 0");
	                break;
	            default:
	                e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");
	        }
	        throw e;
	    }

	    return registrosAfectados;
	}

	/**
	 * Elimina una distribución de gasto dada su ID.
	 * 
	 * @param distribucionId ID de la distribución a eliminar.
	 * @return Número de registros afectados.
	 * @throws ExceptionBillsapp Excepción personalizada con mensajes de error específicos.
	 */
	public Integer eliminarDsitribucion(Integer distribucionId) throws ExceptionBillsapp {

	    String dml = "Delete from DISTRIBUCION_GASTO where DISTRIBUCION_ID = " + distribucionId;
	    int registrosAfectados;
	    try {
	        Statement sentencia = conexion.createStatement();

	        registrosAfectados = sentencia.executeUpdate(dml);

	        sentencia.close();

	    } catch (SQLException ex) {
	        ExceptionBillsapp e = new ExceptionBillsapp();
	        e.setCodigoError(ex.getErrorCode());
	        e.setSentenciaSql(dml);
	        e.setMensajeErrorAdministrador(ex.getMessage());
	        switch (ex.getErrorCode()) {
	            case 2292:
	                e.setMensajeErrorUsuario("NO SE PUEDE ELIMINAR ESTA DISTRIBUCION PORQUE TIENE PARTICIPANTES O GASTOS ASOCIADOS");
	                break;
	            default:
	                e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");
	        }
	        throw e;
	    }

	    return registrosAfectados;
	}

	/**
	 * Actualiza una distribución de gasto existente.
	 * 
	 * @param distribucionId ID de la distribución a actualizar.
	 * @param distribucionG Objeto DistribucionGasto con los nuevos datos.
	 * @return Número de registros afectados.
	 * @throws ExceptionBillsapp Excepción personalizada con mensajes de error específicos.
	 */
	public Integer actualizarDistribucion(Integer distribucionId, DistribucionGasto distribucionG) throws ExceptionBillsapp {

	    String dml = "update DISTRIBUCION_GASTO set CANTIDAD_DEBIDA=?, PARTICIPANTE_ID=?, GASTO_ID=? where DISTRIBUCION_ID=?";
	    Integer registrosAfectados = 0;
	    try {
	        PreparedStatement sentenciaPreparada = conexion.prepareStatement(dml);

	        sentenciaPreparada.setObject(1, distribucionG.getCantidad(), Types.DOUBLE);
	        sentenciaPreparada.setObject(2, distribucionG.getParticipante().getParticipanteId(), Types.INTEGER);
	        sentenciaPreparada.setObject(3, distribucionG.getGasto().getGastoId(), Types.INTEGER);
	        sentenciaPreparada.setObject(4, distribucionId, Types.INTEGER);

	        registrosAfectados = sentenciaPreparada.executeUpdate();

	        sentenciaPreparada.close();

	    } catch (SQLException ex) {
	        ExceptionBillsapp e = new ExceptionBillsapp();
	        e.setCodigoError(ex.getErrorCode());
	        e.setSentenciaSql(dml);
	        e.setMensajeErrorAdministrador(ex.getMessage());
	        switch (ex.getErrorCode()) {
	            case 1407:
	                e.setMensajeErrorUsuario("TODOS LOS CAMPOS SON OBLIGATORIOS");
	                break;
	            case 2291:
	                e.setMensajeErrorUsuario("EL PARTICIPANTE O EL GASTO INTRODUCIDO NO EXISTE");
	                break;
	            case 2290:
	                e.setMensajeErrorUsuario("LA CANTIDAD DEBIDA DEBE SER MAYOR QUE 0");
	                break;
	            default:
	                e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");
	        }
	        throw e;
	    }

	    return registrosAfectados;
	}


	
	/**
	 * Lee una distribución de gasto específica según su ID.
	 * 
	 * @param distribucionId ID de la distribución a buscar.
	 * @return Objeto DistribucionGasto con los datos encontrados o vacíos si no existe.
	 * @throws ExceptionBillsapp Excepción personalizada en caso de error en la consulta.
	 */
	public DistribucionGasto leerDistribucion(Integer distribucionId) throws ExceptionBillsapp {
	    DistribucionGasto distribucionG = new DistribucionGasto();
	    Participante participantePag = new Participante();
	    Gasto gasto = new Gasto();
	    String dql = "select * from DISTRIBUCION_GASTO d, GASTO g, PARTICIPANTE p "
	               + "where d.GASTO_ID = g.GASTO_ID AND d.PARTICIPANTE_ID = p.PARTICIPANTE_ID "
	               + "AND DISTRIBUCION_ID = " + distribucionId;

	    try {
	        Statement sentencia = conexion.createStatement();
	        ResultSet resultado = sentencia.executeQuery(dql);

	        if (resultado.next()) {
	            // Construcción del objeto Gasto
	            int idGasto = resultado.getInt("GASTO_ID");
	            gasto.setGastoId(resultado.wasNull() ? null : idGasto);
	            gasto.setFecha(resultado.getDate("FECHA"));
	            gasto.setDescripcion(resultado.getString("DESCRIPCION"));
	            Double importe = resultado.getDouble("IMPORTE_TOTAL");
	            gasto.setImporteTotal(resultado.wasNull() ? null : importe);

	            // Participante que pagó el gasto
	            int participantePagador = resultado.getInt("PARTICIPANTE_ID_PAGADOR");
	            participantePag.setParticipanteId(resultado.wasNull() ? null : participantePagador);
	            gasto.setParticipantePagador(participantePag);

	            distribucionG.setGasto(gasto);

	            // Participante asociado a la distribución
	            Participante participanteDist = new Participante();
	            int participanteId = resultado.getInt("PARTICIPANTE_ID");
	            participanteDist.setParticipanteId(resultado.wasNull() ? null : participanteId);

	            // Grupo del participante
	            Grupo grupo = new Grupo();
	            int grupoId = resultado.getInt("GRUPO_ID");
	            grupo.setGrupoId(resultado.wasNull() ? null : grupoId);
	            participanteDist.setGrupo(grupo);

	            // Alias del participante
	            participanteDist.setAlias(resultado.getString("ALIAS"));

	            distribucionG.setParticipante(participanteDist);

	            // Datos de la distribución
	            int idDistribucion = resultado.getInt("DISTRIBUCION_ID");
	            distribucionG.setDistribucionId(resultado.wasNull() ? null : idDistribucion);

	            Double cantDebida = resultado.getDouble("CANTIDAD_DEBIDA");
	            distribucionG.setCantidad(resultado.wasNull() ? null : cantDebida);
	        }

	        resultado.close();
	        sentencia.close();

	    } catch (SQLException ex) {
	        ExceptionBillsapp e = new ExceptionBillsapp();
	        e.setCodigoError(ex.getErrorCode());
	        e.setSentenciaSql(dql);
	        e.setMensajeErrorAdministrador(ex.getMessage());
	        e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");
	        throw e;
	    }

	    return distribucionG;
	}

	/**
	 * Lee todas las distribuciones de gasto existentes en la base de datos.
	 * 
	 * @return Lista con todos los objetos DistribucionGasto encontrados.
	 * @throws ExceptionBillsapp Excepción personalizada en caso de error en la consulta.
	 */
	public ArrayList<DistribucionGasto> leerDistribuciones() throws ExceptionBillsapp {
	    ArrayList<DistribucionGasto> listaDistribucion = new ArrayList<>();
	    String dql = "select * from DISTRIBUCION_GASTO d, GASTO g, PARTICIPANTE p "
	               + "where d.GASTO_ID = g.GASTO_ID AND d.PARTICIPANTE_ID = p.PARTICIPANTE_ID";

	    try {
	        Statement sentencia = conexion.createStatement();
	        ResultSet resultado = sentencia.executeQuery(dql);

	        while (resultado.next()) {
	            // Construcción del objeto Gasto
	            Gasto gasto = new Gasto();
	            int idGasto = resultado.getInt("GASTO_ID");
	            gasto.setGastoId(resultado.wasNull() ? null : idGasto);
	            gasto.setFecha(resultado.getDate("FECHA"));
	            gasto.setDescripcion(resultado.getString("DESCRIPCION"));
	            Double importe = resultado.getDouble("IMPORTE_TOTAL");
	            gasto.setImporteTotal(resultado.wasNull() ? null : importe);

	            // Participante que pagó el gasto
	            Participante participantePag = new Participante();
	            int participantePagador = resultado.getInt("PARTICIPANTE_ID_PAGADOR");
	            participantePag.setParticipanteId(resultado.wasNull() ? null : participantePagador);
	            gasto.setParticipantePagador(participantePag);

	            // Construcción del objeto DistribucionGasto
	            DistribucionGasto distribucionG = new DistribucionGasto();
	            distribucionG.setGasto(gasto);

	            // Participante asociado a la distribución
	            Participante participanteDist = new Participante();
	            int participanteId = resultado.getInt("PARTICIPANTE_ID");
	            participanteDist.setParticipanteId(resultado.wasNull() ? null : participanteId);

	            // Grupo del participante
	            Grupo grupo = new Grupo();
	            int grupoId = resultado.getInt("GRUPO_ID");
	            grupo.setGrupoId(resultado.wasNull() ? null : grupoId);
	            participanteDist.setGrupo(grupo);

	            // Alias del participante
	            participanteDist.setAlias(resultado.getString("ALIAS"));

	            distribucionG.setParticipante(participanteDist);

	            // Datos de la distribución
	            int idDistribucion = resultado.getInt("DISTRIBUCION_ID");
	            distribucionG.setDistribucionId(resultado.wasNull() ? null : idDistribucion);

	            Double cantDebida = resultado.getDouble("CANTIDAD_DEBIDA");
	            distribucionG.setCantidad(resultado.wasNull() ? null : cantDebida);

	            // Añade la distribución a la lista
	            listaDistribucion.add(distribucionG);
	        }

	        resultado.close();
	        sentencia.close();

	    } catch (SQLException ex) {
	        ExceptionBillsapp e = new ExceptionBillsapp();
	        e.setCodigoError(ex.getErrorCode());
	        e.setSentenciaSql(dql);
	        e.setMensajeErrorAdministrador(ex.getMessage());
	        e.setMensajeErrorUsuario("ERROR GENERAL DEL SISTEMA. CONSULTE CON EL ADMINISTRADOR");
	        throw e;
	    }

	    return listaDistribucion;
	}

}
