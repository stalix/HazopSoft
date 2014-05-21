/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package herramientas;

import javax.swing.JTable;

/**
 *
 * @author Alvaro Monsalve
 */
public class funciones {
    
    public static void setOcultarColumnas(JTable tbl, int columna[]) {
        for (int i = 0; i < columna.length; i++) {
            tbl.getColumnModel().getColumn(columna[i]).setMaxWidth(0);
            tbl.getColumnModel().getColumn(columna[i]).setMinWidth(0);
            tbl.getTableHeader().getColumnModel().getColumn(columna[i]).setMaxWidth(0);
            tbl.getTableHeader().getColumnModel().getColumn(columna[i]).setMinWidth(0);
        }
    }
    
    public static void setSizeColumnas(JTable tbl,int columna[], int sizeColumn[]){
        for(int i=0;i<columna.length;i++){
            tbl.getColumnModel().getColumn(columna[i]).setMinWidth(sizeColumn[i]);
            tbl.getTableHeader().getColumnModel().getColumn(columna[i]).setMaxWidth(sizeColumn[i]);
        }
    }

}
