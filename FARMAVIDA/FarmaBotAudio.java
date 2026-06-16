import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONArray;
import javax.sound.sampled.*;

public class FarmaBotAudio extends JFrame {

    // Componentes de la interfaz (Basados en image_8f8537.png)
    private JTextArea areaChat;
    private JTextField campoTexto;
    private JButton botonEnviar;
    
    // CONFIGURACIÓN DE LA IA (Reemplaza con tu clave de OpenAI)
    private static final String API_KEY = "TU_API_KEY_AQUÍ"; 
    private static final OkHttpClient client = new OkHttpClient();

    public FarmaBotAudio() {
        // Configuración de la ventana principal
        setTitle("FARMAVIDA - Sistema de Gestión Farmacéutica");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Panel superior simulando las pestañas de image_8f8537.png
        JPanel panelPestañas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelPestañas.add(new JButton("Módulo Operativo de Ventas"));
        panelPestañas.add(new JButton("Historial y Reportes de Auditoría"));
        JButton btnActivo = new JButton("Chatbot Atención al Cliente");
        btnActivo.setEnabled(false); // Simula pestaña activa
        panelPestañas.add(btnActivo);
        add(panelPestañas, BorderLayout.NORTH);

        // Área central: El chat
        areaChat = new JTextArea();
        areaChat.setEditable(false);
        areaChat.setLineWrap(true);
        areaChat.setWrapStyleWord(true);
        areaChat.setFont(new Font("Arial", Font.PLAIN, 14));
        areaChat.setText("FarmaBot: ¡Hola! Bienvenido al servicio de atención automática. Escribe un mensaje para empezar.\n\n");
        
        JScrollPane scrollPane = new JScrollPane(areaChat);
        add(scrollPane, BorderLayout.CENTER);

        // Panel inferior: Campo de texto y botón Enviar
        JPanel panelInferior = new JPanel(new BorderLayout());
        campoTexto = new JTextField();
        campoTexto.setFont(new Font("Arial", Font.PLAIN, 14));
        botonEnviar = new JButton("Enviar Consulta");

        panelInferior.add(campoTexto, BorderLayout.CENTER);
        panelInferior.add(botonEnviar, BorderLayout.EAST);
        add(panelInferior, BorderLayout.SOUTH);

        // Evento al presionar el botón Enviar
        botonEnviar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                procesarConsulta();
            }
        });

        // Evento al presionar ENTER en el teclado
        campoTexto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                procesarConsulta();
            }
        });
    }

    // Lógica principal al enviar un mensaje
    private void procesarConsulta() {
        String textoUsuario = campoTexto.getText().trim();
        if (textoUsuario.isEmpty()) return;

        // 1. Mostrar lo que escribió el usuario en pantalla
        areaChat.append("Usuario: " + textoUsuario + "\n");
        campoTexto.setText(""); // Limpiar entrada

        // Desactivar botón temporalmente mientras procesa
        botonEnviar.setEnabled(false);

        // Ejecutar la petición en un hilo secundario para que la app no se congele
        new Thread(() -> {
            try {
                // 2. Obtener respuesta humana de la IA
                areaChat.append("FarmaBot está pensando...\n");
                String respuestaIA = obtenerRespuestaIA(textoUsuario);
                
                // Borrar el mensaje de "pensando" y poner la respuesta real
                areaChat.append("FarmaBot: " + respuestaIA + "\n\n");

                // 3. Convertir la respuesta a audio y reproducirla automáticamente
                reproducirAudioVoz(respuestaIA);

            } catch (Exception ex) {
                areaChat.append("Sistema: Error al procesar la solicitud o el audio.\n\n");
                ex.printStackTrace();
            } {
                // Reactivar el botón de enviar
                SwingUtilities.invokeLater(() -> botonEnviar.setEnabled(true));
            }
        }).start();
    }

    // CONEXIÓN CON LA IA (Tono Humano)
    private String obtenerRespuestaIA(String mensajeUsuario) throws Exception {
        String url = "https://api.openai.com/v1/chat/completions";

        // Aquí le damos la "personalidad" humana al bot en el campo 'system'
        JSONObject promptSistema = new JSONObject()
                .put("role", "system")
                .put("content", "Eres un farmacéutico humano, muy amable, cálido y servicial de la farmacia FarmaVida. " +
                                "Responde de forma natural, fluida y empática, como si hablaras en persona. " +
                                "Da respuestas profesionales pero cortas y directas, ideales para ser escuchadas en audio.");

        JSONObject promptUsuario = new JSONObject()
                .put("role", "user")
                .put("content", mensajeUsuario);

        JSONArray mensajes = new JSONArray().put(promptSistema).put(promptUsuario);

        JSONObject jsonCuerpo = new JSONObject()
                .put("model", "gpt-4o-mini") // Modelo rápido y económico
                .put("messages", mensajes);

        RequestBody body = RequestBody.create(
                jsonCuerpo.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Error en IA: " + response);
            
            JSONObject jsonRespuesta = new JSONObject(response.body().string());
            return jsonRespuesta.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        }
    }

    // CONEXIÓN CON TEXT-TO-SPEECH (Generación y reproducción del Audio)
    private void reproducirAudioVoz(String textoAI) throws Exception {
        String url = "https://api.openai.com/v1/audio/speech";

        JSONObject jsonCuerpo = new JSONObject()
                .put("model", "tts-1") // Modelo de voz en tiempo real
                .put("input", textoAI)
                .put("voice", "shimmer") // Voces sugeridas en español: 'shimmer', 'alloy', 'echo'
                .put("response_format", "wav"); // Usamos WAV porque Java lo reproduce nativamente sin librerías extra

        RequestBody body = RequestBody.create(
                jsonCuerpo.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Error en Audio: " + response);

            // Obtener el flujo de bytes de audio (WAV)
            byte[] audioBytes = response.body().bytes();
            
            // Reproducir el audio automáticamente usando la tarjeta de sonido
            InputStream inputStream = new ByteArrayInputStream(audioBytes);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(inputStream);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start(); // ¡Aquí empieza a hablar automáticamente!
        }
    }

    // Método de arranque
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FarmaBotAudio().setVisible(true);
        });
    }
}