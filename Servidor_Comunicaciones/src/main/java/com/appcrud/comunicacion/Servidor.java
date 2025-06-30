package com.appcrud.comunicacion;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Clase principal que representa un servidor.
 * Su propósito es escuchar conexiones entrantes en un puerto específico
 * y delegar la atención de cada cliente a un hilo independiente.
 */
public class Servidor {

    public static void main(String[] args) {

        // Puerto en el que el servidor escuchará conexiones entrantes
        int puerto = 5000;

        try {
            // Se crea un objeto ServerSocket que escucha en el puerto especificado
            ServerSocket serverSocket = new ServerSocket(puerto);
            System.out.println("Servidor está escuchando en el puerto: " + puerto);

            // Bucle infinito para aceptar múltiples conexiones de clientes
            while (true) {
                // El método accept() se bloquea hasta que un cliente se conecta
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado: " + socket.getInetAddress());

                // Se crea un nuevo hilo para manejar la comunicación con el cliente
                // Se pasa el socket conectado al constructor del hilo
                HiloCliente hilo = new HiloCliente(socket);

                // Se inicia la ejecución del hilo
                hilo.start();
            }

        } catch (IOException e) {
            // Captura cualquier excepción relacionada con entrada/salida
            // Por ejemplo, error al abrir el puerto o aceptar conexiones
            e.printStackTrace();
        }

    }

}
