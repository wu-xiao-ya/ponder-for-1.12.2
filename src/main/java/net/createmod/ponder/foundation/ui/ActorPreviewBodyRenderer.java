package net.createmod.ponder.foundation.ui;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.ActorPreviewAppearance.ActorPreviewRenderData;
import net.createmod.ponder.foundation.ui.render.GLStateGuard;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

final class ActorPreviewBodyRenderer {

    private ActorPreviewBodyRenderer() {
    }

    static void render(PonderScene.ActorKind kind, ActorPreviewRenderData renderData) {
        ActorBodyRenderer.forKind(kind).render(new ActorBodyDrawContext(renderData));
    }

    private static final class ActorPrimitiveDrawer {

        private ActorPrimitiveDrawer() {
        }

        static void drawActorPrism(float red, float green, float blue, float alpha, float halfX, float height,
            float halfZ) {
            drawActorPrism(red, green, blue, alpha, halfX, height, halfZ, 0.0F, 0.0F, 0.0F);
        }

        static void drawActorPrism(float red, float green, float blue, float alpha, float halfX, float height,
            float halfZ, float offsetX, float offsetY, float offsetZ) {
            float x1 = offsetX - halfX;
            float x2 = offsetX + halfX;
            float y1 = offsetY;
            float y2 = offsetY + height;
            float z1 = offsetZ - halfZ;
            float z2 = offsetZ + halfZ;

            try (GLStateGuard colorGuard = GLStateGuard.color(red, green, blue, alpha)) {
                GL11.glBegin(GL11.GL_QUADS);
                GL11.glVertex3f(x1, y1, z1);
                GL11.glVertex3f(x2, y1, z1);
                GL11.glVertex3f(x2, y2, z1);
                GL11.glVertex3f(x1, y2, z1);

                GL11.glVertex3f(x1, y1, z2);
                GL11.glVertex3f(x2, y1, z2);
                GL11.glVertex3f(x2, y2, z2);
                GL11.glVertex3f(x1, y2, z2);

                GL11.glVertex3f(x1, y1, z1);
                GL11.glVertex3f(x1, y1, z2);
                GL11.glVertex3f(x1, y2, z2);
                GL11.glVertex3f(x1, y2, z1);

                GL11.glVertex3f(x2, y1, z1);
                GL11.glVertex3f(x2, y1, z2);
                GL11.glVertex3f(x2, y2, z2);
                GL11.glVertex3f(x2, y2, z1);

                GL11.glVertex3f(x1, y2, z1);
                GL11.glVertex3f(x2, y2, z1);
                GL11.glVertex3f(x2, y2, z2);
                GL11.glVertex3f(x1, y2, z2);

                GL11.glVertex3f(x1, y1, z1);
                GL11.glVertex3f(x2, y1, z1);
                GL11.glVertex3f(x2, y1, z2);
                GL11.glVertex3f(x1, y1, z2);
                GL11.glEnd();
            }
        }

        static void drawActorHeading(float alpha) {
            int lineColor = withAlpha(0xF2F5F8, alpha * 235.0F);
            float red = ((lineColor >> 16) & 0xFF) / 255.0F;
            float green = ((lineColor >> 8) & 0xFF) / 255.0F;
            float blue = (lineColor & 0xFF) / 255.0F;
            float lineAlpha = ((lineColor >>> 24) & 0xFF) / 255.0F;

            try (GLStateGuard colorGuard = GLStateGuard.color(red, green, blue, lineAlpha);
                GLStateGuard lineWidthGuard = GLStateGuard.lineWidth(2.0F)) {
                GL11.glBegin(GL11.GL_LINES);
                GL11.glVertex3f(0.0F, 0.05F, 0.0F);
                GL11.glVertex3f(0.0F, 0.05F, -0.38F);
                GL11.glEnd();
            }
        }

        static void drawCartWheels(float alpha) {
            int wheelColor = withAlpha(0x2B2B2B, alpha * 255.0F);
            float red = ((wheelColor >> 16) & 0xFF) / 255.0F;
            float green = ((wheelColor >> 8) & 0xFF) / 255.0F;
            float blue = (wheelColor & 0xFF) / 255.0F;
            float wheelAlpha = ((wheelColor >>> 24) & 0xFF) / 255.0F;
            float[][] wheels = new float[][] {
                {-0.22F, -0.12F},
                {0.22F, -0.12F},
                {-0.22F, 0.12F},
                {0.22F, 0.12F}
            };
            for (float[] wheel : wheels) {
                drawActorPrism(red, green, blue, wheelAlpha, 0.05F, 0.05F, 0.05F, wheel[0], -0.12F, wheel[1]);
            }
        }

        private static int withAlpha(int color, float alpha) {
            int alphaChannel = MathHelper.clamp((int) alpha, 0, 255);
            return alphaChannel << 24 | (color & 0x00FFFFFF);
        }
    }

    private static final class ActorBodyDrawContext {

        private final ActorPreviewRenderData renderData;

        private ActorBodyDrawContext(ActorPreviewRenderData renderData) {
            this.renderData = renderData;
        }

        void renderBirb() {
            drawBirbBody();
            ActorPrimitiveDrawer.drawActorHeading(renderData.alpha);
        }

        void renderItem() {
            drawItemBody();
            ActorPrimitiveDrawer.drawActorHeading(renderData.alpha * 0.7F);
        }

        void renderCart() {
            ActorPrimitiveDrawer.drawActorPrism(renderData.red, renderData.green, renderData.blue,
                renderData.alpha, 0.30F, 0.16F, 0.18F);
            ActorPrimitiveDrawer.drawCartWheels(renderData.alpha);
            ActorPrimitiveDrawer.drawActorHeading(renderData.alpha);
        }

        private void drawBirbBody() {
            switch (renderData.birbPoseKind) {
                case DANCE: {
                    ActorPrimitiveDrawer.drawActorPrism(renderData.red, renderData.green, renderData.blue,
                        renderData.alpha, 0.16F, 0.28F, 0.16F);
                    float wing = 0.04F + Math.abs(MathHelper.sin(renderData.currentTick * 0.35F)) * 0.05F;
                    ActorPrimitiveDrawer.drawActorPrism(renderData.red * 0.92F, renderData.green * 0.92F,
                        renderData.blue, renderData.alpha * 0.92F, wing, 0.12F, 0.04F, -0.18F, 0.08F, 0.0F);
                    ActorPrimitiveDrawer.drawActorPrism(renderData.red * 0.92F, renderData.green * 0.92F,
                        renderData.blue, renderData.alpha * 0.92F, wing, 0.12F, 0.04F, 0.18F, 0.08F, 0.0F);
                    return;
                }
                case FACE_CURSOR: {
                    ActorPrimitiveDrawer.drawActorPrism(renderData.red, renderData.green, renderData.blue,
                        renderData.alpha, 0.13F, 0.34F, 0.13F);
                    ActorPrimitiveDrawer.drawActorPrism(renderData.red * 0.85F, renderData.green * 0.95F,
                        renderData.blue, renderData.alpha * 0.9F, 0.04F, 0.10F, 0.10F, 0.0F, 0.26F, -0.14F);
                    return;
                }
                default:
                    ActorPrimitiveDrawer.drawActorPrism(renderData.red, renderData.green, renderData.blue,
                        renderData.alpha, 0.14F, 0.34F, 0.14F);
                    return;
            }
        }

        private void drawItemBody() {
            float sway = Math.abs(MathHelper.sin(renderData.currentTick * 0.25F)) * 0.02F;
            ActorPrimitiveDrawer.drawActorPrism(renderData.red, renderData.green, renderData.blue, renderData.alpha,
                0.12F + sway, 0.06F, 0.12F + sway);
        }
    }

    private enum ActorBodyRenderer {
        BIRB {
            @Override
            void render(ActorBodyDrawContext bodyDrawContext) {
                bodyDrawContext.renderBirb();
            }
        },
        ITEM {
            @Override
            void render(ActorBodyDrawContext bodyDrawContext) {
                bodyDrawContext.renderItem();
            }
        },
        CART {
            @Override
            void render(ActorBodyDrawContext bodyDrawContext) {
                bodyDrawContext.renderCart();
            }
        };

        abstract void render(ActorBodyDrawContext bodyDrawContext);

        static ActorBodyRenderer forKind(PonderScene.ActorKind kind) {
            if (kind == PonderScene.ActorKind.BIRB) {
                return BIRB;
            }
            if (kind == PonderScene.ActorKind.ITEM) {
                return ITEM;
            }
            return CART;
        }
    }
}
