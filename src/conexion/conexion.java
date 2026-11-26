/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package conexion;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author HP
 */
public class conexion {
    
    
    private static conexion instancia;
    private Connection conn;
    
    private static final String URL = System.getenv().getOrDefault("TICKETGO_DB_URL", "jdbc:mysql://localhost:3306/ticketgo");
    private static final String USUARIO = System.getenv().getOrDefault("TICKETGO_DB_USER", "ticketgo_app");
    private static final String CLAVE = System.getenv().getOrDefault("TICKETGO_DB_PASSWORD", "");
    
   
       private conexion() {
        try {
            conn = DriverManager.getConnection(URL, USUARIO, CLAVE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "No se pudo conectar con el servidor. Verifica que MySQL esté encendido.",
                "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }
       
       
       public static conexion getInstancia() {
        if (instancia == null) {
            instancia = new conexion();
        }
        return instancia;
    }
       
      public Connection getConexion() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(URL, USUARIO, CLAVE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "El servidor no está en línea. Intenta de nuevo en unos momentos.",
                "Error de conexión", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return conn;
    }

}
