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
public class Prototipo {

    Agente[] agentes;
    Capa[] capas;
    
    public static void main(String[] args) {
        
    }
    
    public void generarPrototipo(){
        int cantidadDeAgentes=10;
        int cantidadDeCapas=3;
        //----------------------------------------------------
        //Agentes
        agentes= new Agente[cantidadDeAgentes];
        for(int a=0;a<cantidadDeAgentes;a++){
            agentes[a]= new Agente(Integer.toString(a+1));
            //System.out.println("\n"+agentes[a].getNombre());
        }
        //----------------------------------------------------
        //Capas
        capas = new Capa[cantidadDeCapas];
        for(int a=0;a<cantidadDeCapas;a++){
            capas[a]= new Capa();
            if(a==0){
                capas[a].setNombre("Familia");
                capas[a].setEsBidireccional(true);
            }
            else if(a==1){
                capas[a].setNombre("Trabajo");
                capas[a].setEsBidireccional(true);
            }
            else{
                capas[a].setNombre("Amigos");
                capas[a].setEsBidireccional(false);
            }
        }
        //-------------------------------------------------
        //Generar red completa
        //Generar conexiones inactivas
        for(int a=0;a<cantidadDeCapas;a++){//Por cada capa
            if(capas[a].getEsBidireccional()){
                for(int b=0;b<cantidadDeAgentes-1;b++){//Por cada agente
                    for(int c=0;c<cantidadDeAgentes-1-b;c++){//Por cada conexión
                        Conexion con = new Conexion(agentes[b],agentes[c+1],capas[a],true);
                    }
                }
            }
            else{
                
            }
        }
    }
    
}
