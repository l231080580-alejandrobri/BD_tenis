
package sql;

import conexion.conexion_sql;
import java.awt.HeadlessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;


public class crud_productos {
         private conexion_sql conexion;

         public crud_productos() {
        conexion = new conexion_sql();
       }  
    public void Mostrar_productos (JTable paramJTableTotalProductos){
    
        conexion_sql objconexion = new conexion_sql();
//Codigo para inhabilitar columnas
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
    modelo.addColumn("Stock");
    
    paramJTableTotalProductos.setModel(modelo);
        
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
        paramJTableTotalProductos.setModel(modelo);
        
    } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error "+e.toString());
    }
}
         
        public boolean insertar_producto(String nombre, String marca, String tallaD, String color, String precioD, String descripcion, String stockI) {
       // String contraseñaHash = MD5.hashMD5(contraseña).substring(0, 20);
        Connection conn = conexion.conectar();
        String sql = "INSERT INTO productos (nombre, marca, talla, color, precio, descripcion, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try {
           
            PreparedStatement pstmt = conn.prepareStatement(sql);
            //conversion de datos reales 
            double talla = Double.parseDouble(tallaD);
            double precio = Double.parseDouble(precioD);
            int stock = Integer.parseInt(stockI);
           // java.sql.Date fechaRegistro = java.sql.Date.valueOf(fecha_registro); (fecha manualmente 

            //java.sql.Date fechaRegistro = new java.sql.Date(System.currentTimeMillis()); (fecha sistema)
            pstmt.setString(1, nombre);
            pstmt.setString(2, marca);
            pstmt.setDouble(3, talla);   
            pstmt.setString(4, color);
            pstmt.setDouble(5, precio);
            pstmt.setString(6, descripcion);
            pstmt.setInt(7, stock);
                        
            int resultado = pstmt.executeUpdate();
            
            if(resultado > 0) {
                JOptionPane.showMessageDialog(null, "Producto registrado con éxito", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar producto: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } 
       // catch (IllegalArgumentException e){
//JOptionPane.showMessageDialog(null, "Formato de fecha invalido", "Error", JOptionPane.ERROR_MESSAGE);
  //      }
        finally {
            try {
                
                if (conn != null) conn.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error al cerrar conexión: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }
        
        public void seleccionar_modificar_producto(JTable paramJTableTotal_Productos,JTextField paramid_producto, JTextField param_nombre,
        JTextField param_marca,JTextField param_talla,JTextField param_color,
        JTextField param_precio,JTextField param_descripcion,JTextField param_stock){

    try {
        int fila =paramJTableTotal_Productos.getSelectedRow();
        if (fila>=0){
        paramid_producto.setText(paramJTableTotal_Productos.getValueAt(fila, 0).toString());
        param_nombre.setText(paramJTableTotal_Productos.getValueAt(fila, 1).toString());
        param_marca.setText(paramJTableTotal_Productos.getValueAt(fila, 2).toString());
        param_talla.setText(paramJTableTotal_Productos.getValueAt(fila, 3).toString());
        param_color.setText(paramJTableTotal_Productos.getValueAt(fila, 4).toString());
        param_precio.setText(paramJTableTotal_Productos.getValueAt(fila, 5).toString());
        param_descripcion.setText(paramJTableTotal_Productos.getValueAt(fila, 6).toString());
        param_stock.setText(paramJTableTotal_Productos.getValueAt(fila, 7).toString());
        }
        else{
        JOptionPane.showMessageDialog(null, "Fila no selecccionada");
        } 
            
    } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error:"+e.toString());
    }
}

        public boolean modificar_Producto(int id_producto, String nombre, String marca, String tallaD, String color, String precioD, String descripcion,String stockI) {
    if (id_producto <= 0) {
        JOptionPane.showMessageDialog(null, "ID de usuario inválido", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
    }
    
    // Si no se proporciona nueva contraseña, usa la actual (opcional: puedes hashear solo si !contraseña.isEmpty())
  //  String contraseñaHash = contraseña.isEmpty() ? null : MD5.hashMD5(contraseña).substring(0, 20);
    
    Connection conn = conexion.conectar();
    String sql = "UPDATE productos SET nombre = ?, marca = ?, talla = ?, color = ?, precio = ?, descripcion = ?, stock= ? WHERE id_producto = ?";
    
    try {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        
         double talla = Double.parseDouble(tallaD);
         double precio = Double.parseDouble(precioD);
         int stock = Integer.parseInt(stockI);
        //java.sql.Date fecha = java.sql.Date.valueOf(fecha_registro);
        pstmt.setString(1, nombre);
        pstmt.setString(2, marca);
        pstmt.setDouble(3, talla);
        pstmt.setString(4, color);  
        pstmt.setDouble(5, precio);
        pstmt.setString(6, descripcion);
        pstmt.setInt(7, stock);
      pstmt.setInt(8, id_producto);
      
        int resultado = pstmt.executeUpdate();
        
        if (resultado > 0) {
            JOptionPane.showMessageDialog(null, "Producto modificado con éxito", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "No se encontró el producto para modificar", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Error al modificar producto: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        return false;
    } finally {
        try {
          //  if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cerrar conexión: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

        
        public boolean eliminar_Producto(int id_producto) {
    Connection conn = conexion.conectar();
    PreparedStatement pstmt = null;
    
    try {    
        // Verificar primero si el usuario existe (DEBUG)
        String sqlVerificar = "SELECT COUNT(*) as existe FROM productos WHERE id_producto = ?";
        try (PreparedStatement pstmtVerificar = conn.prepareStatement(sqlVerificar)) {
            pstmtVerificar.setInt(1, id_producto);
            try (ResultSet rs = pstmtVerificar.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("existe");
                    System.out.println("DEBUG: Producto ID " + id_producto + " existe: " + (count > 0));
                    
                    if (count == 0) {
                        JOptionPane.showMessageDialog(null,
                                "No se encontró el producto con ID: " + id_producto,
                                "Producto no encontrado",
                                JOptionPane.WARNING_MESSAGE);
                        rs.close();
                        pstmtVerificar.close();
                        return false;
                    }
                }
            }
        }
        
        // Ahora eliminar
        String sql = "DELETE FROM productos WHERE id_producto = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, id_producto);
        
        int resultado = pstmt.executeUpdate();
        
        if (resultado > 0) {
            JOptionPane.showMessageDialog(null, 
                "Producto eliminado correctamente", 
                "Éxito", 
                JOptionPane.INFORMATION_MESSAGE);
            System.out.println("DEBUG: Producto ID " + id_producto + " eliminado exitosamente");
            return true;
        } else {
            JOptionPane.showMessageDialog(null, 
                "No se pudo eliminar el producto con ID: " + id_producto, 
                "Error de eliminación", 
                JOptionPane.WARNING_MESSAGE);
            System.out.println("DEBUG: No se eliminó ningún registro para ID: " + id_producto);
            return false;
        }
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, 
            "Error al eliminar producto: " + e.getMessage(), 
            "Error de base de datos", 
            JOptionPane.ERROR_MESSAGE);
        System.err.println("ERROR SQL: " + e.getMessage());
        return false;
        
    } catch (HeadlessException e) {
        JOptionPane.showMessageDialog(null, 
            "Error inesperado: " + e.getMessage(), 
            "Error general", 
            JOptionPane.ERROR_MESSAGE);
        System.err.println("ERROR GENERAL: " + e.getMessage());
        return false;
        
    } finally {
        // Cerrar recursos en orden correcto
        try {
            if (pstmt != null) {
                pstmt.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar PreparedStatement: " + e.getMessage());
        }
        
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }
}
        
}
