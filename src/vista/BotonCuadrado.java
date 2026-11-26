/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

/**
 *
 * @author HP
 */


import java.awt.*;
import javax.swing.JButton;

public class BotonCuadrado extends JButton {

    public BotonCuadrado() {
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setBackground(new Color(43, 42, 94));
        setForeground(Color.WHITE);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(getModel().isPressed() ? getBackground().darker() : getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());   // rectángulo con esquinas rectas
        g2.dispose();
        super.paintComponent(g);                      // dibuja el texto encima
    }
} 
    

