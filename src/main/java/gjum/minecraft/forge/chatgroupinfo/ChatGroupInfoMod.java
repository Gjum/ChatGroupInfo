package gjum.minecraft.forge.chatgroupinfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.client.gui.AccessHelper.getInputField;

@net.minecraftforge.fml.common.Mod(modid = ChatGroupInfoMod.MODID, name = ChatGroupInfoMod.MODNAME, version = ChatGroupInfoMod.MODVERSION, guiFactory = "com.gmail.nuclearcat1337.snitch_master.gui.ConfigGuiFactory")
public class ChatGroupInfoMod
{
    public static final String MODID = "chatgroupinfo";
    public static final String MODNAME = "Chat Group Info";
    public static final String MODVERSION = "0.1.4";

    private static final Pattern chatGroupPattern = Pattern.compile("You are now chatting in group ([^ ]*)\\.");
    private static final Pattern privateMessagePattern = Pattern.compile("You are now chatting with ([^ ]*)\\.");

    public static final Logger logger = LogManager.getLogger(MODID);

    public static int color = 0xff888888;

    @net.minecraftforge.fml.common.Mod.Instance(MODID)
    public static ChatGroupInfoMod instance;

    private String activeGroupChat;
    private String activePrivateChat;
    private boolean initialized = false;
    private boolean enabled = true;

    @net.minecraftforge.fml.common.Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        activeGroupChat = null;
        activePrivateChat = null;
        enabled = true;

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String chatMsg = TextFormatting.getTextWithoutFormattingCodes(event.getMessage().getUnformattedText());
        Matcher matcher;
        boolean foundMatch = true; // gets set to false if no match
        if ("You are now in global chat.".equals(chatMsg)) {
            activeGroupChat = null;
        } else if ("You left private chat.".equals(chatMsg)) {
            activePrivateChat = null;
        } else if ("You aren't in private chat.".equals(chatMsg)) {
            activePrivateChat = null;
        } else if ("The player you were chatting with has gone offline, you have been moved to regular chat".equals(chatMsg)) {
            activePrivateChat = null;
        } else if ((matcher = chatGroupPattern.matcher(chatMsg)).matches()) {
            if (activeGroupChat == null)
                activePrivateChat = null; // changing from global to a group chat leaves private chat
            activeGroupChat = String.format("[%s]", matcher.group(1));
        } else if ((matcher = privateMessagePattern.matcher(chatMsg)).matches()) {
            activePrivateChat = String.format("<%s>", matcher.group(1));
        } else {
            foundMatch = false;
        }

        if (foundMatch) initialized = true;
    }

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent event) {
        if (!enabled || !initialized) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof GuiChat)) return;

        GuiTextField inputField = getInputField((GuiChat) mc.currentScreen);
        if (!inputField.getText().isEmpty()) return;

        float x = inputField.x + 2 * mc.fontRenderer.getCharWidth('_');
        float y = inputField.y;

        String text = "(global)";
        if (activeGroupChat != null)
            text = activeGroupChat;
        if (activePrivateChat != null)
            text = activePrivateChat;

        mc.fontRenderer.drawStringWithShadow(text, x, y, color);
    }
}
