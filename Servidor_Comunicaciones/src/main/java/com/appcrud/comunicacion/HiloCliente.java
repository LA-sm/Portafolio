package com.appcrud.comunicacion;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

//Importación de loggers y clases de la aplicación
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

import billsapppojos.DistribucionGasto;
import billsapppojos.ExceptionBillsapp;
import billsapppojos.Gasto;
import billsapppojos.Grupo;
import billsapppojos.Participante;
import billsapppojos.Usuario;

/**
 * Clase HiloCliente que atiende una petición de un cliente de forma concurrente.
 * Se ejecuta en un hilo independiente para permitir múltiples conexiones simultáneas.
 */
public class HiloCliente extends Thread {

	private Socket socket;
	private org.apache.log4j.Logger loggerNavegacion;
	private org.apache.log4j.Logger loggerErrores;

	 /**
     * Constructor que recibe el socket del cliente.
     * También configura los loggers para navegación y errores.
     */
	public HiloCliente(Socket socket) {
		this.socket = socket;
		
		// Configuración de logs (log4j)
		PropertyConfigurator.configure("logs\\log4j.properties");
        loggerNavegacion = LogManager.getLogger("NAVEGACION");
        PropertyConfigurator.configure("logs\\log4j.properties");
        loggerErrores = LogManager.getLogger("ERRORES");
	}

	/**
     * Método principal del hilo que atiende la lógica del cliente.
     */
	@Override
	public void run() {

		ObjectInputStream ois = null;
		ObjectOutputStream oos = null;
		CAD cad = null;

		try {
			// Crear los streams de entrada/salida para recibir y enviar objetos
			ois = new ObjectInputStream(socket.getInputStream());
			oos = new ObjectOutputStream(socket.getOutputStream());

			// Crear una instancia del CAD (Clase de Acceso a Datos)
			cad = new CAD();

			// Leer el objeto tipo Peticion enviado por el cliente
			Peticion peticion = (Peticion) ois.readObject();
			System.out.println("Peticion:" + peticion.getTipoOperacion());
			
			// Crear objeto de respuesta que se devolverá al cliente
			Respuesta respuesta = new Respuesta();

			// Procesar la operación indicada en la petición
			switch (peticion.getTipoOperacion()) {
			
			// ================================
            // === OPERACIONES CON USUARIO ===
			// ================================
			case LOGIN:
				loggerNavegacion.trace("El usuario ha intentado 'LOGIN'");
				try {
					// Verificamos si existe ese usuario con la contraseña (hash) en la BD
					boolean loginOk = cad.login(peticion.getEmail(), peticion.getPassword_plano());
					if (loginOk) {
						Usuario usuario = cad.leerUsuario(peticion.getEmail());
						usuario.setPassword(peticion.getPassword_plano());
						respuesta.setUsuario(usuario);
						respuesta.setExito(true);
						respuesta.setMensaje("Login exitoso");
					} else {
						respuesta.setExito(false);
						respuesta.setMensaje("Credenciales incorrectas");
					}
				} catch (ExceptionBillsapp e) {
					e.printStackTrace();
					respuesta.setExito(false);
					respuesta.setMensaje("Excepción al iniciar sesión: " + e.getMessage());
					loggerErrores.error(e.getCodigoError() + " - " + e.getMensajeErrorAdministrador() + " - " + e.getSentenciaSql());
				}
				break;

			/*
			 *  Todos los siguientes casos siguen una lógica similar:
			 *  Verifican si el objeto a procesar no es null,
			 *  llaman a un método en CAD para insertar/actualizar/eliminar/Operaciones de consulta o búsquedas varias, 
			 *  y ajustan la respuesta según el resultado.
			 */			
				
			case CREATE_USER:
				loggerNavegacion.trace("El usuario ha intentado 'CREAR USUARIO'");
				try {
					if (peticion.getUsuario() != null) {
						int regAfectados = cad.insertarUsuario(peticion.getUsuario());
						respuesta.setRegistrosAfectados(regAfectados);

						if (regAfectados != 0) {
							System.out.println("email: " + peticion.getUsuario().getEmail());
							Usuario usuario = cad.leerUsuario(peticion.getUsuario().getEmail());
							usuario.setPassword(peticion.getUsuario().getPassword());
							respuesta.setUsuario(usuario);
							respuesta.setExito(true);
							respuesta.setMensaje("Usuario creado con exito.");
						} else {
							respuesta.setExito(false);
							respuesta.setMensaje("Usuario no creado con exito.");
						}
					} else {
						System.out.println("Usuario es null");
					}

				} catch (ExceptionBillsapp e) {
					respuesta.setExito(false);
					respuesta.setMensaje(e.getMensajeErrorUsuario());
					loggerErrores.error(e.getCodigoError() + " - " + e.getMensajeErrorAdministrador() + " - " + e.getSentenciaSql());
				}

				break;
				
			case UPDATE_USER:
				loggerNavegacion.trace("El usuario ha intentado 'ACTUALIZAR USUARIO'");
				try {
					if (peticion.getUsuario() != null) {
						int regAfectados = cad.actualizarUsuario(peticion.getUsuario().getUserId(), peticion.getUsuario());
						respuesta.setRegistrosAfectados(regAfectados);

						if (regAfectados != 0) {
							respuesta.setExito(true);
							respuesta.setMensaje("Usuario actualizado con exito.");
						} else {
							respuesta.setExito(false);
							respuesta.setMensaje("Usuario no actualizado con exito.");
						}
					} else {
						System.out.println("Usuario es null");
					}

				} catch (ExceptionBillsapp e) {
					respuesta.setExito(false);
					respuesta.setMensaje(e.getMensajeErrorUsuario());
					loggerErrores.error(e.getCodigoError() + " - " + e.getMensajeErrorAdministrador() + " - " + e.getSentenciaSql());
				}

				break;

			case DELETE_USER:
				loggerNavegacion.trace("El usuario ha intentado'ELIMINAR USUARIO'");
				try {
					if (peticion.getId() != null) {
						int regAfectados = cad.eliminarUsuario(peticion.getId());
						respuesta.setRegistrosAfectados(regAfectados);

						if (regAfectados != 0) {
							respuesta.setExito(true);
							respuesta.setMensaje("Usuario eliminado con exito.");
						} else {
							respuesta.setExito(false);
							respuesta.setMensaje("Usuario no eliminado con exito.");
						}

					} else {
						System.out.println("Usuario es null");
					}

				} catch (ExceptionBillsapp e) {
					respuesta.setExito(false);
					respuesta.setMensaje(e.getMensajeErrorUsuario());
					loggerErrores.error(e.getCodigoError() + " - " + e.getMensajeErrorAdministrador() + " - " + e.getSentenciaSql());
				}
				
				break;
			
			case READ_USER:
				loggerNavegacion.trace("El usuario ha intentado'LEER USUARIO'");
				try {
					if (peticion.getEmail() != null) {
						Usuario usuario = cad.leerUsuario(peticion.getEmail());
						respuesta.setUsuario(usuario);

						if (usuario != null) {
							respuesta.setExito(true);
							respuesta.setMensaje("Usuario leido con exito.");
						} else {
							respuesta.setExito(false);
							respuesta.setMensaje("Usuario no encontrado.");
						}

					} else {
						System.out.println("Usuario es null");
					}

				} catch (ExceptionBillsapp e) {
					respuesta.setExito(false);
					respuesta.setMensaje(e.getMensajeErrorUsuario());
					loggerErrores.error(e.getCodigoError() + " - " + e.getMensajeErrorAdministrador() + " - " + e.getSentenciaSql());
				}
				
				
				break;

			case CREATE_GRUPO:
				loggerNavegacion.trace("El usuario ha intentado 'CREAR GRUPO'");
				try {
					if (peticion.getGrupo() != null) {
						int regAfectados = cad.insertarGrupo(peticion.getGrupo());
						respuesta.setRegistrosAfectados(regAfectados);
						if (regAfectados != 0) {
							System.out.println("dentro de if");
							Grupo grupo = cad.leerGrupo(peticion.getGrupo().getUsuarioCreador().getUserId(), peticion.getGrupo().getNombre());
							respuesta.setGrupo(grupo);
							respuesta.setExito(true);
							respuesta.setMensaje("Grupo añadido con exito.");
						} else {
							respuesta.setExito(false);
							respuesta.setMensaje("Grupo no añadido con exito.");
						}
					} else {
						System.out.println("Grupo es null");
					}

				} catch (ExceptionBillsapp e) {
					respuesta.setExito(false);
					respuesta.setMensaje(e.getMensajeErrorUsuario());
					loggerErrores.error(e.getCodigoError() + " - " + e.getMensajeErrorAdministrador() + " - " + e.getSentenciaSql());
				}

				break;

			case UPDATE_GRUPO:
				loggerNavegacion.trace("El usuario ha intentado'ACTUALIZAR GRUPO'");
				try {
					if (peticion.getGrupo() != null) {
						int regAfectados = cad.actualizarGrupo(peticion.getGrupo().getGrupoId(), peticion.getGrupo());
						respuesta.setRegistrosAfectados(regAfectados);

						if (regAfectados != 0) {
							respuesta.setExito(true);
							respuesta.setMensaje("Grupo actualizado con exito.");
						} else {
							respuesta.setExito(false);
							respuesta.setMensaje("Grupo no actualizado con exito.");
						}
					} else {
						System.out.println("Grupo es null");
					}

				} catch (ExceptionBillsapp e) {
					respuesta.setExito(false);
					respuesta.setMensaje(e.getMensajeErrorUsuario());
					loggerErrores.error(e.getCodigoError() + " - " + e.getMensajeErrorAdministrador() + " - " + e.getSentenciaSql());
				}

				break;

			case DELETE_GRUPO:
				loggerNavegacion.trace("El usuario ha intentado 'ELIMINAR GRUPO'");
				try {
					if (peticion.getId() != null) {
						int regAfectados = cad.eliminarGrupo(peticion.getId());
						respuesta.setRegistrosAfectados(regAfectados);

						if (regAfectados != 0) {
							respuesta.setExito(true);
							respuesta.setMensaje("Grupo eliminado con exito.");
						} else {
							respuesta.setExito(false);
							respuesta.setMensaje("Grupo no eliminado con exito.");
						}

					} else {
						System.out.println("Grupo es null");
					}
				} catch (ExceptionBillsapp e) {
					respuesta.setExito(false);
					respuesta.setMensaje(e.getMensajeErrorUsuario());
					loggerErrores.error(e.getCodigoError() + " - " + e.getMensajeErrorAdministrador() + " - " + e.getSentenciaSql());
				}

				break;

			case CREATE_GASTO:
				loggerNavegacion.trace("El usuario ha intentado 'CREAR GASTO'");
				try {
					if (peticion.getGasto() != null) {
						int regAfectados = cad.insertarGasto(peticion.getGasto());
						respuesta.setRegistrosAfectados(regAfectados);

						if (regAfectados != 0) {
							Gasto gasto = cad.leerGasto(peticion.getGasto().getFecha(), peticion.getGasto().getDescripcion());
							respuesta.setGasto(gasto);
							respuesta.setExito(true);
							respuesta.setMensaje("Gasto añadido con exito.");
						} else {
							respuesta.setExito(false);
							respuesta.setMensaje("Gasto no añadido con exito.");
						}
					} else {
						System.out.println("Gasto es null");
					}

				} catch (ExceptionBillsapp e) {
					respuesta.setExito(false);
					respuesta.setMensaje(e.getMensajeErrorUsuario());
					loggerErrores.error(e.getCodigoError() + " - " + e.getMensajeErrorAdministrador() + " - " + e.getSentenciaSql());
				}

				break;

			case UPDATE_GASTO:
				loggerNavegacion.trace("El usuario ha intentado 'ACTUALIZAR GASTO'");
				try {
					if (peticion.getGasto() != null) {
						int regAfectados = cad.actualizarGasto(peticion.getGasto().getGastoId(), peticion.getGasto());
						respuesta.setRegistrosAfectados(regAfectados);

						if (regAfectados != 0) {
							respuesta.setExito(true);
							respuesta.setMensaje("Gasto actualizado con exito.");
						} else {
							respuesta.setExito(false);
							respuesta.setMensaje("Gasto no actualizado con exito.");
						}
					} else {
						System.out.println("Gasto es null");
					}

				} catch (ExceptionBillsapp e) {
					respuesta.setExito(false);
					respuesta.setMensaje(e.getMensajeErrorUsuario());
					loggerErrores.error(e.getCodigoError() + " - " + e.getMensajeErrorAdministrador() + " - " + e.getSentenciaSql());
				}

				break;
			
			case DELETE_GASTO:
				loggerNavegacion.trace("El usuario ha intentado 'ELIMINAR GASTO'");
				try {
					if (peticion.getId() != null) {
						int regAfectados = cad.eliminarGasto(peticion.getId());
						respuesta.setRegistrosAfectados(regAfectados);

						if (regAfectados != 0) {
							respuesta.setExito(true);
							respuesta.setMensaje("Gasto eliminado con exito.");
						} else {
							respuesta.setExito(false);
							respuesta.setMensaje("Gasto no eliminado con exito.");
						}

					} else {
						System.out.println("Gasto es null");
					}
				} catch (ExceptionBillsapp e) {
					respuesta.setExito(false);
					respuesta.setMensaje(e.getMensajeErrorUsuario());
					loggerErrores.error(e.getCodigoError() + " - " + e.getMensajeErrorAdministrador() + " - " + e.getSentenciaSql());
				}

				break;	

			case CREATE_DISTRIBUCION:
				loggerNavegacion.trace("El usuario ha intentado 'CREAR DISTRIBUCION'");
				try {
					if (peticion.getDistribucionGasto() != null) {
						int regAfectados = cad.insertarDistribucion(peticion.getDistribucionGasto());
						respuesta.setRegistrosAfectados(regAfectados);

						if (regAfectados != 0) {
							respuesta.setExito(true);
							respuesta.setMensaje("Distribución de gasto añadida con exito.");
						} else {
							respuesta.setExito(false);
							respuesta.setMensaje("Distribución de gasto no añadida  con exito.");
						}
					} else {
						System.out.println("Distribución de gasto es null");
					}

				} catch (ExceptionBillsapp e) {
					respuesta.setExito(false);
					respuesta.setMensaje(e.getMensajeErrorUsuario());
					loggerErrores.error(e.getCodigoError() + " - " + e.getMensajeErrorAdministrador() + " - " + e.getSentenciaSql());
				}

				break;
				
			case UPDATE_DISTRIBUCION:
				loggerNavegacion.trace("El usuario ha intentado 'ACTUALIZAR DISTRIBUCION'");
				try {
					if (peticion.getDistribucionGasto() != null) {
						int regAfectados = cad.actualizarDistribucion(peticion.getDistribucionGasto().getDistribucionId(), peticion.getDistribucionGasto());
						respuesta.setRegistrosAfectados(regAfectados);

						if (regAfectados != 0) {
							respuesta.setExito(true);
							respuesta.setMensaje("Distribucion actualizada con exito.");
						} else {
							respuesta.setExito(false);
							respuesta.setMensaje("Distribucion no actualizada con exito.");
						}
					} else {
						System.out.println("Distribucion es null");
					}

				} catch (ExceptionBillsapp e) {
					respuesta.setExito(false);
					respuesta.setMensaje(e.getMensajeErrorUsuario());
					loggerErrores.error(e.getCodigoError() + " - " + e.getMensajeErrorAdministrador() + " - " + e.getSentenciaSql());
				}

				break;
			
			case DELETE_DISTRIBUCION:
				loggerNavegacion.trace("El usuario ha intentado 'ELIMINAR DISTRIBUCION'");
				try {
					if (peticion.getId() != null) {
						int regAfectados = cad.eliminarDsitribucion(peticion.getId());
						respuesta.setRegistrosAfectados(regAfectados);

						if (regAfectados != 0) {
							respuesta.setExito(true);
							respuesta.setMensaje("Distribucion eliminada con exito.");
						} else {
							respuesta.setExito(false);
							respuesta.setMensaje("Distribucion no eliminada con exito.");
						}

					} else {
						System.out.println("Distribucion es null");
					}
				} catch (ExceptionBillsapp e) {
					respuesta.setExito(false);
					respuesta.setMensaje(e.getMensajeErrorUsuario());
					loggerErrores.error(e.getCodigoError() + " - " + e.getMensajeErrorAdministrador() + " - " + e.getSentenciaSql());
				}

				break;	

			case CREATE_PARTICIPANTE:
				loggerNavegacion.trace("El usuario ha intentado 'CREAR PARTICIPANTE'");
				try {
					if (peticion.getParticipante() != null) {
						int regAfectados = cad.insertarParticipante(peticion.getParticipante());
						respuesta.setRegistrosAfectados(regAfectados);

						if (regAfectados != 0) {
							respuesta.setExito(true);
							respuesta.setMensaje("Participante añadido con exito.");
						} else {
							respuesta.setExito(false);
							respuesta.setMensaje("Participante no añadido  con exito.");
						}
					} else {
						System.out.println("Participante es null");
					}

				} catch (ExceptionBillsapp e) {
					respuesta.setExito(false);
					respuesta.setMensaje(e.getMensajeErrorUsuario());
					loggerErrores.error(e.getCodigoError() + " - " + e.getMensajeErrorAdministrador() + " - " + e.getSentenciaSql());
				}

				break;
				
			case UPDATE_PARTICIPANTE:
				loggerNavegacion.trace("El usuario ha intentado 'ACTUALIZAR PARTICIPANTE'");
				try {
					if (peticion.getParticipante() != null) {
						int regAfectados = cad.actualizarParticipante(peticion.getParticipante().getParticipanteId(), peticion.getParticipante());
						respuesta.setRegistrosAfectados(regAfectados);

						if (regAfectados != 0) {
							respuesta.setExito(true);
							respuesta.setMensaje("Participante actualizado con exito.");
						} else {
							respuesta.setExito(false);
							respuesta.setMensaje("Participante no actualizado con exito.");
						}
					} else {
						System.out.println("Participante es null");
					}

				} catch (ExceptionBillsapp e) {
					respuesta.setExito(false);
					respuesta.setMensaje(e.getMensajeErrorUsuario());
					loggerErrores.error(e.getCodigoError() + " - " + e.getMensajeErrorAdministrador() + " - " + e.getSentenciaSql());
				}

				break;
				
			case DELETE_PARTICIPANTE:
				loggerNavegacion.trace("El usuario ha intentado 'ELIMINAR PARTICIPANTE'");
				try {
					if (peticion.getId() != null) {
						int regAfectados = cad.eliminarParticipante(peticion.getId());
						respuesta.setRegistrosAfectados(regAfectados);

						if (regAfectados != 0) {
							respuesta.setExito(true);
							respuesta.setMensaje("Participante eliminado con exito.");
						} else {
							respuesta.setExito(false);
							respuesta.setMensaje("Participante no eliminado con exito.");
						}

					} else {
						System.out.println("Participante es null");
					}
				} catch (ExceptionBillsapp e) {
					respuesta.setExito(false);
					respuesta.setMensaje(e.getMensajeErrorUsuario());
					loggerErrores.error(e.getCodigoError() + " - " + e.getMensajeErrorAdministrador() + " - " + e.getSentenciaSql());
				}

				break;	

			case BUSCAR_GRUPOS_POR_USUARIO:
				loggerNavegacion.trace("El usuario ha intentado 'BUSCAR GRUPOS POR USUARIO'");
				try {
					if (peticion.getId() != null) {
						ArrayList<Grupo> listaGrupos = cad.gruposPorUsuario(peticion.getId());
						respuesta.setListaGrupos(listaGrupos);
						respuesta.setExito(true);
						respuesta.setMensaje("Búsqueda realizada con éxito");

					} else {
						System.out.println("Id es null");
					}

				} catch (ExceptionBillsapp e) {
					respuesta.setExito(false);
					respuesta.setMensaje(e.getMensajeErrorUsuario());
					loggerErrores.error(e.getCodigoError() + " - " + e.getMensajeErrorAdministrador() + " - " + e.getSentenciaSql());
				}

				break;

			case BUSCAR_GASTOS_POR_GRUPO:
				loggerNavegacion.trace("El usuario ha intentado 'BUSCAR GASTOS POR GRUPO'");
				try {
					if (peticion.getId() != null) {
						ArrayList<Gasto> listaGastos = cad.gastosPorGrupo(peticion.getId());
						respuesta.setListaGastos(listaGastos);
						respuesta.setExito(true);
						respuesta.setMensaje("Búsqueda realizada con éxito");

					} else {
						System.out.println("Id es null");
					}

				} catch (ExceptionBillsapp e) {
					respuesta.setExito(false);
					respuesta.setMensaje(e.getMensajeErrorUsuario());
					loggerErrores.error(e.getCodigoError() + " - " + e.getMensajeErrorAdministrador() + " - " + e.getSentenciaSql());
				}

				break;
				
			case BUSCAR_PARTICIPANTE_POR_GRUPO:
				loggerNavegacion.trace("El usuario ha intentado 'BUSCAR PARTICIPANTE POR GRUPO'");
				try {
					if (peticion.getId() != null) {
						ArrayList<Participante> listaParticipantes = cad.participantePorGrupo(peticion.getId());
						respuesta.setListaParticipantes(listaParticipantes);
						respuesta.setExito(true);
						respuesta.setMensaje("Búsqueda realizada con éxito");

					} else {
						System.out.println("Id es null");
					}

				} catch (ExceptionBillsapp e) {
					respuesta.setExito(false);
					respuesta.setMensaje(e.getMensajeErrorUsuario());
					loggerErrores.error(e.getCodigoError() + " - " + e.getMensajeErrorAdministrador() + " - " + e.getSentenciaSql());
				}

				break;

			case BUSCAR_DISTRIBUCION_POR_GASTO:
				loggerNavegacion.trace("El usuario ha intentado 'BUSCAR DISTRIBUCION POR GASTO'");
				try {
					if (peticion.getId() != null) {
						ArrayList<DistribucionGasto> listaDistribucion = cad.distribucionPorGasto(peticion.getId());
						respuesta.setListaDistribucionGasto(listaDistribucion);
						respuesta.setExito(true);
						respuesta.setMensaje("Búsqueda realizada con éxito");

					} else {
						System.out.println("Id es null");
					}

				} catch (ExceptionBillsapp e) {
					respuesta.setExito(false);
					respuesta.setMensaje(e.getMensajeErrorUsuario());
					loggerErrores.error(e.getCodigoError() + " - " + e.getMensajeErrorAdministrador() + " - " + e.getSentenciaSql());
				}

				break;

				// =====================
                // === OPERACIÓN PING ===
				// =====================
			case PING:
				
				// Respuesta de comprobación de estado
				respuesta.setExito(true);
				respuesta.setMensaje("Ping exitoso");
				break;

			default:
				// Si la operación no coincide con ninguna conocida
				break;
			}

			 // Enviar el objeto Respuesta de vuelta al cliente
			oos.writeObject(respuesta);
			oos.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExceptionBillsapp e1) {
			e1.printStackTrace();
			System.out.println(e1.getMensajeErrorUsuario());
		} finally {
			try {
				
				// Cerramos la conexión
				if (cad != null) {
	                cad.cerrarConexion();  
	            }
				
				// Cerramos los canales y el socket
				if (ois != null)
					ois.close();
				if (oos != null)
					oos.close();
				if (socket != null)
					socket.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExceptionBillsapp e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
