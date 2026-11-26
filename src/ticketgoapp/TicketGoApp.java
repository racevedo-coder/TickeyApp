/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package ticketgoapp;


import conexion.conexion;
import java.sql.Connection;
/**
 *
 * @author HP
 */
public class TicketGoApp {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
         Connection cn = conexion.getInstancia().getConexion();

        if (cn == null) {
          javax.swing.JOptionPane.showMessageDialog(null,
                "No se pudo conectar con la base de datos.",
                "Error de conexión",
                javax.swing.JOptionPane.ERROR_MESSAGE);
        return;
        } else {
              java.awt.EventQueue.invokeLater(() -> new vista.Login().setVisible(true));
        }
    }
    
    
}
    

