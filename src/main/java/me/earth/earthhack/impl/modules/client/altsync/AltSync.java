package me.earth.earthhack.impl.modules.client.altsync;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.EnumSetting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.api.setting.settings.StringSetting;
import me.earth.earthhack.impl.core.ducks.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

import java.io.*;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class AltSync extends Module {

    protected final Setting<Mode> mode = register(new EnumSetting<>("Mode", Mode.Sender));
    protected final Setting<String> host = register(new StringSetting("Host", "localhost"));
    protected final Setting<Integer> updateDelay = register(new NumberSetting<>("updateDelay", 10, 0, 100));

    private Socket clientSocket;
    private PrintWriter out;
    private Thread serverThread;
    private final int port = 2555;

    protected HashMap<Integer, Boolean> keyStates = new HashMap<>();

    public AltSync() {
        super("Alt-Sync", Category.Dev);
            this.listeners.add(new ListenerKeyPoll(this));
            this.listeners.add(new ListenerMouse(this));
    }

    @Override
    protected void onEnable() {
        if (mode.getValue() == Mode.Sender) {
            try {
                clientSocket = new Socket(host.getValue(), port);
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                info("Connected to server at " + host.getValue() + ":" + port);
            } catch (IOException e) {
                error("Failed to connect to server: " + e.getMessage());
                disable();
            }
        } else if (mode.getValue() == Mode.Receiver) {
            serverThread = new Thread(() -> {
                try (ServerSocket serverSocket = new ServerSocket(port)) {
                    info("Server started on port " + port);
                    Socket socket = serverSocket.accept();
                    info("Client connected: " + socket.getInetAddress());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while (isEnabled()) {
                        String data = reader.readLine();
                        if (data != null) {
                            handleInput(data);
                        }
                        try {
                            Thread.sleep(updateDelay.getValue());
                        } catch (InterruptedException e) {//
                             }
                    }
                    reader.close();
                    socket.close();
                } catch (IOException e) {
                    error("Server encountered an error: " + e.getMessage());
                }
            });
            serverThread.start();
        }
    }

    @Override
    protected void onDisable() {
        if (clientSocket != null && !clientSocket.isClosed()) {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
        if (serverThread != null) {
            serverThread.interrupt();
            serverThread = null;
        }
    }

    public void sendKey(String data) {
        if (out != null) {
            out.println(data);
        }
    }

    private void handleInput(String data) {
        if (data.startsWith("mouse:")) {
            handleMouseInput(data);
        } else {
            handleKeyInput(data);
        }
    }

    private void handleKeyInput(String keyData) {
        try {
            String[] parts = keyData.split(":");
            if (parts.length == 2) {
                int keyID = Integer.parseInt(parts[0]);
                boolean state = Boolean.parseBoolean(parts[1]);


                if (state)
                {
                    if (keyID == 62 && mc.entityRenderer != null)
                    {
                        mc.entityRenderer.switchUseShader();
                    }

                    boolean flag1 = false;

                    if (mc.currentScreen == null)
                    {
                        if (keyID == 1)
                        {
                            mc.displayInGameMenu();
                        }

                    }

                    if (flag1)
                    {
                        KeyBinding.setKeyBindState(keyID, false);
                    }
                    else
                    {
                        KeyBinding.setKeyBindState(keyID, true);
                        KeyBinding.onTick(keyID);
                    }

                }
                else
                {
                    KeyBinding.setKeyBindState(keyID, false);

                }
                net.minecraftforge.fml.common.FMLCommonHandler.instance().fireKeyInput();

                ((IMinecraft) mc).invokeProcessKeyBinds();


                /*try {
                    Class<?> keyboardClass = Keyboard.class;
                    Field keyDownBufferField = keyboardClass.getDeclaredField("keyDownBuffer");
                    keyDownBufferField.setAccessible(true);

                    ByteBuffer keyDownBuffer = (ByteBuffer) keyDownBufferField.get(null);
                    keyDownBuffer.put(keyID, (byte) (state ? 1 : 0));

                    System.out.println("KEYBOARD WAS ALTERED FOR ID" + keyID + ", STATE: " + keyDownBuffer.get(keyID));

                } catch (Exception e) {
                    System.out.println("KB EDIT FAILED");

                    e.printStackTrace();
                }
                Keyboard.poll();*/

                //keyStates.put(keyID, state);
                //info("Updated key " + keyID + " state to " + state);
            } else {
                error("Invalid key data format: " + keyData);
            }
        } catch (Exception e) {
            error("Error processing key data: " + keyData + " - " + e.getMessage());
        }
    }

    private void handleMouseInput(String data) {
        try {
            String[] parts = data.split(":");
            if (parts.length == 3) {
                float yaw = Float.parseFloat(parts[1]);
                float pitch = Float.parseFloat(parts[2]);
                mc.player.rotationYaw = yaw;
                mc.player.rotationPitch = pitch;
            }
        } catch (Exception e) {
        }
    }

    private void info(String message) {
        System.out.println("[AltSync] " + message);
    }

    private void error(String message) {
        System.err.println("[AltSync] " + message);
    }

    public enum Mode {
        Sender,
        Receiver
    }

    public boolean isReceiver(){
        return mode.getValue() == Mode.Receiver && isEnabled();
    }

    public boolean getServerKey(int keyId, boolean original) {
        Boolean brain = keyStates.get(keyId);
        if(brain == null){
            info("Key " + keyId + " not found");
            return original;
        }
        info("Key " + keyId + " FOUNDDDDD");

        return brain.booleanValue();
    }
}
