package com.TFG1;

import com.TFG1.controller.AuthController;
import com.TFG1.controller.ShopController;
import com.TFG1.controller.RoomController;
import com.TFG1.controller.GameWebSocketController;
import com.TFG1.service.RoomService;
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
            HibernateUtil.getSessionFactory();
            System.out.println("Base de Datos conectada");

        } catch (Exception e) {
            System.err.println("ERROR: No se pudo conectar a PostgreSQL");
            e.printStackTrace();

        }

        Javalin api = Javalin.create(config -> {
            config.showJavalinBanner = false;
        }).start(7071);

        api.before(ctx -> {
            String path = ctx.path();

            if (path.startsWith("/api/login") || path.startsWith("/api/register")) {
                return;
            }

            String authHeader = ctx.header("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                ctx.status(401).json("{ \"error\": \"No autorizado - Falta el Token Bearer en la cabecera\" }");
                throw new io.javalin.http.UnauthorizedResponse();
            }

            String token = authHeader.substring(7);
            try {
                String username = com.TFG1.service.JwtService.validateToken(token);
                ctx.attribute("username", username);
            } catch (Exception e) {
                ctx.status(401).json(
                        "{ \"error\": \"No autorizado - Token inválido, caducado o modificado maliciosamente\" }");
                throw new io.javalin.http.UnauthorizedResponse();
            }
        });

        UserRepository userRepository = new UserRepository();
        CardRepository cardRepository = new CardRepository();
        CardRegistry cardRegistry = new CardRegistry();
        ShopService shopService = new ShopService(cardRegistry);
        ShopController shopController = new ShopController(shopService, userRepository, cardRepository);

        com.TFG1.repository.UserStatsRepository userStatsRepository = new com.TFG1.repository.UserStatsRepository();

        RoomService roomService = new RoomService(userStatsRepository);

        RoomController roomController = new RoomController(roomService, cardRegistry);

        AuthController.registerRoutes(api);
        shopController.registerRoutes(api);

        roomController.registerRoutes(api);

        GameWebSocketController.registerRoutes(api, roomService, cardRegistry);

        System.out.println("API REST lista en http://localhost:7071");

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

        viewPort.setBackgroundColor(ColorRGBA.DarkGray);

        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("CuboPrueba", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);

        rootNode.attachChild(geom);

        System.out.println("Ventana de jMonkeyEngine lista.");
    }
}