package com.yurisuika.seasonal.mixin;

import com.yurisuika.seasonal.Seasonal;
import com.yurisuika.seasonal.colors.SeasonFoliageColors;
import com.yurisuika.seasonal.colors.SeasonGrassColors;
import com.yurisuika.seasonal.utils.ColorsCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.BiomeKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;
import java.util.Set;

@Mixin(Biome.class)
public class BiomeMixin {

    @SuppressWarnings("ConstantConditions")
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/BiomeEffects;getGrassColor()Ljava/util/Optional;"), method = "getGrassColorAt")
    public Optional<Integer> getSeasonGrassColor(BiomeEffects effects) {
        Biome biome = (Biome) ((Object) this);

        if(ColorsCache.hasGrassCache(biome)) {
            return ColorsCache.getGrassCache(biome);
        }else{
            Optional<Integer> returnColor = effects.getGrassColor();
            World world = MinecraftClient.getInstance().world;
            if(world != null) {
                Registry<Biome> biomes = world.getRegistryManager().get(RegistryKeys.BIOME);
                Identifier biomeIdentifier = biomes.getId(biome);
                Optional<Integer> seasonGrassColor;
                if(biomes.getEntry(biome).isIn(BiomeTags.IS_BADLANDS)){
                    seasonGrassColor = Optional.of(Seasonal.CONFIG.getMinecraftBadlandsGrass().getColor(Seasonal.getCurrentSeason()));
                }else{
                    seasonGrassColor = Seasonal.CONFIG.getSeasonGrassColor(biome, biomeIdentifier, Seasonal.getCurrentSeason());
                }
                if(seasonGrassColor.isPresent()) {
                    returnColor = seasonGrassColor;
                }
            }
            ColorsCache.createGrassCache(biome, returnColor);
            return returnColor;
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/BiomeEffects;getFoliageColor()Ljava/util/Optional;"), method = "getFoliageColor")
    public Optional<Integer> getSeasonFoliageColor(BiomeEffects effects) {
        Biome biome = (Biome) ((Object) this);

        if(ColorsCache.hasFoliageCache(biome)) {
            return ColorsCache.getFoliageCache(biome);
        }else{
            Optional<Integer> returnColor = effects.getFoliageColor();
            World world = MinecraftClient.getInstance().world;
            if(world != null){
                Registry<Biome> biomes = world.getRegistryManager().get(RegistryKeys.BIOME);
                Identifier biomeIdentifier = biomes.getId(biome);
                Optional<Integer> seasonFoliageColor;
                if(biomes.getEntry(biome).isIn(BiomeTags.IS_BADLANDS)){
                    seasonFoliageColor = Optional.of(Seasonal.CONFIG.getMinecraftBadlandsFoliage().getColor(Seasonal.getCurrentSeason()));
                }else{
                    seasonFoliageColor = Seasonal.CONFIG.getSeasonFoliageColor(biome, biomeIdentifier, Seasonal.getCurrentSeason());
                }
                if(seasonFoliageColor.isPresent()) {
                    returnColor = seasonFoliageColor;
                }
            }
            ColorsCache.createFoliageCache(biome, returnColor);
            return returnColor;
        }

    }

    @SuppressWarnings("removal")
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/BiomeEffects$GrassColorModifier;getModifiedGrassColor(DDI)I"), method = "getGrassColorAt")
    public int getSeasonModifiedGrassColor(BiomeEffects.GrassColorModifier gcm, double x, double z, int color) {
        if(gcm == BiomeEffects.GrassColorModifier.SWAMP) {
            int swampColor1 = Seasonal.CONFIG.getMinecraftSwampGrass1().getColor(Seasonal.getCurrentSeason());
            int swampColor2 = Seasonal.CONFIG.getMinecraftSwampGrass2().getColor(Seasonal.getCurrentSeason());

            double d = Biome.FOLIAGE_NOISE.sample(x * 0.0225D, z * 0.0225D, false);
            return d < -0.1D ? swampColor1 : swampColor2;
        }else{
            return gcm.getModifiedGrassColor(x, z, color);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/color/world/FoliageColors;getColor(DD)I"), method = "getDefaultFoliageColor")
    public int getSeasonDefaultFoliageColor(double d, double e) {
        return SeasonFoliageColors.getColor(Seasonal.getCurrentSeason(), d, e);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/color/world/GrassColors;getColor(DD)I"), method = "getDefaultGrassColor")
    public int getSeasonDefaultGrassColor(double d, double e) {
        return SeasonGrassColors.getColor(Seasonal.getCurrentSeason(), d, e);
    }

}
