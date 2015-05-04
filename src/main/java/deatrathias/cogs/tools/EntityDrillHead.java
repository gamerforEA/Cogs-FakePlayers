package deatrathias.cogs.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import com.gamerforea.cogs.FakePlayerUtils;
import com.mojang.authlib.GameProfile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import deatrathias.cogs.util.ItemLoader;

public class EntityDrillHead extends Entity
{
	private double sourceX;
	private double sourceY;
	private double sourceZ;
	private int tileX;
	private int tileY;
	private int tileZ;
	private int timeToLive;
	private int distance;
	private int breakTime;
	private int maxBreakTime;
	private ItemStack referencePick;
	@SideOnly(Side.CLIENT)
	private float frame;

	// TODO gamerforEA code start
	public GameProfile ownerProfile;
	private FakePlayer ownerFake;

	public FakePlayer getOwnerFake()
	{
		FakePlayer fake = null;
		if (this.ownerFake != null) fake = this.ownerFake;
		else if (this.ownerProfile != null) fake = this.ownerFake = FakePlayerUtils.createFakePlayer(this.ownerProfile, this.worldObj);
		else fake = FakePlayerUtils.getPlayer(this.worldObj);
		return fake;
	}
	// TODO gamerforEA code end

	public EntityDrillHead(World world)
	{
		super(world);
		this.setSize(0.5F, 0.5F);
		this.timeToLive = 100;
		this.distance = 50;
		this.referencePick = new ItemStack(ItemLoader.itemClockworkPick);
		super.noClip = true;
	}

	public EntityDrillHead(World world, EntityLivingBase caster)
	{
		super(world);
		this.setSize(0.5F, 0.5F);
		this.setLocationAndAngles(caster.posX, caster.posY + (double) caster.getEyeHeight(), caster.posZ, caster.rotationYaw, caster.rotationPitch);
		super.posX -= (double) (MathHelper.cos(super.rotationYaw / 180.0F * 3.1415927F) * 0.16F);
		super.posY -= 0.1D;
		super.posZ -= (double) (MathHelper.sin(super.rotationYaw / 180.0F * 3.1415927F) * 0.16F);
		super.motionX = (double) (-MathHelper.sin(super.rotationYaw / 180.0F * 3.1415927F) * MathHelper.cos(super.rotationPitch / 180.0F * 3.1415927F)) / 3.0D;
		super.motionZ = (double) (MathHelper.cos(super.rotationYaw / 180.0F * 3.1415927F) * MathHelper.cos(super.rotationPitch / 180.0F * 3.1415927F)) / 3.0D;
		super.motionY = (double) (-MathHelper.sin(super.rotationPitch / 180.0F * 3.1415927F)) / 3.0D;
		this.sourceX = caster.posX;
		this.sourceY = caster.posY + (double) caster.getEyeHeight();
		this.sourceZ = caster.posZ;
		this.timeToLive = 100;
		this.distance = 50;
		this.referencePick = new ItemStack(ItemLoader.itemClockworkPick);
		super.noClip = true;
		// TODO gamerforEA code start
		if (caster instanceof EntityPlayer) this.ownerProfile = ((EntityPlayer) caster).getGameProfile();
		// TODO gamerforEA code end
	}

	public float getFrame()
	{
		return this.frame;
	}

	public void setFrame(float frame)
	{
		this.frame = frame;
	}

	protected void entityInit()
	{
	}

	public void onUpdate()
	{
		super.onUpdate();
		if (super.worldObj.isRemote)
		{
			this.frame += 20.0F;
			if (this.frame >= 360.0F)
			{
				this.frame -= 360.0F;
			}
		}

		Iterator iter;
		if (this.breakTime == 0)
		{
			List blocks = this.getCollidingBlocks(super.boundingBox.getOffsetBoundingBox(super.motionX, super.motionY, super.motionZ));
			if (!blocks.isEmpty())
			{
				double droppedList = -1.0D;
				List entItem = null;
				iter = blocks.iterator();

				while (iter.hasNext())
				{
					List stack = (List) iter.next();
					double distance1 = Math.pow(super.posX - (double) ((Integer) stack.get(0)).intValue() - 0.5D, 2.0D) + Math.pow(super.posY - (double) ((Integer) stack.get(1)).intValue() - 0.5D, 2.0D) + Math.pow(super.posZ - (double) ((Integer) stack.get(2)).intValue() - 0.5D, 2.0D);
					if (droppedList == -1.0D || distance1 < droppedList)
					{
						droppedList = distance1;
						entItem = stack;
					}
				}

				this.tileX = ((Integer) entItem.get(0)).intValue();
				this.tileY = ((Integer) entItem.get(1)).intValue();
				this.tileZ = ((Integer) entItem.get(2)).intValue();
				this.breakTime = this.getBreakTime(this.tileX, this.tileY, this.tileZ);
				this.maxBreakTime = this.breakTime;
			}
			else
			{
				this.setPosition(super.posX + super.motionX, super.posY + super.motionY, super.posZ + super.motionZ);
				--this.distance;
				if (this.distance <= 0)
				{
					this.setDead();
				}
			}
		}
		else
		{
			Block block = super.worldObj.getBlock(this.tileX, this.tileY, this.tileZ);
			if (block != null && !block.isAir(super.worldObj, this.tileX, this.tileY, this.tileZ))
			{
				--this.breakTime;
				if (!super.worldObj.isRemote)
				{
					super.worldObj.destroyBlockInWorldPartially(this.getEntityId(), this.tileX, this.tileY, this.tileZ, 10 - 10 * this.breakTime / this.maxBreakTime);
				}

				if (this.breakTime == 0 && !super.worldObj.isRemote)
				{
					// TODO gamerforEA code start
					if (FakePlayerUtils.callBlockBreakEvent(this.tileX, this.tileY, this.tileZ, this.getOwnerFake()).isCancelled())
					{
						this.setDead();
						return;
					}
					// TODO gamerforEA code end
					ArrayList drops = block.getDrops(super.worldObj, this.tileX, this.tileY, this.tileZ, super.worldObj.getBlockMetadata(this.tileX, this.tileY, this.tileZ), 0);
					Vec3 vec3 = Vec3.createVectorHelper(this.sourceX - super.posX, this.sourceY + 0.8D - super.posY, this.sourceZ - super.posZ);
					vec3.normalize();
					iter = drops.iterator();

					while (iter.hasNext())
					{
						ItemStack stack = (ItemStack) iter.next();
						EntityItem var11 = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, stack);
						var11.motionX = vec3.xCoord / 8.0D;
						var11.motionY = vec3.yCoord / 8.0D;
						var11.motionZ = vec3.zCoord / 8.0D;
						super.worldObj.spawnEntityInWorld(var11);
					}

					super.worldObj.playAuxSFX(2001, this.tileX, this.tileY, this.tileZ, Block.getIdFromBlock(block) + (super.worldObj.getBlockMetadata(this.tileX, this.tileY, this.tileZ) << 12));
					super.worldObj.setBlock(this.tileX, this.tileY, this.tileZ, Blocks.air, 0, 3);
				}
			}
			else
			{
				this.breakTime = 0;
			}
		}

		--this.timeToLive;
		if (!super.worldObj.isRemote && this.timeToLive <= 0)
		{
			if (this.breakTime != 0)
			{
				super.worldObj.destroyBlockInWorldPartially(this.getEntityId(), this.tileX, this.tileY, this.tileZ, -1);
			}

			this.setDead();
		}
	}

	public int getBreakTime(int x, int y, int z)
	{
		Block block = super.worldObj.getBlock(x, y, z);
		if (block == null)
		{
			return 0;
		}
		else
		{
			int metadata = super.worldObj.getBlockMetadata(x, y, z);
			float hardness = block.getBlockHardness(super.worldObj, x, y, z);
			if (hardness < 0.0F)
			{
				return -1;
			}
			else
			{
				if (!block.getMaterial().isToolNotRequired() && !this.referencePick.getItem().canHarvestBlock(block, this.referencePick))
				{
					hardness *= 100.0F;
				}
				else
				{
					hardness *= 50.0F;
				}

				hardness /= ItemLoader.itemClockworkPick.getDigSpeed(this.referencePick, block, metadata);
				return (int) Math.ceil((double) hardness);
			}
		}
	}

	public List<List<Integer>> getCollidingBlocks(AxisAlignedBB axisAlignedBB)
	{
		int startX = MathHelper.floor_double(axisAlignedBB.minX);
		int endX = MathHelper.floor_double(axisAlignedBB.maxX + 1.0D);
		int startY = MathHelper.floor_double(axisAlignedBB.minY);
		int endY = MathHelper.floor_double(axisAlignedBB.maxY + 1.0D);
		int startZ = MathHelper.floor_double(axisAlignedBB.minZ);
		int endZ = MathHelper.floor_double(axisAlignedBB.maxZ + 1.0D);
		ArrayList bbList = new ArrayList();
		ArrayList result = new ArrayList();

		for (int x = startX; x < endX; ++x)
		{
			for (int z = startZ; z < endZ; ++z)
			{
				if (super.worldObj.blockExists(x, 64, z))
				{
					for (int y = startY - 1; y < endY; ++y)
					{
						Block block = super.worldObj.getBlock(x, y, z);
						if (block != null)
						{
							bbList.clear();
							block.addCollisionBoxesToList(super.worldObj, x, y, z, axisAlignedBB, bbList, this);
							if (!bbList.isEmpty())
							{
								result.add(Arrays.asList(new Integer[] { Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(z) }));
							}
						}
					}
				}
			}
		}

		return result;
	}

	public void setDead()
	{
		if (!super.worldObj.isRemote && this.breakTime != 0)
		{
			super.worldObj.destroyBlockInWorldPartially(this.getEntityId(), this.tileX, this.tileY, this.tileZ, -1);
		}

		super.setDead();
	}

	protected void readEntityFromNBT(NBTTagCompound nbt)
	{
		this.setDead();
	}

	protected void writeEntityToNBT(NBTTagCompound nbt)
	{
	}

	protected boolean canTriggerWalking()
	{
		return false;
	}

	@SideOnly(Side.CLIENT)
	public float getShadowSize()
	{
		return 0.0F;
	}
}