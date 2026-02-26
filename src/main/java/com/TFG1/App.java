package com.TFG1;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;

public class App extends SimpleApplication {

    public static void main(String[] args) {
        App app = new App();
        
        // Configuraciones básicas para la ventana gráfica
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Mi Primera App jMonkeyEngine");
        settings.setResolution(1024, 768);
        settings.setVSync(true);
        app.setSettings(settings);
        
        app.setShowSettings(false); // Ocultar el diálogo inicial de configuración
        app.start(); // Iniciar el motor
    }

    @Override
    public void simpleInitApp() {
        // 1. Crear la forma (malla/mesh) - Un cubo 1x1x1 puro
        Box b = new Box(1, 1, 1);
        
        // 2. Crear una geometría basándose en esa malla
        Geometry geom = new Geometry("Box", b);
        
        // 3. Crear el material que determinará cómo se ve la superficie (color)
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue); // Hacer el cubo azul
        geom.setMaterial(mat);
        
        // 4. Conectar la geometría al nodo raíz para que el motor la renderice en pantalla
        rootNode.attachChild(geom);
    }
}
