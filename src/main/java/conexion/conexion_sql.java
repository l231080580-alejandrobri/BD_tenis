
package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import javax.swing.JOptionPane;

public class conexion_sql {
    
        Connection conn=null;
    String url="jdbc:postgresql://localhost/BD_tenis";
    String usuario="postgres";
    String clave="camilo1020";
    
       public Connection conectar(){
    try {
            Class.forName("org.postgresql.Driver");
            conn=DriverManager.getConnection(url,usuario,clave);
            //JOptionPane.showMessageDialog(null, "Conexion exitosa");
            
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(null, "Error al conectar "+e,"Error",JOptionPane.ERROR_MESSAGE);
        }
    return conn;
    }
}
