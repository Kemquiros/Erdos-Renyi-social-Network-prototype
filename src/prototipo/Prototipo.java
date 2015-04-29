/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package prototipo;

import java.awt.Toolkit;
import java.math.BigInteger;

/**
 *
 * @author jedisson.tapias
 */
public class Prototipo {

    static Agente[] agentes;
    static Capa[] capas;
    static double probabilidad;
    
    public static void main(String[] args) {
        generarPrototipo();
        mostrarPrototipo();
        generarParametroErdos(0.9,8,100);
        generarRed();
        mostrarPrototipo();
    }
    
    public static void generarPrototipo(){
        int cantidadDeAgentes=100;
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
    
    private static long factorial(long x){
        
        if(x<1){
            return 1;
        }
        else{
            return x*factorial(x-1);
        }
                
        
    }
    private static long combinatoria(long x,int y){
        return (x*(x-1))/y;
    }
    
    private static void generarParametroErdos(double p,int m,int n){
        //Erdos-Renyi Network
        //Statiscal Random Network
        //p: parameter  m: edged n: nodes
        //BigInteger n= BigInteger.valueOf(_n);
        //double combinatoria= factorial(n)/(factorial()*factorial(BigInteger.valueOf(2)));
        //System.out.print("\n\n>>factorial(+"+n+"): "+factorial(n)+"\n");
        double m1=combinatoria(n,2)*p;
        System.out.print("\n\n>>Aristas: "+m1+"\n");
        System.out.print("\n\n>>Combinatoria: "+combinatoria(n,2)+"\n");
        System.out.print("\n\n>>Combinatoria - Aristas: "+ (combinatoria(n,2) - m1 )+"\n");
        System.out.print("\n\n>>pow(1-"+p+")^Combinatoria - Aristas: "+ (Math.pow(1-p, (combinatoria(n,2)- m1 )))+"\n");
        probabilidad = Math.pow(p,  m1)*(Math.pow(1- p, (combinatoria(n,2)- m1 )));      
        System.out.print("\n\n>>Probabilidad: "+probabilidad+"\n\n");
        probabilidad=0.5;
        
        
    }

    private static void generarRed() {
        Toolkit tk = Toolkit.getDefaultToolkit (); 
        for(int a=0;a< capas.length;a++){
            for(int b=0;b<capas[a].getConexiones().size();b++){
                Conexion c=(Conexion)capas[a].getConexionesI(b);
                if(Math.random()<=probabilidad){
                    c.setEsActiva(true);
                    //tk.beep();
                }
                
            }
        }
    }
    
}
