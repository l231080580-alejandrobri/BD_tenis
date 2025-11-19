package sql;

import conexion.conexion_sql;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
public class crud_usuarios {
     private conexion_sql conexion;
     
       public crud_usuarios() {
        conexion = new conexion_sql();
       }
       
public String loginUsuario(String correo, String contraseña) {
    String contraseñaHash = MD5.hashMD5(contraseña).substring(0, 20);
    Connection conn = conexion.conectar();
    String sql = "SELECT rol FROM usuarios WHERE correo = ? AND contraseña = ?";

    try {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, correo);
        pstmt.setString(2, contraseñaHash);

        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            String rol = rs.getString("rol");
            JOptionPane.showMessageDialog(null, "Bienvenido " + rol, "Mensaje", JOptionPane.INFORMATION_MESSAGE);
return rol;
        } else {
            JOptionPane.showMessageDialog(null, "Uno de los campos es incorrectos", "Error de autenticacion", JOptionPane.ERROR_MESSAGE);
            return null;
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Error al verificar usuario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        return null;
    } finally {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cerrar conexión: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
      
public void Mostrar_usuarios (JTable paramJTableTotalUsuarios){
    
        conexion_sql objconexion = new conexion_sql();

   // DefaultTableModel modelo = new DefaultTableModel();
      DefaultTableModel modelo = new DefaultTableModel(){
    @Override
    public boolean isCellEditable(int row, int column){
    return false;
    }
    }; 
    String sql="";
    modelo.addColumn("Id_usuario");
    modelo.addColumn("Nombre");
    modelo.addColumn("Apellido");
    modelo.addColumn("Correo");
    modelo.addColumn("Contraseña");
    modelo.addColumn("Telefono");
    modelo.addColumn("Rol");
    modelo.addColumn("Fecha_registro");
    
    paramJTableTotalUsuarios.setModel(modelo);
    
    sql= "SELECT * FROM usuarios;";
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
        paramJTableTotalUsuarios.setModel(modelo);
        
    } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error "+e.toString());
    }
}
        
public boolean insertar_Usuario(String nombre, String apellido, String correo, String contraseña, String telefono, String rol, String fecha_registro) {
        String contraseñaHash = MD5.hashMD5(contraseña).substring(0, 20);
        Connection conn = conexion.conectar();
        String sql = "INSERT INTO usuarios (nombre, apellido, correo, contraseña,telefono,rol, fecha_registro) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try {
           
            PreparedStatement pstmt = conn.prepareStatement(sql);
         
            java.sql.Date fechaRegistro = java.sql.Date.valueOf(fecha_registro);

            //java.sql.Date fechaRegistro = new java.sql.Date(System.currentTimeMillis());
            pstmt.setString(1, nombre);
            pstmt.setString(2, apellido);
            pstmt.setString(3, correo);   
            pstmt.setString(4, contraseñaHash);
            pstmt.setString(5, telefono);
            pstmt.setString(6, rol);
            pstmt.setDate(7, fechaRegistro);
                        
            int resultado = pstmt.executeUpdate();
            
            if(resultado > 0) {
                JOptionPane.showMessageDialog(null, "Usuario registrado con éxito", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar usuario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } 
        catch (IllegalArgumentException e){
                       // JOptionPane.showMessageDialog(null, "formato de fecha invalido" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
JOptionPane.showMessageDialog(null, "Formato de fecha invalido", "Error", JOptionPane.ERROR_MESSAGE);
        }
        finally {
            try {
                
                if (conn != null) conn.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error al cerrar conexión: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

public void seleccionar_modificar_usuario(JTable paramJTableTotal_Usuarios,JTextField paramid_usu, JTextField param_nombre,
        JTextField param_apellido,JTextField param_Correo,JTextField param_Contraseña,
JTextField param_telefono,JTextField param_rol,JTextField paramfecha_registro){

    try {
        int fila =paramJTableTotal_Usuarios.getSelectedRow();
        if (fila>=0){
        paramid_usu.setText(paramJTableTotal_Usuarios.getValueAt(fila, 0).toString());
        param_nombre.setText(paramJTableTotal_Usuarios.getValueAt(fila, 1).toString());
        param_apellido.setText(paramJTableTotal_Usuarios.getValueAt(fila, 2).toString());
        param_Correo.setText(paramJTableTotal_Usuarios.getValueAt(fila, 3).toString());
        param_Contraseña.setText(paramJTableTotal_Usuarios.getValueAt(fila, 4).toString());
        param_telefono.setText(paramJTableTotal_Usuarios.getValueAt(fila, 5).toString());
        param_rol.setText(paramJTableTotal_Usuarios.getValueAt(fila, 6).toString());
        paramfecha_registro.setText(paramJTableTotal_Usuarios.getValueAt(fila, 7).toString());
        }
        else{
        JOptionPane.showMessageDialog(null, "Fila no selecccionada");
        } 
            
    } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error:"+e.toString());

    }
}


public boolean modificar_Usuario(int id_usuario, String nombre, String apellido, String correo, String contraseña, String telefono, String rol,String fecha_registro) {
    if (id_usuario <= 0) {
        JOptionPane.showMessageDialog(null, "ID de usuario inválido", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
    }
    
    // Si no se proporciona nueva contraseña, usa la actual (opcional: puedes hashear solo si !contraseña.isEmpty())
    String contraseñaHash = contraseña.isEmpty() ? null : MD5.hashMD5(contraseña).substring(0, 20);
    
    Connection conn = conexion.conectar();
    String sql = "UPDATE usuarios SET nombre = ?, apellido = ?, correo = ?, contraseña = ?, telefono = ?, rol = ?, fecha_registro= ? WHERE id_usuario = ?";
    
    try {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        java.sql.Date fecha = java.sql.Date.valueOf(fecha_registro);
        pstmt.setString(1, nombre);
        pstmt.setString(2, apellido);
        pstmt.setString(3, correo);
        pstmt.setString(4, contraseñaHash != null ? contraseñaHash : "");  // Si no hay nueva, deja vacía o maneja como quieras
        pstmt.setString(5, telefono);
        pstmt.setString(6, rol);
        pstmt.setDate(7, fecha);
      pstmt.setInt(8, id_usuario);
      
        int resultado = pstmt.executeUpdate();
        
        if (resultado > 0) {
            JOptionPane.showMessageDialog(null, "Usuario modificado con éxito", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "No se encontró el usuario para modificar", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Error al modificar usuario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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


public boolean eliminar_Usuario(int id_usuario) {
    Connection conn = conexion.conectar();
    PreparedStatement pstmt = null;
    
    try {    
        // Verificar primero si el usuario existe (DEBUG)
        String sqlVerificar = "SELECT COUNT(*) as existe FROM usuarios WHERE id_usuario = ?";
        PreparedStatement pstmtVerificar = conn.prepareStatement(sqlVerificar);
        pstmtVerificar.setInt(1, id_usuario);
        ResultSet rs = pstmtVerificar.executeQuery();
        
        if (rs.next()) {
            int count = rs.getInt("existe");
            System.out.println("DEBUG: Usuario ID " + id_usuario + " existe: " + (count > 0));
            
            if (count == 0) {
                JOptionPane.showMessageDialog(null, 
                    "No se encontró el usuario con ID: " + id_usuario, 
                    "Usuario no encontrado", 
                    JOptionPane.WARNING_MESSAGE);
                rs.close();
                pstmtVerificar.close();
                return false;
            }
        }
        rs.close();
        pstmtVerificar.close();
        
        // Ahora eliminar
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, id_usuario);
        
        int resultado = pstmt.executeUpdate();
        
        if (resultado > 0) {
            JOptionPane.showMessageDialog(null, 
                "Usuario eliminado correctamente", 
                "Éxito", 
                JOptionPane.INFORMATION_MESSAGE);
            System.out.println("DEBUG: Usuario ID " + id_usuario + " eliminado exitosamente");
            return true;
        } else {
            JOptionPane.showMessageDialog(null, 
                "No se pudo eliminar el usuario con ID: " + id_usuario, 
                "Error de eliminación", 
                JOptionPane.WARNING_MESSAGE);
            System.out.println("DEBUG: No se eliminó ningún registro para ID: " + id_usuario);
            return false;
        }
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, 
            "Error al eliminar usuario: " + e.getMessage(), 
            "Error de base de datos", 
            JOptionPane.ERROR_MESSAGE);
        System.err.println("ERROR SQL: " + e.getMessage());
        e.printStackTrace();
        return false;
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, 
            "Error inesperado: " + e.getMessage(), 
            "Error general", 
            JOptionPane.ERROR_MESSAGE);
        System.err.println("ERROR GENERAL: " + e.getMessage());
        e.printStackTrace();
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

