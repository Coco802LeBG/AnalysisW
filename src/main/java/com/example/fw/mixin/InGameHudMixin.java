package com.example.fw.mixin;

import com.example.fw.AnalysisSuite;
import com.example.fw.module.HudCleaner;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void fw$hideSidebar(DrawContext ctx, ScoreboardObjective obj, CallbackInfo ci) {
        if (HudCleaner.hideScoreboard) ci.cancel();
    }

    /** Overlay ESP : dessiné en 2D par-dessus le monde (pas d'API de rendu 3D à maintenir). */
    @Inject(method = "render", at = @At("TAIL"))
    private void fw$overlay(DrawContext ctx, RenderTickCounter tickCounter, CallbackInfo ci) {
        AnalysisSuite.renderOverlay(ctx, tickCounter.getTickProgress(false));
    }
}
