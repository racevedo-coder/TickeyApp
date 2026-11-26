/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package vista;

import conexion.conexion;
import conexion.Sesion;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author HP
 */
public class Cliente_misboletas extends javax.swing.JFrame {

    private TableRowSorter<DefaultTableModel> sorterBoletas;
    private java.text.NumberFormat moneda
            = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("es", "CO"));
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Cliente_misboletas.class.getName());

    /**
     * Creates new form Cliente_misboletas
     */
    public Cliente_misboletas() {
        initComponents();

        tblBoletas.setRowHeight(32);
        txtPerfilCorreo.setEditable(false);
        txtPerfilDocumento.setEditable(false);
        limitar(txtPerfilNombre, 80, false);
        limitar(txtPerfilTelefono, 20, true);
        limitar(pwdActual, 50, false);
        limitar(pwdNueva, 50, false);
        limitar(pwdConfirmar, 50, false);

        txtBuscarBoleta.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filtrarBoletas();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filtrarBoletas();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filtrarBoletas();
            }
        });
        tblBoletas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    verDetalleBoleta();
                }
            }
        });

        cargarPerfil();
        cargarBoletas();
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

    private void cargarBoletas() {
        DefaultTableModel modelo = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        String[] columnas = {"N.º", "Evento", "Fecha", "Ubicación", "Zona", "Cant.", "Total", "Estado"};
        for (String c : columnas) {
            modelo.addColumn(c);
        }

        java.text.SimpleDateFormat formato = new java.text.SimpleDateFormat(
                "d MMM yyyy, h:mm a", new java.util.Locale("es", "CO"));

        try (CallableStatement cs = conexion.getInstancia().getConexion()
                .prepareCall("{CALL sp_mis_boletas(?)}")) {
            cs.setInt(1, Sesion.idUsuario);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    String estado = rs.getString("estado");
                    modelo.addRow(new Object[]{
                        rs.getInt("id_venta"),
                        rs.getString("evento"),
                        formato.format(rs.getTimestamp("fecha")),
                        rs.getString("lugar"),
                        rs.getString("zona"),
                        rs.getInt("cantidad"),
                        moneda.format(rs.getBigDecimal("total")),
                        estado.equals("pagada") ? "Confirmada"
                        : estado.substring(0, 1).toUpperCase() + estado.substring(1)
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar tus boletas: " + e.getMessage());
        }

        tblBoletas.setModel(modelo);
        sorterBoletas = new TableRowSorter<>(modelo);
        tblBoletas.setRowSorter(sorterBoletas);

        tblBoletas.getColumnModel().getColumn(7).setCellRenderer(
                new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable tabla,
                    Object valor, boolean seleccionada, boolean foco, int fila, int columna) {
                java.awt.Component c = super.getTableCellRendererComponent(
                        tabla, valor, seleccionada, foco, fila, columna);
                setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                if (!seleccionada) {
                    boolean ok = "Confirmada".equals(valor);
                    c.setBackground(ok ? new java.awt.Color(214, 245, 220)
                            : new java.awt.Color(250, 220, 224));
                    c.setForeground(ok ? new java.awt.Color(25, 110, 50)
                            : new java.awt.Color(170, 30, 50));
                }
                return c;
            }
        });

        filtrarBoletas();
    }

    private void filtrarBoletas() {
        if (sorterBoletas == null) {
            return;
        }
        String texto = txtBuscarBoleta.getText().trim();
        if (texto.isEmpty()) {
            sorterBoletas.setRowFilter(null);
        } else {
            sorterBoletas.setRowFilter(
                    RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto)));
        }
    }

    private void verDetalleBoleta() {
        int fila = tblBoletas.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una compra de la tabla.");
            return;
        }
        String detalle = "Compra N.º " + tblBoletas.getValueAt(fila, 0)
                + "\nEvento: " + tblBoletas.getValueAt(fila, 1)
                + "\nFecha: " + tblBoletas.getValueAt(fila, 2)
                + "\nUbicación: " + tblBoletas.getValueAt(fila, 3)
                + "\nZona: " + tblBoletas.getValueAt(fila, 4)
                + "\nBoletas: " + tblBoletas.getValueAt(fila, 5)
                + "\nTotal pagado: " + tblBoletas.getValueAt(fila, 6)
                + "\nEstado: " + tblBoletas.getValueAt(fila, 7);
        JOptionPane.showMessageDialog(this, detalle, "Detalle de tu compra",
                JOptionPane.INFORMATION_MESSAGE);
    }
    //Perfil

    private void cargarPerfil() {
        String sql = "SELECT documento, nombre, correo, telefono FROM usuario WHERE id_usuario = ?";
        try (PreparedStatement ps = conexion.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setInt(1, Sesion.idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nombre = rs.getString("nombre");
                    txtPerfilDocumento.setText(rs.getString("documento"));
                    txtPerfilNombre.setText(nombre);
                    txtPerfilCorreo.setText(rs.getString("correo"));
                    String tel = rs.getString("telefono");
                    txtPerfilTelefono.setText(tel == null ? "" : tel);
                    lblSaludo.setText("¡Hola, " + nombre.split(" ")[0] + "!");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar tu perfil.");
        }
        pwdActual.setText("");
        pwdNueva.setText("");
        pwdConfirmar.setText("");
    }

    private void guardarPerfil() {
        String nombre = txtPerfilNombre.getText().trim();
        String telefono = txtPerfilTelefono.getText().trim();
        String actual = new String(pwdActual.getPassword());
        String nueva = new String(pwdNueva.getPassword());
        String confirmar = new String(pwdConfirmar.getPassword());

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío.");
            return;
        }
        if (!nueva.isEmpty()) {
            if (actual.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Escribe tu contraseña actual para cambiarla.");
                return;
            }
            if (nueva.length() < 6) {
                JOptionPane.showMessageDialog(this, "La nueva contraseña debe tener al menos 6 caracteres.");
                return;
            }
            if (!nueva.equals(confirmar)) {
                JOptionPane.showMessageDialog(this, "La confirmación no coincide con la nueva contraseña.");
                return;
            }
        }

        try (CallableStatement cs = conexion.getInstancia().getConexion()
                .prepareCall("{CALL sp_actualizar_perfil(?, ?, ?, ?, ?, ?)}")) {
            cs.setInt(1, Sesion.idUsuario);
            cs.setString(2, nombre);
            cs.setString(3, telefono.isEmpty() ? null : telefono);
            cs.setString(4, actual);
            cs.setString(5, nueva);
            cs.registerOutParameter(6, java.sql.Types.VARCHAR);
            cs.execute();

            switch (cs.getString(6)) {
                case "actualizado":
                    JOptionPane.showMessageDialog(this, "Tus datos se actualizaron.");
                    cargarPerfil();
                    break;
                case "clave_incorrecta":
                    JOptionPane.showMessageDialog(this, "La contraseña actual no es correcta.");
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Los datos no son válidos.");
                    break;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "No se pudo actualizar: " + e.getMessage());
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
        lblSaludo = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        txtBuscarBoleta = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBoletas = new javax.swing.JTable();
        btnActualizarBoletas = new javax.swing.JButton();
        btnVerBoleta = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        txtPerfilNombre = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtPerfilDocumento = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtPerfilCorreo = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtPerfilTelefono = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        pwdActual = new javax.swing.JPasswordField();
        jLabel12 = new javax.swing.JLabel();
        pwdNueva = new javax.swing.JPasswordField();
        jLabel13 = new javax.swing.JLabel();
        pwdConfirmar = new javax.swing.JPasswordField();
        jButton1 = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        btnInicio = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        btnInicio1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblSaludo.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblSaludo.setForeground(new java.awt.Color(255, 255, 255));
        jPanel1.add(lblSaludo, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 80, 230, 40));

        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Aquí puedes consultar tus boletas y administrar tu perfil");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 120, -1, -1));

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/usuario (3).png"))); // NOI18N
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 80, 70, 60));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/fondo2.png"))); // NOI18N
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 940, 95));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setForeground(new java.awt.Color(51, 0, 153));
        jLabel4.setText("Consulta tus boletas compradas y sus detalles");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 50, 263, -1));
        jPanel2.add(txtBuscarBoleta, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 110, 280, 30));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/buscar (1).png"))); // NOI18N
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, -1, 28));

        tblBoletas.setBackground(new java.awt.Color(218, 218, 255));
        tblBoletas.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblBoletas);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 170, 500, 240));

        btnActualizarBoletas.setBackground(new java.awt.Color(0, 0, 102));
        btnActualizarBoletas.setForeground(new java.awt.Color(255, 255, 255));
        btnActualizarBoletas.setText("Actualizar");
        btnActualizarBoletas.addActionListener(this::btnActualizarBoletasActionPerformed);
        jPanel2.add(btnActualizarBoletas, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 110, 90, 30));

        btnVerBoleta.setBackground(new java.awt.Color(0, 0, 102));
        btnVerBoleta.setForeground(new java.awt.Color(255, 255, 255));
        btnVerBoleta.setText("Ver detalle");
        btnVerBoleta.addActionListener(this::btnVerBoletaActionPerformed);
        jPanel2.add(btnVerBoleta, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 430, -1, 30));

        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/entradas (1).png"))); // NOI18N
        jPanel2.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(51, 0, 153));
        jLabel15.setText("Mis boletas");
        jPanel2.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(76, 18, -1, -1));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 190, 520, 490));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(51, 0, 153));
        jLabel3.setText("Mi perfil");
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 10, -1, -1));

        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/usuario (4).png"))); // NOI18N
        jPanel3.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, -1, -1));

        jLabel17.setText("Aquí puedes ver y actualizar tu información");
        jPanel3.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 40, -1, -1));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(221, 221, 221)));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel4.add(txtPerfilNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 40, 190, -1));

        jLabel7.setText("Nombre:");
        jPanel4.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, -1, -1));
        jPanel4.add(txtPerfilDocumento, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 70, 190, -1));

        jLabel8.setText("Documento:");
        jPanel4.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, -1, -1));

        txtPerfilCorreo.addActionListener(this::txtPerfilCorreoActionPerformed);
        jPanel4.add(txtPerfilCorreo, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 100, 190, -1));

        jLabel9.setText("Correo:");
        jPanel4.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, -1, -1));
        jPanel4.add(txtPerfilTelefono, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 130, 190, -1));

        jLabel10.setText("Teléfono:");
        jPanel4.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, -1, -1));

        jPanel5.setBackground(new java.awt.Color(218, 218, 255));

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(0, 51, 102));
        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/usuario (5).png"))); // NOI18N
        jLabel19.setText("Información personal");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(79, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(8, Short.MAX_VALUE)
                .addComponent(jLabel19)
                .addContainerGap())
        );

        jPanel4.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 330, 30));

        jPanel3.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 330, 170));

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(221, 221, 221)));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel7.setBackground(new java.awt.Color(218, 218, 255));
        jPanel7.setPreferredSize(new java.awt.Dimension(300, 30));

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(0, 51, 102));
        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/cerrar-con-llave.png"))); // NOI18N
        jLabel18.setText("Cambiar contraseña");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(79, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel18)
                .addContainerGap(8, Short.MAX_VALUE))
        );

        jPanel6.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 330, -1));

        jLabel11.setText("Contraseña actual:");
        jPanel6.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, -1, -1));
        jPanel6.add(pwdActual, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 50, 170, -1));

        jLabel12.setText("Nueva contraseñal:");
        jPanel6.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, -1));
        jPanel6.add(pwdNueva, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 80, 170, -1));

        jLabel13.setText("Confirmar contraseña:");
        jPanel6.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, -1, -1));
        jPanel6.add(pwdConfirmar, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 110, 170, -1));

        jPanel3.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 260, 330, 170));

        jButton1.setBackground(new java.awt.Color(0, 0, 102));
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Guardar cambios");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        jPanel3.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 440, -1, 30));

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 190, 370, 490));

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));
        jPanel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 255)));

        jLabel20.setFont(new java.awt.Font("Serif", 0, 18)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(0, 0, 0));
        jLabel20.setText("TicketGo");

        btnInicio.setText("Inicio");
        btnInicio.setBorderPainted(false);
        btnInicio.setContentAreaFilled(false);
        btnInicio.addActionListener(this::btnInicioActionPerformed);

        jButton2.setText("Eventos");
        jButton2.setBorderPainted(false);
        jButton2.setContentAreaFilled(false);
        jButton2.addActionListener(this::jButton2ActionPerformed);

        jButton3.setText("Mi actividad");
        jButton3.setBorderPainted(false);
        jButton3.setContentAreaFilled(false);
        jButton3.addActionListener(this::jButton3ActionPerformed);

        btnInicio1.setForeground(new java.awt.Color(51, 0, 153));
        btnInicio1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/cerrar-sesion (1).png"))); // NOI18N
        btnInicio1.setText("Cerrar sesión");
        btnInicio1.setBorderPainted(false);
        btnInicio1.setContentAreaFilled(false);
        btnInicio1.addActionListener(this::btnInicio1ActionPerformed);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 473, Short.MAX_VALUE)
                .addComponent(btnInicio)
                .addGap(18, 18, 18)
                .addComponent(jButton2)
                .addGap(18, 18, 18)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnInicio1)
                .addGap(14, 14, 14))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(btnInicio)
                    .addComponent(jButton2)
                    .addComponent(jButton3)
                    .addComponent(btnInicio1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 980, 50));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 719, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnActualizarBoletasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarBoletasActionPerformed
        // TODO add your handling code here:
        cargarBoletas();
    }//GEN-LAST:event_btnActualizarBoletasActionPerformed

    private void btnVerBoletaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerBoletaActionPerformed
        // TODO add your handling code here:
        verDetalleBoleta();
    }//GEN-LAST:event_btnVerBoletaActionPerformed

    private void txtPerfilCorreoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPerfilCorreoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPerfilCorreoActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        guardarPerfil();
    }//GEN-LAST:event_jButton1ActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new Cliente_misboletas().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizarBoletas;
    private javax.swing.JButton btnInicio;
    private javax.swing.JButton btnInicio1;
    private javax.swing.JButton btnVerBoleta;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
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
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblSaludo;
    private javax.swing.JPasswordField pwdActual;
    private javax.swing.JPasswordField pwdConfirmar;
    private javax.swing.JPasswordField pwdNueva;
    private javax.swing.JTable tblBoletas;
    private javax.swing.JTextField txtBuscarBoleta;
    private javax.swing.JTextField txtPerfilCorreo;
    private javax.swing.JTextField txtPerfilDocumento;
    private javax.swing.JTextField txtPerfilNombre;
    private javax.swing.JTextField txtPerfilTelefono;
    // End of variables declaration//GEN-END:variables
}
