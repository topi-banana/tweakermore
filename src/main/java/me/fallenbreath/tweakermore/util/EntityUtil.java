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

package me.fallenbreath.tweakermore.util;

//import fi.dy.masa.malilib.MaLiLib;
//import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.malilib.util.restrictions.UsageRestriction;
import java.util.List;
import java.util.Set;
import me.fallenbreath.tweakermore.util.compat.tweakeroo.TweakerooAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class EntityUtil
{
	public static PlayerAbilities getPlayerAbilities(PlayerEntity player)
	{
		//#if MC >= 11700
		//$$ return player.getAbilities();
		//#else
		return player.abilities;
		//#endif
	}

	public static boolean isFlyingCreativePlayer(Entity entity)
	{
		if (entity instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity)entity;
			return player.isCreative() && getPlayerAbilities(player).flying;
		}
		return false;
	}

	private static final boolean TWEAKEROO_LOADED = FabricUtil.isModLoaded(ModIds.tweakeroo);

	@Nullable
	public static ClientPlayerEntity getCurrentPlayerOrFreeCameraEntity()
	{
		if (TWEAKEROO_LOADED)
		{
			ClientPlayerEntity freecam = TweakerooAccess.getFreecamEntity();
			if (freecam != null)
			{
				return freecam;
			}
		}
		return MinecraftClient.getInstance().player;
	}

	public static class EntityRestriction extends UsageRestriction<EntityType<?>>
	{
		@Override
		protected void setValuesForList(Set<EntityType<?>> set, List<String> names)
		{
			for (String name : names)
			{
				Identifier rl = null;

				try
				{
					rl = Identifier.tryParse(name);
				}
				catch (Exception ignore) {}

				EntityType<?> item = rl != null ? Registry.ENTITY_TYPE.get(rl) : null;

				if (item != null)
				{
					set.add(item);
				}
//				else
//				{
//					MaLiLib.logger.warn(StringUtils.translate("malilib.error.invalid_entity_blacklist_entry", name));
//				}
			}
		}
	}
}
