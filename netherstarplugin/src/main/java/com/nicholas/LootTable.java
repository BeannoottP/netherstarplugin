package com.nicholas;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.yaml.snakeyaml.Yaml;

public class LootTable {
    int tiers;
    ArrayList<HashMap<LootDrop, Integer>> dropTable;

    public LootTable(int t) {
        tiers = t;
        dropTable = new ArrayList<>();
        for (int i = 0; i < t; i++) {
            dropTable.add(new HashMap<>());
        }
    }

    @SuppressWarnings("unchecked")
    public void loadFromFile() {
    try (InputStream in = getClass().getClassLoader().getResourceAsStream("loottable.yml")) {
        if (in == null) {
            throw new IllegalStateException("Could not find loottable.yml in resources!");
        }
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(in);

        for (Object tierKey : data.keySet()) {
            Map<String, Object> tierData = (Map<String, Object>) data.get(tierKey);
            int tierIndex = (int) tierData.get("tier");

            Map<String, Object> drops = (Map<String, Object>) tierData.get("drops");
            for (Object dropKey : drops.keySet()) {
                Map<String, Object> dropData = (Map<String, Object>) drops.get(dropKey);

                // Parse material
                String matStr = (String) dropData.get("Material");
                Material mat = Material.valueOf(matStr.replace("Material.", ""));

                int low = (int) dropData.get("lowdrop");
                int high = (int) dropData.get("highdrop");
                int rarity = (int) dropData.get("rarity");

                ItemStack item = new ItemStack(mat);

                // Handle enchantments if meta present
                Map<String, Object> meta = (Map<String, Object>) dropData.get("meta");
                if (meta != null && meta.containsKey("enchant")) {
                    Map<String, Object> enchantData = (Map<String, Object>) meta.get("enchant");

                    String enchantName = ((String) enchantData.get("Enchantment"))
                                            .replace("enchantment.", "").toUpperCase();
                    int level = (int) enchantData.get("Level");

                    Enchantment enchant = Enchantment.getByName(enchantName);
                    if (enchant != null) {
                        item.addUnsafeEnchantment(enchant, level);
                    }
                }
                if (meta != null && meta.containsKey("enchant_book")) {
                    Map<String, Object> enchantData = (Map<String, Object>) meta.get("enchant");

                    String enchantName = ((String) enchantData.get("Enchantment"))
                                            .replace("enchantment.", "").toUpperCase();
                    int level = (int) enchantData.get("Level");

                    Enchantment enchant = Enchantment.getByName(enchantName);

                    EnchantmentStorageMeta esm = (EnchantmentStorageMeta) item.getItemMeta();
                    esm.addStoredEnchant(enchant, level, true);
                    item.setItemMeta(esm);
                }

                LootDrop drop = new LootDrop(item, low, high);
                addDrop(drop, rarity, tierIndex);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}


    public void addDrop(LootDrop drop, int rarity, int tier) {
        dropTable.get(tier).put(drop, rarity);
    }

    public ItemStack getItemDrop(int tier) {
        ArrayList<LootDrop> drops = new ArrayList<>();
        for (LootDrop drop : dropTable.get(tier).keySet()) {
            for (int i = 0; i < dropTable.get(tier).get(drop); i++) {
                drops.add(drop);
            }
        }

        LootDrop drop = drops.get((int) (Math.random() * drops.size()));
        ItemStack finalDrop = new ItemStack(drop.item);
        finalDrop.setAmount(drop.getNumber());

        return finalDrop;
    }

    public class LootDrop {
        ItemStack item;
        int lowestDrop;
        int highestDrop;

        public LootDrop(ItemStack i, int low, int high) {
            item = i;
            lowestDrop = low;
            highestDrop = high;
        }

        public int getNumber() {
            return ((int) (Math.random() * (highestDrop - lowestDrop))) + lowestDrop;
        }
    }

}
