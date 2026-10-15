package com.app;

import com.app.view.VentanaArticulos;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Tema FlatLaf claro
        FlatMacLightLaf.setup();

        SwingUtilities.invokeLater(() -> {
            VentanaArticulos ventana = new VentanaArticulos();
            ventana.setVisible(true);
        });
    }
}