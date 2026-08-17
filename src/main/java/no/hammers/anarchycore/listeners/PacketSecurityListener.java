package no.hammers.anarchycore.listeners;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeclareCommands;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PacketSecurityListener extends PacketListenerAbstract {

    private final Set<String> blockedCommands = Set.of(
            "plugins", "pl",
            "version", "ver", "about",
            "icanhasbukkit",
            "paper", "folia", "bukkit"
    );

    @Override
    public void onPacketSend(PacketSendEvent event) {
        // 1. Intercept Server Brand Packet (F3 Menu Display)
        if (event.getPacketType() == PacketType.Play.Server.PLUGIN_MESSAGE) {
            WrapperPlayServerPluginMessage packet = new WrapperPlayServerPluginMessage(event);
            String channel = packet.getChannelName();

            if ("minecraft:brand".equalsIgnoreCase(channel) || "MC|Brand".equalsIgnoreCase(channel)) {
                packet.setData(createBrandPayload("2b2t.no"));
            }
            return;
        }

        // 2. Intercept the Brigadier Command Tree packet sent to the client upon joining or typing commands
        if (event.getPacketType() == PacketType.Play.Server.DECLARE_COMMANDS) {
            Player player = (Player) event.getPlayer();

            // Allow Admins/OPs to see all commands in autocompletion
            if (player != null && player.hasPermission("anarchycore.admin")) {
                return;
            }

            WrapperPlayServerDeclareCommands packet = new WrapperPlayServerDeclareCommands(event);
            var nodes = packet.getNodes();

            if (nodes == null || nodes.isEmpty()) return;

            var rootNode = nodes.get(packet.getRootIndex());
            List<Integer> originalChildren = rootNode.getChildren();

            if (originalChildren == null) return;

            // Strip out blocked commands and plugin namespace prefixes (e.g., "bukkit:ver")
            List<Integer> filteredChildren = originalChildren.stream()
                    .filter(childIndex -> {
                        if (childIndex == null || childIndex < 0 || childIndex >= nodes.size()) return false;
                        var child = nodes.get(childIndex);

                        var nameOpt = child.getName();
                        if (nameOpt == null || nameOpt.isEmpty()) return true;

                        String cleanName = nameOpt.get().toLowerCase();
                        return !cleanName.contains(":") && !blockedCommands.contains(cleanName);
                    })
                    .collect(Collectors.toList());

            // Overwrite the root node's child command list before packet goes over the wire
            rootNode.setChildren(filteredChildren);
        }
    }

    /**
     * Constructs a VarInt-prefixed UTF-8 byte payload for the custom server brand string.
     */
    private byte[] createBrandPayload(String brand) {
        byte[] brandBytes = brand.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Write VarInt string length
        int length = brandBytes.length;
        while ((length & -128) != 0) {
            out.write(length & 127 | 128);
            length >>>= 7;
        }
        out.write(length);

        // Write String bytes
        out.writeBytes(brandBytes);
        return out.toByteArray();
    }
}