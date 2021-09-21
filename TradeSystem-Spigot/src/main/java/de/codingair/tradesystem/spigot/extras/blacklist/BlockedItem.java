package de.codingair.tradesystem.spigot.extras.blacklist;

import de.codingair.codingapi.server.specification.Version;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.tradesystem.spigot.utils.ShulkerBoxHelper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Objects;

public class BlockedItem {
    private final @Nullable Material material;
    private final byte data;
    private final @Nullable String name;
    private final String lore;

    public BlockedItem(@Nullable Material material, byte data, @Nullable String name, String lore) {
        this.material = material;
        this.data = data;
        this.name = name;
        this.lore=lore;
    }

    public BlockedItem(@Nullable Material material, byte data) {
        this(material, data, null,null);
    }

    public BlockedItem(@Nullable String name) {
        this(null, (byte) 0, name,null);
    }

    public static BlockedItem fromString(String s) {
        try {
            JSONObject json = (JSONObject) new JSONParser().parse(s);

            Material material = json.get("Material") == null ? null : Material.valueOf((String) json.get("Material"));
            byte data = material == null ? 0 : Byte.parseByte(json.get("Data") + "");
            String name = json.get("Displayname") == null ? null : (String) json.get("Displayname");
            String lore = json.get("Lore") == null ? null : (String) json.get("Lore");

            return new BlockedItem(material, data, name, lore);
        } catch (NoSuchFieldError | IllegalArgumentException ex) {
            return null;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean matches(@NotNull ItemStack item) {
        boolean matches = false;

        if (material != null) {
            if (Version.get().isBiggerThan(Version.v1_12)) {
                if (item.getType() == this.material) matches = true;
            } else {
                //noinspection deprecation, ConstantConditions
                if (item.getType() == this.material && data == item.getData().getData()) matches = true;
            }
        }

        if (name != null && item.hasItemMeta() && item.getItemMeta() != null) {
            String displayName = item.getItemMeta().getDisplayName();
            if (displayName.equals(getStrippedName())) matches = true;
        }

        if (Version.atLeast(11) && !matches) {
            for (ItemStack itemStack : ShulkerBoxHelper.getItems(item)) {
                if (matches(itemStack)) return true;
            }
        }
        if (lore != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            if (item.getItemMeta().getLore().toString().contains(lore)) matches=false;
        }

        return matches;
    }

    @Nullable
    private String getStrippedName() {
        if (name == null) return null;
        return ChatColor.translateAlternateColorCodes('&', name);
    }

    @Nullable
    public Material getMaterial() {
        return material;
    }

    public byte getData() {
        return data;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();

        if (this.material != null) {
            json.put("Material", this.material.name());
            json.put("Data", this.data);
        }

        if (this.name != null) {
            json.put("Displayname", this.name);
        }

        return json.toJSONString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockedItem that = (BlockedItem) o;
        return data == that.data &&
                material == that.material &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        int material = this.material == null ? 0 : this.material.ordinal();
        return Objects.hash(material, data, name + "");
    }
}
