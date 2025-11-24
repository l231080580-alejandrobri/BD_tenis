
package sql;

import conexion.conexion_sql;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import javax.swing.JOptionPane;
import javax.swing.JTable;




public class pdf_ticket {
         private conexion_sql conexion;

           public pdf_ticket() {
       conexion = new conexion_sql();
       }
    

public void generarTicketPDF(JTable tablaCarrito, double totalVenta, String nombreCliente) {
    try {
        
        Document document = new Document();
        String archivo = "ticket_venta.pdf";

        PdfWriter.getInstance(document, new FileOutputStream(archivo));
        document.open();

        // Nombre de la tienda
        Paragraph titulo = new Paragraph("INVENT_SHOES",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20));
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        document.add(new Paragraph(" "));

        // Información del cliente
        document.add(new Paragraph("Cliente: " + nombreCliente,
                FontFactory.getFont(FontFactory.HELVETICA, 12)));

        // Fecha

String fechaActual = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date());
document.add(new Paragraph("Fecha: " + fechaActual,
        FontFactory.getFont(FontFactory.HELVETICA, 12)));


        document.add(new Paragraph("--------------------------------------------------------------------------------------------------------------"));
        document.add(new Paragraph(" "));

        // Tabla con detalles del carrito
        PdfPTable pdfTabla = new PdfPTable(4);
        pdfTabla.addCell("ID");
        pdfTabla.addCell("Cantidad");
        pdfTabla.addCell("Precio Unitario");
        pdfTabla.addCell("Subtotal");

        for (int i = 0; i < tablaCarrito.getRowCount(); i++) {
            pdfTabla.addCell(tablaCarrito.getValueAt(i, 0).toString());
            pdfTabla.addCell(tablaCarrito.getValueAt(i, 1).toString());
            pdfTabla.addCell(tablaCarrito.getValueAt(i, 2).toString());
            pdfTabla.addCell(tablaCarrito.getValueAt(i, 3).toString());
        }

        document.add(pdfTabla);

        document.add(new Paragraph("--------------------------------------------------------------------------------------------------------------"));

        // Total
        Paragraph totalTxt = new Paragraph("TOTAL A PAGAR: $" + totalVenta,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
        totalTxt.setAlignment(Element.ALIGN_RIGHT);
        document.add(totalTxt);

        document.add(new Paragraph(" "));
        document.add(new Paragraph("¡Gracias por su compra! 📦",
                FontFactory.getFont(FontFactory.HELVETICA, 12)));

        document.close();

        JOptionPane.showMessageDialog(null,
                "✅ Ticket generado correctamente: " + archivo);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "❌ ERROR TICKET: " + e.getMessage());
    }
}
    
           
           
           
}
