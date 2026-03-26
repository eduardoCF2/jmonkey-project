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
import com.TFG1.core.cards.CardRegistry;
import com.TFG1.repository.CardRepository;
import com.TFG1.repository.UserRepository;
import com.TFG1.service.ShopService;
import io.javalin.Javalin;

public class App extends SimpleApplication {

    public static void main(String[] args) {

        System.out.println("--- INICIANDO SISTEMAS DEL TFG ---");

        try {
            // HibernateUtil.getSessionFactory();
            // System.out.println("Base de Datos conectada");
            System.out.println("Base de Datos temporalmente DESACTIVADA para poder probar la tienda.");
        } catch (Exception e) {
            System.err.println("ERROR: No se pudo conectar a PostgreSQL");
            e.printStackTrace();
            // return; // Si no hay base de datos, no arrancamos nada
        }

        // Levantar el api
        Javalin api = Javalin.create(config -> {
            config.showJavalinBanner = false;
        }).start(7071);

        // Instanciar repositorios y servicios para inyectarlos en los controladores
        UserRepository userRepository = new UserRepository();
        CardRepository cardRepository = new CardRepository();
        CardRegistry cardRegistry = new CardRegistry();
        ShopService shopService = new ShopService(cardRegistry);
        ShopController shopController = new ShopController(shopService, userRepository, cardRepository);

        // Endpoints
        AuthController.registerRoutes(api);
        shopController.registerRoutes(api);
        System.out.println("API REST lista en http://localhost:7071");

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