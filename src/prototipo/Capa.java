/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package prototipo;

import java.util.ArrayList;

/**
 *
 * @author jedisson.tapias
 */
public class Capa {
    ArrayList conexiones= new ArrayList();
    Boolean esBidireccional;
    String nombre;

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    
    public ArrayList getConexiones() {
        return conexiones;
    }

    public String getNombre() {
        return nombre;
    }

    public void setConexiones(ArrayList conexiones) {
        this.conexiones = conexiones;
    }
    public void setConexionesEnOrden(Object o){
        conexiones.add(o);
    }
    
    public Object getConexionesI(int i) {
        return conexiones.get(i);
    }
    public void setConexionesI(int i,Object o) {
        conexiones.set(i, o);
    }

    public void setEsBidireccional(Boolean esBidireccional) {
        this.esBidireccional = esBidireccional;
    }

    public Boolean getEsBidireccional() {
        return esBidireccional;
    }
    
    
    
}
