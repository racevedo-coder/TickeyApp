/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package vista;

import conexion.Sesion;
import conexion.conexion;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author HP
 */
public class registrar extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(registrar.class.getName());
    private boolean esAdmin;
    private int idUsuarioEditar = 0;
    private Administrador administrador; 
    private java.util.ArrayList<Integer> idsRol = new java.util.ArrayList<>();

    /**
     * Creates new form registrar
     */
    public registrar(boolean esAdmin) {
        this(esAdmin, 0, false);
    }
    
    public void setAdministrador(Administrador administrador) {
    this.administrador = administrador;
}

    public registrar(boolean esAdmin, int idUsuario, boolean soloLectura) {
        initComponents();
        setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        this.esAdmin = esAdmin;
        this.idUsuarioEditar = idUsuario;

        limitar(txtDocumento, 20, true);
        limitar(txtNombre, 80, false);
        limitar(txtCorreo, 100, false);
        limitar(txtTelefono, 20, true);
        limitar(txtClave, 50, false);

        if (esAdmin) {
            cargarRoles();
            aceptar.setVisible(false);
        } else {
            cmbRol.setVisible(false);
            lblRol.setVisible(false);
        }

        if (idUsuario > 0) {
            setTitle(soloLectura ? "Detalle del usuario" : "Editar usuario");
            cargarUsuario();
            txtDocumento.setEditable(false);
            txtClave.setToolTipText("Déjala vacía para no cambiarla");
            if (idUsuario == Sesion.idUsuario) {
                cmbRol.setEnabled(false);
            }
        } else {
            setTitle(esAdmin ? "Nuevo usuario" : "Registro");
        }
        if (soloLectura) {
            activarLectura();
        }
    }

    private void cargarRoles() {
        String sql = "SELECT id_rol, nombre_rol FROM rol";
        try (PreparedStatement ps = conexion.getInstancia().getConexion().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            cmbRol.removeAllItems();
            idsRol.clear();
            while (rs.next()) {
                idsRol.add(rs.getInt("id_rol"));            // posición 0 de la lista = posición 0 del combo
                cmbRol.addItem(rs.getString("nombre_rol"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los roles.");
        }
    }

    private boolean existeDocumento(String documento) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE documento = ?";
        try (PreparedStatement ps = conexion.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;    // true si ya hay un usuario con ese documento
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "No se pudo verificar el documento.");
        }
        return false;
    }

    private int obtenerIdRol(String nombreRol) {
        String sql = "SELECT id_rol FROM rol WHERE nombre_rol = ?";
        try (PreparedStatement ps = conexion.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setString(1, nombreRol);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_rol");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "No se pudo obtener el rol.");
        }
        return -1;   // -1 = no encontrado
    }

    private void limitar(javax.swing.text.JTextComponent campo, int max, boolean soloNumeros) {
        campo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isISOControl(c)) {
                    if (soloNumeros && !Character.isDigit(c)) {
                        e.consume();
                    } else if (campo.getDocument().getLength() >= max) {
                        e.consume();
                    }
                }
            }
        });
    }

    private void cargarUsuario() {
        String sql = "SELECT documento, nombre, correo, telefono, id_rol FROM usuario WHERE id_usuario = ?";
        try (PreparedStatement ps = conexion.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setInt(1, idUsuarioEditar);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    txtDocumento.setText(rs.getString("documento"));
                    txtNombre.setText(rs.getString("nombre"));
                    txtCorreo.setText(rs.getString("correo"));
                    String tel = rs.getString("telefono");
                    txtTelefono.setText(tel == null ? "" : tel);
                    int pos = idsRol.indexOf(rs.getInt("id_rol"));
                    if (pos >= 0) {
                        cmbRol.setSelectedIndex(pos);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el usuario.");
        }
    }

    private void activarLectura() {
        txtDocumento.setEditable(false);
        txtNombre.setEditable(false);
        txtCorreo.setEditable(false);
        txtTelefono.setEditable(false);
        txtClave.setEnabled(false);
        cmbRol.setEnabled(false);
        btnGuardar.setVisible(false);
    }

    private void guardarUsuario() {
        String documento = txtDocumento.getText().trim();
        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String clave = new String(txtClave.getPassword());

        if (documento.isEmpty() || nombre.isEmpty() || correo.isEmpty()
                || (idUsuarioEditar == 0 && clave.isEmpty())
                || (esAdmin && cmbRol.getSelectedIndex() < 0)) {
            JOptionPane.showMessageDialog(this, "Completa todos los campos obligatorios.");
            return;
        }
        if (!correo.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            JOptionPane.showMessageDialog(this, "El correo no tiene un formato válido.");
            return;
        }
        if (!clave.isEmpty() && clave.length() < 6) {
            JOptionPane.showMessageDialog(this, "La contraseña debe tener al menos 6 caracteres.");
            return;
        }
        if (!esAdmin && !aceptar.isSelected()) {
            JOptionPane.showMessageDialog(this, "Debes aceptar los términos y condiciones para registrarte.");
            return;
        }

        int idRol;
        if (esAdmin) {
            idRol = idsRol.get(cmbRol.getSelectedIndex());
        } else {
            idRol = obtenerIdRol("Cliente");
            if (idRol == -1) {
                return;
            }
        }

        if (idUsuarioEditar == 0 && existeDocumento(documento)) {
            JOptionPane.showMessageDialog(this,
                    esAdmin ? "Ya existe un usuario con ese documento. Para modificarlo, usa Editar."
                            : "Ya existe una cuenta con ese documento.");
            return;
        }

        try (CallableStatement cs = conexion.getInstancia().getConexion()
                .prepareCall("{CALL sp_guardar_usuario(?, ?, ?, ?, ?, ?, ?)}")) {
            cs.setString(1, documento);
            cs.setString(2, nombre);
            cs.setString(3, correo);
            cs.setString(4, clave);
            cs.setString(5, telefono.isEmpty() ? null : telefono);
            cs.setInt(6, idRol);
            cs.registerOutParameter(7, java.sql.Types.VARCHAR);
            cs.execute();

            JOptionPane.showMessageDialog(this,
                    cs.getString(7).equals("creado") ? "Usuario creado correctamente."
                    : "Usuario actualizado correctamente.");
            dispose();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                JOptionPane.showMessageDialog(this, "Ese correo o documento ya está registrado por otro usuario.");
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo guardar: " + e.getMessage());
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        btnGuardar = new BotonCuadrado();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtDocumento = new javax.swing.JTextField();
        txtNombre = new javax.swing.JTextField();
        txtCorreo = new javax.swing.JTextField();
        txtTelefono = new javax.swing.JTextField();
        txtClave = new javax.swing.JPasswordField();
        lblRol = new javax.swing.JLabel();
        cmbRol = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        aceptar = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        btnregresar = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Documento:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 110, -1, -1));

        jLabel2.setText("Nombre:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 150, -1, -1));

        jLabel3.setText("Correo:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 190, -1, -1));

        btnGuardar.setBackground(new java.awt.Color(0, 51, 102));
        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("Registrarse");
        btnGuardar.setBorder(null);
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);
        jPanel1.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 390, 80, 30));

        jLabel4.setText("Teléfono:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 230, -1, -1));

        jLabel5.setText("Contraseña:");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 270, -1, -1));
        jPanel1.add(txtDocumento, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 110, 156, -1));
        jPanel1.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 150, 156, -1));
        jPanel1.add(txtCorreo, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 190, 156, -1));
        jPanel1.add(txtTelefono, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 230, 156, -1));
        jPanel1.add(txtClave, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 270, 156, -1));

        lblRol.setText("Rol:");
        jPanel1.add(lblRol, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 310, -1, -1));

        cmbRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(cmbRol, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 310, 156, -1));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(102, 102, 102));
        jLabel9.setText("Ya tienes una cuenta?");
        jPanel1.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 430, -1, -1));

        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        jButton1.setForeground(new java.awt.Color(102, 102, 255));
        jButton1.setText("Inicia sesión");
        jButton1.setBorder(null);
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setFocusPainted(false);
        jButton1.addActionListener(this::jButton1ActionPerformed);
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 430, -1, -1));

        aceptar.setText("Acepto términos y condiciones");
        jPanel1.add(aceptar, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 350, -1, -1));

        jLabel7.setFont(new java.awt.Font("Serif", 0, 36)); // NOI18N
        jLabel7.setText("TicketGo");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 10, 150, 60));

        jLabel8.setBackground(new java.awt.Color(204, 0, 102));
        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(153, 153, 153));
        jLabel8.setText("Inicia sesión para continuar");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 60, -1, -1));

        jPanel2.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 140, 350, 470));

        jPanel3.setBackground(new java.awt.Color(204, 204, 255));

        jLabel10.setBackground(new java.awt.Color(204, 204, 204));
        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(153, 153, 153));
        jLabel10.setText("Copyright © 2026  Ticketgo . Todos los derechos reservados");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(270, 270, 270)
                .addComponent(jLabel10)
                .addContainerGap(274, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel10)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 640, 850, 50));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel2.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 450, 850, -1));

        btnregresar.setForeground(new java.awt.Color(255, 255, 255));
        btnregresar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/volver.png"))); // NOI18N
        btnregresar.setText("Regresar");
        btnregresar.setBorderPainted(false);
        btnregresar.setContentAreaFilled(false);
        btnregresar.addActionListener(this::btnregresarActionPerformed);
        jPanel2.add(btnregresar, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 10, -1, -1));

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/fondo1..png"))); // NOI18N
        jPanel2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 850, 470));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 844, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        // TODO add your handling code here:

        guardarUsuario();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:

        Login ventana = new Login();
        ventana.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnregresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnregresarActionPerformed
        // TODO add your handling code here:
        if (esAdmin) {
            if (administrador != null) {
                administrador.irAUsuarios();   // selecciona la pestaña del JTabbedPane
                administrador.setVisible(true);
                administrador.toFront();
            }
            this.dispose();
        } else {
            Login login = new Login();
            login.setVisible(true);
            this.dispose();
        }


    }//GEN-LAST:event_btnregresarActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new registrar(false).setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox aceptar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnregresar;
    private javax.swing.JComboBox<String> cmbRol;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel lblRol;
    private javax.swing.JPasswordField txtClave;
    private javax.swing.JTextField txtCorreo;
    private javax.swing.JTextField txtDocumento;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtTelefono;
    // End of variables declaration//GEN-END:variables
}
