/*
 * This file is part of the TweakerMore project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2023  Fallen_Breath and contributors
 *
 * TweakerMore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TweakerMore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TweakerMore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.fallenbreath.tweakermore.impl.mc_tweaks.shulkerItemContentHint;

import me.fallenbreath.tweakermore.config.TweakerMoreConfigs;
import me.fallenbreath.tweakermore.util.InventoryUtil;
import me.fallenbreath.tweakermore.util.ItemUtil;
import me.fallenbreath.tweakermore.util.render.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;

import java.util.Optional;

//#if MC >= 11700
//$$ import com.mojang.blaze3d.systems.RenderSystem;
//#endif

//#if MC >= 11500
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
//#else
//$$ import com.mojang.blaze3d.platform.GlStateManager;
//#endif

public class ShulkerItemContentHintRenderer
{
	private static final ThreadLocal<Boolean> isRendering = ThreadLocal.withInitial(() -> false);

	public static void render(
			//#if MC >= 11904
			//$$ MatrixStack matrices,
			//#endif
			ItemRenderer itemRenderer, ItemStack itemStack, int x, int y
	)
	{
		if (!TweakerMoreConfigs.SHULKER_ITEM_CONTENT_HINT.getBooleanValue())
		{
			return;
		}
		if (isRendering.get())
		{
			return;
		}
		if (!ItemUtil.isShulkerBox(itemStack.getItem()))
		{
			return;
		}
		Optional<DefaultedList<ItemStack>> stackList = InventoryUtil.getStoredItems(itemStack);
		if (!stackList.isPresent())
		{
			return;
		}

		ItemStack std = null;
		boolean useQuestionMark = false;
		for (ItemStack stack : stackList.get())
		{
			if (!stack.isEmpty())
			{
				if (std == null)
				{
					std = stack;
				}
				else if (!(ItemStack.areItemsEqual(stack, std) && ItemStack.areTagsEqual(stack, std)))
				{
					useQuestionMark = true;
					break;
				}
			}
		}
		if (std == null)
		{
			return;
		}

		// the display width of a slot
		final int SLOT_WIDTH = 16;
		double scale = TweakerMoreConfigs.SHULKER_ITEM_CONTENT_HINT_SCALE.getDoubleValue();

		if (scale <= 0)
		{
			return;
		}

		//#if MC >= 11904
		//$$ MatrixStack textMatrixStack = matrices;
		//$$ textMatrixStack.push();
		//#elseif MC >= 11500
		MatrixStack textMatrixStack = new MatrixStack();
		//#endif

		RenderUtil.Scaler scaler = RenderUtil.createScaler(x, y + SLOT_WIDTH, scale);
		scaler.apply(
				//#if MC >= 11904
				//$$ textMatrixStack
				//#elseif MC >= 11700
				//$$ useQuestionMark ? textMatrixStack : RenderSystem.getModelViewStack()
				//#elseif MC >= 11600
				//$$ textMatrixStack
				//#endif
		);

		if (useQuestionMark)
		{
			String text = "...";
			float width = RenderUtil.getRenderWidth(text);
			float height = RenderUtil.TEXT_HEIGHT;
			float textX = x + (SLOT_WIDTH - width) * 0.5F;
			float textY = y + SLOT_WIDTH - height - 3;
			int color = 0xDDDDDD;

			RenderUtil.Scaler textScaler = RenderUtil.createScaler(textX + width * 0.5, textY + height * 0.5, SLOT_WIDTH / height * 0.7);
			textScaler.apply(
					//#if MC >= 11600
					//$$ textMatrixStack
					//#endif
			);
			TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

			//#if MC >= 11500
			textMatrixStack.translate(
					0.0, 0.0,
					//#if MC >= 11904
					//$$ 150
					//#else
					itemRenderer.zOffset + 150
					//#endif
			);
			VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
			textRenderer.draw(
					text,
					textX,
					textY,
					color,
					true,
					textMatrixStack.peek().getModel(),
					immediate,
					//#if MC >= 11904
					//$$ TextRenderer.TextLayerType.NORMAL,
					//#else
					false,
					//#endif
					0,
					0xF000F0
			);
			immediate.draw();
			//#else
			//$$ GlStateManager.disableLighting();
			//$$ GlStateManager.disableDepthTest();
			//$$ GlStateManager.disableBlend();
			//$$ textRenderer.drawWithShadow(text, textX, textY, color);
			//$$ GlStateManager.enableBlend();
			//$$ GlStateManager.enableLighting();
			//$$ GlStateManager.enableDepthTest();
			//#endif

			textScaler.restore();
		}
		else
		{
			isRendering.set(true);

			//#if MC < 11904
			float zOffset = itemRenderer.zOffset;
			//#endif

			try
			{
				//#if MC >= 11904
				//$$ matrices.push();
				//$$ matrices.translate(0, 0, 10);
				//#else
				itemRenderer.zOffset += 10;
				// scale the z axis, so the lighting of the item can render correctly
				// see net.minecraft.client.render.item.ItemRenderer.renderGuiItemModel for z offset applying
				scaler.getRenderContext().scale(1, 1, scale);
				scaler.getRenderContext().translate(0, 0, (100.0F + itemRenderer.zOffset) * (1 / scale - 1));
				//#endif

				// we do this manually so no need to care about extra z-offset modification of itemRenderer in its ItemRenderer#renderGuiItem
				itemRenderer.renderGuiItemIcon(
						//#if MC >= 11904
						//$$ matrices,
						//#endif
						std, x, y
				);
			}
			finally
			{
				isRendering.set(false);

				//#if MC >= 11904
				//$$ matrices.pop();
				//#else
				itemRenderer.zOffset = zOffset;
				//#endif
			}
		}

		scaler.restore();
		//#if MC >= 11700 && MC < 11904
		//$$ RenderSystem.applyModelViewMatrix();
		//#endif

		//#if MC >= 11904
		//$$ textMatrixStack.pop();
		//#endif
	}
}