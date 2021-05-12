package malekire.devilrycraft.objects.blockentities.sealhelpers;

import com.qouteall.immersive_portals.my_util.DQuaternion;
import com.qouteall.immersive_portals.portal.Portal;
import com.qouteall.immersive_portals.portal.PortalManipulation;
import malekire.devilrycraft.util.CrystalType;
import malekire.devilrycraft.util.math.beziercurves.BezierCurve;
import malekire.devilrycraft.util.math.beziercurves.Point;
import malekire.devilrycraft.util.portalutil.PortalFinderUtil;
import malekire.devilrycraft.util.portalutil.PortalFunctionUtil;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static malekire.devilrycraft.common.DevilryBlocks.SEAL_BLOCK;
import static malekire.devilrycraft.util.CrystalType.AIR_TYPE;
import static malekire.devilrycraft.util.CrystalType.VIS_TYPE;
import static malekire.devilrycraft.util.DevilryProperties.FOURTH_LAYER;
import static malekire.devilrycraft.util.DevilryProperties.THIRD_LAYER;

public class SealPortalHelper extends AbstractSealHelperClass {
    double maxWidth = 3;
    double maxHeight = 3;
    public double width = 0;
    public double height = 0;
    double timeValue = 0;
    public double PORTAL_OFFSET_ANIMATION_TICKS = 0.0;
    public double PORTAL_ANIMATION_TICKS = 80.0;
    BezierCurve bezierCurve = new BezierCurve();
    Portal entrancePortal;
    Portal exitPortal;
    public boolean hasPortal = false;
    public int numTriangles = 1;
    public int TICKS_TO_DESTROY = -1;
    public static final int LIGHTNING_SPAWN_CHANCE = 10;
    int ticksSinceCreation = 0;
    public boolean doSecondSound = false;
    public SealBlockEntity opposingSealBlockEntity;
    public SealPortalHelper() {
        super("gateway", VIS_TYPE, AIR_TYPE);
        addBezierCurves();
    }

    public void addBezierCurves() {
        bezierCurve.addPoint(new Point(0, 0));
        bezierCurve.addPoint(new Point(0.5, 0));
        bezierCurve.addPoint(new Point(0.5, 1));
        bezierCurve.addPoint(new Point(1, 1));
    }
    public int tickTime = 0;
    @Override
    public void tick() {
        if(tickTime > 3)
        {
            tickTime = 3;
        }
        if(entrancePortal != null) {
            duringTickAnimatePortal();
            hasPortal = true;
            tickTime++;
        }


        if(entrancePortal != null && tickTime > 2) {
            if (world.getClosestPlayer(entrancePortal, 3) != null|| world.getClosestPlayer(exitPortal, 3) != null)
            {
                if(!growPortal)
                    growPortal();
            }
            else
            {
                if(!shrinkIsAnimated)
                    shrinkPortal();
            }
        }
    }
    boolean shrinkIsAnimated = false;
    public void animatePortals(double timeValue) {
        width = bezierCurve.getY(timeValue) * ((double) maxWidth);
        height = bezierCurve.getY(timeValue) * ((double) maxHeight);

        setPortalsSize(width, height);
        if (width < 0.2) {
            setNullShape();
        } else {
            if (width < 0.3) {
                numTriangles = 15;
            } else if (width < 0.11) {
                numTriangles = 20;
            } else {
                numTriangles = 50;
            }
            PortalFunctionUtil.makeRoundPortal(entrancePortal, numTriangles);
            PortalFunctionUtil.makeRoundPortal(exitPortal, numTriangles);
        }
        if(width < 0.01) {
            return;
        }
        reloadPortals();
    }
    public void setPortalsSize(double width, double height) {
        PortalFunctionUtil.setSize(this.entrancePortal, width, height);
        PortalFunctionUtil.setSize(this.exitPortal, width, height);
    }

    public void reloadPortals() {
        this.entrancePortal.reloadAndSyncToClient();
        this.exitPortal.reloadAndSyncToClient();
    }

    public void setNullShape() {
        this.entrancePortal.specialShape = null;
        this.exitPortal.specialShape = null;
    }
    double portalDestructionAnimationTicks = PORTAL_ANIMATION_TICKS;
    public void performPortalFunction() {
        System.out.println("performing portal fucntion 1");
        if(world.getBlockState(pos).getBlock() == Blocks.AIR)
            return;
        System.out.println("performing portal fucntion 2");
        CrystalType firstCode = world.getBlockState(pos).get(THIRD_LAYER);
        CrystalType secondCode = world.getBlockState(pos).get(FOURTH_LAYER);
        BlockPos portalPosition = this.blockEntity.getPos();
        BlockPos outputPos;
        System.out.println("has portal : " + hasPortal);
        if(hasPortal)
            return;
        System.out.println("preforming oprtal function 3");
        for(SealBlockEntity secondBlockEntity : PortalFinderUtil.sealBlockEntities)
        {
            System.out.println("trying list");
            if(!this.hasPortal) {

                if (world.getBlockState(secondBlockEntity.getPos()).getBlock() == SEAL_BLOCK && world.getBlockState(pos).getBlock() == SEAL_BLOCK && secondBlockEntity != this.blockEntity && world.getBlockState(secondBlockEntity.getPos()).get(THIRD_LAYER) == firstCode) {
                    if (world.getBlockState(secondBlockEntity.getPos()).get(FOURTH_LAYER) == secondCode) {
                        if (hasPortal)
                            return;
                        opposingSealBlockEntity = secondBlockEntity;
                        System.out.println("making portal");
                        hasPortal = true;
                        outputPos = secondBlockEntity.getPos();
                        this.entrancePortal = Portal.entityType.create(world);
                        final float portalVisualOffset = 0.9F;

                        Vec3d myPortalPosition = Vec3d.of(portalPosition).add(0.5, 0.5, 0.5);



                        Vec3d originPos = Vec3d.of(portalPosition);


                        Vec3d destPos = Vec3d.of(outputPos).add(0.5, 0.5, 0.5);
                        /*
                                .subtract(
                                        ((double)secondBlockEntity.facing.getOffsetX())*portalVisualOffset,
                                        ((double)secondBlockEntity.facing.getOffsetY())*portalVisualOffset,
                                        ((double)secondBlockEntity.facing.getOffsetZ())*portalVisualOffset);

                         */
                        originPos = myPortalPosition;


                        Direction reverseFacing = blockEntity.facing.getOpposite();

                        entrancePortal.setDestinationDimension(secondBlockEntity.getWorld().getRegistryKey());


                        reverseFacing = blockEntity.facing.getOpposite();
                       originPos.add(Vec3d.of(reverseFacing.getVector()).multiply(portalVisualOffset));
                        destPos.add(Vec3d.of(secondBlockEntity.facing.getOpposite().getVector()).multiply(portalVisualOffset));

                        entrancePortal.setOriginPos(originPos);
                        entrancePortal.setDestination(destPos);
                        double rotation = 0;
                        double degrees = 0;
                        rotation = blockEntity.facing.asRotation();
                        degrees = secondBlockEntity.facing.getOpposite().asRotation();

                        entrancePortal.setRotationTransformation(DQuaternion.rotationByDegrees(new Vec3d(0, 1, 0), degrees+180).toMcQuaternion());
                        float rotation2 = 90;

                        switch (reverseFacing) {
                            case UP:
                                entrancePortal.setOrientationAndSize(
                                        new Vec3d(1, 0, 0).rotateY((float) Math.toRadians(rotation)), // axisW
                                        new Vec3d(0, 1, 0).rotateX((float) Math.toRadians(-rotation2)), // axisH
                                        0.01, // width
                                        0.01 // height
                                );
                                break;
                            case DOWN:
                                entrancePortal.setOrientationAndSize(
                                        new Vec3d(1, 0, 0).rotateY((float) Math.toRadians(rotation)), // axisW
                                        new Vec3d(0, 1, 0).rotateX((float) Math.toRadians(rotation2)), // axisH
                                        0.1, // width
                                        0.1 // height
                                );
                                break;
                            default:
                                entrancePortal.setOrientationAndSize(
                                        new Vec3d(1, 0, 0).rotateY((float) Math.toRadians(rotation)), // axisW
                                        new Vec3d(0, 1, 0), // axisH
                                        0.1, // width
                                        0.1 // height
                                );
                                break;
                        }
                        entrancePortal.world.spawnEntity(entrancePortal);
                        //exitPortal = PortalManipulation.createReversePortal(entrancePortal, Portal.entityType);


                        exitPortal = PortalManipulation.completeBiWayPortal(entrancePortal, Portal.entityType);
                        //exitPortal.world.spawnEntity(exitPortal);
                        //((PortableHoleBlockEntity)context.getWorld().getBlockEntity(portalPosition)).resultPos = new BlockPos(destPos);
                        //exitPortal.world.spawnEntity(exitPortal);


                        return;
                    }
                }
            }
        }
    }
    public void shrinkPortal()
    {
        shrinkIsAnimated = true;
        growPortal = false;
        TICKS_TO_DESTROY = (int) ((ticksSinceCreation + 80));
        portalDestructionAnimationTicks = PORTAL_ANIMATION_TICKS;
    }
    public void playSpawnInSounds() {

    }
    boolean growPortal = false;
    public void growPortal()
    {
        growPortal = true;
        shrinkIsAnimated = false;
        ticksSinceCreation = 0;
        TICKS_TO_DESTROY = -1;
    }
    public void animateShrinkingPortal() {
        if(shrinkIsAnimated) {
            if (ticksSinceCreation > TICKS_TO_DESTROY - (PORTAL_ANIMATION_TICKS) && !(ticksSinceCreation > TICKS_TO_DESTROY - 2)) {
                portalDestructionAnimationTicks--;
                if(timeValue > portalDestructionAnimationTicks / PORTAL_ANIMATION_TICKS)
                    timeValue = portalDestructionAnimationTicks / PORTAL_ANIMATION_TICKS;
                if (entrancePortal != null && timeValue >= 0) {
                    animatePortals(timeValue);
                }

            }
        }
    }
    public void duringTickAnimatePortal() {
        if (this.hasPortal) {
            ticksSinceCreation++;
            animateShrinkingPortal();

            if (ticksSinceCreation > TICKS_TO_DESTROY)
                //destroyItself();
                if (ticksSinceCreation == 1) {


                    doSecondSound = true;
                    //thePortal.commandsOnTeleported = new ArrayList<>();
                    //thePortal.commandsOnTeleported.add("playsound devilry_craft:chaos_portal block "+resultPos.getX()+" "+resultPos.getY()+" "+resultPos.getZ()+" 1 1");
                }


            if(ticksSinceCreation == 4)
            {
                playSpawnInSounds();
            }
            if (ticksSinceCreation < PORTAL_ANIMATION_TICKS && ticksSinceCreation > 4 && growPortal) {
                if(timeValue < ((double) ticksSinceCreation + PORTAL_OFFSET_ANIMATION_TICKS) / (PORTAL_ANIMATION_TICKS + PORTAL_OFFSET_ANIMATION_TICKS))
                    timeValue = ((double) ticksSinceCreation + PORTAL_OFFSET_ANIMATION_TICKS) / (PORTAL_ANIMATION_TICKS + PORTAL_OFFSET_ANIMATION_TICKS);
                width = bezierCurve.getY(timeValue) * maxWidth;
                height = bezierCurve.getY(timeValue) * maxHeight;
                animatePortals(timeValue);
            }
        }
    }


    @Override
    public void oneOffTick() {
        PortalFinderUtil.sealBlockEntities.add(blockEntity);
        performPortalFunction();
    }

    @Override
    public AbstractSealHelperClass getNewInstance() {
        return new SealPortalHelper();
    }
}
