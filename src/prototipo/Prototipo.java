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

    static Agente[] agentes;
    static Capa[] capas;
    
    public static void main(String[] args) {
        generarPrototipo();
        mostrarPrototipo();
    }
    
    public static void generarPrototipo(){
        int cantidadDeAgentes=5;
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
            
                for(int b=0;b<cantidadDeAgentes-1;b++){//Por cada agente
                    for(int c=0;c<cantidadDeAgentes-1-b;c++){//Por cada conexión
                        Conexion con = new Conexion(agentes[b],agentes[b+c+1],capas[a],false);
                        capas[a].setConexionesEnOrden(con);
                        if(!capas[a].getEsBidireccional()){
                            con = new Conexion(agentes[b+c+1],agentes[b],capas[a],false);
                            capas[a].setConexionesEnOrden(con);
                        }
                        
                        
                        
                    }
                }
            
        }
    }

    private static void mostrarPrototipo() {
        for(int a=0;a<capas.length;a++){
            System.out.print("\n>> Capa "+capas[a].getNombre()+" --------------");
            for(int b=0;b<capas[a].getConexiones().size();b++){
                Conexion c=(Conexion)capas[a].getConexionesI(b);
                System.out.print("\n "+c.getOrigen().getNombre()+"\t"+c.getDestino().getNombre()+"\t"+c.getCapa().getNombre()+"\t"+c.getEsActiva());
            }
        }
    }
    
}
