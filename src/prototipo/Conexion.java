/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package prototipo;

/**
 *
 * @author jedisson.tapias
 */
public class Conexion {
    Agente origen;
    Agente destino;
    Capa capa;
    Boolean esActiva=false;
    
    public Conexion(Agente o, Agente d, Capa c, Boolean flag){
        origen= o;
        destino=d;
        capa=c;
        esActiva=flag;
    }

    public Agente getOrigen() {
        return origen;
    }

    public Agente getDestino() {
        return destino;
    }

    public Capa getCapa() {
        return capa;
    }

    public Boolean getEsActiva() {
        return esActiva;
    }

    public void setOrigen(Agente origen) {
        this.origen = origen;
    }

    public void setDestino(Agente destino) {
        this.destino = destino;
    }

    public void setCapa(Capa capa) {
        this.capa = capa;
    }

    public void setEsActiva(Boolean esActiva) {
        this.esActiva = esActiva;
    }
    
}
