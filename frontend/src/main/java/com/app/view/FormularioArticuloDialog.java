package com.app.view;

import com.app.model.Articulo;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class FormularioArticuloDialog extends JDialog {

    private JTextField txtNombre;
    private JTextField txtCategoria;
    private JTextField txtPrecio;
    private JTextField txtStock;

    private boolean guardado = false;
    private Articulo articulo;

    private final Color COLOR_GUARDAR = new Color(40, 167, 69);
    private final Color COLOR_BORDE_INPUT = new Color(210, 210, 215);

    public FormularioArticuloDialog(Frame parent, Articulo articuloAEditar) {
        super(parent, articuloAEditar == null ? "Nuevo Artículo" : "Editar Artículo", true);
        this.articulo = articuloAEditar;

        setSize(460, 420);
        setResizable(false);
        setLocationRelativeTo(parent);

        inicializarInterfaz();
        if (articulo != null) {
            cargarDatos();
        }
    }

    private void inicializarInterfaz() {
        JPanel principal = new JPanel(new BorderLayout(0, 18));
        principal.setBorder(new EmptyBorder(22, 26, 20, 26));

        // encabezado
        JPanel panelHeader = new JPanel();
        panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.Y_AXIS));

        String tituloTexto = articulo == null ? "Registrar" : "Actualizar datos";
        JLabel lblTitulo = new JLabel(tituloTexto);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 17));

        panelHeader.add(lblTitulo);

        // inputs del form
        JPanel formulario = new JPanel(new GridLayout(4, 1, 0, 12));

        txtNombre = crearCampoTexto();
        txtCategoria = crearCampoTexto();
        txtPrecio = crearCampoTexto();
        txtStock = crearCampoTexto();

        formulario.add(crearFila("Nombre:", txtNombre));
        formulario.add(crearFila("Categoría:", txtCategoria));
        formulario.add(crearFila("Precio:", txtPrecio));
        formulario.add(crearFila("Stock:", txtStock));

        // botones de abajo
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancelar.setPreferredSize(new Dimension(95, 34));
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> dispose());

        FlatSVGIcon iconCheck = new FlatSVGIcon("icons/check.svg", 14, 14);
        iconCheck.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));

        JButton btnGuardar = new JButton("Guardar", iconCheck);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGuardar.setBackground(COLOR_GUARDAR);
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setPreferredSize(new Dimension(110, 34));
        btnGuardar.setFocusPainted(false);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.putClientProperty("JButton.buttonType", "roundRect");
        btnGuardar.addActionListener(e -> validarYGuardar());

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);

        principal.add(panelHeader, BorderLayout.NORTH);
        principal.add(formulario, BorderLayout.CENTER);
        principal.add(panelBotones, BorderLayout.SOUTH);

        setContentPane(principal);
    }

    private JTextField crearCampoTexto() {
        JTextField campo = new JTextField();
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campo.setPreferredSize(new Dimension(0, 38));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE_INPUT, 1, true),
                new EmptyBorder(0, 12, 0, 12)
        ));
        campo.putClientProperty("JComponent.roundRect", true);
        return campo;
    }

    private JPanel crearFila(String etiqueta, JTextField campo) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(60, 60, 67));

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(campo, BorderLayout.CENTER);
        return panel;
    }

    private void cargarDatos() {
        txtNombre.setText(articulo.getNombre());
        txtCategoria.setText(articulo.getCategoria());
        txtPrecio.setText(String.valueOf(articulo.getPrecio()));
        txtStock.setText(String.valueOf(articulo.getStock()));
    }

    private void validarYGuardar() {
        String nombre = txtNombre.getText().trim();
        String categoria = txtCategoria.getText().trim();
        String precioStr = txtPrecio.getText().trim();
        String stockStr = txtStock.getText().trim();

        if (nombre.isEmpty() || categoria.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioStr);
            if (precio <= 0) {
                JOptionPane.showMessageDialog(this, "El precio debe ser mayor a cero.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingresa un precio válido.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int stock;
        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                JOptionPane.showMessageDialog(this, "El stock no puede ser negativo.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingresa un número entero en stock.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (articulo == null) {
            articulo = new Articulo(nombre, categoria, precio, stock);
        } else {
            articulo.setNombre(nombre);
            articulo.setCategoria(categoria);
            articulo.setPrecio(precio);
            articulo.setStock(stock);
        }

        guardado = true;
        dispose();
    }

    public boolean isGuardado() {
        return guardado;
    }

    public Articulo getArticulo() {
        return articulo;
    }
}
