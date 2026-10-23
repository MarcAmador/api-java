package com.app.view;

import com.app.controller.ArticuloController;
import com.app.model.Articulo;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class VentanaArticulos extends JFrame {

    private final ArticuloController controller;
    private final List<Articulo> listaArticulos = new ArrayList<>();

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JButton btnNuevo;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnCargar;
    private JLabel lblEstado;

    private CardLayout cardLayout;
    private JPanel panelCentro;
    private PanelCargaOverlay overlayCarga;

    private boolean operacionEnCurso = false;
    private boolean conexionExitosa = false;

    // colores de la vista
    private final Color COLOR_VERDE_CREAR = new Color(40, 167, 69);
    private final Color COLOR_MORADO_EDITAR = new Color(142, 68, 173);
    private final Color COLOR_ROJO_ELIMINAR = new Color(220, 53, 69);
    private final Color COLOR_AZUL_CARGAR = new Color(0, 122, 255);
    private final Color COLOR_TEXTO_MUTED = new Color(142, 142, 147);
    private final Color COLOR_LINEAS = new Color(229, 229, 234);

    private FlatSVGIcon iconoRefreshEstatico;
    private SpinnerIcon iconoSpinnerBoton;
    private Timer timerGiroBoton;

    public VentanaArticulos() {
        this.controller = new ArticuloController();
        configurarVentana();
        inicializarComponentes();
    }

    private void configurarVentana() {
        setTitle("Gestor de Artículos");
        setSize(960, 600);
        setMinimumSize(new Dimension(840, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void inicializarComponentes() {
        JPanel panelContenedor = new JPanel(new BorderLayout(0, 18));
        panelContenedor.setBorder(new EmptyBorder(22, 26, 18, 26));

        // cabecera
        JPanel panelHeader = new JPanel(new BorderLayout());

        JPanel panelTitulos = new JPanel();
        panelTitulos.setLayout(new BoxLayout(panelTitulos, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel("Inventario de Artículos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));

        panelTitulos.add(lblTitulo);
        panelTitulos.add(Box.createVerticalStrut(2));

        // botones de accion
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        // boton nuevo empieza desactivado hasta conectar
        btnNuevo = crearBotonConColor("Nuevo", "icons/plus.svg", COLOR_VERDE_CREAR);
        btnNuevo.setEnabled(false);
        btnNuevo.addActionListener(e -> {
            if (!operacionEnCurso && conexionExitosa) abrirFormularioNuevo();
        });

        // desactivados al inicio
        btnEditar = crearBotonConColor("Editar", "icons/edit.svg", COLOR_MORADO_EDITAR);
        btnEditar.setEnabled(false);
        btnEditar.addActionListener(e -> {
            if (!operacionEnCurso) abrirFormularioEditar();
        });

        btnEliminar = crearBotonConColor("Eliminar", "icons/trash.svg", COLOR_ROJO_ELIMINAR);
        btnEliminar.setEnabled(false);
        btnEliminar.addActionListener(e -> {
            if (!operacionEnCurso) ejecutarEliminar();
        });

        // boton cargar
        iconoRefreshEstatico = new FlatSVGIcon("icons/refresh.svg", 14, 14);
        iconoRefreshEstatico.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
        iconoSpinnerBoton = new SpinnerIcon(14, Color.WHITE);

        btnCargar = new JButton("Cargar Datos", iconoRefreshEstatico);
        btnCargar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCargar.setForeground(Color.WHITE);
        btnCargar.setBackground(COLOR_AZUL_CARGAR);
        btnCargar.setFocusPainted(false);
        btnCargar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCargar.putClientProperty("JButton.buttonType", "roundRect");
        btnCargar.setPreferredSize(new Dimension(130, 34));
        btnCargar.addActionListener(e -> {
            if (!operacionEnCurso) ejecutarCargaDatos();
        });

        timerGiroBoton = new Timer(25, e -> {
            iconoSpinnerBoton.avanzar();
            btnCargar.repaint();
        });

        panelAcciones.add(btnNuevo);
        panelAcciones.add(btnEditar);
        panelAcciones.add(btnEliminar);
        panelAcciones.add(btnCargar);

        panelHeader.add(panelTitulos, BorderLayout.WEST);
        panelHeader.add(panelAcciones, BorderLayout.EAST);

        // paneles segun el estado
        cardLayout = new CardLayout();
        panelCentro = new JPanel(cardLayout);

        String[] columnas = {"ID", "Nombre del Artículo", "Categoría", "Precio Unitario", "Stock"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(38);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(COLOR_LINEAS);
        tabla.setIntercellSpacing(new Dimension(0, 1));
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.putClientProperty("JTable.alternateRowColor", true);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (operacionEnCurso) return;
            boolean hayFila = tabla.getSelectedRow() >= 0;
            btnEditar.setEnabled(hayFila && conexionExitosa);
            btnEliminar.setEnabled(hayFila && conexionExitosa);
        });

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!operacionEnCurso && conexionExitosa && e.getClickCount() == 2 && tabla.getSelectedRow() >= 0) {
                    abrirFormularioEditar();
                }
            }
        });

        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 38));
        tabla.getTableHeader().setReorderingAllowed(false);

        tabla.getColumnModel().getColumn(0).setPreferredWidth(60);
        tabla.getColumnModel().getColumn(0).setMaxWidth(80);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(320);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(160);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(130);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(120);

        DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
        centro.setHorizontalAlignment(SwingConstants.CENTER);
        tabla.getColumnModel().getColumn(0).setCellRenderer(centro);
        tabla.getColumnModel().getColumn(4).setCellRenderer(centro);

        DefaultTableCellRenderer derecha = new DefaultTableCellRenderer();
        derecha.setHorizontalAlignment(SwingConstants.RIGHT);
        tabla.getColumnModel().getColumn(3).setCellRenderer(derecha);

        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_LINEAS, 1));

        // tabla con overlay encima
        JPanel panelTablaConOverlay = new JPanel();
        panelTablaConOverlay.setLayout(new OverlayLayout(panelTablaConOverlay));

        overlayCarga = new PanelCargaOverlay();
        overlayCarga.setVisible(false);

        panelTablaConOverlay.add(overlayCarga);
        panelTablaConOverlay.add(scrollPane);

        // estado inicial
        JPanel panelInicio = crearPanelEstado(
                "icons/empty.svg",
                "Sin datos",
                "Presiona 'Cargar Datos' para consultar."
        );

        // cuando no hay nada cargado
        JPanel panelVacio = crearPanelEstado(
                "icons/empty.svg",
                "No hay artículos",
                null
        );

        // si no hay coneccion
        JPanel panelError = crearPanelEstado(
                "icons/offline.svg",
                "Sin conexión",
                "No se pudo establecer conexión con el servidor"
        );

        panelCentro.add(panelInicio, "INICIO");
        panelCentro.add(panelTablaConOverlay, "TABLA");
        panelCentro.add(panelVacio, "VACIO");
        panelCentro.add(panelError, "ERROR");

        cardLayout.show(panelCentro, "INICIO");

        // barra de abajo
        JPanel panelFooter = new JPanel(new BorderLayout());
        lblEstado = new JLabel("Listo", new FlatSVGIcon("icons/info.svg", 14, 14), SwingConstants.LEFT);
        lblEstado.setIconTextGap(8);
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEstado.setForeground(COLOR_TEXTO_MUTED);

        panelFooter.add(lblEstado, BorderLayout.WEST);

        panelContenedor.add(panelHeader, BorderLayout.NORTH);
        panelContenedor.add(panelCentro, BorderLayout.CENTER);
        panelContenedor.add(panelFooter, BorderLayout.SOUTH);

        setContentPane(panelContenedor);
    }

    private JPanel crearPanelEstado(String iconoPath, String titulo, String subtitulo) {
        JPanel contenedor = new JPanel(new GridBagLayout());
        contenedor.setBorder(BorderFactory.createLineBorder(COLOR_LINEAS, 1));

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        JLabel lblIcono = new JLabel(new FlatSVGIcon(iconoPath, 110, 110));
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        contenido.add(lblIcono);
        contenido.add(Box.createVerticalStrut(14));
        contenido.add(lblTitulo);

        if (subtitulo != null && !subtitulo.isBlank()) {
            JLabel lblSub = new JLabel(subtitulo);
            lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblSub.setForeground(COLOR_TEXTO_MUTED);
            lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
            contenido.add(Box.createVerticalStrut(6));
            contenido.add(lblSub);
        }

        contenedor.add(contenido);
        return contenedor;
    }

    private JButton crearBotonConColor(String texto, String iconoPath, Color colorFondo) {
        FlatSVGIcon icono = new FlatSVGIcon(iconoPath, 14, 14);
        icono.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));

        JButton btn = new JButton(texto, icono);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(colorFondo);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setPreferredSize(new Dimension(100, 34));
        return btn;
    }

    private void setCargando(boolean cargando, String mensajeOverlay) {
        this.operacionEnCurso = cargando;

        if (cargando) {
            btnCargar.setText("Cargando...");
            btnCargar.setIcon(iconoSpinnerBoton);
            timerGiroBoton.start();

            btnNuevo.setEnabled(false);
            btnEditar.setEnabled(false);
            btnEliminar.setEnabled(false);

            cardLayout.show(panelCentro, "TABLA");
            overlayCarga.iniciar(mensajeOverlay);
            lblEstado.setIcon(new FlatSVGIcon("icons/info.svg", 14, 14));
            lblEstado.setText(mensajeOverlay);
        } else {
            timerGiroBoton.stop();
            btnCargar.setIcon(iconoRefreshEstatico);
            btnCargar.setText("Cargar Datos");

            btnNuevo.setEnabled(conexionExitosa);
            boolean hayFila = tabla.getSelectedRow() >= 0;
            btnEditar.setEnabled(hayFila && conexionExitosa);
            btnEliminar.setEnabled(hayFila && conexionExitosa);

            overlayCarga.detener();
        }
    }

    // cargar datos de la api
    private void ejecutarCargaDatos() {
        setCargando(true, "Cargando...");

        SwingWorker<List<Articulo>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Articulo> doInBackground() throws Exception {
                return controller.obtenerArticulosDeApi();
            }

            @Override
            protected void done() {
                try {
                    List<Articulo> articulos = get();
                    conexionExitosa = true;

                    listaArticulos.clear();
                    listaArticulos.addAll(articulos);
                    modeloTabla.setRowCount(0);

                    setCargando(false, null);

                    if (articulos.isEmpty()) {
                        cardLayout.show(panelCentro, "VACIO");
                        lblEstado.setIcon(new FlatSVGIcon("icons/info.svg", 14, 14));
                        lblEstado.setText("Sin datos");
                        return;
                    }

                    for (Articulo art : articulos) {
                        modeloTabla.addRow(new Object[]{
                                art.getId(),
                                art.getNombre(),
                                art.getCategoria(),
                                String.format("Q %,.2f", art.getPrecio()),
                                art.getStock() + " unidades"
                        });
                    }

                    cardLayout.show(panelCentro, "TABLA");
                    lblEstado.setIcon(new FlatSVGIcon("icons/check.svg", 14, 14));
                    lblEstado.setText(articulos.size() + " artículos");

                } catch (Exception ex) {
                    conexionExitosa = false;
                    setCargando(false, null);

                    cardLayout.show(panelCentro, "ERROR");
                    lblEstado.setIcon(new FlatSVGIcon("icons/alert.svg", 14, 14));
                    lblEstado.setText("Sin conexión");

                    JOptionPane.showMessageDialog(
                            VentanaArticulos.this,
                            "No se pudo conectar al servidor.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    // modal de nuevo articulo
    private void abrirFormularioNuevo() {
        if (!conexionExitosa) {
            JOptionPane.showMessageDialog(this, "Sin conexión al servidor.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FormularioArticuloDialog dialog = new FormularioArticuloDialog(this, null);
        dialog.setVisible(true);

        if (dialog.isGuardado()) {
            Articulo nuevo = dialog.getArticulo();
            setCargando(true, "Guardando...");

            SwingWorker<Articulo, Void> worker = new SwingWorker<>() {
                @Override
                protected Articulo doInBackground() throws Exception {
                    return controller.crearArticulo(nuevo);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        lblEstado.setIcon(new FlatSVGIcon("icons/check.svg", 14, 14));
                        lblEstado.setText("Guardado");
                        ejecutarCargaDatos();
                    } catch (Exception ex) {
                        setCargando(false, null);
                        lblEstado.setIcon(new FlatSVGIcon("icons/alert.svg", 14, 14));
                        lblEstado.setText("Error al guardar");
                        JOptionPane.showMessageDialog(VentanaArticulos.this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    // editar el que este seleccionado
    private void abrirFormularioEditar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= listaArticulos.size()) {
            return;
        }

        Articulo seleccionado = listaArticulos.get(fila);
        FormularioArticuloDialog dialog = new FormularioArticuloDialog(this, seleccionado);
        dialog.setVisible(true);

        if (dialog.isGuardado()) {
            setCargando(true, "Actualizando...");

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    controller.actualizarArticulo(seleccionado);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        lblEstado.setIcon(new FlatSVGIcon("icons/check.svg", 14, 14));
                        lblEstado.setText("Actualizado");
                        ejecutarCargaDatos();
                    } catch (Exception ex) {
                        setCargando(false, null);
                        lblEstado.setIcon(new FlatSVGIcon("icons/alert.svg", 14, 14));
                        lblEstado.setText("Error al actualizar");
                        JOptionPane.showMessageDialog(VentanaArticulos.this, "Error al actualizar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    // borrar articulo
    private void ejecutarEliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= listaArticulos.size()) {
            return;
        }

        Articulo seleccionado = listaArticulos.get(fila);

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Eliminar '" + seleccionado.getNombre() + "'?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            setCargando(true, "Eliminando...");

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    controller.eliminarArticulo(seleccionado.getId());
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        lblEstado.setIcon(new FlatSVGIcon("icons/check.svg", 14, 14));
                        lblEstado.setText("Eliminado");
                        ejecutarCargaDatos();
                    } catch (Exception ex) {
                        setCargando(false, null);
                        lblEstado.setIcon(new FlatSVGIcon("icons/alert.svg", 14, 14));
                        lblEstado.setText("Error al eliminar");
                        JOptionPane.showMessageDialog(VentanaArticulos.this, "Error al eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    // animacion mientras carga
    private static class PanelCargaOverlay extends JPanel {
        private double angulo = 0;
        private final Timer timerAnimacion;
        private String mensaje = "Cargando...";

        public PanelCargaOverlay() {
            setOpaque(false);
            timerAnimacion = new Timer(25, e -> {
                angulo = (angulo + 14) % 360;
                repaint();
            });
        }

        public void iniciar(String mensaje) {
            this.mensaje = mensaje != null ? mensaje : "Cargando...";
            this.angulo = 0;
            setVisible(true);
            timerAnimacion.start();
        }

        public void detener() {
            timerAnimacion.stop();
            setVisible(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(255, 255, 255, 215));
            g2.fillRect(0, 0, w, h);

            int size = 40;
            int cx = (w - size) / 2;
            int cy = (h - size) / 2 - 12;

            g2.setColor(new Color(225, 225, 232));
            g2.setStroke(new BasicStroke(3.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(cx, cy, size, size, 0, 360);

            g2.setColor(new Color(0, 122, 255));
            g2.drawArc(cx, cy, size, size, (int) angulo, 100);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.setColor(new Color(30, 30, 35));
            FontMetrics fm = g2.getFontMetrics();
            int textX = (w - fm.stringWidth(mensaje)) / 2;
            g2.drawString(mensaje, textX, cy + size + 26);

            g2.dispose();
        }
    }

    // icono giratorio boton
    private static class SpinnerIcon implements Icon {
        private double angulo = 0;
        private final int size;
        private final Color color;

        public SpinnerIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        public void avanzar() {
            this.angulo = (this.angulo + 18) % 360;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(color);
            int arcSize = size - 4;
            g2.drawArc(x + 2, y + 2, arcSize, arcSize, (int) angulo, 270);
            g2.dispose();
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }
}
