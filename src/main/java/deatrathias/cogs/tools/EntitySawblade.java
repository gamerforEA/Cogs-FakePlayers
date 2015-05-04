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
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.gamerforea.cogs.FakePlayerUtils;
import com.mojang.authlib.GameProfile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import deatrathias.cogs.util.ItemLoader;

public class EntitySawblade extends Entity
{
	private int tileX;
	private int tileY;
	private int tileZ;
	private int timeToLive;
	private int distanceLife;
	private int breakTime;
	private int maxBreakTime;
	private ItemStack referenceAxe;
	private EntityLivingBase caster;
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

	public EntitySawblade(World world)
	{
		super(world);
		this.setSize(0.5F, 0.5F);
		this.timeToLive = 60;
		this.distanceLife = 15;
		this.referenceAxe = new ItemStack(ItemLoader.itemPortableSaw);
		super.noClip = true;
	}

	public EntitySawblade(World world, EntityLivingBase caster)
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
		this.timeToLive = 60;
		this.distanceLife = 15;
		this.tileY = -100;
		this.referenceAxe = new ItemStack(ItemLoader.itemPortableSaw);
		super.noClip = true;
		this.caster = caster;
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

		if (this.breakTime == 0)
		{
			AxisAlignedBB hitEntities = super.boundingBox.getOffsetBoundingBox(super.motionX, super.motionY, super.motionZ);
			List source = this.getCollidingBlocks(hitEntities);
			double i$;
			if (!source.isEmpty())
			{
				i$ = -1.0D;
				List list = null;
				Iterator iter = source.iterator();

				while (iter.hasNext())
				{
					List coordBlock = (List) iter.next();
					double distance = Math.pow(super.posX - (double) ((Integer) coordBlock.get(0)).intValue() - 0.5D, 2.0D) + Math.pow(super.posY - (double) ((Integer) coordBlock.get(1)).intValue() - 0.5D, 2.0D) + Math.pow(super.posZ - (double) ((Integer) coordBlock.get(2)).intValue() - 0.5D, 2.0D);
					if (i$ == -1.0D || distance < i$)
					{
						i$ = distance;
						list = coordBlock;
					}
				}

				this.tileX = ((Integer) list.get(0)).intValue();
				this.tileY = ((Integer) list.get(1)).intValue();
				this.tileZ = ((Integer) list.get(2)).intValue();
				Block block = super.worldObj.getBlock(this.tileX, this.tileY, this.tileZ);
				if (ItemLoader.itemPortableSaw.getDigSpeed(this.referenceAxe, block, super.worldObj.getBlockMetadata(this.tileX, this.tileY, this.tileZ)) <= 1.0F && block.getBlockHardness(super.worldObj, this.tileX, this.tileY, this.tileZ) > 0.2F)
				{
					this.breakTime = -1;
				}
				else
				{
					this.breakTime = this.getBreakTime(this.tileX, this.tileY, this.tileZ);
					this.maxBreakTime = this.breakTime;
					super.rotationYaw = (float) (Math.atan2((double) this.tileZ + 0.5D - super.posZ, (double) this.tileX + 0.5D - super.posX) * 180.0D / 3.141592653589793D) - 90.0F;
					super.rotationPitch = (float) (Math.atan2(super.posY - (double) this.tileY - 0.5D, Math.sqrt(Math.pow((double) this.tileX + 0.5D - super.posX, 2.0D) + Math.pow((double) this.tileZ + 0.5D - super.posZ, 2.0D))) * 180.0D / 3.141592653589793D);
					super.motionX = (double) (-MathHelper.sin(super.rotationYaw / 180.0F * 3.1415927F) * MathHelper.cos(super.rotationPitch / 180.0F * 3.1415927F)) / 3.0D;
					super.motionZ = (double) (MathHelper.cos(super.rotationYaw / 180.0F * 3.1415927F) * MathHelper.cos(super.rotationPitch / 180.0F * 3.1415927F)) / 3.0D;
					super.motionY = (double) (-MathHelper.sin(super.rotationPitch / 180.0F * 3.1415927F)) / 3.0D;
				}
			}
			else
			{
				this.setPosition(super.posX + super.motionX, super.posY + super.motionY, super.posZ + super.motionZ);
				if (this.tileY >= 0)
				{
					i$ = Math.pow(super.posX - (double) this.tileX, 2.0D) + Math.pow(super.posY - (double) this.tileY, 2.0D) + Math.pow(super.posZ - (double) this.tileZ, 2.0D);
					if (i$ < 1.0D)
					{
						int[] var17 = this.findNextTile();
						if (var17 != null)
						{
							this.tileX = var17[0];
							this.tileY = var17[1];
							this.tileZ = var17[2];
							super.rotationYaw = (float) (Math.atan2((double) this.tileZ + 0.5D - super.posZ, (double) this.tileX + 0.5D - super.posX) * 180.0D / 3.141592653589793D) - 90.0F;
							super.rotationPitch = (float) (Math.atan2(super.posY - (double) this.tileY - 0.5D, Math.sqrt(Math.pow((double) this.tileX + 0.5D - super.posX, 2.0D) + Math.pow((double) this.tileZ + 0.5D - super.posZ, 2.0D))) * 180.0D / 3.141592653589793D);
							super.motionX = (double) (-MathHelper.sin(super.rotationYaw / 180.0F * 3.1415927F) * MathHelper.cos(super.rotationPitch / 180.0F * 3.1415927F)) / 3.0D;
							super.motionZ = (double) (MathHelper.cos(super.rotationYaw / 180.0F * 3.1415927F) * MathHelper.cos(super.rotationPitch / 180.0F * 3.1415927F)) / 3.0D;
							super.motionY = (double) (-MathHelper.sin(super.rotationPitch / 180.0F * 3.1415927F)) / 3.0D;
						}
					}
				}

				--this.distanceLife;
				if (!super.worldObj.isRemote && this.distanceLife <= 0)
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
				if (!super.worldObj.isRemote && this.breakTime >= 0)
				{
					super.worldObj.destroyBlockInWorldPartially(this.getEntityId(), this.tileX, this.tileY, this.tileZ, 10 - 10 * this.breakTime / this.maxBreakTime);
				}

				if (this.breakTime == 0)
				{
					this.distanceLife += 5;
					this.timeToLive += this.maxBreakTime;
					if (!super.worldObj.isRemote)
					{
						// TODO gamerforEA code start
						if (FakePlayerUtils.callBlockBreakEvent(this.tileX, this.tileY, this.tileZ, this.getOwnerFake()).isCancelled())
						{
							this.setDead();
							return;
						}
						// TODO gamerforEA code end
						ArrayList drops = block.getDrops(super.worldObj, this.tileX, this.tileY, this.tileZ, super.worldObj.getBlockMetadata(this.tileX, this.tileY, this.tileZ), 0);
						Iterator iter = drops.iterator();

						while (iter.hasNext())
						{
							ItemStack stack = (ItemStack) iter.next();
							EntityItem item = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, stack);
							item.motionX = 0.0D;
							item.motionY = 0.0D;
							item.motionZ = 0.0D;
							super.worldObj.spawnEntityInWorld(item);
						}

						super.worldObj.playAuxSFX(2001, this.tileX, this.tileY, this.tileZ, Block.getIdFromBlock(block) + (super.worldObj.getBlockMetadata(this.tileX, this.tileY, this.tileZ) << 12));
						super.worldObj.setBlock(this.tileX, this.tileY, this.tileZ, Blocks.air, 0, 3);
					}
				}
			}
			else
			{
				this.breakTime = 0;
			}
		}

		ArrayList list = new ArrayList(super.worldObj.getEntitiesWithinAABBExcludingEntity(this.caster, super.boundingBox.expand(0.5D, 0.5D, 0.5D)));
		if (list != null && !list.isEmpty())
		{
			DamageSource source = (new EntityDamageSourceIndirect("sawblade", this, this.caster)).setProjectile();
			Iterator<Entity> iter = list.iterator();

			while (iter.hasNext())
			{
				Entity entity = iter.next();
				if (entity.canBeCollidedWith())
				{
					// TODO gamerforEA code start
					if (FakePlayerUtils.callEntityDamageByEntityEvent(this.getOwnerFake(), entity, DamageCause.ENTITY_ATTACK, 8.0D).isCancelled())
					{
						this.setDead();
						return;
					}
					// TODO gamerforEA code end
					entity.attackEntityFrom(source, 8.0F);
				}
			}
		}

		--this.timeToLive;
		if (this.timeToLive <= 0 && !super.worldObj.isRemote)
		{
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
				if (!block.getMaterial().isToolNotRequired() && !ItemLoader.itemPortableSaw.canHarvestBlock(block, this.referenceAxe))
				{
					hardness *= 100.0F;
				}
				else
				{
					hardness *= 50.0F;
				}

				hardness /= ItemLoader.itemPortableSaw.getDigSpeed(this.referenceAxe, block, metadata);
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
							else if (!super.worldObj.isRemote && block.getBlockHardness(super.worldObj, x, y, z) == 0.0F)
							{
								// TODO gamerforEA code start
								if (FakePlayerUtils.callBlockBreakEvent(x, y, z, this.getOwnerFake()).isCancelled())
								{
									this.setDead();
									return result;
								}
								// TODO gamerforEA code end
								super.worldObj.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(block) + (super.worldObj.getBlockMetadata(x, y, z) << 12));
								block.harvestBlock(super.worldObj, (EntityPlayer) this.caster, x, y, z, super.worldObj.getBlockMetadata(x, y, z));
								super.worldObj.setBlock(x, y, z, Blocks.air, 0, 3);
							}
						}
					}
				}
			}
		}

		return result;
	}

	private int[] findNextTile()
	{
		int[] nextCoord = new int[3];

		for (int dir = 1; dir < 7; ++dir)
		{
			ForgeDirection direction = ForgeDirection.getOrientation(dir % 6);
			nextCoord[0] = this.tileX + direction.offsetX;
			nextCoord[1] = this.tileY + direction.offsetY;
			nextCoord[2] = this.tileZ + direction.offsetZ;
			Block block = super.worldObj.getBlock(nextCoord[0], nextCoord[1], nextCoord[2]);
			if (block != null && block.getBlockHardness(super.worldObj, nextCoord[0], nextCoord[1], nextCoord[2]) > 0.0F && ItemLoader.itemPortableSaw.getDigSpeed(this.referenceAxe, block, super.worldObj.getBlockMetadata(nextCoord[0], nextCoord[1], nextCoord[2])) > 1.0F)
			{
				return nextCoord;
			}
		}

		return null;
	}

	public void setDead()
	{
		if (!super.worldObj.isRemote && this.breakTime != 0)
		{
			super.worldObj.destroyBlockInWorldPartially(this.getEntityId(), this.tileX, this.tileY, this.tileZ, -1);
		}

		super.setDead();
	}

	protected void readEntityFromNBT(NBTTagCompound var1)
	{
		this.setDead();
	}

	protected void writeEntityToNBT(NBTTagCompound var1)
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