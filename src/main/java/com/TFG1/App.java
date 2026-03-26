package com.TFG1;

import com.TFG1.controller.AuthController;
import com.TFG1.controller.ShopController;
import com.TFG1.repository.HibernateUtil;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import io.javalin.Javalin;

public class App extends SimpleApplication {

    public static void main(String[] args) {

        System.out.println("--- INICIANDO SISTEMAS DEL TFG ---");

        try {
            HibernateUtil.getSessionFactory();
            System.out.println("Base de Datos conectada");
        } catch (Exception e) {
            System.err.println("ERROR: No se pudo conectar a PostgreSQL");
            e.printStackTrace();
            return; // Si no hay base de datos, no arrancamos nada
        }

        // Levantar el api
        Javalin api = Javalin.create(config -> {
            config.showJavalinBanner = false;
        }).start(8080);

        // Endpoints
        AuthController.registerRoutes(api);
        ShopController.registerRoutes(api);
        System.out.println("API REST lista en http://localhost:8080");

        // Motor grafico
        App app = new App();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("GAMEGAME");
        settings.setResolution(1024, 768);
        settings.setVSync(true);

        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Configuramos la escena 3D inicial
        viewPort.setBackgroundColor(ColorRGBA.DarkGray);

        // El cubo azul de prueba pa ver que funciona el jmonkey
        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("CuboPrueba", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);

        rootNode.attachChild(geom);

        System.out.println("Ventana de jMonkeyEngine lista.");
    }
}