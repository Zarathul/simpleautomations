package net.zarathul.simpleautomations.blocks;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

public enum PipeConnection implements StringRepresentable
{
	NORTH_TO_SOUTH("north_to_south", Direction.NORTH, Direction.SOUTH),
	EAST_TO_WEST(  "east_to_west",   Direction.EAST,  Direction.WEST),
	DOWN_TO_UP(    "down_to_up",     Direction.DOWN,  Direction.UP),

	NORTH_TO_UP(  "north_to_up",   Direction.NORTH, Direction.UP),
	NORTH_TO_EAST("north_to_east", Direction.NORTH, Direction.EAST),
	NORTH_TO_DOWN("north_to_down", Direction.NORTH, Direction.DOWN),
	NORTH_TO_WEST("north_to_west", Direction.NORTH, Direction.WEST),

	SOUTH_TO_UP(  "south_to_up",   Direction.SOUTH, Direction.UP),
	SOUTH_TO_EAST("south_to_east", Direction.SOUTH, Direction.EAST),
	SOUTH_TO_DOWN("south_to_down", Direction.SOUTH, Direction.DOWN),
	SOUTH_TO_WEST("south_to_west", Direction.SOUTH, Direction.WEST),

	EAST_TO_UP(  "east_to_up",   Direction.EAST, Direction.UP),
	EAST_TO_DOWN("east_to_down", Direction.EAST, Direction.DOWN),

	WEST_TO_UP(  "west_to_up",   Direction.WEST, Direction.UP),
	WEST_TO_DOWN("west_to_down", Direction.WEST, Direction.DOWN);

	private static final PipeConnection[][] CONNECTIONS =
	{
		{	// 0: DOWN
			null,			// 0: DOWN->DOWN
			DOWN_TO_UP,		// 1: DOWN->UP
			NORTH_TO_DOWN,  // 2: DOWN->NORTH
			SOUTH_TO_DOWN, 	// 3: DOWN->SOUTH
			WEST_TO_DOWN,	// 4: DOWN->WEST
			EAST_TO_DOWN,	// 5: DOWN->EAST
		},
		{	// 1: UP
			DOWN_TO_UP,		// 0: UP->DOWN
			null,			// 1: UP->UP
			NORTH_TO_UP,	// 2: UP->NORTH
			SOUTH_TO_UP, 	// 3: UP->SOUTH
			WEST_TO_UP,		// 4: UP->WEST
			EAST_TO_UP,		// 5: UP->EAST
		},
		{	// 2: NORTH
			NORTH_TO_DOWN,	// 0: NORTH->DOWN
			NORTH_TO_UP,	// 1: NORTH->UP
			null,			// 2: NORTH->NORTH
			NORTH_TO_SOUTH,	// 3: NORTH->SOUTH
			NORTH_TO_WEST,	// 4: NORTH->WEST
			NORTH_TO_EAST,	// 5: NORTH->EAST
		},
		{	// 3: SOUTH
			SOUTH_TO_DOWN,	// 0: SOUTH->DOWN
			SOUTH_TO_UP,	// 1: SOUTH->UP
			NORTH_TO_SOUTH, // 2: SOUTH->NORTH
			null,		 	// 3: SOUTH->SOUTH
			SOUTH_TO_WEST,	// 4: SOUTH->WEST
			SOUTH_TO_EAST,	// 5: SOUTH->EAST
		},
		{	// 4: WEST
			WEST_TO_DOWN,	// 0: WEST->DOWN
			WEST_TO_UP,		// 1: WEST->UP
			NORTH_TO_WEST,  // 2: WEST->NORTH
			SOUTH_TO_WEST, 	// 3: WEST->SOUTH
			null,			// 4: WEST->WEST
			EAST_TO_WEST,	// 5: WEST->EAST
		},
		{	// 5: EAST
			EAST_TO_DOWN,	// 0: EAST->DOWN
			EAST_TO_UP,		// 1: EAST->UP
			NORTH_TO_EAST,  // 2: EAST->NORTH
			SOUTH_TO_EAST, 	// 3: EAST->SOUTH
			EAST_TO_WEST,	// 4: EAST->WEST
			null,			// 5: EAST->EAST
		}
	};

	private final String name;

	private final Direction from;
	private final Direction to;

	public String getName()
	{
		return name;
	}

	public Direction from()
	{
		return from;
	}

	public Direction to()
	{
		return to;
	}

	PipeConnection(String name, Direction from, Direction to)
	{
		this.name = name;
		this.from = from;
		this.to = to;
	}

	@Override
	public String getSerializedName()
	{
		return name;
	}

	public boolean has(Direction direction)
	{
		return from == direction || to == direction;
	}

	public Direction getOtherDirection(Direction direction)
	{
		if (from == direction) return to;
		else if (to == direction) return from;

		return null;
	}

	public static PipeConnection get(Direction from, Direction to)
	{
		return CONNECTIONS[from.get3DDataValue()][to.get3DDataValue()];
	}
}