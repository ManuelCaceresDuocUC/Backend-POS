package com.posbarlacteo.PosBarLacteo.service;

import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.springframework.stereotype.Service;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.posbarlacteo.PosBarLacteo.model.Venta;
import com.posbarlacteo.PosBarLacteo.model.VentaDetalle;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValeCreditoPdfService {

    public String generarGuardarYImprimirVale(Venta venta, String local) {
        // Declaramos el documento y el flujo de datos fuera del try para poder cerrarlos siempre
        Document documento = null;
        FileOutputStream fos = null;
        
        try {
            String userHome = System.getProperty("user.home");
            Path rutaDirectorio = Paths.get(userHome, "PosBarLacteo", local.toUpperCase(), "pendientes");
            Files.createDirectories(rutaDirectorio); 
            
            String nombreArchivo = "VALE_CREDITO_" + venta.getId() + ".pdf";
            File archivoPdf = new File(rutaDirectorio.toFile(), nombreArchivo);

            Rectangle pageSize = new Rectangle(226, PageSize.A4.getHeight());
            documento = new Document(pageSize, 5, 5, 10, 10);
            fos = new FileOutputStream(archivoPdf);
            
            PdfWriter.getInstance(documento, fos);
            documento.open();

            Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            DecimalFormat df = new DecimalFormat("#,##0");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            // CABECERA
            documento.add(new Paragraph("==========================", bold));
            documento.add(new Paragraph("⚓ VALE DE CRÉDITO ⚓", titulo));
            documento.add(new Paragraph("==========================", bold));
            documento.add(new Paragraph("Local: " + local, normal));
            documento.add(new Paragraph("Fecha: " + venta.getFechaHora().format(formatter), normal));
            documento.add(new Paragraph("Venta ID: " + venta.getId(), normal));
            
            if(venta.getCliente() != null) {
                documento.add(new Paragraph("Cliente: " + venta.getCliente().getNombre().toUpperCase(), bold));
                documento.add(new Paragraph("RUT: " + (venta.getCliente().getRut() != null ? venta.getCliente().getRut() : "N/A"), normal));
            }
            documento.add(new Paragraph("--------------------------------", normal));

            // DETALLE DE LA COMPRA
            documento.add(new Paragraph("DETALLE:", bold));
            for (VentaDetalle detalle : venta.getDetalles()) {
                
                // 🟢 SOLUCIÓN: Verificamos si el precio unitario viene nulo. Si es así, usamos el precio base del producto.
                Double precioObtenido = detalle.getPrecioUnitario();
                if (precioObtenido == null) {
                    precioObtenido = detalle.getProducto().getPrecio();
                }
                
                // Si por alguna razón el producto tampoco tiene precio, usamos 0 para evitar caída.
                double precioUnitario = (precioObtenido != null) ? precioObtenido : 0.0;
                double subtotal = detalle.getCantidad() * precioUnitario;

                String linea = detalle.getCantidad() + "x " + detalle.getProducto().getDescripcion() 
                             + " ($" + df.format(precioUnitario) + ") = $" + df.format(subtotal);
                            
                documento.add(new Paragraph(linea, normal));
            }
            documento.add(new Paragraph("--------------------------------", normal));

            // TOTAL Y FIRMA
            documento.add(new Paragraph("TOTAL ADEUDADO: $" + df.format(venta.getTotal()), titulo));
            documento.add(new Paragraph(" ", normal));
            documento.add(new Paragraph(" ", normal));
            documento.add(new Paragraph("__________________________", bold));
            documento.add(new Paragraph("Firma del Cliente", normal));
            documento.add(new Paragraph("Acepto la deuda detallada", normal));
            documento.add(new Paragraph(" ", normal));
            documento.add(new Paragraph(".", normal)); 

            // Cerramos de forma segura
            documento.close();
            fos.close();

            // MANDAR A IMPRIMIR
            imprimirTicket(archivoPdf);

            return archivoPdf.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            // Aseguramos cerrar recursos si ocurre un error inesperado
            if (documento != null && documento.isOpen()) {
                documento.close();
            }
            try {
                if (fos != null) fos.close();
            } catch (Exception ignored) {}
            
            return null;
        }
    }

    private void imprimirTicket(File archivoPdf) {
        try {
            PDDocument document = PDDocument.load(archivoPdf);
            PrintService myPrintService = PrintServiceLookup.lookupDefaultPrintService();
            
            if (myPrintService != null) {
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setPageable(new PDFPageable(document));
                job.setPrintService(myPrintService);
                job.print();
            } else {
                System.out.println("No se encontró una impresora por defecto instalada.");
            }
            
            document.close();
        } catch (Exception e) {
            System.err.println("Error al intentar imprimir el vale: " + e.getMessage());
            e.printStackTrace();
        }
    }
}