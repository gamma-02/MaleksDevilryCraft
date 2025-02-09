package malekire.devilrycraft.objects.items;

import malekire.devilrycraft.common.DevilryBlocks;
import malekire.devilrycraft.objects.blockentities.seals.AbstractSeal;
import malekire.devilrycraft.objects.blockentities.seals.SealBlockEntity;
import malekire.devilrycraft.util.world.WorldUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.system.CallbackI;

import java.util.Optional;

import static malekire.devilrycraft.Devilrycraft.MOD_ID;

public class SealWrangler extends Item {
    public static final Identifier CHEST_MODE = new Identifier(MOD_ID, "is_in_chest_mode");
    public static final Identifier ACTIVE = new Identifier(MOD_ID, "is_active");


    public SealWrangler(Settings settings) {
        super(settings);
    }
    public static Optional<BlockPos> tryGetValidBlock(World world, BlockPos pos) {
        return WorldUtil.findFirstBlockInRange(world, pos, new Vec3d(2, 2, 2), (pos1) -> blockTry(world, (BlockPos)pos1));
    }
    public static boolean blockTry(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock().equals(DevilryBlocks.SEAL_BLOCK);
    }
    public static boolean hasSealPos(ItemStack stack) {
        if(stack.getTag() == null)
            return false;
        return stack.getTag().contains("seal_pos");
    }
    public static boolean hasBlockSelected(ItemStack stack) {
        if(stack.getTag()==null)
            return false;
        return stack.getTag().contains("selected_block_pos");
    }
    public static AbstractSeal getSeal(World world, BlockPos pos) {
        return ((SealBlockEntity)world.getBlockEntity(pos)).getSeal();
    }
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if(context.getWorld().isClient()) {
            context.getStack().getOrCreateTag().putBoolean(ACTIVE.toString(), true);
            return ActionResult.PASS;
        }
        if(hasSealPos(context.getStack())) {
            System.out.println("has seal pos");
            addPosToItemStack(context.getStack(), context.getBlockPos(), "selected_block_pos");
            System.out.println(getPosFromItemStack(context.getStack(), "seal_pos"));
            System.out.println(context.getWorld().getBlockState(getPosFromItemStack(context.getStack(), "seal_pos")).getBlock());
            getSeal(context.getWorld(), getPosFromItemStack(context.getStack(), "seal_pos")).sealWranglerFunction(context.getStack());
            context.getStack().getOrCreateTag().putBoolean(ACTIVE.toString(), true);
        } else {
            Optional<BlockPos> possibleSealPos = tryGetValidBlock(context.getWorld(), context.getBlockPos());
            System.out.println(possibleSealPos);
            possibleSealPos.ifPresent(blockPos -> {
                addPosToItemStack(context.getStack(), possibleSealPos.get(), "seal_pos");
                context.getStack().getOrCreateTag().putBoolean(ACTIVE.toString(), true);
            });
        }
        return ActionResult.PASS;
    }
    public static void addPosToItemStack(ItemStack itemStack, BlockPos pos, String key) {
        itemStack.getOrCreateTag().put(key, BlockPos.CODEC.encode(pos, NbtOps.INSTANCE, NbtOps.INSTANCE.empty()).getOrThrow(false, (string) -> {}));
    }
    public static BlockPos getPosFromItemStack(ItemStack itemStack, String key) {
        return BlockPos.CODEC.decode(NbtOps.INSTANCE, itemStack.getTag().get(key)).getOrThrow(false, (string) -> {}).getFirst();
    }
}
