/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package vista;


import java.sql.CallableStatement;
import conexion.conexion;
import conexion.Sesion;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;


/**
 *
 * @author HP
 */
public class Cliente_detalleEvento extends javax.swing.JFrame {
    private int idEvento;
    private java.util.ArrayList<Integer> idsZona = new java.util.ArrayList<>();
    private java.util.ArrayList<java.math.BigDecimal> precios = new java.util.ArrayList<>();
    private java.util.ArrayList<Integer> cupos = new java.util.ArrayList<>();
    private java.text.NumberFormat moneda =
        java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("es", "CO"));
    private String codigoAplicado = null;
    private java.math.BigDecimal porcentajeActual = java.math.BigDecimal.ZERO;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Cliente_detalleEvento.class.getName());

    /**
     * Creates new form Cliente_detalleEvento
     */
    public Cliente_detalleEvento(int idEvento) {
        initComponents();
        spnCantidad.setModel(new javax.swing.SpinnerNumberModel(1, 1, 10, 1));
        cmbMetodo.setModel(new javax.swing.DefaultComboBoxModel<>(
        new String[]{"Tarjeta", "PSE", "Nequi", "Efectivo", "Daviplata"}));
        this.idEvento = idEvento;
        cargarEvento();
        cargarZonas();
        aplicarEstilo();
        
        
        // Conexión de los controles desde el código: cada cambio recalcula el resumen
    cmbZona.addActionListener(e -> actualizarResumen());
    spnCantidad.addChangeListener(e -> actualizarResumen());
    
       actualizarResumen();
    }
    
    
    private void ponerImagen(javax.swing.JLabel lbl, String ruta, int ancho, int alto) {
    if (ruta == null || ruta.isEmpty()) {
        return;
    }
    java.io.File archivo = new java.io.File(ruta);
    if (archivo.exists()) {
        java.awt.Image img = new javax.swing.ImageIcon(archivo.getAbsolutePath()).getImage()
                .getScaledInstance(ancho, alto, java.awt.Image.SCALE_SMOOTH);
        lbl.setIcon(new javax.swing.ImageIcon(img));
    }
    }
    
   private java.math.BigDecimal obtenerPorcentaje(String codigo, int cantidad) {
    String sql = "SELECT porcentaje FROM descuento "
               + "WHERE cod_descuento = ? "
               + "AND CURDATE() BETWEEN fecha_inicio AND fecha_fin "
               + "AND ? >= cant_min";
    try (PreparedStatement ps = conexion.getInstancia().getConexion().prepareStatement(sql)) {
        ps.setString(1, codigo);
        ps.setInt(2, cantidad);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal("porcentaje");
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudo validar el código de descuento.");
    }
    return null;
}
   
    
    private void cargarEvento() {
    String sql = "SELECT nombre, lugar, fecha, imagen FROM evento WHERE id_evento = ?";
    try (PreparedStatement ps = conexion.getInstancia().getConexion().prepareStatement(sql)) {
        ps.setInt(1, idEvento);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                lblNombreEvento.setText(rs.getString("nombre"));
                lblLugar.setText(rs.getString("lugar"));
                lblLugar.setToolTipText(rs.getString("lugar"));
                java.text.SimpleDateFormat formato = new java.text.SimpleDateFormat(
                        "d MMM yyyy, h:mm a", new java.util.Locale("es", "CO"));
                lblFecha.setText(formato.format(rs.getTimestamp("fecha")));
                ponerImagen(lblImagen, rs.getString("imagen"), 150, 120);
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudo cargar el evento.");
    }
}

    
   private void cargarZonas() {
    cmbZona.removeAllItems();
    idsZona.clear();
    precios.clear();
    cupos.clear();

    String sql = "SELECT id_zona, nombre, precio, cupo_disponible FROM zona "
               + "WHERE id_evento = ? ORDER BY precio";

    try (PreparedStatement ps = conexion.getInstancia().getConexion().prepareStatement(sql)) {
        ps.setInt(1, idEvento);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int cupo = rs.getInt("cupo_disponible");
                idsZona.add(rs.getInt("id_zona"));          // las listas se llenan ANTES de agregar el ítem al combo
                precios.add(rs.getBigDecimal("precio"));
                cupos.add(cupo);
                String detalle = (cupo > 0) ? moneda.format(rs.getBigDecimal("precio")) : "AGOTADO";
                cmbZona.addItem(rs.getString("nombre") + " - " + detalle);
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudieron cargar las zonas.");
    }
}
    
   private void actualizarResumen() {
    int i = cmbZona.getSelectedIndex();
    if (i < 0 || i >= precios.size()) {
        lblSubtotal.setText(moneda.format(0));
        lblDescuento.setText("Sin descuento");
        lblTotal.setText(moneda.format(0));
        return;
    }

    boolean hayCupo = cupos.get(i) > 0;
    spnCantidad.setEnabled(hayCupo);
    btnComprar.setEnabled(hayCupo);

    int cantidad = (Integer) spnCantidad.getValue();
    java.math.BigDecimal subtotal = precios.get(i).multiply(java.math.BigDecimal.valueOf(cantidad));

    // Si hay un código aplicado, se revisa de nuevo con la cantidad actual
    porcentajeActual = java.math.BigDecimal.ZERO;
    if (codigoAplicado != null) {
        java.math.BigDecimal p = obtenerPorcentaje(codigoAplicado, cantidad);
        if (p != null) {
            porcentajeActual = p;
        }
    }

    java.math.BigDecimal descuento = subtotal.multiply(porcentajeActual)
            .divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
    java.math.BigDecimal total = subtotal.subtract(descuento);

    lblSubtotal.setText(moneda.format(subtotal));
    if (porcentajeActual.signum() > 0) {
        lblDescuento.setText("Descuento " + porcentajeActual.stripTrailingZeros().toPlainString()
                + "%: -" + moneda.format(descuento));
    } else {
        lblDescuento.setText("Sin descuento");
    }
    lblTotal.setText(moneda.format(total));
}
   
    private void aplicarEstilo() {
    java.awt.Color azul  = new java.awt.Color(0, 48, 95);
    java.awt.Color lila  = new java.awt.Color(236, 236, 252);
    java.awt.Color borde = new java.awt.Color(205, 205, 235);
    java.awt.Color gris  = new java.awt.Color(90, 90, 110);

    // Título del evento y datos
    lblNombreEvento.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
    lblNombreEvento.setForeground(azul);
    lblFecha.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
    lblLugar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
    lblFecha.setForeground(gris);
    lblLugar.setForeground(gris);

    // Panel del resumen con borde redondeado
    pnlResumen.setBackground(lila);
    pnlResumen.setBorder(new javax.swing.border.LineBorder(borde, 2, true));

    // Valores del resumen
    lblSubtotal.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15));
    lblDescuento.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15));
    lblDescuento.setForeground(new java.awt.Color(200, 30, 60));
    lblTotal.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
    lblTotal.setForeground(azul);
    lblSubtotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    lblDescuento.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    lblTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

    // Botones
    for (javax.swing.JButton b : new javax.swing.JButton[]{btnAplicar, btnComprar}) {
        b.setBackground(azul);
        b.setForeground(java.awt.Color.WHITE);
        b.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }
    btnComprar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
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
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        btnInicio = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        btnInicio1 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        lblImagen = new javax.swing.JLabel();
        lblNombreEvento = new javax.swing.JLabel();
        cmbZona = new javax.swing.JComboBox<>();
        pnlResumen = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        spnCantidad = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        btnAplicar = new BotonCuadrado();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel7 = new javax.swing.JLabel();
        lblDescuento = new javax.swing.JLabel();
        lblSubtotal = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        btnComprar = new BotonCuadrado();
        jLabel13 = new javax.swing.JLabel();
        cmbMetodo = new javax.swing.JComboBox<>();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        lblFecha = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        lblLugar = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Serif", 0, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText("TicketGo");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(35, 11, -1, -1));

        btnInicio.setText("Inicio");
        btnInicio.setBorderPainted(false);
        btnInicio.setContentAreaFilled(false);
        btnInicio.addActionListener(this::btnInicioActionPerformed);
        jPanel2.add(btnInicio, new org.netbeans.lib.awtextra.AbsoluteConstraints(411, 14, -1, -1));

        jButton2.setText("Eventos");
        jButton2.setBorderPainted(false);
        jButton2.setContentAreaFilled(false);
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jPanel2.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(511, 14, -1, -1));

        jButton3.setText("Mi actividad");
        jButton3.setBorderPainted(false);
        jButton3.setContentAreaFilled(false);
        jButton3.addActionListener(this::jButton3ActionPerformed);
        jPanel2.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(609, 14, -1, -1));

        btnInicio1.setForeground(new java.awt.Color(51, 0, 153));
        btnInicio1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/cerrar-sesion (1).png"))); // NOI18N
        btnInicio1.setText("Cerrar sesión");
        btnInicio1.setBorderPainted(false);
        btnInicio1.setContentAreaFilled(false);
        btnInicio1.addActionListener(this::btnInicio1ActionPerformed);
        jPanel2.add(btnInicio1, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 10, -1, 30));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 850, 50));

        jSeparator1.setBackground(new java.awt.Color(0, 0, 51));
        jPanel1.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 830, 30));
        jPanel1.add(lblImagen, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 110, 240, 130));
        lblImagen.getAccessibleContext().setAccessibleDescription("");

        lblNombreEvento.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jPanel1.add(lblNombreEvento, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 70, 340, 30));

        cmbZona.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(cmbZona, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 420, 160, -1));

        pnlResumen.setBackground(new java.awt.Color(227, 227, 250));
        pnlResumen.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setText("Resumen de compra");

        jLabel5.setText("Cantidad de boletas");

        jLabel6.setText("Código de descuento");

        txtCodigo.setBackground(new java.awt.Color(255, 255, 255));
        txtCodigo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 102)));
        txtCodigo.addActionListener(this::txtCodigoActionPerformed);

        btnAplicar.setBackground(new java.awt.Color(0, 51, 102));
        btnAplicar.setText("Aplicar");
        btnAplicar.addActionListener(this::btnAplicarActionPerformed);

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setText("Subtotal");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setText("Total");

        btnComprar.setBackground(new java.awt.Color(0, 51, 102));
        btnComprar.setText("Realizar compra");
        btnComprar.addActionListener(this::btnComprarActionPerformed);

        jLabel13.setText("Método de pago");

        cmbMetodo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pse", "Efectivo", "Daviplata", "Nequi", "Tarjeta", " " }));

        javax.swing.GroupLayout pnlResumenLayout = new javax.swing.GroupLayout(pnlResumen);
        pnlResumen.setLayout(pnlResumenLayout);
        pnlResumenLayout.setHorizontalGroup(
            pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenLayout.createSequentialGroup()
                .addGap(92, 92, 92)
                .addComponent(btnComprar)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(pnlResumenLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlResumenLayout.createSequentialGroup()
                        .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                            .addGroup(pnlResumenLayout.createSequentialGroup()
                                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel6)
                                    .addComponent(spnCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(txtCodigo))
                                .addGap(18, 18, 18)
                                .addComponent(btnAplicar))
                            .addComponent(jLabel7)
                            .addComponent(jSeparator3))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlResumenLayout.createSequentialGroup()
                        .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(jLabel13))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(cmbMetodo, 0, 108, Short.MAX_VALUE)
                            .addComponent(lblTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(37, 37, 37))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlResumenLayout.createSequentialGroup()
                        .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblDescuento, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(pnlResumenLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(lblSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(33, 33, 33))))
        );
        pnlResumenLayout.setVerticalGroup(
            pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(spnCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAplicar))
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(lblSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addComponent(lblDescuento, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(lblTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(cmbMetodo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addComponent(btnComprar, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
        );

        jPanel1.add(pnlResumen, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 100, 300, 420));

        jPanel3.setBackground(new java.awt.Color(204, 204, 255));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/calendario.png"))); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 260, 270, 40));

        jPanel4.setBackground(new java.awt.Color(204, 204, 255));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/ubicacion.png"))); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblLugar, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblLugar, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 320, -1, 40));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setText("Zona");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 390, -1, -1));

        jPanel5.setBackground(new java.awt.Color(204, 204, 255));

        jLabel9.setBackground(new java.awt.Color(204, 204, 204));
        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(153, 153, 153));
        jLabel9.setText("Copyright © 2026  Ticketgo . Todos los derechos reservados");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(270, 270, 270)
                .addComponent(jLabel9)
                .addContainerGap(274, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel9)
                .addContainerGap(10, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 560, 850, 40));

        jButton1.setBackground(new java.awt.Color(0, 0, 102));
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/volver.png"))); // NOI18N
        jButton1.setText("Regresar");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 530, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnInicioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInicioActionPerformed
        // TODO add your handling code here:
        // btnInicio, dentro de Eventos
        new Cliente_inicio().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnInicioActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        // btnEventos, dentro de Cliente
        new Cliente_eventos().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
         new Cliente_misboletas().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void txtCodigoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCodigoActionPerformed

    private void btnAplicarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAplicarActionPerformed
        // TODO add your handling code here:
        String codigo = txtCodigo.getText().trim();

    if (codigo.isEmpty()) {
        codigoAplicado = null;
        actualizarResumen();
        return;
    }

    int cantidad = (Integer) spnCantidad.getValue();
    if (obtenerPorcentaje(codigo, cantidad) == null) {
        JOptionPane.showMessageDialog(this,
            "El código no existe, está vencido o no cumple la cantidad mínima de boletas.");
        codigoAplicado = null;
    } else {
        codigoAplicado = codigo;
    }
    actualizarResumen();
    }//GEN-LAST:event_btnAplicarActionPerformed

    private void btnComprarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnComprarActionPerformed
        // TODO add your handling code here:
         int i = cmbZona.getSelectedIndex();
    if (i < 0) {
        JOptionPane.showMessageDialog(this, "Selecciona una zona.");
        return;
    }
    if (cupos.get(i) <= 0) {
        JOptionPane.showMessageDialog(this, "Esa zona está agotada.");
        return;
    }

    int cantidad = (Integer) spnCantidad.getValue();
    String metodo = cmbMetodo.getSelectedItem().toString();
    String codigo = (porcentajeActual.signum() > 0) ? codigoAplicado : null;

    int confirmar = JOptionPane.showConfirmDialog(this,
            "¿Confirmas la compra de " + cantidad + " boleta(s)?\nTotal: " + lblTotal.getText(),
            "Confirmar compra", JOptionPane.YES_NO_OPTION);
    if (confirmar != JOptionPane.YES_OPTION) {
        return;
    }

    // La venta se registra llamando al SP (sin INSERT escritos en Java)
    try (CallableStatement cs = conexion.getInstancia().getConexion().prepareCall("{CALL sp_registrar_venta(?, ?, ?, ?, ?)}")) {
        cs.setInt(1, Sesion.idUsuario);
        cs.setInt(2, idsZona.get(i));
        cs.setInt(3, cantidad);
        cs.setString(4, metodo);
        cs.setString(5, codigo);

        try (ResultSet rs = cs.executeQuery()) {
            if (rs.next()) {
                String mensaje = rs.getString("mensaje");
                if (mensaje.equals("Venta registrada")) {
                    JOptionPane.showMessageDialog(this,
                        "¡Compra exitosa! Número de venta: " + rs.getInt("id_venta"));
                    txtCodigo.setText("");
                    codigoAplicado = null;
                    spnCantidad.setValue(1);
                    cargarZonas();           // el trigger ya descontó el cupo
                    actualizarResumen();
                } else {
                    JOptionPane.showMessageDialog(this, mensaje);
                }
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudo completar la compra: " + e.getMessage());
    }
    }//GEN-LAST:event_btnComprarActionPerformed

    private void btnInicio1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInicio1ActionPerformed
        // TODO add your handling code here:
        int opcion = JOptionPane.showConfirmDialog(
            this,
            "¿Seguro que deseas cerrar sesión?",
            "Cerrar sesión",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (opcion == JOptionPane.YES_OPTION) {
            Login login = new Login();
            login.setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_btnInicio1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        Cliente_eventos ventana = new Cliente_eventos();
        ventana.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new Cliente_detalleEvento(1).setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAplicar;
    private javax.swing.JButton btnComprar;
    private javax.swing.JButton btnInicio;
    private javax.swing.JButton btnInicio1;
    private javax.swing.JComboBox<String> cmbMetodo;
    private javax.swing.JComboBox<String> cmbZona;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
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
    private javax.swing.JPanel jPanel5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel lblDescuento;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblImagen;
    private javax.swing.JLabel lblLugar;
    private javax.swing.JLabel lblNombreEvento;
    private javax.swing.JLabel lblSubtotal;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel pnlResumen;
    private javax.swing.JSpinner spnCantidad;
    private javax.swing.JTextField txtCodigo;
    // End of variables declaration//GEN-END:variables
}
