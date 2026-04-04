package com.posbarlacteo.PosBarLacteo.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.stereotype.Service;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import posintegradogetnet.POSCommands;
import posintegradogetnet.POSIntegrado;
import posintegradogetnet.exceptions.CustomException;

@Service
public class GetnetService {

    private final String PUERTO_COM = "COM1";
    private CompletableFuture<Boolean> exitoFuture;

    public void conectar() throws CustomException {
        // 1. Limpieza total de puertos antes de iniciar
        try { 
            POSIntegrado.getInstance().disposePort(); 
        } catch (Exception e) {
            System.out.println("Puerto ya estaba libre.");
        }
        
        // 2. Conexión estándar al COM1
        POSIntegrado.getInstance().usbConnect(PUERTO_COM, 115200);
    }

    public void iniciarCobro(int monto) throws Exception {
        this.exitoFuture = new CompletableFuture<>();

        // 3. Obtener el puerto y configurar el listener de respuestas
        SerialPort port = POSIntegrado.getInstance().getnetSerialPort();
        
        if (port != null) {
            port.removeDataListener();
            port.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }

                @Override
                public void serialEvent(SerialPortEvent spe) {
                    try {
                        // Obtenemos el texto crudo del puerto
                        String dataReceived = POSIntegrado.getInstance().dataReceived();
                        if (dataReceived == null || dataReceived.isEmpty()) return;
                        
                        System.out.println("📥 DEBUG POS: " + dataReceived);

                        // 1. Saltamos el mensaje de confirmación de recepción
                        if (dataReceived.contains("Received\":true")) return;

                        // 2. BUSQUEDA FLEXIBLE (Ignorando escapes y espacios)
                        // Buscamos la combinación de FunctionCode 100 (Venta) y ResponseCode 0 (Aprobado)
                        boolean esVenta = dataReceived.contains("FunctionCode") && dataReceived.contains("100");
                        boolean esAprobado = dataReceived.contains("ResponseCode") && (dataReceived.contains(":0") || dataReceived.contains(": 0"));

                        if (esVenta && esAprobado) {
                            System.out.println("⭐ ¡VENTA APROBADA DETECTADA! Liberando React...");
                            // Esto es lo que hace que el .get() de Java termine y le responda a React
                            exitoFuture.complete(true); 
                        } 
                        else if (dataReceived.contains("ResponseCode")) {
                            // Si hay un código de respuesta pero no es 0
                            System.out.println("❌ Venta Rechazada o Error en POS.");
                            exitoFuture.complete(false);
                        }

                    } catch (Exception e) {
                        System.err.println("Error procesando datos del puerto: " + e.getMessage());
                    }
                }
            });
        }

        // 4. Pequeña pausa para asegurar que el buffer esté listo
        Thread.sleep(500); 

        // 5. ENVIAR LA VENTA
        System.out.println("🚀 Enviando orden de venta al Simulador...");
        POSIntegrado.getInstance().sale(monto, "0001", true, POSCommands.SaleType.SALE, false, 1, 60);

        // 6. ESPERA SINCRÓNICA PARA REACT
        try {
            Boolean resultado = exitoFuture.get(90, TimeUnit.SECONDS);
            if (!resultado) throw new Exception("Venta rechazada.");
            System.out.println("💰 Pago Aprobado!");
        } catch (TimeoutException e) {
            throw new Exception("El simulador no respondió. Verifica que el Agente POS esté en COM2.");
        } finally {
            if (port != null) port.removeDataListener();
        }
    }
}