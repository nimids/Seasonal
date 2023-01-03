package com.yurisuika.seasonal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.yurisuika.seasonal.commands.SeasonCommand;
import com.yurisuika.seasonal.utils.ModConfig;
import com.yurisuika.seasonal.utils.Season;
import com.yurisuika.seasonal.utils.WeatherCache;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Set;

public class Seasonal implements ModInitializer {

    public static final String MOD_ID = "seasonal";
    public static final Logger LOGGER = LogManager.getLogger("Seasonal");

    public static ModConfig CONFIG;

    public static final JsonParser JSON_PARSER = new JsonParser();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Block ORIGINAL_ICE;
    public static Block ORIGINAL_SNOW;

    public static HashMap<Item, Block> SEEDS_MAP = new HashMap<>();


    public static Identifier ASK_FOR_CONFIG = new Identifier(MOD_ID, "ask_for_config");
    public static Identifier ANSWER_CONFIG = new Identifier(MOD_ID, "answer_config");

    @Override
    public void onInitialize() {

        Path configPath = FabricLoader.getInstance().getConfigDir();
        File configFile = new File(configPath + File.separator + "seasonal.json");

        LOGGER.info("Trying to read config file...");
        try {
            if (configFile.createNewFile()) {
                LOGGER.info("No config file found, creating a new one...");
                String json = GSON.toJson(JSON_PARSER.parse(GSON.toJson(new ModConfig())));
                try (PrintWriter out = new PrintWriter(configFile)) {
                    out.println(json);
                }
                CONFIG = new ModConfig();
                LOGGER.info("Successfully created default config file.");
            } else {
                LOGGER.info("A config file was found, loading it..");
                CONFIG = GSON.fromJson(new String(Files.readAllBytes(configFile.toPath())), ModConfig.class);
                if(CONFIG == null) {
                    throw new NullPointerException("The config file was empty.");
                }else{
                    LOGGER.info("Successfully loaded config file.");
                }
            }
        }catch (Exception exception) {
            LOGGER.error("There was an error creating/loading the config file!", exception);
            CONFIG = new ModConfig();
            LOGGER.warn("Defaulting to original config.");
        }

        //net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> SeasonCommand.register(dispatcher));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            if(environment == CommandManager.RegistrationEnvironment.ALL)SeasonCommand.register(dispatcher);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            SEEDS_MAP.clear();
            Registries.ITEM.forEach(item -> {
                if(item instanceof BlockItem) {
                    Block block = ((BlockItem) item).getBlock();
                    if(block instanceof CropBlock || block instanceof StemBlock || block instanceof CocoaBlock || block instanceof SaplingBlock) {
                        Seasonal.SEEDS_MAP.put(item, ((BlockItem) item).getBlock());
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(ASK_FOR_CONFIG, (server, player, handler, buf, responseSender) -> {
            String configJson = GSON.toJson(JSON_PARSER.parse(GSON.toJson(CONFIG)));
            PacketByteBuf configBuf = PacketByteBufs.create();
            configBuf.writeInt(configJson.length());
            configBuf.writeString(configJson, configJson.length());
            ServerPlayNetworking.send(player, ANSWER_CONFIG, configBuf);
        });
    }

    public static Season getCurrentSeason(World world) {
        RegistryKey<World> dimension = world.getRegistryKey();
        if (CONFIG.isValidInDimension(dimension)) {
            if(CONFIG.isSeasonLocked()) {
                return CONFIG.getLockedSeason();
            }
            if(CONFIG.isSeasonTiedWithSystemTime()) {
                return getCurrentSystemSeason();
            }
            int worldTime = Math.toIntExact(world.getTimeOfDay());
            int seasonTime = (worldTime / CONFIG.getSeasonLength());
            return Season.values()[seasonTime % 12];
        }
        return Season.EARLY_SPRING;
    }

    @Environment(EnvType.CLIENT)
    public static Season getCurrentSeason() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if(player != null && player.world != null) {
            return getCurrentSeason(player.world);
        }
        return Season.EARLY_SPRING;
    }

    private static Season getCurrentSystemSeason() {
        LocalDateTime date = LocalDateTime.now();
        int m = date.getMonthValue();
        int d = date.getDayOfMonth();
        Season season;

        if (CONFIG.isInNorthHemisphere()) {
            if (m == 1)
                season = Season.EARLY_WINTER;
            else if (m == 2)
                season = Season.MID_WINTER;
            else if (m == 3)
                season = Season.LATE_WINTER;
            else if (m == 4)
                season = Season.EARLY_SPRING;
            else if (m == 5)
                season = Season.MID_SPRING;
            else if (m == 6)
                season = Season.LATE_SPRING;
            else if (m == 7)
                season = Season.EARLY_SUMMER;
            else if (m == 8)
                season = Season.MID_SUMMER;
            else if (m == 9)
                season = Season.LATE_SUMMER;
            else if (m == 10)
                season = Season.EARLY_AUTUMN;
            else if (m == 11)
                season = Season.MID_AUTUMN;
            else
                season = Season.LATE_AUTUMN;

            if (m == 3 && d > 19)
                season = Season.EARLY_SPRING;
            else if (m == 6 && d > 20)
                season = Season.EARLY_SUMMER;
            else if (m == 9 && d > 21)
                season = Season.EARLY_AUTUMN;
            else if (m == 12 && d > 20)
                season = Season.EARLY_WINTER;
        } else {
            if (m == 1)
                season = Season.EARLY_SUMMER;
            else if (m == 2)
                season = Season.MID_SUMMER;
            else if (m == 3)
                season = Season.LATE_SUMMER;
            else if (m == 4)
                season = Season.EARLY_AUTUMN;
            else if (m == 5)
                season = Season.MID_AUTUMN;
            else if (m == 6)
                season = Season.LATE_AUTUMN;
            else if (m == 7)
                season = Season.EARLY_WINTER;
            else if (m == 8)
                season = Season.MID_WINTER;
            else if (m == 9)
                season = Season.LATE_WINTER;
            else if (m == 10)
                season = Season.EARLY_SPRING;
            else if (m == 11)
                season = Season.MID_SPRING;
            else
                season = Season.LATE_SPRING;

            if (m == 3 && d > 19)
                season = Season.EARLY_AUTUMN;
            else if (m == 6 && d > 20)
                season = Season.EARLY_WINTER;
            else if (m == 9 && d > 21)
                season = Season.EARLY_SPRING;
            else if (m == 12 && d > 20)
                season = Season.EARLY_SUMMER;
        }

        return season;
    }

    public static void injectBiomeTemperature(RegistryEntry<Biome> biome, World world) {
        if(!CONFIG.doTemperatureChanges()) return;

        Set<TagKey<Biome>> ignoredCategories = Set.of(BiomeTags.IS_NETHER, BiomeTags.IS_END, BiomeTags.IS_OCEAN);
        if(ignoredCategories.stream().anyMatch(biome::isIn)) return;

        Season season = Seasonal.getCurrentSeason(world);

        Identifier biomeIdentifier = world.getRegistryManager().get(RegistryKeys.BIOME).getId(biome.value());
        Biome.Weather currentWeather = biome.value().weather;

        Biome.Weather originalWeather;
        if (!WeatherCache.hasCache(biomeIdentifier)) {
            originalWeather = new Biome.Weather(currentWeather.precipitation, currentWeather.temperature, currentWeather.temperatureModifier, currentWeather.downfall);
            WeatherCache.setCache(biomeIdentifier, originalWeather);
        }else{
            originalWeather = WeatherCache.getCache(biomeIdentifier);
        }

        if(originalWeather == null) {
            return;
        }
        float temp = originalWeather.temperature;
        Biome.Precipitation precipitation = originalWeather.precipitation;

        if(biome.isIn(BiomeTags.IS_JUNGLE) || biome.isIn(BiomeTags.HAS_CLOSER_WATER_FOG)) {
            //Jungle Biomes
            currentWeather.precipitation = switch (season) {
                case EARLY_WINTER, MID_WINTER, LATE_WINTER -> precipitation;
                default -> precipitation;
            };
            currentWeather.temperature = switch (season) {
                case EARLY_WINTER, MID_WINTER, LATE_WINTER -> temp - 0.1f;
                default -> temp;
            };
        }else if(biome.isIn(BiomeTags.IS_BADLANDS)) {
            //Badlands Biomes
            currentWeather.precipitation = switch (season) {
                case EARLY_SPRING, MID_SPRING, LATE_SPRING -> Biome.Precipitation.RAIN;
                case EARLY_SUMMER, MID_SUMMER, LATE_SUMMER -> precipitation;
                case EARLY_WINTER, MID_WINTER, LATE_WINTER -> Biome.Precipitation.SNOW;
                default -> precipitation;
            };
            currentWeather.temperature = switch (season) {
                case EARLY_SPRING, MID_SPRING, LATE_SPRING -> temp;
                case EARLY_SUMMER, MID_SUMMER, LATE_SUMMER -> temp + 0.2f;
                case EARLY_WINTER, MID_WINTER, LATE_WINTER -> temp - 2.0f;
                default -> temp;
            };
        }
        else if(temp <= 0.1) {
            //Frozen Biomes
            currentWeather.precipitation = switch (season) {
                case EARLY_SUMMER, MID_SUMMER, LATE_SUMMER -> Biome.Precipitation.RAIN;
                case EARLY_WINTER, MID_WINTER, LATE_WINTER -> Biome.Precipitation.SNOW;
                default -> precipitation;
            };
            currentWeather.temperature = switch (season) {
                case EARLY_SUMMER, MID_SUMMER, LATE_SUMMER -> temp + 0.3f;
                case EARLY_WINTER, MID_WINTER, LATE_WINTER -> temp - 0.2f;
                default -> temp;
            };
        }
        else if(temp <= 0.3) {
            //Cold Biomes
            currentWeather.precipitation = switch (season) {
                case EARLY_SPRING, MID_SPRING, LATE_SPRING -> Biome.Precipitation.RAIN;
                case EARLY_SUMMER, MID_SUMMER, LATE_SUMMER -> Biome.Precipitation.RAIN;
                case EARLY_WINTER, MID_WINTER, LATE_WINTER -> Biome.Precipitation.SNOW;
                default -> precipitation;
            };
            currentWeather.temperature = switch (season) {
                case EARLY_SPRING, MID_SPRING, LATE_SPRING -> temp;
                case EARLY_SUMMER, MID_SUMMER, LATE_SUMMER -> temp + 0.2f;
                case EARLY_WINTER, MID_WINTER, LATE_WINTER -> temp - 0.2f;
                default -> temp;
            };
        }
        else if(temp <= 0.95) {
            //Temperate Biomes
            currentWeather.precipitation = switch (season) {
                case EARLY_SUMMER, MID_SUMMER, LATE_SUMMER -> precipitation;
                case EARLY_AUTUMN, MID_AUTUMN, LATE_AUTUMN -> precipitation;
                case EARLY_WINTER, MID_WINTER, LATE_WINTER -> Biome.Precipitation.SNOW;
                default -> precipitation;
            };
            currentWeather.temperature = switch (season) {
                case EARLY_SUMMER, MID_SUMMER, LATE_SUMMER -> temp + 0.2f;
                case EARLY_AUTUMN, MID_AUTUMN, LATE_AUTUMN -> temp - 0.1f;
                case EARLY_WINTER, MID_WINTER, LATE_WINTER -> temp - 0.7f;
                default -> temp;
            };
        } else {
            //Hot Biomes
            currentWeather.precipitation = switch (season) {
                case EARLY_SUMMER, MID_SUMMER, LATE_SUMMER -> precipitation;
                case EARLY_WINTER, MID_WINTER, LATE_WINTER -> Biome.Precipitation.RAIN;
                default -> precipitation;
            };
            currentWeather.temperature = switch (season) {
                case EARLY_SUMMER, MID_SUMMER, LATE_SUMMER -> temp + 0.2f;
                case EARLY_WINTER, MID_WINTER, LATE_WINTER -> temp - 0.2f;
                default -> temp;
            };
        }
    }

}
