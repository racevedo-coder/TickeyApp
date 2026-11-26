/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package vista;

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
public class FrmDescuento extends javax.swing.JDialog {
    private boolean esNuevo;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmDescuento.class.getName());

    /**
     * Creates new form FrmDescuento
     */
   public FrmDescuento(java.awt.Frame parent, boolean modal, String codigo, boolean soloLectura) {
    super(parent, modal);
    initComponents();
    setLocationRelativeTo(parent);
    this.esNuevo = (codigo == null);

    spnInicio.setModel(new javax.swing.SpinnerDateModel());
    spnInicio.setEditor(new javax.swing.JSpinner.DateEditor(spnInicio, "yyyy-MM-dd"));
    spnFin.setModel(new javax.swing.SpinnerDateModel());
    spnFin.setEditor(new javax.swing.JSpinner.DateEditor(spnFin, "yyyy-MM-dd"));

    limitar(txtCodigoDescuento, 20, false);
    limitar(txtPorcentaje, 3, true);
    limitar(txtCantMin, 3, true);

    cmbTipoCondicion.setEditable(true);
    cargarTipos();
    if (esNuevo) {
        lblTituloDescuento.setText("Nuevo descuento");
    } else {
        txtCodigoDescuento.setText(codigo);
        txtCodigoDescuento.setEditable(false);
        cargarDescuento(codigo);
        lblTituloDescuento.setText(soloLectura ? "Detalle del descuento" : "Editar descuento");
    }
    if (soloLectura) {
        activarLectura();
    }
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
   
  private void cargarDescuento(String codigo) {
    String sql = "SELECT tipo_condicion, porcentaje, cant_min, fecha_inicio, fecha_fin "
               + "FROM descuento WHERE cod_descuento = ?";
    try (PreparedStatement ps = conexion.getInstancia().getConexion().prepareStatement(sql)) {
        ps.setString(1, codigo);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                cmbTipoCondicion.setSelectedItem(rs.getString("tipo_condicion"));
                txtPorcentaje.setText(rs.getBigDecimal("porcentaje")
                        .stripTrailingZeros().toPlainString());
                txtCantMin.setText(String.valueOf(rs.getInt("cant_min")));
                spnInicio.setValue(new java.util.Date(rs.getDate("fecha_inicio").getTime()));
                spnFin.setValue(new java.util.Date(rs.getDate("fecha_fin").getTime()));
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudo cargar el descuento.");
    }
} 
   

private void activarLectura() {
    txtCodigoDescuento.setEditable(false);
    txtPorcentaje.setEditable(false);
    txtCantMin.setEditable(false);
    spnInicio.setEnabled(false);
    spnFin.setEnabled(false);
    btnGuardarDescuento.setVisible(false);
    btnCancelarDescuento.setText("Cerrar");
    cmbTipoCondicion.setEnabled(false);
}

private void cargarTipos() {
    java.util.LinkedHashSet<String> tipos = new java.util.LinkedHashSet<>();
    try (PreparedStatement ps = conexion.getInstancia().getConexion()
            .prepareStatement("SELECT DISTINCT tipo_condicion FROM descuento ORDER BY tipo_condicion");
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            tipos.add(rs.getString("tipo_condicion"));
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudieron cargar los tipos de condición.");
    }
    cmbTipoCondicion.setModel(new javax.swing.DefaultComboBoxModel<>(tipos.toArray(new String[0])));
    cmbTipoCondicion.setSelectedItem(tipos.isEmpty() ? "" : tipos.iterator().next());
}

private void guardarDescuento() {
    String codigo = txtCodigoDescuento.getText().trim().toUpperCase();
    Object tipoSel = cmbTipoCondicion.getSelectedItem();
    String tipo = (tipoSel == null) ? "" : tipoSel.toString().trim();
    String porcTxt = txtPorcentaje.getText().trim();
    String minTxt = txtCantMin.getText().trim();

    if (codigo.isEmpty() || tipo.isEmpty() || porcTxt.isEmpty() || minTxt.isEmpty()) {
        JOptionPane.showMessageDialog(this,
            "Completa el código, el tipo de condición, el porcentaje y el mínimo de boletas.");
        return;
    }
    if (tipo.length() > 20) {
        JOptionPane.showMessageDialog(this, "El tipo de condición admite máximo 20 caracteres.");
        return;
    }

    int porcentaje = Integer.parseInt(porcTxt);
    int minimo = Integer.parseInt(minTxt);
    if (porcentaje < 1 || porcentaje > 100) {
        JOptionPane.showMessageDialog(this, "El porcentaje debe estar entre 1 y 100.");
        return;
    }
    if (minimo < 1) {
        JOptionPane.showMessageDialog(this, "El mínimo de boletas debe ser al menos 1.");
        return;
    }

    java.util.Date inicio = (java.util.Date) spnInicio.getValue();
    java.util.Date fin = (java.util.Date) spnFin.getValue();
    if (fin.before(inicio)) {
        JOptionPane.showMessageDialog(this, "La fecha final no puede ser anterior a la inicial.");
        return;
    }

    try (CallableStatement cs = conexion.getInstancia().getConexion()
            .prepareCall("{CALL sp_guardar_descuento(?, ?, ?, ?, ?, ?, ?, ?)}")) {
        cs.setString(1, codigo);
        cs.setString(2, tipo);
        cs.setBigDecimal(3, new java.math.BigDecimal(porcentaje));
        cs.setInt(4, minimo);
        cs.setDate(5, new java.sql.Date(inicio.getTime()));
        cs.setDate(6, new java.sql.Date(fin.getTime()));
        cs.setString(7, esNuevo ? "nuevo" : "editar");
        cs.registerOutParameter(8, java.sql.Types.VARCHAR);
        cs.execute();

        switch (cs.getString(8)) {
            case "creado":
                JOptionPane.showMessageDialog(this, "Descuento creado.");
                dispose();
                break;
            case "actualizado":
                JOptionPane.showMessageDialog(this, "Descuento actualizado.");
                dispose();
                break;
            case "duplicado":
                JOptionPane.showMessageDialog(this, "Ya existe un descuento con ese código.");
                break;
            default:
                JOptionPane.showMessageDialog(this, "Los datos del descuento no son válidos.");
                break;
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudo guardar: " + e.getMessage());
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

        jPanel1 = new javax.swing.JPanel();
        lblTituloDescuento = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        txtCantMin = new javax.swing.JTextField();
        txtCodigoDescuento = new javax.swing.JTextField();
        txtPorcentaje = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        spnFin = new javax.swing.JSpinner();
        spnInicio = new javax.swing.JSpinner();
        btnGuardarDescuento = new javax.swing.JButton();
        btnCancelarDescuento = new javax.swing.JButton();
        cmbTipoCondicion = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTituloDescuento.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        lblTituloDescuento.setForeground(new java.awt.Color(255, 255, 255));
        jPanel1.add(lblTituloDescuento, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 30, 310, 40));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/fondo2.png"))); // NOI18N
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 740, 90));

        jLabel2.setText("Código:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 130, -1, -1));

        jLabel3.setText("Tipo condición:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 170, -1, -1));

        jLabel4.setText("Porcentaje(%):");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 210, -1, -1));

        jLabel5.setText("Mínimo de boletas:");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 250, -1, -1));

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jPanel1.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 130, 40, 140));
        jPanel1.add(txtCantMin, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 250, 160, -1));
        jPanel1.add(txtCodigoDescuento, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 130, 160, -1));
        jPanel1.add(txtPorcentaje, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 210, 160, -1));

        jLabel6.setText("Válido desde:");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 130, -1, -1));

        jLabel7.setText("Válido hasta:");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 170, -1, -1));
        jPanel1.add(spnFin, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 170, 150, -1));
        jPanel1.add(spnInicio, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 130, 150, -1));

        btnGuardarDescuento.setBackground(new java.awt.Color(0, 0, 102));
        btnGuardarDescuento.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardarDescuento.setText("Guardar");
        btnGuardarDescuento.addActionListener(this::btnGuardarDescuentoActionPerformed);
        jPanel1.add(btnGuardarDescuento, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 320, 110, 40));

        btnCancelarDescuento.setBackground(new java.awt.Color(0, 0, 102));
        btnCancelarDescuento.setForeground(new java.awt.Color(255, 255, 255));
        btnCancelarDescuento.setText("Cancelar");
        btnCancelarDescuento.addActionListener(this::btnCancelarDescuentoActionPerformed);
        jPanel1.add(btnCancelarDescuento, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 320, 100, 40));

        cmbTipoCondicion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(cmbTipoCondicion, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 170, 160, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 741, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarDescuentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarDescuentoActionPerformed
        // TODO add your handling code here:
        guardarDescuento();
    }//GEN-LAST:event_btnGuardarDescuentoActionPerformed

    private void btnCancelarDescuentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarDescuentoActionPerformed
        // TODO add your handling code here:
        dispose();
    }//GEN-LAST:event_btnCancelarDescuentoActionPerformed

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

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
             FrmDescuento dialog = new FrmDescuento(new javax.swing.JFrame(), true, null, false);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelarDescuento;
    private javax.swing.JButton btnGuardarDescuento;
    private javax.swing.JComboBox<String> cmbTipoCondicion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblTituloDescuento;
    private javax.swing.JSpinner spnFin;
    private javax.swing.JSpinner spnInicio;
    private javax.swing.JTextField txtCantMin;
    private javax.swing.JTextField txtCodigoDescuento;
    private javax.swing.JTextField txtPorcentaje;
    // End of variables declaration//GEN-END:variables
}
