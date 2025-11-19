
package sql;

import conexion.conexion_sql;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;


public class crud_clientes {
     private conexion_sql conexion;
     
     //guarde el id_cliente
     public static int idClientelogeado = -1;
     
      public crud_clientes() {
       conexion = new conexion_sql();
       }
public boolean loginCliente(String correo, String contraseña) {
        String contraseñaHash = MD5.hashMD5(contraseña).substring(0, 20);
        Connection conn = conexion.conectar();
        String sql = "SELECT * FROM clientes WHERE correo = ? AND contraseña = ?";
        
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, correo);
            pstmt.setString(2, contraseñaHash);
            
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                int idcliente = rs.getInt("id_clientes");
                idClientelogeado = idcliente;
                String nombre = rs.getString("nombre");
            JOptionPane.showMessageDialog(null, "Bienvenido " +nombre, "Mensaje" , JOptionPane.INFORMATION_MESSAGE);
            return true;
}else{
 JOptionPane.showMessageDialog(null, "Uno de los campos es incorrectos", "Error de autenticacion", JOptionPane.ERROR_MESSAGE);
            return false;
                    }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al verificar usuario: " + e.getMessage(), "Error de autenticacion", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error al cerrar conexión: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
         
          }


public boolean RegistrarCliente(String nombre, String apellido, String correo, String contraseña, String telefono) {
        String contraseñaHash = MD5.hashMD5(contraseña).substring(0, 20);
        Connection conn = conexion.conectar();
        String sql = "INSERT INTO clientes (nombre, apellido, correo, contraseña, telefono) VALUES (?, ?, ?, ?, ?)";
        
        try {
           
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nombre);
            pstmt.setString(2, apellido);
            pstmt.setString(3, correo);   
            pstmt.setString(4, contraseñaHash);
            pstmt.setString(5, telefono);

            int resultado = pstmt.executeUpdate();
            
            if(resultado > 0) {
                JOptionPane.showMessageDialog(null, "Cliente registrado con éxito", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar usuario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error al cerrar conexión: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }
//Metodo para mostrar productos a clientes     
public void Mostrar_productos_clientes (JTable paramJTableTotalProductosClientes){
    
        conexion_sql objconexion = new conexion_sql();

    //DefaultTableModel modelo = new DefaultTableModel();
      DefaultTableModel modelo = new DefaultTableModel(){
    @Override
    public boolean isCellEditable(int row, int column){
    return false;
    }
    }; 
    String sql="";
    modelo.addColumn("Id_producto");
    modelo.addColumn("Nombre");
    modelo.addColumn("Marca");
    modelo.addColumn("Talla");
    modelo.addColumn("Color");
    modelo.addColumn("Precio");
    modelo.addColumn("Descripcion");
    modelo.addColumn("Disponible");
    
    paramJTableTotalProductosClientes.setModel(modelo);
    
    sql= "SELECT * FROM productos;";
    String [] datos = new String[8];
    Statement st;
    try {
        
        st= objconexion.conectar().createStatement();
        
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()){
            datos [0]=rs.getString(1);
            datos [1]=rs.getString(2);
            datos [2]=rs.getString(3);
            datos [3]=rs.getString(4);
            datos [4]=rs.getString(5);
            datos [5]=rs.getString(6);
            datos [6]=rs.getString(7);
            datos [7]=rs.getString(8);
            modelo.addRow(datos);
        }
        paramJTableTotalProductosClientes.setModel(modelo);
        
    } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error "+e.toString());
    }
}
//Metodo para que cliente seleccione producto para agregar al carrito 
public void seleccionar_producto_clientes(JTable paramJTableTotal_Productos_clientes,JTextField paramid_producto, JTextField param_nombre,
        JTextField param_marca,JTextField param_talla,JTextField param_color,
        JTextField param_precio,JTextField param_descripcion,JTextField param_disponible){

    try {
        int fila =paramJTableTotal_Productos_clientes.getSelectedRow();
        if (fila>=0){
        paramid_producto.setText(paramJTableTotal_Productos_clientes.getValueAt(fila, 0).toString());
        param_nombre.setText(paramJTableTotal_Productos_clientes.getValueAt(fila, 1).toString());
        param_marca.setText(paramJTableTotal_Productos_clientes.getValueAt(fila, 2).toString());
        param_talla.setText(paramJTableTotal_Productos_clientes.getValueAt(fila, 3).toString());
        param_color.setText(paramJTableTotal_Productos_clientes.getValueAt(fila, 4).toString());
        param_precio.setText(paramJTableTotal_Productos_clientes.getValueAt(fila, 5).toString());
        param_descripcion.setText(paramJTableTotal_Productos_clientes.getValueAt(fila, 6).toString());
        param_disponible.setText(paramJTableTotal_Productos_clientes.getValueAt(fila, 7).toString());
        }
        else{
        JOptionPane.showMessageDialog(null, "Fila no selecccionada");
        } 
            
    } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error:"+e.toString());
    }
}

// Método para agregar producto al carrito 
public void agregarAlCarrito(JTable tablaCarrito,
                             JTable tablaProductos,
                             JTextField param_id_producto,
                             JTextField param_precio,
                             JTextField param_disponible) {

    try {
        if (param_id_producto.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Seleccione un producto","Mensaje", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String cantidadStr = JOptionPane.showInputDialog(null, "Cantidad a comprar:","Mensaje", JOptionPane.INFORMATION_MESSAGE);

       if (cantidadStr == null || cantidadStr.trim().isEmpty()) {
           JOptionPane.showMessageDialog(null, "Debe ingresar una cantidad","Mensaje", JOptionPane.INFORMATION_MESSAGE);
          return;
        }

        // Solo sean números (no letras ni símbolos)
        if (!cantidadStr.matches("\\d+")) {
            JOptionPane.showMessageDialog(null, "Solo se permiten números (sin letras ni símbolos)","Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int cantidad = Integer.parseInt(cantidadStr);
        int stock = Integer.parseInt(param_disponible.getText());

        if (cantidad <= 0) {
            JOptionPane.showMessageDialog(null, "La cantidad debe ser mayor a 0","Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (cantidad > stock) {
            JOptionPane.showMessageDialog(null, "Stock insuficiente, disponible: " + stock,"Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idProd = Integer.parseInt(param_id_producto.getText());
        double precio = Double.parseDouble(param_precio.getText());

        // Evitar duplicados en carrito
         for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
            int idTabla = Integer.parseInt(modeloCarrito.getValueAt(i, 0).toString());

            if (idTabla == idProd) {
                int cantidadExistente = Integer.parseInt(modeloCarrito.getValueAt(i, 1).toString());
                int nuevaCantidad = cantidadExistente + cantidad;

                if (nuevaCantidad > stock) {
                    JOptionPane.showMessageDialog(null, "No hay suficiente stock","Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                modeloCarrito.setValueAt(nuevaCantidad, i, 1);
                modeloCarrito.setValueAt(nuevaCantidad * precio, i, 3);

           
                actualizarStockProductos(tablaProductos, idProd, stock - cantidad);
                actualizarStockBD(idProd, stock - cantidad);

                param_disponible.setText(String.valueOf(stock - cantidad));
                JOptionPane.showMessageDialog(null, "Cantidad actualizada","Mensaje", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        // Producto nuevo en el carrito
        modeloCarrito.addRow(new Object[]{idProd, cantidad, precio, precio * cantidad});

        int nuevoStock = stock - cantidad;
        param_disponible.setText(String.valueOf(nuevoStock));

    
        actualizarStockProductos(tablaProductos, idProd, nuevoStock);
        actualizarStockBD(idProd, nuevoStock);

        JOptionPane.showMessageDialog(null, "Producto agregado","Exito", JOptionPane.INFORMATION_MESSAGE);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "ERROR: " + e.getMessage());
    }
}


//Metodo para que no se repita producto del carrito
public void actualizarStockProductos(JTable tablaProductos, int idProducto, int nuevoStock) {
    DefaultTableModel modelo = (DefaultTableModel) tablaProductos.getModel();

    for (int i = 0; i < modelo.getRowCount(); i++) {
        int idTabla = Integer.parseInt(modelo.getValueAt(i, 0).toString());
        if (idTabla == idProducto) {
            modelo.setValueAt(String.valueOf(nuevoStock), i, 7);
            break;
        }
    }
}


//Metodo para actualizar stock de BD y en la tb_productos despues de eliminarlos del carrito 
public void actualizarStockBD(int idProducto, int nuevoStock) {
        Connection conn = conexion.conectar();

    try {
        String sql = "UPDATE productos SET stock = ? WHERE id_producto = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, nuevoStock);
        pstmt.setInt(2, idProducto);
        pstmt.executeUpdate();
        conn.close();
    } catch (Exception e) {
        System.out.println("Error al actualizar stock BD: " + e);
    }
}

// Modelo global para el carrito
public static DefaultTableModel modeloCarrito = new DefaultTableModel(
    new Object[]{"Id_producto", "Cantidad", "Precio", "Total"}, 0
){
    //desabilitar columnas
    @Override
    public boolean isCellEditable(int row, int column){
    return false;
    }
};
//Metodo para mostrar carrito
public void MostrarCarrito(JTable tablaCarrito) {
    tablaCarrito.setModel(modeloCarrito);
}


//Metodo para eliminar producto del carrito
public void eliminarDelCarrito(JTable tablaCarrito) {

    int fila = tablaCarrito.getSelectedRow();

    if (fila == -1) {
        JOptionPane.showMessageDialog(null, "Seleccione un producto del carrito", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        // Obtener datos del carrito
        int idProd = Integer.parseInt(modeloCarrito.getValueAt(fila, 0).toString());
        int cantidadCarrito = Integer.parseInt(modeloCarrito.getValueAt(fila, 1).toString());

        //  Recuperar stock de BD
        int stockActualBD = obtenerStockBD(idProd);
        int nuevoStock = stockActualBD + cantidadCarrito;

        //  Actualizar BD con el nuevo stock
        actualizarStockBD(idProd, nuevoStock);

        //  Eliminar del carrito
        modeloCarrito.removeRow(fila);

        JOptionPane.showMessageDialog(null, "Producto eliminado", "Éxito", JOptionPane.INFORMATION_MESSAGE);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "ERROR al eliminar: " + e.getMessage());
    }
}

// Metodo para obtener stock actual directo desde BD despues de borrar del carrito
private int obtenerStockBD(int idProducto) {
    int stock = 0;
    Connection conn = conexion.conectar();

    try {
        String sql = "SELECT stock FROM productos WHERE id_producto = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, idProducto);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            stock = rs.getInt("stock");
        }
        conn.close();
    } catch (Exception e) {
        System.out.println("Error consultando stock BD: " + e);
    }
    return stock;
}


public void finalizarCompra(JTable tablaCarrito) {

    if (modeloCarrito.getRowCount() == 0) {
        JOptionPane.showMessageDialog(null, "No hay productos en el carrito","Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    if (idClientelogeado == -1) {
        JOptionPane.showMessageDialog(null, "No se encontró cliente","Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    int confirmacion = JOptionPane.showConfirmDialog(null, 
        "¿Finalizar compra?",
        "Confirmar venta",
        JOptionPane.YES_NO_OPTION);

    if (confirmacion != JOptionPane.YES_OPTION) {
        return;
    }

    Connection conn = conexion.conectar();

    try {

        // Calcular total de la venta
        double totalVenta = 0.0;
        for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
            totalVenta += Double.parseDouble(modeloCarrito.getValueAt(i, 3).toString());
        }

        String sql = "INSERT INTO ventas (id_producto, id_clientes, cantidad, total, fecha_salida) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        PreparedStatement pstmt = conn.prepareStatement(sql);

        for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
            int idProducto = Integer.parseInt(modeloCarrito.getValueAt(i, 0).toString());
            int cantidad = Integer.parseInt(modeloCarrito.getValueAt(i, 1).toString());
            double totalFila = Double.parseDouble(modeloCarrito.getValueAt(i, 3).toString());

            pstmt.setInt(1, idProducto);
            pstmt.setInt(2, idClientelogeado);
            pstmt.setInt(3, cantidad);
            pstmt.setDouble(4, totalFila);
            pstmt.executeUpdate();
        }

// Obtener el nombre del cliente desde BD
String nombreCliente = "Cliente General";
try {
    PreparedStatement pstmtCliente = conn.prepareStatement(
            "SELECT nombre FROM clientes WHERE id_clientes = ?");
    pstmtCliente.setInt(1, idClientelogeado);
    ResultSet rsCliente = pstmtCliente.executeQuery();
    if (rsCliente.next()) {
        nombreCliente = rsCliente.getString("nombre");
    }
} catch (Exception ex) {
    System.out.println("No se encontró nombre, usando Cliente General");
}

// clase donde está el método del PDF
pdf_ticket ticket = new pdf_ticket();
ticket.generarTicketPDF(tablaCarrito, totalVenta, nombreCliente);


        modeloCarrito.setRowCount(0); // limpiar carrito tras venta

        JOptionPane.showMessageDialog(null, "¡Compra realizada con éxito!","Éxito", JOptionPane.INFORMATION_MESSAGE);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error al registrar venta: " + e.getMessage());
    } finally {
        try {
            conn.close();
        } catch (Exception e) {}
    }
}










        }


