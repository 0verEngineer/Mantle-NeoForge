package slimeknights.mantle.plugin.jei.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.RequiredArgsConstructor;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.ingredient.EntityIngredient;
import slimeknights.mantle.recipe.ingredient.EntityIngredient.EntityInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Renderer for entity type ingredients
 */
@RequiredArgsConstructor
public class EntityIngredientRenderer implements IIngredientRenderer<EntityIngredient.EntityInput> {
  private static final ResourceLocation MISSING = Mantle.getResource("textures/item/missingno.png");
  /** Entity types that will not render, as they either errored or are the wrong type */
  private static final Set<EntityType<?>> IGNORED_ENTITIES = new HashSet<>();

  /** Square size of the renderer in pixels */
  private final int size;

  /** Cache of entities for each entity type */
  private final Map<EntityType<?>,Entity> ENTITY_MAP = new HashMap<>();

  @Override
  public int getWidth() {
    return size;
  }

  @Override
  public int getHeight() {
    return size;
  }

  @Override
  public void render(GuiGraphics graphics, EntityInput ingredient) {
    render(graphics, ingredient, 0, 0);
  }

  @Override
  public void render(GuiGraphics graphics, EntityInput input, int posX, int posY) {
    Level world = Minecraft.getInstance().level;
    EntityType<?> type = input.type();
    if (world != null && !IGNORED_ENTITIES.contains(type)) {
      Entity entity;
      // players cannot be created using the type, but we can use the client player
      // side effect is it renders armor/items
      if (type == EntityType.PLAYER) {
        entity = Minecraft.getInstance().player;
      } else {
        // entity is created with the client world, but the entity map is thrown away when JEI restarts so they should be okay I think
        entity = ENTITY_MAP.computeIfAbsent(type, t -> t.create(world));
      }
      // only can draw living entities, plus non-living ones don't get recipes anyways
      if (entity instanceof LivingEntity livingEntity) {
        // scale down large mobs, but don't scale up small ones
        int scale = size / 2;
        float height = entity.getBbHeight();
        float width = entity.getBbWidth();
        if (height > 2 || width > 2) {
          scale = (int)(size / Math.max(height, width));
        }
        // catch exceptions drawing the entity to be safe, any caught exceptions blacklist the entity
        try {
          renderEntity(graphics, posX, posY, scale, livingEntity);
          return;
        } catch (Exception e) {
          Mantle.logger.error("Error drawing entity " + BuiltInRegistries.ENTITY_TYPE.getKey(type), e);
          IGNORED_ENTITIES.add(type);
          ENTITY_MAP.remove(type);
        }
      } else {
        // not living, so might as well skip next time
        IGNORED_ENTITIES.add(type);
        ENTITY_MAP.remove(type);
      }
    }

    // fallback, draw a pink and black "spawn egg"
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderColor(1, 1, 1, 1);
    int offset = (size - 16) / 2;
    graphics.blit(MISSING, offset, offset, 0, 0, 16, 16, 16, 16);
  }

  /**
   * Renders the entity centered in the slot without the scissor used by {@link InventoryScreen#renderEntityInInventoryFollowsAngle}.
   * That helper assumes absolute coordinates, while JEI positions ingredients through the current pose.
   */
  private void renderEntity(GuiGraphics graphics, int posX, int posY, int scale, LivingEntity entity) {
    // angles are computed from slot-local coordinates; absolute positions would saturate atan and turn the entity sideways
    float angleX = (float)Math.atan((size / 2.0F) / 40.0F);
    float angleY = (float)Math.atan((size / 2.0F - 10.0F) / 40.0F);
    // mirrors the vanilla angle math, just without the scissor
    Quaternionf pose = new Quaternionf().rotateZ((float)Math.PI);
    Quaternionf camera = new Quaternionf().rotateX(angleY * 20.0F * (float)(Math.PI / 180.0));
    pose.mul(camera);
    float bodyRot = entity.yBodyRot;
    float yRot = entity.getYRot();
    float xRot = entity.getXRot();
    float headRotO = entity.yHeadRotO;
    float headRot = entity.yHeadRot;
    entity.yBodyRot = 180.0F + angleX * 20.0F;
    entity.setYRot(180.0F + angleX * 40.0F);
    entity.setXRot(-angleY * 20.0F);
    entity.yHeadRot = entity.getYRot();
    entity.yHeadRotO = entity.getYRot();
    float entityScale = entity.getScale();
    Vector3f translate = new Vector3f(0.0F, entity.getBbHeight() / 2.0F + 0.0625F * entityScale, 0.0F);
    InventoryScreen.renderEntityInInventory(graphics, posX + size / 2.0F, posY + size / 2.0F, scale / entityScale, translate, pose, camera, entity);
    entity.yBodyRot = bodyRot;
    entity.setYRot(yRot);
    entity.setXRot(xRot);
    entity.yHeadRotO = headRotO;
    entity.yHeadRot = headRot;
  }

  @SuppressWarnings("removal")
  @Override
  public List<Component> getTooltip(EntityIngredient.EntityInput ingredient, TooltipFlag flag) {
    EntityType<?> type = ingredient.type();
    List<Component> tooltip = new ArrayList<>();
    tooltip.add(type.getDescription());
    if (flag.isAdvanced()) {
      tooltip.add(Component.literal(Loadables.ENTITY_TYPE.getString(type)).withStyle(ChatFormatting.DARK_GRAY));
    }
    return tooltip;
  }

  @SuppressWarnings("removal")
  @Override
  public void getTooltip(ITooltipBuilder tooltip, EntityInput ingredient, TooltipFlag flag) {
    EntityType<?> type = ingredient.type();
    tooltip.add(type.getDescription());
    if (flag.isAdvanced()) {
      tooltip.add(Component.literal(Loadables.ENTITY_TYPE.getString(type)).withStyle(ChatFormatting.DARK_GRAY));
    }
  }
}
