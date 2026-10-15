package com.app.view;

import com.app.controller.ArticuloController;
import com.app.model.Articulo;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaArticulos extends JFrame {

    private final ArticuloController controller;
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JButton btnCargar;
    private JLabel lblEstado;
    private JLabel lblBadgeTotal;
    private CardLayout cardLayout;
    private JPanel panelCentro;

    private final Color COLOR_APPLE_BLUE = new Color(0, 122, 255);
    private final Color COLOR_TEXTO_SECUNDARIO = new Color(142, 142, 147);
    private final Color COLOR_LINEAS = new Color(229, 229, 234);

    public VentanaArticulos() {
        this.controller = new ArticuloController();
        configurarVentana();
        inicializarComponentes();
    }

    private void configurarVentana() {
        setTitle("Artículos - Oracle DB");
        setSize(940, 580);
        setMinimumSize(new Dimension(820, 480));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar en pantalla
    }

    private void inicializarComponentes() {
        JPanel panelContenedor = new JPanel(new BorderLayout(0, 18));
        panelContenedor.setBorder(new EmptyBorder(26, 28, 22, 28));

        JPanel panelHeader = new JPanel(new BorderLayout());

        JPanel panelTitulos = new JPanel();
        panelTitulos.setLayout(new BoxLayout(panelTitulos, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel("Inventario de Artículos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));

        panelTitulos.add(lblTitulo);
        panelTitulos.add(Box.createVerticalStrut(4));

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));

        lblBadgeTotal = new JLabel("0 artículos");
        lblBadgeTotal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBadgeTotal.setForeground(COLOR_TEXTO_SECUNDARIO);
        lblBadgeTotal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_LINEAS, 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));

        FlatSVGIcon iconoRefresh = new FlatSVGIcon("icons/refresh.svg", 16, 16);
        iconoRefresh.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));

        btnCargar = new JButton("Cargar Datos", iconoRefresh);
        btnCargar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCargar.setForeground(Color.WHITE);
        btnCargar.setBackground(COLOR_APPLE_BLUE);
        btnCargar.setFocusPainted(false);
        btnCargar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCargar.putClientProperty("JButton.buttonType", "roundRect");
        btnCargar.setPreferredSize(new Dimension(150, 36));
        btnCargar.addActionListener(e -> ejecutarCargaDatos());

        panelAcciones.add(lblBadgeTotal);
        panelAcciones.add(btnCargar);

        panelHeader.add(panelTitulos, BorderLayout.WEST);
        panelHeader.add(panelAcciones, BorderLayout.EAST);

        // CardLayout para alternar entre la tabla y el estado vacío
        cardLayout = new CardLayout();
        panelCentro = new JPanel(cardLayout);

        String[] columnas = {"ID", "Nombre del Artículo", "Categoría", "Precio Unitario", "Stock"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Deshabilitar edición de celdas
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(36);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setShowVerticalLines(false); // Ocultar líneas verticales
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(COLOR_LINEAS);
        tabla.setIntercellSpacing(new Dimension(0, 1));
        tabla.setFillsViewportHeight(true);
        tabla.putClientProperty("JTable.alternateRowColor", true);

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

        JPanel panelVacio = new JPanel(new GridBagLayout());
        panelVacio.setBorder(BorderFactory.createLineBorder(COLOR_LINEAS, 1));

        JPanel contenidoVacio = new JPanel();
        contenidoVacio.setLayout(new BoxLayout(contenidoVacio, BoxLayout.Y_AXIS));

        JLabel lblIconoVacio = new JLabel(new FlatSVGIcon("icons/empty.svg", 76, 76));
        lblIconoVacio.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSinDatos = new JLabel("No hay artículos cargados");
        lblSinDatos.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSinDatos.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubSinDatos = new JLabel("Presiona 'Cargar Datos' para consultar.");
        lblSubSinDatos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubSinDatos.setForeground(COLOR_TEXTO_SECUNDARIO);
        lblSubSinDatos.setAlignmentX(Component.CENTER_ALIGNMENT);

        contenidoVacio.add(lblIconoVacio);
        contenidoVacio.add(Box.createVerticalStrut(14));
        contenidoVacio.add(lblSinDatos);
        contenidoVacio.add(Box.createVerticalStrut(6));
        contenidoVacio.add(lblSubSinDatos);

        panelVacio.add(contenidoVacio);

        panelCentro.add(panelVacio, "VACIO");
        panelCentro.add(scrollPane, "TABLA");
        cardLayout.show(panelCentro, "VACIO");

        JPanel panelFooter = new JPanel(new BorderLayout());
        lblEstado = new JLabel("No hay datos aún", new FlatSVGIcon("icons/info.svg", 14, 14), SwingConstants.LEFT);
        lblEstado.setIconTextGap(8);
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEstado.setForeground(COLOR_TEXTO_SECUNDARIO);

        panelFooter.add(lblEstado, BorderLayout.WEST);

        panelContenedor.add(panelHeader, BorderLayout.NORTH);
        panelContenedor.add(panelCentro, BorderLayout.CENTER);
        panelContenedor.add(panelFooter, BorderLayout.SOUTH);

        setContentPane(panelContenedor);
    }

    private void ejecutarCargaDatos() {
        btnCargar.setEnabled(false);
        lblEstado.setIcon(new FlatSVGIcon("icons/info.svg", 14, 14));
        lblEstado.setText("Consultando API...");

        // Petición en segundo plano para no congelar la interfaz
        SwingWorker<List<Articulo>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Articulo> doInBackground() throws Exception {
                return controller.obtenerArticulosDeApi();
            }

            @Override
            protected void done() {
                btnCargar.setEnabled(true);
                try {
                    List<Articulo> articulos = get();
                    modeloTabla.setRowCount(0);

                    // Si la lista viene vacía se mantiene el panel con el svg
                    if (articulos.isEmpty()) {
                        lblBadgeTotal.setText("0 artículos");
                        cardLayout.show(panelCentro, "VACIO");
                        lblEstado.setIcon(new FlatSVGIcon("icons/info.svg", 14, 14));
                        lblEstado.setText("Sin artículos disponibles");
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

                    lblBadgeTotal.setText(articulos.size() + " artículos");
                    cardLayout.show(panelCentro, "TABLA");

                    lblEstado.setIcon(new FlatSVGIcon("icons/check.svg", 14, 14));
                    lblEstado.setText("Carga exitosa");

                } catch (Exception ex) {
                    lblEstado.setIcon(new FlatSVGIcon("icons/alert.svg", 14, 14));
                    lblEstado.setText("Error al consultar API");

                    JOptionPane.showMessageDialog(
                            VentanaArticulos.this,
                            "No se pudo consultar la API:\n" + ex.getMessage(),
                            "Error de Conexión",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }
}