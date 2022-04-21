package me.fallenbreath.tweakermore.mixins.tweaks.features.tweakmAutoPickSchematicBlock;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.materials.MaterialCache;
import fi.dy.masa.litematica.tool.ToolMode;
import fi.dy.masa.litematica.util.EntityUtils;
import fi.dy.masa.litematica.util.InventoryUtils;
import fi.dy.masa.litematica.util.ItemUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.util.LayerRange;
import fi.dy.masa.malilib.util.PositionUtils;
import fi.dy.masa.tweakeroo.tweaks.PlacementTweaks;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import me.fallenbreath.tweakermore.config.TweakerMoreConfigs;
import me.fallenbreath.tweakermore.util.ModIds;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC >= 11600
//$$ import net.minecraft.screen.slot.Slot;
//#endif

@Restriction(require = {@Condition(ModIds.tweakeroo), @Condition(ModIds.litematica)})
@Mixin(PlacementTweaks.class)
public abstract class PlacementTweaksMixin
{
	@Inject(method = "tryPlaceBlock", at = @At("HEAD"), remap = false)
	private static void tweakmAutoPickSchematicBlock(
			ClientPlayerInteractionManager controller,
			ClientPlayerEntity player,
			ClientWorld world,
			BlockPos posIn,
			Direction sideIn,
			Direction sideRotatedIn,
			float playerYaw,
			Vec3d hitVec,
			Hand hand,
			PositionUtils.HitPart hitPart,
			boolean isFirstClick,
			CallbackInfoReturnable<ActionResult> cir
	)
	{
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player != null)
		{
			if (DataManager.getToolMode() != ToolMode.REBUILD && !Configs.Generic.EASY_PLACE_MODE.getBooleanValue())
			{
				if (TweakerMoreConfigs.TWEAKM_AUTO_PICK_SCHEMATIC_BLOCK.getBooleanValue() && EntityUtils.shouldPickBlock(mc.player))
				{
					BlockHitResult hitResult = new BlockHitResult(hitVec, sideIn, posIn, false);
					ItemPlacementContext ctx = new ItemPlacementContext(new ItemUsageContext(player, hand, hitResult));
					doSchematicWorldPickBlock(mc, ctx.getBlockPos(), hand);
				}
			}
		}
	}

	/**
	 * Stolen from {@link fi.dy.masa.litematica.util.WorldUtils#doSchematicWorldPickBlock}
	 */
	private static void doSchematicWorldPickBlock(MinecraftClient mc, BlockPos pos, Hand hand)
	{
		World schematicWorld = SchematicWorldHandler.getSchematicWorld();
		World clientWorld = mc.world;
		if (schematicWorld != null && mc.player != null && clientWorld != null && mc.interactionManager != null)
		{
			LayerRange layerRange = DataManager.getRenderLayerRange();
			if (!layerRange.isPositionWithinRange(pos))
			{
				return;
			}
			BlockState state = schematicWorld.getBlockState(pos);

			ItemStack stack = MaterialCache.getInstance().
					//#if MC >= 11500
					getRequiredBuildItemForState
					//#else
					//$$ getItemForState
					//#endif
							(state, schematicWorld, pos);


			//#if MC >= 11700
			//$$ InventoryUtils.schematicWorldPickBlock(stack, pos, schematicWorld, mc);
			//#endif

			if (!stack.isEmpty())
			{
				//#if MC < 11700
				PlayerInventory inv = mc.player.inventory;
				stack = stack.copy();
				if (mc.player.abilities.creativeMode)
				{
					BlockEntity te = schematicWorld.getBlockEntity(pos);
					if (GuiBase.isCtrlDown() && te != null && clientWorld.isAir(pos))
					{
						ItemUtils.storeTEInStack(stack, te);
					}

					InventoryUtils.setPickedItemToHand(stack, mc);
					mc.interactionManager.clickCreativeStack(mc.player.getStackInHand(Hand.MAIN_HAND), 36 + inv.selectedSlot);
				}
				else
				{
					int slot = inv.getSlotWithStack(stack);
					boolean shouldPick = inv.selectedSlot != slot;
					if (shouldPick && slot != -1)
					{
						InventoryUtils.setPickedItemToHand(stack, mc);
					}
					//#if MC >= 11600
					//$$ else if (slot == -1 && Configs.Generic.PICK_BLOCK_SHULKERS.getBooleanValue())
					//$$ {
					//$$ 	slot = InventoryUtils.findSlotWithBoxWithItem(mc.player.playerScreenHandler, stack, false);
					//$$ 	if (slot != -1)
					//$$ 	{
					//$$ 		ItemStack boxStack = ((Slot) mc.player.playerScreenHandler.slots.get(slot)).getStack();
					//$$ 		InventoryUtils.setPickedItemToHand(boxStack, mc);
					//$$ 	}
					//$$ }
					//#endif
				}
				//#endif  // if MC < 11700

				// so hand restore works fine
				PlacementTweaks.cacheStackInHand(hand);
			}
		}
	}
}