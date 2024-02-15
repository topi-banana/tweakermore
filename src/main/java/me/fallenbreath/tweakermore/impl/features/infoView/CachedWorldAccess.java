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

package me.fallenbreath.tweakermore.impl.features.infoView;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CachedWorldAccess
{
	private final World world;
	private final Long2ObjectOpenHashMap<Chunk> chunkCache = new Long2ObjectOpenHashMap<>();  // chunkPos -> chunk
	private final Long2ObjectOpenHashMap<BlockState> stateCache = new Long2ObjectOpenHashMap<>();  // blockPos -> blockState
	private final Long2ObjectOpenHashMap<Optional<BlockEntity>> blockEntityCache = new Long2ObjectOpenHashMap<>();  // blockPos -> blockEntity

	public CachedWorldAccess(World world)
	{
		this.world = world;
	}

	public Chunk getChunk(BlockPos blockPos)
	{
		ChunkPos chunkPos = new ChunkPos(blockPos);
		return this.chunkCache.computeIfAbsent(
				chunkPos.toLong(),
				k -> this.world.getChunk(chunkPos.x, chunkPos.z)
		);
	}

	public BlockState getBlockState(BlockPos blockPos)
	{
		return this.stateCache.computeIfAbsent(
				blockPos.asLong(),
				bp -> this.getChunk(blockPos).getBlockState(blockPos)
		);
	}

	@Nullable
	public BlockEntity getBlockEntity(BlockPos blockPos)
	{
		return this.blockEntityCache.computeIfAbsent(
				blockPos.asLong(),
				bp -> Optional.ofNullable(this.getChunk(blockPos).getBlockEntity(blockPos))
		).orElse(null);
	}
}