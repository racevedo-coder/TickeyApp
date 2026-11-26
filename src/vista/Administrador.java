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
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
/**
 *
 * @author HP
 */
public class Administrador extends javax.swing.JFrame {
    private TableRowSorter<DefaultTableModel> sorterEventos;
    private TableRowSorter<DefaultTableModel> sorterUsuarios;
    private TableRowSorter<DefaultTableModel> sorterVentas;
    private java.util.ArrayList<Integer> idsEventoAforo = new java.util.ArrayList<>();
    private boolean cargandoAforo = false;
    private java.text.NumberFormat moneda =
    java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("es", "CO"));
    private TableRowSorter<DefaultTableModel> sorterDescuentos;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Administrador.class.getName());

    /**
     * Creates new form Administrador
     */
    public Administrador() {
        initComponents();
         tblMisEventos.setRowHeight(28);
         tblVentas.setRowHeight(28);
         tblAforo.setRowHeight(28);
         tblDescuentosInicio.setRowHeight(28);
         
    btnNuevoEvento.setToolTipText("Nuevo evento");
    btnEditarEvento.setToolTipText("Editar");
    btnEliminarEvento.setToolTipText("Eliminar");
    btnVerEvento.setToolTipText("Ver detalle");

    
    txtPerfilDocumento.setEditable(false);
    txtPerfilCorreo.setEditable(false);
     limitar(txtPerfilNombre, 80, false);
     limitar(txtPerfilTelefono, 20, true);
     limitar(pwdActual, 50, false);
     limitar(pwdNueva, 50, false);
     limitar(pwdConfirmar, 50, false);
     cargarPerfil();

    txtBuscarEvento.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        public void insertUpdate(javax.swing.event.DocumentEvent e)  { filtrarEventos(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e)  { filtrarEventos(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrarEventos(); }
    });
    
     txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        public void insertUpdate(javax.swing.event.DocumentEvent e)  { filtrarVentas(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e)  { filtrarVentas(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrarVentas(); }
    });
     
     txtBuscarUsuario.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
    public void insertUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltro(sorterUsuarios, txtBuscarUsuario); }
    public void removeUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltro(sorterUsuarios, txtBuscarUsuario); }
    public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(sorterUsuarios, txtBuscarUsuario); }
});
     tblUsuarios.setRowHeight(28);
     cargarUsuarios();

    tblMisEventos.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            if (e.getClickCount() == 2) {
                int id = idFilaSeleccionada();
                if (id > 0) {
                    abrirFormularioEvento(id, true);
                }
            }
        }
    });
    
    tblUsuarios.addMouseListener(new java.awt.event.MouseAdapter() {
    @Override
    public void mouseClicked(java.awt.event.MouseEvent e) {
        if (e.getClickCount() == 2) {
            int id = idUsuarioSeleccionado();
            if (id > 0) {
                abrirFormularioUsuario(id, true);
            }
        }
    }
});

      cmbEventoAforo.addActionListener(e -> {
        if (!cargandoAforo) {
            cargarAforo();
        }
    });

   tabPrincipal.addChangeListener(e -> {
    int idx = tabPrincipal.getSelectedIndex();
    if (idx < 0) {
        return;
    }
    switch (tabPrincipal.getTitleAt(idx)) {
        case "Inicio":      cargarInicio();      break;
        case "Ventas":      cargarReporte();     break;
        case "Mis eventos": cargarMisEventos();  break;
        case "Descuentos":  cargarDescuentos();  break;
        case "Usuarios":    cargarUsuarios();    break;
        case "Mi perfil":   cargarPerfil();      break;
        default: break;
    }
});
  
  txtBuscarDescuento.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
    public void insertUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltro(sorterDescuentos, txtBuscarDescuento); }
    public void removeUpdate(javax.swing.event.DocumentEvent e)  { aplicarFiltro(sorterDescuentos, txtBuscarDescuento); }
    public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(sorterDescuentos, txtBuscarDescuento); }
});
   tblDescuentos.setRowHeight(28);
   tblDescuentos.addMouseListener(new java.awt.event.MouseAdapter() {
    @Override
    public void mouseClicked(java.awt.event.MouseEvent e) {
        if (e.getClickCount() == 2) {
            String cod = codigoSeleccionado();
            if (cod != null) {
                abrirFormularioDescuento(cod, true);
            }
        }
    }
    });
    cargarDescuentos();
    
    cargarMisEventos();
    cargarReporte();
    cargarInicio();

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
  

    
    private void cargarMisEventos() {
    DefaultTableModel modelo = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    String[] columnas = {"ID", "Evento", "Categoría", "Fecha", "Lugar", "Estado", "Zonas"};
    for (String c : columnas) {
        modelo.addColumn(c);
    }

    String sql = "SELECT e.id_evento, e.nombre, e.categoria, e.fecha, e.lugar, e.estado, "
               + "COUNT(z.id_zona) AS zonas "
               + "FROM evento e LEFT JOIN zona z ON z.id_evento = e.id_evento "
               + "GROUP BY e.id_evento, e.nombre, e.categoria, e.fecha, e.lugar, e.estado "
               + "ORDER BY e.fecha DESC";

    try (PreparedStatement ps = conexion.getInstancia().getConexion().prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            modelo.addRow(new Object[]{
                rs.getInt("id_evento"),
                rs.getString("nombre"),
                rs.getString("categoria"),
                rs.getTimestamp("fecha"),
                rs.getString("lugar"),
                rs.getString("estado"),
                rs.getInt("zonas")
            });
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudieron cargar los eventos.");
    }

    tblMisEventos.setModel(modelo);
    sorterEventos = new TableRowSorter<>(modelo);
    tblMisEventos.setRowSorter(sorterEventos);
    filtrarEventos();
}

    
    private void filtrarEventos() {
    if (sorterEventos == null) {
        return;
    }
    String texto = txtBuscarEvento.getText().trim();
    if (texto.isEmpty()) {
        sorterEventos.setRowFilter(null);
    } else {
        sorterEventos.setRowFilter(
            RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto)));
    }
}
    
    private int idFilaSeleccionada() {
    int fila = tblMisEventos.getSelectedRow();
    if (fila < 0) {
        JOptionPane.showMessageDialog(this, "Selecciona un evento de la tabla.");
        return 0;
    }
    return (Integer) tblMisEventos.getValueAt(fila, 0);
}
    
    private void abrirFormularioEvento(int id, boolean soloLectura) {
    FrmEvento dialogo = new FrmEvento(this, true, id, soloLectura);
    dialogo.setVisible(true);
    if (!soloLectura) {
        cargarMisEventos();
    }
}
    
    private void eliminarEvento() {
    int id = idFilaSeleccionada();
    if (id <= 0) {
        return;
    }
    int confirmar = JOptionPane.showConfirmDialog(this,
            "¿Eliminar este evento? Esta acción no se puede deshacer.",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    if (confirmar != JOptionPane.YES_OPTION) {
        return;
    }

    try (CallableStatement cs = conexion.getInstancia().getConexion()
            .prepareCall("{CALL sp_eliminar_evento(?, ?)}")) {
        cs.setInt(1, id);
        cs.registerOutParameter(2, java.sql.Types.VARCHAR);
        cs.execute();

        String resultado = cs.getString(2);
        if (resultado.equals("eliminado")) {
            JOptionPane.showMessageDialog(this, "Evento eliminado.");
            cargarMisEventos();
        } else if (resultado.equals("con_ventas")) {
            JOptionPane.showMessageDialog(this,
                "Este evento ya tiene boletas vendidas y no se puede eliminar.\n"
                + "Cámbialo a borrador desde la edición.");
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar el evento.");
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudo eliminar el evento: " + e.getMessage());
    }
}
    
    //Pestaña ventas
    
    private void cargarReporte() {
    DefaultTableModel modelo = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    String[] columnas = {"Venta", "Fecha", "Cliente", "Documento", "Evento", "Zona",
                         "Precio zona", "Método", "Cantidad", "Total", "Estado"};
    for (String c : columnas) {
        modelo.addColumn(c);
    }

    try (CallableStatement cs = conexion.getInstancia().getConexion()
            .prepareCall("{CALL sp_reporte_ventas()}");
         ResultSet rs = cs.executeQuery()) {
        while (rs.next()) {
            modelo.addRow(new Object[]{
                rs.getInt("id_venta"),
                rs.getTimestamp("fecha_venta"),
                rs.getString("cliente"),
                rs.getString("documento"),
                rs.getString("evento"),
                rs.getString("zona"),
                rs.getString("precio_zona"),
                rs.getString("metodo_pago"),
                rs.getInt("cantidad"),
                rs.getBigDecimal("total"),
                rs.getString("estado")
            });
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudo cargar el reporte: " + e.getMessage());
    }

    tblVentas.setModel(modelo);
    sorterVentas = new TableRowSorter<>(modelo);
    tblVentas.setRowSorter(sorterVentas);

    tblVentas.getColumnModel().getColumn(9).setCellRenderer(
        new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object valor) {
                setText(valor instanceof Number ? moneda.format(valor) : "");
                setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            }
        });

    filtrarVentas();
}
    
    
    //Filtrar las ventas
    private void filtrarVentas() {
    if (sorterVentas == null) {
        return;
    }
    String texto = txtBuscar.getText().trim();
    if (texto.isEmpty()) {
        sorterVentas.setRowFilter(null);
    } else {
        sorterVentas.setRowFilter(
            RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto)));
    }

    java.math.BigDecimal suma = java.math.BigDecimal.ZERO;
    for (int i = 0; i < tblVentas.getRowCount(); i++) {
        Object valor = tblVentas.getValueAt(i, 9);
        if (valor instanceof Number) {
            suma = suma.add(new java.math.BigDecimal(valor.toString()));
        }
    }
    lblResumen.setText(tblVentas.getRowCount() + " ventas - Total: " + moneda.format(suma));
}
    
    //Pestaña inicio
    private void cargarInicio() {
    cargarResumen();
    cargarEventosAforo();
    cargarDescuentosInicio();
}
    
    private void cargarResumen() {
    try (CallableStatement cs = conexion.getInstancia().getConexion()
            .prepareCall("{CALL sp_resumen_admin()}");
         ResultSet rs = cs.executeQuery()) {
        if (rs.next()) {
            lblBoletasMes.setText(String.valueOf(rs.getInt("boletas_mes")));
            lblVentasTotal.setText(String.valueOf(rs.getInt("ventas_total")));
            lblDescuentosAplicados.setText(String.valueOf(rs.getInt("descuentos_aplicados")));
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudo cargar el resumen: " + e.getMessage());
    }
}
    
    //El combo de eventos del aforo
    private void cargarEventosAforo() {
    cargandoAforo = true;
    cmbEventoAforo.removeAllItems();
    idsEventoAforo.clear();

    String sql = "SELECT id_evento, nombre, fecha FROM evento "
           + "WHERE estado = 'publicado' AND fecha >= CURDATE() ORDER BY fecha";
    try (PreparedStatement ps = conexion.getInstancia().getConexion().prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        java.text.SimpleDateFormat formato =
                new java.text.SimpleDateFormat("d MMM", new java.util.Locale("es", "CO"));
        while (rs.next()) {
            idsEventoAforo.add(rs.getInt("id_evento"));
            cmbEventoAforo.addItem(rs.getString("nombre") + " - "
                    + formato.format(rs.getTimestamp("fecha")));
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudieron cargar los eventos.");
    }
    cargandoAforo = false;
    cargarAforo();
}
    
    // La tabla del aforo del evento elegido
    private void cargarAforo() {
    DefaultTableModel modelo = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    modelo.addColumn("Zona");
    modelo.addColumn("Disponibles");
    modelo.addColumn("Cupo total");

    int i = cmbEventoAforo.getSelectedIndex();
    if (i >= 0 && i < idsEventoAforo.size()) {
     String sql = "SELECT nombre, cupo_disponible, cupo_total FROM zona "
           + "WHERE id_evento = ? ORDER BY precio DESC";
        try (PreparedStatement ps = conexion.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setInt(1, idsEventoAforo.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelo.addRow(new Object[]{
                        rs.getString("nombre"),
                        String.valueOf(rs.getInt("cupo_disponible")),
                        String.valueOf(rs.getInt("cupo_total"))
                    });
                }
            }
        } catch (SQLException e) {
    JOptionPane.showMessageDialog(this, "No se pudo cargar el aforo: " + e.getMessage());
}
        
    }
    tblAforo.setModel(modelo);

    javax.swing.table.DefaultTableCellRenderer colorear = new javax.swing.table.DefaultTableCellRenderer() {
        @Override
        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable tabla,
                Object valor, boolean seleccionada, boolean foco, int fila, int columna) {
            java.awt.Component c = super.getTableCellRendererComponent(
                    tabla, valor, seleccionada, foco, fila, columna);
            if (!seleccionada) {
                int disp = Integer.parseInt(tabla.getValueAt(fila, 1).toString());
                int total = Integer.parseInt(tabla.getValueAt(fila, 2).toString());
                boolean casiAgotada = total > 0 && disp * 100 / total <= 20;
                c.setBackground(casiAgotada ? new java.awt.Color(250, 225, 225)
                                            : new java.awt.Color(222, 245, 222));
                c.setForeground(java.awt.Color.BLACK);
            }
            return c;
        }
    };
    for (int col = 0; col < tblAforo.getColumnCount(); col++) {
        tblAforo.getColumnModel().getColumn(col).setCellRenderer(colorear);
    }
}
    
    private void cargarDescuentosInicio() {
    DefaultTableModel modelo = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    String[] columnas = {"Código", "% desc.", "Mín. boletas", "Usos", "Vence", "Estado"};
    for (String c : columnas) {
        modelo.addColumn(c);
    }

    String sql = "SELECT cod_descuento, porcentaje, cant_min, usado, fecha_fin, "
               + "(CURDATE() BETWEEN fecha_inicio AND fecha_fin) AS vigente "
               + "FROM descuento ORDER BY usado DESC";
    try (PreparedStatement ps = conexion.getInstancia().getConexion().prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            modelo.addRow(new Object[]{
                rs.getString("cod_descuento"),
                rs.getBigDecimal("porcentaje").stripTrailingZeros().toPlainString() + " %",
                rs.getInt("cant_min"),
                rs.getInt("usado"),
                rs.getDate("fecha_fin"),
                rs.getBoolean("vigente") ? "Vigente" : "Vencido"
            });
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudieron cargar los descuentos.");
    }
    tblDescuentosInicio.setModel(modelo);
}
    
    //Descuentos
    
    
private void aplicarFiltro(TableRowSorter<DefaultTableModel> sorter, javax.swing.JTextField campo) {
    if (sorter == null) {
        return;
    }
    String texto = campo.getText().trim();
    if (texto.isEmpty()) {
        sorter.setRowFilter(null);
    } else {
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto)));
    }
}

private void cargarDescuentos() {
    DefaultTableModel modelo = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    String[] columnas = {"Código", "Tipo", "% desc.", "Mín. boletas", "Inicio", "Fin", "Usos", "Estado"};
    for (String c : columnas) {
        modelo.addColumn(c);
    }

    String sql = "SELECT cod_descuento, tipo_condicion, porcentaje, cant_min, fecha_inicio, "
               + "fecha_fin, usado, (CURDATE() BETWEEN fecha_inicio AND fecha_fin) AS vigente "
               + "FROM descuento ORDER BY fecha_fin DESC";
    try (PreparedStatement ps = conexion.getInstancia().getConexion().prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            modelo.addRow(new Object[]{
                rs.getString("cod_descuento"),
                rs.getString("tipo_condicion"),
                rs.getBigDecimal("porcentaje").stripTrailingZeros().toPlainString() + " %",
                rs.getInt("cant_min"),
                rs.getDate("fecha_inicio"),
                rs.getDate("fecha_fin"),
                rs.getInt("usado"),
                rs.getBoolean("vigente") ? "Vigente" : "Vencido"
            });
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudieron cargar los descuentos.");
    }

    tblDescuentos.setModel(modelo);
    sorterDescuentos = new TableRowSorter<>(modelo);
    tblDescuentos.setRowSorter(sorterDescuentos);
    aplicarFiltro(sorterDescuentos, txtBuscarDescuento);
}
    
    private String codigoSeleccionado() {
    int fila = tblDescuentos.getSelectedRow();
    if (fila < 0) {
        JOptionPane.showMessageDialog(this, "Selecciona un descuento de la tabla.");
        return null;
    }
    return tblDescuentos.getValueAt(fila, 0).toString();
}

private void abrirFormularioDescuento(String codigo, boolean soloLectura) {
    FrmDescuento dialogo = new FrmDescuento(this, true, codigo, soloLectura);
    dialogo.setVisible(true);
    if (!soloLectura) {
        cargarDescuentos();
    }
}


private void eliminarDescuento() {
    String codigo = codigoSeleccionado();
    if (codigo == null) {
        return;
    }
    int confirmar = JOptionPane.showConfirmDialog(this,
            "¿Eliminar el descuento " + codigo + "?", "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    if (confirmar != JOptionPane.YES_OPTION) {
        return;
    }

    try (CallableStatement cs = conexion.getInstancia().getConexion()
            .prepareCall("{CALL sp_eliminar_descuento(?, ?)}")) {
        cs.setString(1, codigo);
        cs.registerOutParameter(2, java.sql.Types.VARCHAR);
        cs.execute();

        if (cs.getString(2).equals("eliminado")) {
            JOptionPane.showMessageDialog(this, "Descuento eliminado.");
            cargarDescuentos();
        } else {
            JOptionPane.showMessageDialog(this,
                "Este descuento ya se usó en ventas y no se puede eliminar.");
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudo eliminar: " + e.getMessage());
    }
}

    //Usuarios

private void cargarUsuarios() {
    DefaultTableModel modelo = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    String[] columnas = {"ID", "Documento", "Nombre", "Correo", "Teléfono", "Rol", "Registro"};
    for (String c : columnas) {
        modelo.addColumn(c);
    }

    String sql = "SELECT u.id_usuario, u.documento, u.nombre, u.correo, u.telefono, "
               + "r.nombre_rol, u.fecha_creacion "
               + "FROM usuario u INNER JOIN rol r ON u.id_rol = r.id_rol "
               + "ORDER BY u.fecha_creacion DESC";
    try (PreparedStatement ps = conexion.getInstancia().getConexion().prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            modelo.addRow(new Object[]{
                rs.getInt("id_usuario"),
                rs.getString("documento"),
                rs.getString("nombre"),
                rs.getString("correo"),
                rs.getString("telefono"),
                rs.getString("nombre_rol"),
                rs.getTimestamp("fecha_creacion")
            });
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudieron cargar los usuarios.");
    }

    tblUsuarios.setModel(modelo);
    sorterUsuarios = new TableRowSorter<>(modelo);
    tblUsuarios.setRowSorter(sorterUsuarios);
    aplicarFiltro(sorterUsuarios, txtBuscarUsuario);
}

private int idUsuarioSeleccionado() {
    int fila = tblUsuarios.getSelectedRow();
    if (fila < 0) {
        JOptionPane.showMessageDialog(this, "Selecciona un usuario de la tabla.");
        return 0;
    }
    return (Integer) tblUsuarios.getValueAt(fila, 0);
}

private void abrirFormularioUsuario(int id, boolean soloLectura) {
    registrar r = new registrar(true, id, soloLectura);
    r.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosed(java.awt.event.WindowEvent e) {
            if (!soloLectura) {
                cargarUsuarios();
            }
        }
    });
    r.setVisible(true);
}

  private void eliminarUsuario() {
    int id = idUsuarioSeleccionado();
    if (id <= 0) {
        return;
    }
    int confirmar = JOptionPane.showConfirmDialog(this,
            "¿Eliminar este usuario? Esta acción no se puede deshacer.",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    if (confirmar != JOptionPane.YES_OPTION) {
        return;
    }

    try (CallableStatement cs = conexion.getInstancia().getConexion()
            .prepareCall("{CALL sp_eliminar_usuario(?, ?, ?)}")) {
        cs.setInt(1, id);
        cs.setInt(2, Sesion.idUsuario);
        cs.registerOutParameter(3, java.sql.Types.VARCHAR);
        cs.execute();

        switch (cs.getString(3)) {
            case "eliminado":
                JOptionPane.showMessageDialog(this, "Usuario eliminado.");
                cargarUsuarios();
                break;
            case "es_propio":
                JOptionPane.showMessageDialog(this, "No puedes eliminar tu propia cuenta.");
                break;
            case "con_ventas":
                JOptionPane.showMessageDialog(this,
                    "Este usuario tiene compras registradas y no se puede eliminar.");
                break;
            case "con_eventos":
                JOptionPane.showMessageDialog(this,
                    "Este usuario creó eventos y no se puede eliminar.");
                break;
            default:
                JOptionPane.showMessageDialog(this, "No se pudo eliminar el usuario.");
                break;
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "No se pudo eliminar: " + e.getMessage());
    }}
    
    //Perfil
    private void cargarPerfil() {
    String sql = "SELECT documento, nombre, correo, telefono FROM usuario WHERE id_usuario = ?";
    try (PreparedStatement ps = conexion.getInstancia().getConexion().prepareStatement(sql)) {
        ps.setInt(1, Sesion.idUsuario);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                txtPerfilDocumento.setText(rs.getString("documento"));
                txtPerfilNombre.setText(rs.getString("nombre"));
                txtPerfilCorreo.setText(rs.getString("correo"));
                String tel = rs.getString("telefono");
                txtPerfilTelefono.setText(tel == null ? "" : tel);
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
    
    public void irAUsuarios() {
    for (int i = 0; i < tabPrincipal.getTabCount(); i++) {
        if (tabPrincipal.getTitleAt(i).equals("Usuarios")) {
            tabPrincipal.setSelectedIndex(i);
            break;
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

        tabPrincipal = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        lblBoletasMes = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        lblVentasTotal = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        lblDescuentosAplicados = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        btnVerEventos = new javax.swing.JButton();
        btnVerDescuentos = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblDescuentosInicio = new javax.swing.JTable();
        btnCerrarSesion = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        cmbEventoAforo = new javax.swing.JComboBox<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblAforo = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        btnCerrarSesion1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblVentas = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        lblResumen = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        lblResultados = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        btnCerrarSesion2 = new javax.swing.JButton();
        jLabel41 = new javax.swing.JLabel();
        jSeparator8 = new javax.swing.JSeparator();
        jLabel40 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblMisEventos = new javax.swing.JTable();
        btnEditarEvento = new javax.swing.JButton();
        btnEliminarEvento = new javax.swing.JButton();
        btnVerEvento = new javax.swing.JButton();
        btnNuevoEvento = new javax.swing.JButton();
        txtBuscarEvento = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel39 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel19 = new javax.swing.JLabel();
        txtBuscarDescuento = new javax.swing.JTextField();
        jScrollPane5 = new javax.swing.JScrollPane();
        tblDescuentos = new javax.swing.JTable();
        btnEditarDescuento = new javax.swing.JButton();
        btnVerDescuento = new javax.swing.JButton();
        btnEliminarDescuento = new javax.swing.JButton();
        btnNuevoDescuento = new javax.swing.JButton();
        btnEliminarDescuento2 = new javax.swing.JButton();
        btnCerrarSesion3 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jSeparator7 = new javax.swing.JSeparator();
        jScrollPane6 = new javax.swing.JScrollPane();
        tblUsuarios = new javax.swing.JTable();
        jLabel38 = new javax.swing.JLabel();
        txtBuscarUsuario = new javax.swing.JTextField();
        btnEliminarUsuario = new javax.swing.JButton();
        btnNuevoUsuario = new javax.swing.JButton();
        btnEditarUsuario = new javax.swing.JButton();
        btnVerUsuario = new javax.swing.JButton();
        btnCerrarSesion4 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        txtPerfilCorreo = new javax.swing.JTextField();
        txtPerfilDocumento = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        txtPerfilTelefono = new javax.swing.JTextField();
        txtPerfilNombre = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JSeparator();
        jButton4 = new javax.swing.JButton();
        pwdActual = new javax.swing.JPasswordField();
        pwdNueva = new javax.swing.JPasswordField();
        btnCerrarSesion5 = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        pwdConfirmar = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        tabPrincipal.setBackground(new java.awt.Color(153, 153, 255));
        tabPrincipal.setTabPlacement(javax.swing.JTabbedPane.LEFT);

        jPanel3.setBackground(new java.awt.Color(250, 250, 253));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel9.setBackground(new java.awt.Color(228, 228, 249));
        jPanel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel7.setForeground(new java.awt.Color(0, 0, 0));
        jLabel7.setText("Boletas registradas(mes)");

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        lblBoletasMes.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N

        jLabel42.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/boleto.png"))); // NOI18N

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel42)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(lblBoletasMes, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator1))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(lblBoletasMes, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)
                        .addGap(8, 8, 8)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel42, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
        );

        jPanel3.add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 150, 240, 100));

        jPanel10.setBackground(new java.awt.Color(228, 228, 249));
        jPanel10.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jLabel8.setText("Total ventas registradas");

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        lblVentasTotal.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N

        jLabel43.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/carro.png"))); // NOI18N

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel43)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblVentasTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(24, 24, 24))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(lblVentasTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel43)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.add(jPanel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 150, 240, 100));

        jPanel11.setBackground(new java.awt.Color(228, 228, 249));
        jPanel11.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel9.setForeground(new java.awt.Color(0, 0, 0));
        jLabel9.setText("Descuentos aplicados");

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);

        lblDescuentosAplicados.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N

        jLabel44.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/etiqueta.png"))); // NOI18N

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap(19, Short.MAX_VALUE)
                .addComponent(jLabel44, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDescuentosAplicados, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator3, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(lblDescuentosAplicados, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel9)
                        .addGap(8, 8, 8)))
                .addContainerGap())
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabel44, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.add(jPanel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 150, 240, 100));

        btnVerEventos.setBackground(new java.awt.Color(51, 0, 153));
        btnVerEventos.setForeground(new java.awt.Color(255, 255, 255));
        btnVerEventos.setText("Ver todos los eventos");
        btnVerEventos.addActionListener(this::btnVerEventosActionPerformed);
        jPanel3.add(btnVerEventos, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 480, -1, 30));

        btnVerDescuentos.setBackground(new java.awt.Color(51, 0, 153));
        btnVerDescuentos.setForeground(new java.awt.Color(255, 255, 255));
        btnVerDescuentos.setText("Ver todos los descuentos");
        btnVerDescuentos.addActionListener(this::btnVerDescuentosActionPerformed);
        jPanel3.add(btnVerDescuentos, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 480, -1, 30));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText("Seguimiento");
        jPanel3.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 40, 220, 40));

        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("Consulta el rendimiento de ticketgo");
        jPanel3.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 80, 230, -1));

        jPanel12.setBackground(new java.awt.Color(228, 228, 249));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setText("Descuentos aplicados");

        tblDescuentosInicio.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane4.setViewportView(tblDescuentosInicio);

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(100, 100, 100)
                .addComponent(jLabel10)
                .addContainerGap(257, Short.MAX_VALUE))
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4)
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel3.add(jPanel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 280, 500, 190));

        btnCerrarSesion.setBackground(new java.awt.Color(255, 255, 255));
        btnCerrarSesion.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCerrarSesion.setForeground(new java.awt.Color(102, 51, 255));
        btnCerrarSesion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/cerrar-sesion (1).png"))); // NOI18N
        btnCerrarSesion.setText("Cerrar sesión");
        btnCerrarSesion.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        btnCerrarSesion.addActionListener(this::btnCerrarSesionActionPerformed);
        jPanel3.add(btnCerrarSesion, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 20, 110, 30));

        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/fondo2.png"))); // NOI18N
        jPanel3.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 870, 110));

        jPanel13.setBackground(new java.awt.Color(228, 228, 249));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel14.setText("Eventos:");

        cmbEventoAforo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        tblAforo.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane3.setViewportView(tblAforo);

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cmbEventoAforo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(192, Short.MAX_VALUE))
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(cmbEventoAforo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel3.add(jPanel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 280, 340, 190));

        tabPrincipal.addTab("Inicio", jPanel3);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Ventas");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 40, 120, 40));

        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Consulta y gestiona la venta de boletas");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 80, 310, -1));

        btnCerrarSesion1.setBackground(new java.awt.Color(255, 255, 255));
        btnCerrarSesion1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCerrarSesion1.setForeground(new java.awt.Color(102, 51, 255));
        btnCerrarSesion1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/cerrar-sesion (1).png"))); // NOI18N
        btnCerrarSesion1.setText("Cerrar sesión");
        btnCerrarSesion1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        btnCerrarSesion1.addActionListener(this::btnCerrarSesion1ActionPerformed);
        jPanel1.add(btnCerrarSesion1, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 20, 110, 30));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/fondo2.png"))); // NOI18N
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 870, 110));

        tblVentas.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(tblVentas);

        jPanel1.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 190, 790, 230));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("Buscar:");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 140, -1, -1));
        jPanel1.add(txtBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 140, 380, -1));

        jPanel8.setBackground(new java.awt.Color(204, 204, 255));

        jLabel6.setBackground(new java.awt.Color(0, 0, 0));
        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(0, 0, 0));
        jLabel6.setText("Resumen");

        lblResumen.setForeground(new java.awt.Color(0, 0, 0));

        jButton3.setBackground(new java.awt.Color(51, 0, 153));
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setText("Actualizar");
        jButton3.addActionListener(this::jButton3ActionPerformed);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(lblResumen, javax.swing.GroupLayout.PREFERRED_SIZE, 501, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(58, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblResumen, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 440, 700, 70));

        tabPrincipal.addTab("Ventas", jPanel1);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jPanel7.setBackground(new java.awt.Color(204, 204, 255));
        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Mis eventos");
        jPanel7.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 20, -1, -1));
        jPanel7.add(lblResultados, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 120, 360, 20));

        jLabel23.setForeground(new java.awt.Color(255, 255, 255));
        jLabel23.setText("Consulta y gestiona los eventos creados en TicketGo");
        jPanel7.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 70, -1, -1));

        btnCerrarSesion2.setBackground(new java.awt.Color(255, 255, 255));
        btnCerrarSesion2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCerrarSesion2.setForeground(new java.awt.Color(102, 51, 255));
        btnCerrarSesion2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/cerrar-sesion (1).png"))); // NOI18N
        btnCerrarSesion2.setText("Cerrar sesión");
        btnCerrarSesion2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        btnCerrarSesion2.addActionListener(this::btnCerrarSesion2ActionPerformed);
        jPanel7.add(btnCerrarSesion2, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 10, 110, 30));

        jLabel41.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/evento.png"))); // NOI18N
        jPanel7.add(jLabel41, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 80, 70));

        jSeparator8.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator8.setForeground(new java.awt.Color(255, 255, 255));
        jSeparator8.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jPanel7.add(jSeparator8, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 10, 10, 90));

        jLabel40.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/fondo2.png"))); // NOI18N
        jLabel40.setText("jLabel40");
        jPanel7.add(jLabel40, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 830, 110));

        tblMisEventos.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblMisEventos);

        btnEditarEvento.setBackground(new java.awt.Color(51, 0, 153));
        btnEditarEvento.setForeground(new java.awt.Color(255, 255, 255));
        btnEditarEvento.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/lapiz.png"))); // NOI18N
        btnEditarEvento.setText("Editar");
        btnEditarEvento.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnEditarEvento.addActionListener(this::btnEditarEventoActionPerformed);

        btnEliminarEvento.setBackground(new java.awt.Color(255, 153, 153));
        btnEliminarEvento.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminarEvento.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/borrar.png"))); // NOI18N
        btnEliminarEvento.setText("Eliminar");
        btnEliminarEvento.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnEliminarEvento.addActionListener(this::btnEliminarEventoActionPerformed);

        btnVerEvento.setBackground(new java.awt.Color(153, 102, 255));
        btnVerEvento.setForeground(new java.awt.Color(255, 255, 255));
        btnVerEvento.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/ojo.png"))); // NOI18N
        btnVerEvento.setText("Ver");
        btnVerEvento.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnVerEvento.addActionListener(this::btnVerEventoActionPerformed);

        btnNuevoEvento.setBackground(new java.awt.Color(102, 51, 255));
        btnNuevoEvento.setForeground(new java.awt.Color(255, 255, 255));
        btnNuevoEvento.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/agregar.png"))); // NOI18N
        btnNuevoEvento.setText("Nuevo evento");
        btnNuevoEvento.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNuevoEvento.addActionListener(this::btnNuevoEventoActionPerformed);

        jButton1.setBackground(new java.awt.Color(0, 51, 153));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/buscar.png"))); // NOI18N
        jButton1.setText("Buscar");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        jLabel39.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel39.setText("Buscar:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 818, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 851, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnNuevoEvento)
                                .addGap(18, 18, 18)
                                .addComponent(btnEditarEvento, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnVerEvento, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(27, 27, 27)
                                .addComponent(btnEliminarEvento))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                .addGap(38, 38, 38)
                                .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtBuscarEvento, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(411, 411, 411)))))
                .addContainerGap(36, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtBuscarEvento, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(101, 101, 101)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnEliminarEvento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnEditarEvento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnNuevoEvento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnVerEvento, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(33, 33, 33)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(38, Short.MAX_VALUE))
        );

        tabPrincipal.addTab("Mis eventos", jPanel2);

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/fondod.png"))); // NOI18N
        jPanel4.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 520));

        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/descuento (1).png"))); // NOI18N
        jPanel4.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(273, 23, -1, 74));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel17.setText("Descuentos");
        jPanel4.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 30, -1, 28));

        jLabel18.setText("Consulta, administtra y gestiona los eventos disponibles");
        jPanel4.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 70, -1, -1));
        jPanel4.add(jSeparator4, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 110, 580, 10));

        jLabel19.setText("Buscar:");
        jPanel4.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 140, 100, -1));
        jPanel4.add(txtBuscarDescuento, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 140, 300, -1));

        tblDescuentos.setBackground(new java.awt.Color(204, 204, 255));
        tblDescuentos.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane5.setViewportView(tblDescuentos);

        jPanel4.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 270, 650, 170));

        btnEditarDescuento.setBackground(new java.awt.Color(51, 0, 153));
        btnEditarDescuento.setForeground(new java.awt.Color(255, 255, 255));
        btnEditarDescuento.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/lapiz.png"))); // NOI18N
        btnEditarDescuento.setText("Editar");
        btnEditarDescuento.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnEditarDescuento.addActionListener(this::btnEditarDescuentoActionPerformed);
        jPanel4.add(btnEditarDescuento, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 220, 90, 40));

        btnVerDescuento.setBackground(new java.awt.Color(153, 102, 255));
        btnVerDescuento.setForeground(new java.awt.Color(255, 255, 255));
        btnVerDescuento.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/ojo.png"))); // NOI18N
        btnVerDescuento.setText("Ver");
        btnVerDescuento.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnVerDescuento.addActionListener(this::btnVerDescuentoActionPerformed);
        jPanel4.add(btnVerDescuento, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 220, 90, 40));

        btnEliminarDescuento.setBackground(new java.awt.Color(255, 153, 153));
        btnEliminarDescuento.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminarDescuento.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/borrar.png"))); // NOI18N
        btnEliminarDescuento.setText("Eliminar");
        btnEliminarDescuento.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnEliminarDescuento.addActionListener(this::btnEliminarDescuentoActionPerformed);
        jPanel4.add(btnEliminarDescuento, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 220, 100, 40));

        btnNuevoDescuento.setBackground(new java.awt.Color(102, 51, 255));
        btnNuevoDescuento.setForeground(new java.awt.Color(255, 255, 255));
        btnNuevoDescuento.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/agregar.png"))); // NOI18N
        btnNuevoDescuento.setText("Nuevo");
        btnNuevoDescuento.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNuevoDescuento.addActionListener(this::btnNuevoDescuentoActionPerformed);
        jPanel4.add(btnNuevoDescuento, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 220, 90, 40));

        btnEliminarDescuento2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/borrar.png"))); // NOI18N
        btnEliminarDescuento2.setText("Eliminar");
        btnEliminarDescuento2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnEliminarDescuento2.addActionListener(this::btnEliminarDescuento2ActionPerformed);
        jPanel4.add(btnEliminarDescuento2, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 220, 100, 40));

        btnCerrarSesion3.setBackground(new java.awt.Color(211, 211, 242));
        btnCerrarSesion3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCerrarSesion3.setForeground(new java.awt.Color(0, 0, 0));
        btnCerrarSesion3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/cerrar-sesion (1).png"))); // NOI18N
        btnCerrarSesion3.setText("Cerrar sesión");
        btnCerrarSesion3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 0, 153)));
        btnCerrarSesion3.addActionListener(this::btnCerrarSesion3ActionPerformed);
        jPanel4.add(btnCerrarSesion3, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 10, 110, 30));

        tabPrincipal.addTab("Descuentos", jPanel4);

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel35.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/usuarios..png"))); // NOI18N
        jPanel6.add(jLabel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 30, 70, 60));

        jLabel36.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel36.setText("Usuarios");
        jPanel6.add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 20, 340, 50));

        jLabel37.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Fondou.png"))); // NOI18N
        jPanel6.add(jLabel37, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 200, 530));

        jLabel34.setText("Gestiona los usuarios registrados en TicketGo");
        jPanel6.add(jLabel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 70, 260, -1));

        jSeparator7.setForeground(new java.awt.Color(153, 153, 255));
        jPanel6.add(jSeparator7, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 100, 630, 10));

        tblUsuarios.setBackground(new java.awt.Color(204, 204, 255));
        tblUsuarios.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane6.setViewportView(tblUsuarios);

        jPanel6.add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 260, 670, 220));

        jLabel38.setText("Buscar:");
        jPanel6.add(jLabel38, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 140, 50, 20));
        jPanel6.add(txtBuscarUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 140, 290, -1));

        btnEliminarUsuario.setBackground(new java.awt.Color(255, 153, 153));
        btnEliminarUsuario.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminarUsuario.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/borrar.png"))); // NOI18N
        btnEliminarUsuario.setText("Eliminar");
        btnEliminarUsuario.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnEliminarUsuario.addActionListener(this::btnEliminarUsuarioActionPerformed);
        jPanel6.add(btnEliminarUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 200, 100, 40));

        btnNuevoUsuario.setBackground(new java.awt.Color(102, 51, 255));
        btnNuevoUsuario.setForeground(new java.awt.Color(255, 255, 255));
        btnNuevoUsuario.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/agregar.png"))); // NOI18N
        btnNuevoUsuario.setText("Nuevo");
        btnNuevoUsuario.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNuevoUsuario.addActionListener(this::btnNuevoUsuarioActionPerformed);
        jPanel6.add(btnNuevoUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 200, 90, 40));

        btnEditarUsuario.setBackground(new java.awt.Color(51, 0, 153));
        btnEditarUsuario.setForeground(new java.awt.Color(255, 255, 255));
        btnEditarUsuario.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/lapiz.png"))); // NOI18N
        btnEditarUsuario.setText("Editar");
        btnEditarUsuario.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnEditarUsuario.addActionListener(this::btnEditarUsuarioActionPerformed);
        jPanel6.add(btnEditarUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 200, 90, 40));

        btnVerUsuario.setBackground(new java.awt.Color(153, 102, 255));
        btnVerUsuario.setForeground(new java.awt.Color(255, 255, 255));
        btnVerUsuario.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/ojo.png"))); // NOI18N
        btnVerUsuario.setText("Ver");
        btnVerUsuario.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnVerUsuario.addActionListener(this::btnVerUsuarioActionPerformed);
        jPanel6.add(btnVerUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 200, 90, 40));

        btnCerrarSesion4.setBackground(new java.awt.Color(211, 211, 242));
        btnCerrarSesion4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCerrarSesion4.setForeground(new java.awt.Color(0, 0, 0));
        btnCerrarSesion4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/cerrar-sesion (1).png"))); // NOI18N
        btnCerrarSesion4.setText("Cerrar sesión");
        btnCerrarSesion4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 0, 153)));
        btnCerrarSesion4.addActionListener(this::btnCerrarSesion4ActionPerformed);
        jPanel6.add(btnCerrarSesion4, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 10, 110, 30));

        tabPrincipal.addTab("Usuarios", jPanel6);

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jSeparator5.setForeground(new java.awt.Color(255, 255, 255));
        jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jPanel5.add(jSeparator5, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 20, 10, 90));

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(255, 255, 255));
        jLabel21.setText("Mi perfil");
        jPanel5.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 20, -1, -1));

        jLabel22.setForeground(new java.awt.Color(255, 255, 255));
        jLabel22.setText("Gestiona tu información y cambia tu contraseña");
        jPanel5.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 70, -1, -1));

        jLabel30.setText("Contraseña actual:");
        jPanel5.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 350, -1, -1));

        jLabel33.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/usuario (2).png"))); // NOI18N
        jPanel5.add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 30, 70, 70));

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel24.setText("Información personal");
        jPanel5.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 140, -1, -1));

        jLabel25.setText("Documento:");
        jPanel5.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 190, -1, -1));

        jLabel26.setText("Correo:");
        jPanel5.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 230, -1, -1));

        txtPerfilCorreo.setBackground(new java.awt.Color(227, 227, 253));
        jPanel5.add(txtPerfilCorreo, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 230, 170, -1));

        txtPerfilDocumento.setBackground(new java.awt.Color(227, 227, 253));
        jPanel5.add(txtPerfilDocumento, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 190, 170, -1));

        jLabel27.setText("Nombre:");
        jPanel5.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 190, -1, -1));

        jLabel28.setText("Teléfono");
        jPanel5.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 230, -1, -1));

        txtPerfilTelefono.setBackground(new java.awt.Color(227, 227, 253));
        jPanel5.add(txtPerfilTelefono, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 230, 200, -1));

        txtPerfilNombre.setBackground(new java.awt.Color(227, 227, 253));
        jPanel5.add(txtPerfilNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 190, 200, -1));

        jLabel29.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel29.setText("Cambiar contraseña");
        jPanel5.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 300, -1, -1));

        jLabel31.setText("Contraseña nueva:");
        jPanel5.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 376, -1, 30));

        jLabel32.setText("Confirmar contraseña");
        jPanel5.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 410, -1, -1));

        jSeparator6.setForeground(new java.awt.Color(153, 153, 153));
        jPanel5.add(jSeparator6, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 280, 780, 20));

        jButton4.setBackground(new java.awt.Color(51, 0, 153));
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setText("Guardar cambios");
        jButton4.addActionListener(this::jButton4ActionPerformed);
        jPanel5.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 370, 130, 40));
        jPanel5.add(pwdActual, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 350, 280, -1));
        jPanel5.add(pwdNueva, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 380, 280, -1));

        btnCerrarSesion5.setBackground(new java.awt.Color(255, 255, 255));
        btnCerrarSesion5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCerrarSesion5.setForeground(new java.awt.Color(102, 51, 255));
        btnCerrarSesion5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/cerrar-sesion (1).png"))); // NOI18N
        btnCerrarSesion5.setText("Cerrar sesión");
        btnCerrarSesion5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        btnCerrarSesion5.addActionListener(this::btnCerrarSesion5ActionPerformed);
        jPanel5.add(btnCerrarSesion5, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 10, 110, 30));

        jLabel20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/fondo2.png"))); // NOI18N
        jPanel5.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 910, 120));
        jPanel5.add(pwdConfirmar, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 410, 280, -1));

        tabPrincipal.addTab("Mi perfil", jPanel5);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(tabPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 1017, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 525, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
         filtrarEventos();

    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnNuevoEventoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoEventoActionPerformed
        // TODO add your handling code here:
        abrirFormularioEvento(0, false);
    }//GEN-LAST:event_btnNuevoEventoActionPerformed

    private void btnEditarEventoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarEventoActionPerformed
        // TODO add your handling code here:
        int id = idFilaSeleccionada();
         if (id > 0) {
         abrirFormularioEvento(id, false);
         }
    }//GEN-LAST:event_btnEditarEventoActionPerformed

    private void btnVerEventoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerEventoActionPerformed
        // TODO add your handling code here:
        int id = idFilaSeleccionada(); 
        if (id > 0) {
        abrirFormularioEvento(id, true);
        }
    }//GEN-LAST:event_btnVerEventoActionPerformed

    private void btnEliminarEventoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarEventoActionPerformed
        // TODO add your handling code here:
        eliminarEvento();
    }//GEN-LAST:event_btnEliminarEventoActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        cargarReporte();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void btnVerEventosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerEventosActionPerformed
        // TODO add your handling code here:
     tabPrincipal.setSelectedIndex(tabPrincipal.indexOfTab("Mis eventos"));
    }//GEN-LAST:event_btnVerEventosActionPerformed

    private void btnVerDescuentosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerDescuentosActionPerformed
        // TODO add your handling code here:
        tabPrincipal.setSelectedIndex(tabPrincipal.indexOfTab("Descuentos"));
    }//GEN-LAST:event_btnVerDescuentosActionPerformed

    private void btnNuevoDescuentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoDescuentoActionPerformed
        // TODO add your handling code here:
        abrirFormularioDescuento(null, false);

    }//GEN-LAST:event_btnNuevoDescuentoActionPerformed

    private void btnEditarDescuentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarDescuentoActionPerformed
        // TODO add your handling code here:
        String cod = codigoSeleccionado();
       if (cod != null) {
       abrirFormularioDescuento(cod, false);
}
    }//GEN-LAST:event_btnEditarDescuentoActionPerformed

    private void btnVerDescuentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerDescuentoActionPerformed
        // TODO add your handling code here:
        String cod = codigoSeleccionado();
        if (cod != null) {
         abrirFormularioDescuento(cod, true);
}
    }//GEN-LAST:event_btnVerDescuentoActionPerformed

    private void btnEliminarDescuentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarDescuentoActionPerformed
        // TODO add your handling code here:
        eliminarDescuento();
    }//GEN-LAST:event_btnEliminarDescuentoActionPerformed

    private void btnEliminarDescuento2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarDescuento2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnEliminarDescuento2ActionPerformed

    private void btnEliminarUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarUsuarioActionPerformed
        // TODO add your handling code here:
        eliminarUsuario();
    }//GEN-LAST:event_btnEliminarUsuarioActionPerformed

    private void btnNuevoUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoUsuarioActionPerformed
        // TODO add your handling code here:
       
    abrirFormularioUsuario(0, false);
    }//GEN-LAST:event_btnNuevoUsuarioActionPerformed

    private void btnEditarUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarUsuarioActionPerformed
        // TODO add your handling code here:
        int id = idUsuarioSeleccionado();
        if (id > 0) {
        abrirFormularioUsuario(id, false);
}
    }//GEN-LAST:event_btnEditarUsuarioActionPerformed

    private void btnVerUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerUsuarioActionPerformed
        // TODO add your handling code here:
        int id = idUsuarioSeleccionado();
        if (id > 0) {
        abrirFormularioUsuario(id, true);
}
    }//GEN-LAST:event_btnVerUsuarioActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        guardarPerfil();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void btnCerrarSesionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarSesionActionPerformed
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
        
    }//GEN-LAST:event_btnCerrarSesionActionPerformed

    private void btnCerrarSesion1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarSesion1ActionPerformed
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
    }//GEN-LAST:event_btnCerrarSesion1ActionPerformed

    private void btnCerrarSesion2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarSesion2ActionPerformed
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
    }//GEN-LAST:event_btnCerrarSesion2ActionPerformed

    private void btnCerrarSesion3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarSesion3ActionPerformed
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
    }//GEN-LAST:event_btnCerrarSesion3ActionPerformed

    private void btnCerrarSesion4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarSesion4ActionPerformed
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
    }//GEN-LAST:event_btnCerrarSesion4ActionPerformed

    private void btnCerrarSesion5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarSesion5ActionPerformed
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
    }//GEN-LAST:event_btnCerrarSesion5ActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new Administrador().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrarSesion;
    private javax.swing.JButton btnCerrarSesion1;
    private javax.swing.JButton btnCerrarSesion2;
    private javax.swing.JButton btnCerrarSesion3;
    private javax.swing.JButton btnCerrarSesion4;
    private javax.swing.JButton btnCerrarSesion5;
    private javax.swing.JButton btnEditarDescuento;
    private javax.swing.JButton btnEditarEvento;
    private javax.swing.JButton btnEditarUsuario;
    private javax.swing.JButton btnEliminarDescuento;
    private javax.swing.JButton btnEliminarDescuento2;
    private javax.swing.JButton btnEliminarEvento;
    private javax.swing.JButton btnEliminarUsuario;
    private javax.swing.JButton btnNuevoDescuento;
    private javax.swing.JButton btnNuevoEvento;
    private javax.swing.JButton btnNuevoUsuario;
    private javax.swing.JButton btnVerDescuento;
    private javax.swing.JButton btnVerDescuentos;
    private javax.swing.JButton btnVerEvento;
    private javax.swing.JButton btnVerEventos;
    private javax.swing.JButton btnVerUsuario;
    private javax.swing.JComboBox<String> cmbEventoAforo;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
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
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JLabel lblBoletasMes;
    private javax.swing.JLabel lblDescuentosAplicados;
    private javax.swing.JLabel lblResultados;
    private javax.swing.JLabel lblResumen;
    private javax.swing.JLabel lblVentasTotal;
    private javax.swing.JPasswordField pwdActual;
    private javax.swing.JPasswordField pwdConfirmar;
    private javax.swing.JPasswordField pwdNueva;
    private javax.swing.JTabbedPane tabPrincipal;
    private javax.swing.JTable tblAforo;
    private javax.swing.JTable tblDescuentos;
    private javax.swing.JTable tblDescuentosInicio;
    private javax.swing.JTable tblMisEventos;
    private javax.swing.JTable tblUsuarios;
    private javax.swing.JTable tblVentas;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtBuscarDescuento;
    private javax.swing.JTextField txtBuscarEvento;
    private javax.swing.JTextField txtBuscarUsuario;
    private javax.swing.JTextField txtPerfilCorreo;
    private javax.swing.JTextField txtPerfilDocumento;
    private javax.swing.JTextField txtPerfilNombre;
    private javax.swing.JTextField txtPerfilTelefono;
    // End of variables declaration//GEN-END:variables
}
