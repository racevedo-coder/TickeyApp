/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package vista;

import conexion.conexion;
import conexion.Sesion;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author HP
 */
public class FrmEvento extends javax.swing.JDialog {
    private int idEvento;
    private int idZonaSeleccionada = 0;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmEvento.class.getName());

    /**
     * Creates new form FrmEvento
     */
   public FrmEvento(java.awt.Frame parent, boolean modal, int idEvento, boolean soloLectura) {
    super(parent, modal);
    initComponents();
    activarScroll();
    this.idEvento = idEvento;
    setLocationRelativeTo(parent);

    spnFechaEvento.setModel(new javax.swing.SpinnerDateModel());
    spnFechaEvento.setEditor(new javax.swing.JSpinner.DateEditor(spnFechaEvento, "yyyy-MM-dd HH:mm"));
    cmbCategoriaEvento.setEditable(true);
    txtImagenEvento.setEditable(false);

    limitar(txtZonaNombre, 30, false);
    limitar(txtZonaCupo, 6, true);
    limitar(txtZonaPrecio, 8, true);

    tblZonas.setRowHeight(26);
    tblZonas.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            cargarZonaEnCampos();
        }
    });

    cargarCombos();
    if (idEvento > 0) {
        cargarEvento();
        cargarZonas();
    }
    habilitarZonas(idEvento > 0);

    if (soloLectura) {
        lblTituloEvento.setText("Detalle del evento");
        activarLectura();
    } else if (idEvento > 0) {
        lblTituloEvento.setText("Editar evento");
    } else {
        lblTituloEvento.setText("Nuevo evento");
    }
}
   
    private void cargarCombos() {
    java.util.LinkedHashSet<String> estados = new java.util.LinkedHashSet<>();
    estados.add("borrador");
    estados.add("publicado");
    java.util.LinkedHashSet<String> categorias = new java.util.LinkedHashSet<>();

    try (PreparedStatement ps = conexion.getInstancia().getConexion()
            .prepareStatement("SELECT DISTINCT estado, categoria FROM evento");
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            estados.add(rs.getString("estado"));
            categorias.add(rs.getString("categoria"));
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudieron cargar las opciones.");
    }

    cmbEstadoEvento.setModel(new javax.swing.DefaultComboBoxModel<>(estados.toArray(new String[0])));
    cmbCategoriaEvento.setModel(new javax.swing.DefaultComboBoxModel<>(categorias.toArray(new String[0])));
}

    private void cargarEvento() {
    String sql = "SELECT nombre, categoria, fecha, lugar, estado, imagen "
               + "FROM evento WHERE id_evento = ?";
    try (PreparedStatement ps = conexion.getInstancia().getConexion().prepareStatement(sql)) {
        ps.setInt(1, idEvento);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                txtNombreEvento.setText(rs.getString("nombre"));
                cmbCategoriaEvento.setSelectedItem(rs.getString("categoria"));
                spnFechaEvento.setValue(new java.util.Date(rs.getTimestamp("fecha").getTime()));
                txtLugarEvento.setText(rs.getString("lugar"));
                cmbEstadoEvento.setSelectedItem(rs.getString("estado"));
                String img = rs.getString("imagen");
                txtImagenEvento.setText(img == null ? "" : img);
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudo cargar el evento.");
    }
  }  
   
    
    
    
    private void guardarEvento() {
    String nombre = txtNombreEvento.getText().trim();
    String lugar = txtLugarEvento.getText().trim();
    String imagen = txtImagenEvento.getText().trim();
    Object cat = cmbCategoriaEvento.getSelectedItem();
    Object est = cmbEstadoEvento.getSelectedItem();
    String categoria = (cat == null) ? "" : cat.toString().trim();

    if (nombre.isEmpty() || lugar.isEmpty() || categoria.isEmpty() || est == null) {
        JOptionPane.showMessageDialog(this, "Completa nombre, categoría, lugar y estado.");
        return;
    }
    if (nombre.length() > 100 || lugar.length() > 100 || categoria.length() > 20) {
        JOptionPane.showMessageDialog(this,
            "Máximo: nombre 100, lugar 100 y categoría 20 caracteres.");
        return;
    }

    if (est.toString().equals("publicado")
            && (idEvento == 0 || tblZonas.getRowCount() == 0)) {
        JOptionPane.showMessageDialog(this,
            "Para publicar, el evento necesita al menos una zona.\n"
            + "Guárdalo como borrador, agrega sus zonas y luego publícalo.");
        return;
    }

    java.util.Date fechaElegida = (java.util.Date) spnFechaEvento.getValue();

    try (CallableStatement cs = conexion.getInstancia().getConexion()
            .prepareCall("{CALL sp_guardar_evento(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")) {
        cs.setInt(1, idEvento);
        cs.setString(2, nombre);
        cs.setString(3, categoria);
        cs.setTimestamp(4, new java.sql.Timestamp(fechaElegida.getTime()));
        cs.setString(5, lugar);
        cs.setString(6, est.toString());
        if (imagen.isEmpty()) {
            cs.setNull(7, java.sql.Types.VARCHAR);
        } else {
            cs.setString(7, imagen);
        }
        cs.setInt(8, Sesion.idUsuario);
        cs.registerOutParameter(9, java.sql.Types.VARCHAR);
        cs.registerOutParameter(10, java.sql.Types.INTEGER);
        cs.execute();

        String resultado = cs.getString(9);
        int idGuardado = cs.getInt(10);

        if (resultado.equals("creado")) {
            idEvento = idGuardado;
            lblTituloEvento.setText("Editar evento");
            btnCancelar.setText("Cerrar");
            habilitarZonas(true);
            cargarZonas();
            JOptionPane.showMessageDialog(this,
                "Evento creado. Ahora agrega sus zonas con su cupo y precio.");
        } else {
            JOptionPane.showMessageDialog(this, "Evento actualizado.");
            dispose();
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudo guardar el evento: " + e.getMessage());
    }
}
    
    
    private void cargarZonas() {
    DefaultTableModel modelo = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    String[] columnas = {"ID", "Zona", "Cupo total", "Disponibles", "Precio"};
    for (String c : columnas) {
        modelo.addColumn(c);
    }

    String sql = "SELECT id_zona, nombre, cupo_total, cupo_disponible, precio "
               + "FROM zona WHERE id_evento = ? ORDER BY precio";
    try (PreparedStatement ps = conexion.getInstancia().getConexion().prepareStatement(sql)) {
        ps.setInt(1, idEvento);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getInt("id_zona"),
                    rs.getString("nombre"),
                    rs.getInt("cupo_total"),
                    rs.getInt("cupo_disponible"),
                    rs.getBigDecimal("precio")
                });
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudieron cargar las zonas.");
    }

    tblZonas.setModel(modelo);
    tblZonas.getColumnModel().getColumn(4).setCellRenderer(
        new javax.swing.table.DefaultTableCellRenderer() {
            private final java.text.NumberFormat moneda =
                    java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("es", "CO"));
            @Override
            protected void setValue(Object valor) {
                setText(valor instanceof Number ? moneda.format(valor) : "");
                setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            }
        });
    limpiarZona();
}
    
    private void cargarZonaEnCampos() {
    int fila = tblZonas.getSelectedRow();
    if (fila < 0) {
        return;
    }
    idZonaSeleccionada = (Integer) tblZonas.getValueAt(fila, 0);
    txtZonaNombre.setText(String.valueOf(tblZonas.getValueAt(fila, 1)));
    txtZonaCupo.setText(String.valueOf(tblZonas.getValueAt(fila, 2)));
    txtZonaPrecio.setText(new java.math.BigDecimal(tblZonas.getValueAt(fila, 4).toString())
            .stripTrailingZeros().toPlainString());
}

private void limpiarZona() {
    idZonaSeleccionada = 0;
    tblZonas.clearSelection();
    txtZonaNombre.setText("");
    txtZonaCupo.setText("");
    txtZonaPrecio.setText("");
}

private void guardarZona() {
    String nombre = txtZonaNombre.getText().trim();
    String cupoTxt = txtZonaCupo.getText().trim();
    String precioTxt = txtZonaPrecio.getText().trim();

    if (nombre.isEmpty() || cupoTxt.isEmpty() || precioTxt.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Completa el nombre, el cupo y el precio de la zona.");
        return;
    }

    int cupo;
    java.math.BigDecimal precio;
    try {
        cupo = Integer.parseInt(cupoTxt);
        precio = new java.math.BigDecimal(precioTxt);
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "El cupo y el precio deben ser números.");
        return;
    }
    if (cupo < 1 || precio.signum() <= 0) {
        JOptionPane.showMessageDialog(this, "El cupo y el precio deben ser mayores que cero.");
        return;
    }

    try (CallableStatement cs = conexion.getInstancia().getConexion()
            .prepareCall("{CALL sp_guardar_zona(?, ?, ?, ?, ?, ?)}")) {
        cs.setInt(1, idZonaSeleccionada);
        cs.setInt(2, idEvento);
        cs.setString(3, nombre);
        cs.setInt(4, cupo);
        cs.setBigDecimal(5, precio);
        cs.registerOutParameter(6, java.sql.Types.VARCHAR);
        cs.execute();

        String resultado = cs.getString(6);
        switch (resultado) {
            case "creada":
                JOptionPane.showMessageDialog(this, "Zona creada.");
                break;
            case "actualizada":
                JOptionPane.showMessageDialog(this, "Zona actualizada.");
                break;
            case "cupo_menor_vendidas":
                JOptionPane.showMessageDialog(this,
                    "El cupo no puede ser menor que las boletas ya vendidas de esta zona.");
                return;
            default:
                JOptionPane.showMessageDialog(this, "Datos de zona no válidos.");
                return;
        }
        cargarZonas();
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudo guardar la zona: " + e.getMessage());
    }
}


private void quitarZona() {
    if (idZonaSeleccionada <= 0) {
        JOptionPane.showMessageDialog(this, "Selecciona una zona de la tabla.");
        return;
    }
    int confirmar = JOptionPane.showConfirmDialog(this,
            "¿Quitar esta zona?", "Confirmar", JOptionPane.YES_NO_OPTION);
    if (confirmar != JOptionPane.YES_OPTION) {
        return;
    }

    try (CallableStatement cs = conexion.getInstancia().getConexion()
            .prepareCall("{CALL sp_eliminar_zona(?, ?)}")) {
        cs.setInt(1, idZonaSeleccionada);
        cs.registerOutParameter(2, java.sql.Types.VARCHAR);
        cs.execute();

        if (cs.getString(2).equals("eliminada")) {
            JOptionPane.showMessageDialog(this, "Zona eliminada.");
            cargarZonas();
        } else {
            JOptionPane.showMessageDialog(this,
                "Esta zona ya tiene boletas vendidas y no se puede quitar.");
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudo quitar la zona: " + e.getMessage());
    }
}
    
  private void activarLectura() {
    txtNombreEvento.setEditable(false);
    txtLugarEvento.setEditable(false);
    cmbCategoriaEvento.setEnabled(false);
    cmbEstadoEvento.setEnabled(false);
    spnFechaEvento.setEnabled(false);
    btnExaminarImagen.setVisible(false);
    btnGuardarEvento.setVisible(false);
    pnlGestionZona.setVisible(false);
    lblAvisoZonas.setVisible(false);
    btnCancelar.setText("Cerrar");
}
    
    private void habilitarZonas(boolean habilitar) {
    txtZonaNombre.setEnabled(habilitar);
    txtZonaCupo.setEnabled(habilitar);
    txtZonaPrecio.setEnabled(habilitar);
    btnGuardarZona.setEnabled(habilitar);
    btnNuevaZona.setEnabled(habilitar);
    btnQuitarZona.setEnabled(habilitar);
    lblAvisoZonas.setVisible(!habilitar);
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
  
  
  
  private void activarScroll() {
    java.awt.Container contenido = getContentPane();
    javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(contenido);
    scroll.setBorder(null);
    scroll.getVerticalScrollBar().setUnitIncrement(16);
    setContentPane(scroll);

    java.awt.Rectangle pantalla = java.awt.GraphicsEnvironment
            .getLocalGraphicsEnvironment().getMaximumWindowBounds();
    setSize(Math.min(getWidth(), pantalla.width),
            Math.min(getHeight(), pantalla.height - 20));
}/**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtNombreEvento = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtLugarEvento = new javax.swing.JTextField();
        cmbCategoriaEvento = new javax.swing.JComboBox<>();
        spnFechaEvento = new javax.swing.JSpinner();
        cmbEstadoEvento = new javax.swing.JComboBox<>();
        btnExaminarImagen = new javax.swing.JButton();
        btnGuardarEvento = new javax.swing.JButton();
        txtImagenEvento = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        lblTituloEvento = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        lblAvisoZonas = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblZonas = new javax.swing.JTable();
        pnlGestionZona = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        txtZonaNombre = new javax.swing.JTextField();
        btnGuardarZona = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        txtZonaCupo = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtZonaPrecio = new javax.swing.JTextField();
        btnNuevaZona = new javax.swing.JButton();
        btnQuitarZona = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        btnCancelar = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(234, 234, 252));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel2.setBackground(new java.awt.Color(153, 153, 255));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setPreferredSize(new java.awt.Dimension(600, 508));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Nombre del Evento");
        jPanel3.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 55, -1, -1));

        jLabel2.setText("Categoría");
        jPanel3.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 83, -1, -1));

        jLabel3.setText("Fecha del evento");
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 114, -1, -1));

        jLabel4.setText("Lugar");
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 142, -1, -1));
        jPanel3.add(txtNombreEvento, new org.netbeans.lib.awtextra.AbsoluteConstraints(214, 52, 186, -1));

        jLabel5.setText("Estado");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 179, -1, -1));

        jLabel6.setText("Imagen del evento");
        jPanel3.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 219, -1, -1));
        jPanel3.add(txtLugarEvento, new org.netbeans.lib.awtextra.AbsoluteConstraints(214, 142, 186, -1));

        cmbCategoriaEvento.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel3.add(cmbCategoriaEvento, new org.netbeans.lib.awtextra.AbsoluteConstraints(214, 80, 186, -1));
        jPanel3.add(spnFechaEvento, new org.netbeans.lib.awtextra.AbsoluteConstraints(214, 108, 186, -1));

        cmbEstadoEvento.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel3.add(cmbEstadoEvento, new org.netbeans.lib.awtextra.AbsoluteConstraints(214, 176, 186, -1));

        btnExaminarImagen.setText("Examinar");
        btnExaminarImagen.addActionListener(this::btnExaminarImagenActionPerformed);
        jPanel3.add(btnExaminarImagen, new org.netbeans.lib.awtextra.AbsoluteConstraints(406, 216, -1, -1));

        btnGuardarEvento.setBackground(new java.awt.Color(0, 0, 102));
        btnGuardarEvento.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardarEvento.setText("Guardar evento");
        btnGuardarEvento.addActionListener(this::btnGuardarEventoActionPerformed);
        jPanel3.add(btnGuardarEvento, new org.netbeans.lib.awtextra.AbsoluteConstraints(199, 257, -1, 32));

        txtImagenEvento.addActionListener(this::txtImagenEventoActionPerformed);
        jPanel3.add(txtImagenEvento, new org.netbeans.lib.awtextra.AbsoluteConstraints(214, 216, 186, -1));

        jPanel6.setBackground(new java.awt.Color(225, 239, 253));
        jPanel6.setForeground(new java.awt.Color(0, 0, 0));

        lblTituloEvento.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTituloEvento.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(123, 123, 123)
                .addComponent(lblTituloEvento, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(151, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTituloEvento, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(8, Short.MAX_VALUE))
        );

        jPanel3.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 520, 40));

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel5.add(lblAvisoZonas, new org.netbeans.lib.awtextra.AbsoluteConstraints(22, 57, 507, 20));

        tblZonas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tblZonas);

        jPanel5.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(22, 83, 600, 165));

        pnlGestionZona.setBackground(new java.awt.Color(225, 239, 253));

        jLabel10.setText("Nombre zona");

        btnGuardarZona.setText("Guardar zona");
        btnGuardarZona.addActionListener(this::btnGuardarZonaActionPerformed);

        jLabel11.setText("Cupo:");

        jLabel12.setText("Precio");

        btnNuevaZona.setText("Nueva Zona");
        btnNuevaZona.addActionListener(this::btnNuevaZonaActionPerformed);

        btnQuitarZona.setText("Quitar Zona");
        btnQuitarZona.addActionListener(this::btnQuitarZonaActionPerformed);

        javax.swing.GroupLayout pnlGestionZonaLayout = new javax.swing.GroupLayout(pnlGestionZona);
        pnlGestionZona.setLayout(pnlGestionZonaLayout);
        pnlGestionZonaLayout.setHorizontalGroup(
            pnlGestionZonaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGestionZonaLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(pnlGestionZonaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlGestionZonaLayout.createSequentialGroup()
                        .addComponent(btnGuardarZona, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addComponent(btnNuevaZona)
                        .addGap(18, 18, 18)
                        .addComponent(btnQuitarZona))
                    .addGroup(pnlGestionZonaLayout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtZonaNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtZonaCupo, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel12)
                        .addGap(18, 18, 18)
                        .addComponent(txtZonaPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        pnlGestionZonaLayout.setVerticalGroup(
            pnlGestionZonaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGestionZonaLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(pnlGestionZonaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtZonaNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(txtZonaCupo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(txtZonaPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addGroup(pnlGestionZonaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardarZona, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNuevaZona, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnQuitarZona, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23))
        );

        jPanel5.add(pnlGestionZona, new org.netbeans.lib.awtextra.AbsoluteConstraints(22, 266, 590, -1));

        jPanel7.setBackground(new java.awt.Color(225, 239, 253));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setText("Zonas y precios");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel9)
                .addContainerGap(497, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addComponent(jLabel9)
                .addContainerGap())
        );

        jPanel5.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 620, 40));

        btnCancelar.setBackground(new java.awt.Color(0, 0, 102));
        btnCancelar.setForeground(new java.awt.Color(255, 255, 255));
        btnCancelar.setText("Cerrar");
        btnCancelar.addActionListener(this::btnCancelarActionPerformed);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(112, 112, 112)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 513, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(92, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(57, 57, 57))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 331, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 404, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(102, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(298, -6, 740, 920));

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/fondoe.png"))); // NOI18N
        jLabel8.setText("jLabel8");
        jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(-3, -4, 300, 880));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 876, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtImagenEventoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtImagenEventoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtImagenEventoActionPerformed

    private void btnGuardarEventoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarEventoActionPerformed
        // TODO add your handling code here:
        guardarEvento();
    }//GEN-LAST:event_btnGuardarEventoActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        // TODO add your handling code here:
        dispose();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnExaminarImagenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExaminarImagenActionPerformed
        // TODO add your handling code here:
        javax.swing.JFileChooser selector = new javax.swing.JFileChooser();
selector.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
        "Imágenes", "jpg", "jpeg", "png"));
if (selector.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
    java.io.File origen = selector.getSelectedFile();
    java.io.File carpeta = new java.io.File("imagenes");
    carpeta.mkdirs();
    java.io.File destino = new java.io.File(carpeta, origen.getName());
    try {
        java.nio.file.Files.copy(origen.toPath(), destino.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        txtImagenEvento.setText("imagenes/" + origen.getName());
    } catch (java.io.IOException ex) {
        JOptionPane.showMessageDialog(this, "No se pudo copiar la imagen.");
    }
}
    }//GEN-LAST:event_btnExaminarImagenActionPerformed

    private void btnGuardarZonaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarZonaActionPerformed
        // TODO add your handling code here:
        guardarZona();
    }//GEN-LAST:event_btnGuardarZonaActionPerformed

    private void btnNuevaZonaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevaZonaActionPerformed
        // TODO add your handling code here:
        limpiarZona();
    }//GEN-LAST:event_btnNuevaZonaActionPerformed

    private void btnQuitarZonaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarZonaActionPerformed
        // TODO add your handling code here:
        quitarZona();
    }//GEN-LAST:event_btnQuitarZonaActionPerformed

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
                FrmEvento dialog = new FrmEvento(new javax.swing.JFrame(), true, 0, false);
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
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnExaminarImagen;
    private javax.swing.JButton btnGuardarEvento;
    private javax.swing.JButton btnGuardarZona;
    private javax.swing.JButton btnNuevaZona;
    private javax.swing.JButton btnQuitarZona;
    private javax.swing.JComboBox<String> cmbCategoriaEvento;
    private javax.swing.JComboBox<String> cmbEstadoEvento;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAvisoZonas;
    private javax.swing.JLabel lblTituloEvento;
    private javax.swing.JPanel pnlGestionZona;
    private javax.swing.JSpinner spnFechaEvento;
    private javax.swing.JTable tblZonas;
    private javax.swing.JTextField txtImagenEvento;
    private javax.swing.JTextField txtLugarEvento;
    private javax.swing.JTextField txtNombreEvento;
    private javax.swing.JTextField txtZonaCupo;
    private javax.swing.JTextField txtZonaNombre;
    private javax.swing.JTextField txtZonaPrecio;
    // End of variables declaration//GEN-END:variables
}
