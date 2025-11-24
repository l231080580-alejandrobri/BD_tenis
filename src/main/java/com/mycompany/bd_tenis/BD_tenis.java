

package com.mycompany.bd_tenis;

import conexion.conexion_sql;
import vistas.Inicio;


public class BD_tenis {

    public static void main(String[] args) {
        //conexion_sql objetoConexion=new conexion_sql();
        //objetoConexion.conectar();
      Inicio abrir=new Inicio();
      abrir.setVisible(true);
      abrir.setLocationRelativeTo(null);
    }
}
