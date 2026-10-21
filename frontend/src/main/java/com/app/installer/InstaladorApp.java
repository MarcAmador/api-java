package com.app.installer;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class InstaladorApp extends JFrame {

    private static final String API_URL_FIJA = "https://gestor-articulos.loca.lt/api/articulos";

    private final CardLayout cardLayout;
    private final JPanel panelContenido;

    // Header
    private JLabel lblPasoIndicador;

    // Botones de navegación
    private JButton btnAnterior;
    private JButton btnSiguiente;
    private JButton btnCancelar;

    // Paso 2 Opciones
    private JTextField txtRutaInstalacion;
    private JCheckBox chkAccesoEscritorio;
    private JCheckBox chkAccesoInicio;

    // Paso 3 Progreso
    private JProgressBar barraProgreso;
    private JLabel lblEstadoProgreso;

    // Paso 4 Finalizado
    private JCheckBox chkEjecutarAlFinalizar;
    private File directorioInstalado;

    private int pasoActual = 1;
    private final int TOTAL_PASOS = 4;

    private final Color COLOR_PRIMARIO = new Color(0, 122, 255);
    private final Color COLOR_FONDO_HEADER = new Color(248, 248, 250);
    private final Color COLOR_TEXTO_MUTED = new Color(110, 110, 115);

    public InstaladorApp() {
        setTitle("Instalar Gestor de Artículos");
        setSize(640, 480);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        panelContenido = new JPanel(cardLayout);

        inicializarInterfaz();
    }

    private void inicializarInterfaz() {
        setLayout(new BorderLayout());

        // Header
        add(crearHeader(), BorderLayout.NORTH);

        // Paneles
        panelContenido.add(crearPaso1Bienvenida(), "paso1");
        panelContenido.add(crearPaso2Opciones(), "paso2");
        panelContenido.add(crearPaso3Progreso(), "paso3");
        panelContenido.add(crearPaso4Finalizado(), "paso4");

        add(panelContenido, BorderLayout.CENTER);

        // Barra de Navegación
        add(crearBarraNavegacion(), BorderLayout.SOUTH);

        actualizarNavegacion();
    }

    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_FONDO_HEADER);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 225, 230)),
                new EmptyBorder(14, 24, 14, 24)
        ));

        JPanel panelIzquierdo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panelIzquierdo.setOpaque(false);

        JLabel lblIconoLogo = new JLabel(new FlatSVGIcon("icons/package.svg", 30, 30));

        JPanel panelTextos = new JPanel();
        panelTextos.setLayout(new BoxLayout(panelTextos, BoxLayout.Y_AXIS));
        panelTextos.setOpaque(false);

        JLabel lblTitulo = new JLabel("Instalador de Gestor de Artículos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));

        panelTextos.add(lblTitulo);
        panelTextos.add(Box.createVerticalStrut(2));

        panelIzquierdo.add(lblIconoLogo);
        panelIzquierdo.add(panelTextos);

        lblPasoIndicador = new JLabel("Paso 1 de " + TOTAL_PASOS);
        lblPasoIndicador.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPasoIndicador.setForeground(COLOR_TEXTO_MUTED);

        header.add(panelIzquierdo, BorderLayout.WEST);
        header.add(lblPasoIndicador, BorderLayout.EAST);

        return header;
    }

    // PASO 1: BIENVENIDA
    private JPanel crearPaso1Bienvenida() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(36, 40, 24, 40));

        JPanel panelCentro = new JPanel();
        panelCentro.setLayout(new BoxLayout(panelCentro, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel("Bienvenido");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 17));

        JLabel lblDesc1 = new JLabel("Este asistente instalará el Gestor de Artículos en tu equipo.");
        lblDesc1.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel lblDesc2 = new JLabel("Podrás elegir la carpeta de instalación y los accesos directos.");
        lblDesc2.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel lblDesc3 = new JLabel("Haz clic en Siguiente para continuar.");
        lblDesc3.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        panelCentro.add(lblTitulo);
        panelCentro.add(Box.createVerticalStrut(14));
        panelCentro.add(lblDesc1);
        panelCentro.add(Box.createVerticalStrut(6));
        panelCentro.add(lblDesc2);
        panelCentro.add(Box.createVerticalStrut(14));
        panelCentro.add(lblDesc3);

        panel.add(panelCentro, BorderLayout.CENTER);

        return panel;
    }

    // PASO 2: OPCIONES DE INSTALACIÓN
    private JPanel crearPaso2Opciones() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(26, 40, 20, 40));

        // Carpeta de instalación
        JLabel lblRuta = new JLabel("Carpeta de instalación:");
        lblRuta.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel panelRuta = new JPanel(new BorderLayout(8, 0));
        panelRuta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        panelRuta.setPreferredSize(new Dimension(500, 32));

        String rutaDefault = System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Local" + File.separator + "GestorArticulos";
        txtRutaInstalacion = new JTextField(rutaDefault);
        txtRutaInstalacion.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton btnExaminar = new JButton("Examinar...", new FlatSVGIcon("icons/folder.svg", 15, 15));
        btnExaminar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExaminar.addActionListener(e -> seleccionarCarpeta());

        panelRuta.add(txtRutaInstalacion, BorderLayout.CENTER);
        panelRuta.add(btnExaminar, BorderLayout.EAST);

        // Accesos directos
        JLabel lblAccesos = new JLabel("Accesos directos:");
        lblAccesos.setFont(new Font("Segoe UI", Font.BOLD, 13));

        chkAccesoEscritorio = new JCheckBox("Crear acceso directo en el Escritorio", true);
        chkAccesoEscritorio.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        chkAccesoInicio = new JCheckBox("Crear acceso directo en el Menú Inicio", true);
        chkAccesoInicio.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        panel.add(lblRuta);
        panel.add(Box.createVerticalStrut(8));
        panel.add(panelRuta);
        panel.add(Box.createVerticalStrut(22));

        panel.add(lblAccesos);
        panel.add(Box.createVerticalStrut(8));
        panel.add(chkAccesoEscritorio);
        panel.add(Box.createVerticalStrut(4));
        panel.add(chkAccesoInicio);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private void seleccionarCarpeta() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setSelectedFile(new File(txtRutaInstalacion.getText()));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtRutaInstalacion.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    // PASO 3: PROGRESO DE INSTALACIÓN
    private JPanel crearPaso3Progreso() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(50, 40, 40, 40));

        JLabel lblTituloProgreso = new JLabel("Instalando componentes...");
        lblTituloProgreso.setFont(new Font("Segoe UI", Font.BOLD, 15));

        barraProgreso = new JProgressBar(0, 100);
        barraProgreso.setStringPainted(true);
        barraProgreso.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        barraProgreso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        barraProgreso.setPreferredSize(new Dimension(500, 20));

        lblEstadoProgreso = new JLabel("Iniciando copia de archivos...");
        lblEstadoProgreso.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEstadoProgreso.setForeground(COLOR_TEXTO_MUTED);

        panel.add(lblTituloProgreso);
        panel.add(Box.createVerticalStrut(16));
        panel.add(barraProgreso);
        panel.add(Box.createVerticalStrut(8));
        panel.add(lblEstadoProgreso);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private void iniciarProcesoInstalacion() {
        directorioInstalado = new File(txtRutaInstalacion.getText().trim());
        boolean crearEscritorio = chkAccesoEscritorio.isSelected();
        boolean crearInicio = chkAccesoInicio.isSelected();

        btnAnterior.setEnabled(false);
        btnSiguiente.setEnabled(false);
        btnCancelar.setEnabled(false);

        SwingWorker<Void, String> instaladorWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Crear directorio
                actualizar(15, "Creando carpeta de destino...");
                Thread.sleep(250);
                if (!directorioInstalado.exists()) {
                    directorioInstalado.mkdirs();
                }

                // Copiar el JAR ejecutable
                actualizar(35, "Copiando aplicación...");
                Thread.sleep(300);
                File jarDestino = new File(directorioInstalado, "gestor-articulos.jar");
                File jarOrigen = obtenerJarOrigen();
                if (jarOrigen != null && jarOrigen.exists()) {
                    Files.copy(jarOrigen.toPath(), jarDestino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.writeString(jarDestino.toPath(), "app-binario");
                }

                // Copiar icono de la app
                actualizar(50, "Copiando icono de la aplicación...");
                File icoDestino = new File(directorioInstalado, "app.ico");
                try (InputStream in = getClass().getResourceAsStream("/icons/app.ico")) {
                    if (in != null) {
                        Files.copy(in, icoDestino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (Exception ignored) {}

                //Guardar config.properties con URL
                actualizar(65, "Guardando configuración...");
                Thread.sleep(250);
                File configFile = new File(directorioInstalado, "config.properties");
                try (PrintWriter writer = new PrintWriter(configFile)) {
                    writer.println("api.url=" + API_URL_FIJA);
                }

                // Lanzador VBS
                actualizar(80, "Configurando lanzador del sistema...");
                Thread.sleep(250);
                File vbsLauncher = new File(directorioInstalado, "iniciar.vbs");
                try (PrintWriter writer = new PrintWriter(vbsLauncher)) {
                    writer.println("Set WshShell = CreateObject(\"WScript.Shell\")");
                    writer.println("WshShell.CurrentDirectory = \"" + directorioInstalado.getAbsolutePath() + "\"");
                    writer.println("WshShell.Run \"javaw -jar \"\"" + jarDestino.getAbsolutePath() + "\"\"\", 0, False");
                }

                // Accesos directos con icono
                String rutaIcono = icoDestino.exists() ? icoDestino.getAbsolutePath() : null;

                if (crearEscritorio) {
                    actualizar(88, "Creando acceso directo en el Escritorio...");
                    String rutaDesktop = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Gestor de Artículos.lnk";
                    crearAccesoDirectoWindows(rutaDesktop, vbsLauncher.getAbsolutePath(), directorioInstalado.getAbsolutePath(), rutaIcono);
                }

                if (crearInicio) {
                    actualizar(95, "Creando acceso directo en el Menú Inicio...");
                    String rutaStartMenu = System.getenv("APPDATA") + File.separator + "Microsoft" + File.separator + "Windows" + File.separator + "Start Menu" + File.separator + "Programs" + File.separator + "Gestor de Artículos.lnk";
                    crearAccesoDirectoWindows(rutaStartMenu, vbsLauncher.getAbsolutePath(), directorioInstalado.getAbsolutePath(), rutaIcono);
                }

                actualizar(100, "Instalación completada.");
                Thread.sleep(300);
                return null;
            }

            private void actualizar(int progreso, String texto) {
                SwingUtilities.invokeLater(() -> {
                    barraProgreso.setValue(progreso);
                    lblEstadoProgreso.setText(texto);
                });
            }

            @Override
            protected void done() {
                pasoActual = 4;
                cardLayout.show(panelContenido, "paso4");
                actualizarNavegacion();
            }
        };

        instaladorWorker.execute();
    }

    private File obtenerJarOrigen() {
        File targetJar = new File("target/gestor-articulos.jar");
        if (targetJar.exists()) return targetJar;

        File targetJarFrontend = new File("frontend/target/gestor-articulos.jar");
        if (targetJarFrontend.exists()) return targetJarFrontend;

        try {
            return new File(InstaladorApp.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception ex) {
            return null;
        }
    }

    private void crearAccesoDirectoWindows(String rutaLnk, String rutaDestino, String workingDir, String rutaIcono) {
        try {
            String iconoScript = (rutaIcono != null && new File(rutaIcono).exists())
                    ? String.format("$s.IconLocation = '%s,0'; ", rutaIcono.replace("'", "''"))
                    : "";

            String script = String.format(
                    "$ws = New-Object -ComObject WScript.Shell; " +
                    "$s = $ws.CreateShortcut('%s'); " +
                    "$s.TargetPath = '%s'; " +
                    "$s.WorkingDirectory = '%s'; " +
                    "%s" +
                    "$s.Description = 'Gestor de Artículos'; " +
                    "$s.Save()",
                    rutaLnk.replace("'", "''"),
                    rutaDestino.replace("'", "''"),
                    workingDir.replace("'", "''"),
                    iconoScript
            );

            ProcessBuilder pb = new ProcessBuilder("powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", script);
            pb.start().waitFor();
        } catch (Exception ex) {
            System.err.println("No se pudo crear acceso directo: " + ex.getMessage());
        }
    }

    // PASO 4: COMPLETADO
    private JPanel crearPaso4Finalizado() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(36, 40, 24, 40));

        JLabel lblIconoCheck = new JLabel(new FlatSVGIcon("icons/check.svg", 48, 48));
        lblIconoCheck.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTituloFin = new JLabel("Instalación completada");
        lblTituloFin.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTituloFin.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblMensaje = new JLabel("El Gestor de Artículos ya está listo para usarse.");
        lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMensaje.setForeground(COLOR_TEXTO_MUTED);
        lblMensaje.setAlignmentX(Component.CENTER_ALIGNMENT);

        chkEjecutarAlFinalizar = new JCheckBox("Abrir la aplicación al salir", true);
        chkEjecutarAlFinalizar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkEjecutarAlFinalizar.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(lblIconoCheck);
        panel.add(Box.createVerticalStrut(12));
        panel.add(lblTituloFin);
        panel.add(Box.createVerticalStrut(6));
        panel.add(lblMensaje);
        panel.add(Box.createVerticalStrut(18));
        panel.add(chkEjecutarAlFinalizar);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    // BARRA DE NAVEGACIÓN
    private JPanel crearBarraNavegacion() {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        nav.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 225, 230)));

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancelar.addActionListener(e -> System.exit(0));

        btnAnterior = new JButton("< Anterior");
        btnAnterior.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnAnterior.addActionListener(e -> retrocederPaso());

        btnSiguiente = new JButton("Siguiente >");
        btnSiguiente.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSiguiente.setBackground(COLOR_PRIMARIO);
        btnSiguiente.setForeground(Color.WHITE);
        btnSiguiente.addActionListener(e -> avanzarPaso());

        nav.add(btnCancelar);
        nav.add(btnAnterior);
        nav.add(btnSiguiente);

        return nav;
    }

    private void avanzarPaso() {
        if (pasoActual == 1) {
            pasoActual = 2;
            cardLayout.show(panelContenido, "paso2");
        } else if (pasoActual == 2) {
            pasoActual = 3;
            cardLayout.show(panelContenido, "paso3");
            iniciarProcesoInstalacion();
        } else if (pasoActual == 4) {
            finalizarInstalacion();
        }
        actualizarNavegacion();
    }

    private void retrocederPaso() {
        if (pasoActual == 2) {
            pasoActual = 1;
            cardLayout.show(panelContenido, "paso1");
        }
        actualizarNavegacion();
    }

    private void actualizarNavegacion() {
        lblPasoIndicador.setText("Paso " + pasoActual + " de " + TOTAL_PASOS);
        btnAnterior.setEnabled(pasoActual == 2);
        btnCancelar.setVisible(pasoActual < 4);

        if (pasoActual == 2) {
            btnSiguiente.setText("Instalar");
        } else if (pasoActual == 4) {
            btnSiguiente.setText("Finalizar");
            btnSiguiente.setEnabled(true);
        } else {
            btnSiguiente.setText("Siguiente >");
        }
    }

    private void finalizarInstalacion() {
        if (chkEjecutarAlFinalizar.isSelected() && directorioInstalado != null) {
            try {
                File launcher = new File(directorioInstalado, "iniciar.vbs");
                if (launcher.exists()) {
                    new ProcessBuilder("wscript", launcher.getAbsolutePath()).start();
                } else {
                    File jar = new File(directorioInstalado, "gestor-articulos.jar");
                    new ProcessBuilder("javaw", "-jar", jar.getAbsolutePath()).start();
                }
            } catch (Exception ex) {
                System.err.println("No se pudo iniciar la aplicación: " + ex.getMessage());
            }
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        FlatMacLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            new InstaladorApp().setVisible(true);
        });
    }
}
