package com.kreidev.cmpackagecouriers.ponder;

import com.kreidev.cmpackagecouriers.PackageCouriers;
import com.kreidev.cmpackagecouriers.plane.CardboardPlaneEntity;
import com.kreidev.cmpackagecouriers.plane.CardboardPlaneItem;
import com.kreidev.cmpackagecouriers.plane.CardboardPlaneReg;
import com.kreidev.cmpackagecouriers.transmitter.LocationTransmitterReg;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.logistics.depot.EjectorBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.simibubi.create.foundation.ponder.element.BeltItemElement;
import com.simibubi.create.infrastructure.ponder.scenes.highLogistics.PonderHilo;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.ParticleEmitter;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PonderScenes implements PonderPlugin {
    @Override
    public @NotNull String getModId() {
        return PackageCouriers.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);
        HELPER.forComponents(CardboardPlaneReg.CARDBOARD_PLANE_PARTS_ITEM)
                .addStoryBoard("planes", PonderScenes::sendingPlanes)
                .addStoryBoard("planes", PonderScenes::receivingPlanes)
                .addStoryBoard("plane_other_crafting", PonderScenes::specialCraftingInteractions)
                .addStoryBoard("plane_address_override", PonderScenes::addressOverrides);
    }

    public static void sendingPlanes(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("sending_planes", "Sending Planes");
        scene.configureBasePlate(0,0, 5);
        scene.world().showSection(util.select().cuboid(util.grid().at(0,0,0), new Vec3i(4,0,4)), Direction.UP);
        scene.idle(10);

        List<BlockPos> reverseSpeedPos = new ArrayList<>();
        reverseSpeedPos.add(util.grid().at(1,1,2));
        reverseSpeedPos.add(util.grid().at(1,1,3));
        reverseSpeedPos.add(util.grid().at(3,1,3));
        reverseSpeedPos.add(util.grid().at(3,3,2));
        reverseSpeedPos.add(util.grid().at(3,3,3));
        reverseSpeedPos.add(util.grid().at(5,0,2));

        BlockPos deployerPos = util.grid().at(3,3,2);
        BlockPos ejectorPos = util.grid().at(1,1,2);
        BlockPos beltEndPos = util.grid().at(4,1,2);
        ItemStack packageItem = PackageStyles.getDefaultBox();
        ItemStack planeItem = CardboardPlaneItem.withPackage(packageItem);
        ItemStack planePartsItem = CardboardPlaneReg.CARDBOARD_PLANE_PARTS_ITEM.asStack();

        ElementLink<EntityElement> packageEntity = scene.world().createEntity(w -> {
            Vec3 p = util.vector().topOf(util.grid().at(2, 0, 2));
            PackageEntity entity = PackageEntity.fromItemStack(w, p, packageItem);
            entity.setYRot(120);
            entity.yRotO = 120;
            return entity;
        });
        scene.idle(12);

        scene.overlay().showText(40)
                .placeNearTarget()
                .pointAt(util.vector().topOf(util.grid().at(2, 0, 2)))
                .text("Package planes can be created from packages");
        scene.idle(50);

        scene.overlay().showControls(util.vector().topOf(util.grid().at(2,1,2)), Pointing.DOWN, 20)
                .rightClick()
                .withItem(planePartsItem);
        scene.idle(10);
        scene.world().modifyEntity(packageEntity, Entity::discard);
        ElementLink<EntityElement> itemEntity = scene.world().createEntity(w -> {
            Vec3 p = util.vector().topOf(util.grid().at(2, 0, 2));
            return new ItemEntity(w, p.x(), p.y(), p.z(), CardboardPlaneItem.withPackage(packageItem));
        });
        scene.idle(50);

        scene.world().modifyEntity(itemEntity, Entity::discard);
        scene.world().showSection(util.select().fromTo(0,1,0, 5, 4,4), Direction.DOWN);
        scene.world().showSection(util.select().position(util.grid().at(5,0,2)), Direction.DOWN);
        setRotationSpeed(scene, util, 16, reverseSpeedPos);
        scene.idle(10);
        scene.overlay().showText(60)
                .placeNearTarget()
                .attachKeyFrame()
                .text("... and can be automated using a deployer");
        scene.world().modifyBlockEntityNBT(
                util.select().position(util.grid().at(3,3,2)),
                DeployerBlockEntity.class,
                nbt -> nbt.put("HeldItem", planePartsItem.serializeNBT()));
        scene.idle(10);
        ElementLink<BeltItemElement> beltItem = scene.world().createItemOnBelt(beltEndPos, Direction.EAST, packageItem);
        scene.idle(50);
        scene.world().stallBeltItem(beltItem, true);

        scene.world().moveDeployer(deployerPos, 1, 15);
        scene.idle(15);
        scene.world().moveDeployer(deployerPos, -1, 15);
        scene.world().changeBeltItemTo(beltItem, planeItem);
        scene.idle(10);
        scene.world().stallBeltItem(beltItem, false);
        scene.idle(20);

        scene.overlay().showText(60)
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(ejectorPos.getCenter())
                .text("An ejector is needed to launch planes");
        scene.idle(35);

        ElementLink<EntityElement> planeEntity = launchPlane(scene, util, ejectorPos, packageItem, -30f, 90f);
        scene.idle(20);

        scene.world().modifyEntity(planeEntity, Entity::discard);
        scene.overlay().showText(80)
                .placeNearTarget()
                .attachKeyFrame()
                .text("Planes can also be launched by hand with right-click while holding the plane");
        scene.idle(40);
    }

    public static void receivingPlanes(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("receiving_planes", "Receiving Planes & Addresses");
        scene.configureBasePlate(0,0, 5);
        scene.removeShadow();

        List<BlockPos> reverseSpeedPos = new ArrayList<>();
        reverseSpeedPos.add(util.grid().at(1,1,2));
        reverseSpeedPos.add(util.grid().at(1,1,3));
        reverseSpeedPos.add(util.grid().at(3,1,3));
        reverseSpeedPos.add(util.grid().at(3,3,2));
        reverseSpeedPos.add(util.grid().at(3,3,3));
        reverseSpeedPos.add(util.grid().at(5,0,2));

        BlockPos deployerPos = util.grid().at(3,3,2);
        BlockPos ejectorPos = util.grid().at(1,1,2);
        BlockPos beltEndPos = util.grid().at(4,1,2);
        BlockPos depotPos = util.grid().at(8, 1,2);
        ItemStack packageItem = PackageStyles.getDefaultBox();
        ItemStack planeItem = CardboardPlaneItem.withPackage(packageItem);
        ItemStack planePartsItem = CardboardPlaneReg.CARDBOARD_PLANE_PARTS_ITEM.asStack();

        ElementLink<WorldSectionElement> mainSection = scene.world()
                .showIndependentSection(util.select().cuboid(util.grid().at(0,0,0), new Vec3i(5,3,4)), Direction.DOWN);
        setRotationSpeed(scene, util, 2, reverseSpeedPos);
        scene.world().modifyBlockEntityNBT(
                util.select().position(util.grid().at(3,3,2)),
                DeployerBlockEntity.class,
                nbt -> nbt.put("HeldItem", planePartsItem.serializeNBT()));
        scene.idle(10);

        scene.overlay().showText(80)
                .text("Planes will deliver to the player with their name matching to the package address");
        scene.idle(30);
        ElementLink<BeltItemElement> beltItem = scene.world().createItemOnBelt(beltEndPos, Direction.EAST, packageItem);
        scene.idle(10);

        scene.overlay().showText(30)
                .placeNearTarget()
                .pointAt(util.vector().topOf(beltEndPos))
                .colored(PonderPalette.OUTPUT)
                .text("→ John Create");
        scene.idle(30);

        setRotationSpeed(scene, util, 32, reverseSpeedPos);
        scene.idle(20);
        scene.world().stallBeltItem(beltItem, true);
        scene.world().moveDeployer(deployerPos, 1, 10);
        scene.idle(10);
        scene.world().removeItemsFromBelt(deployerPos.relative(Direction.DOWN, 2));
        beltItem = scene.world().createItemOnBelt(deployerPos.relative(Direction.DOWN, 2), Direction.UP, planeItem);
        scene.world().moveDeployer(deployerPos, -1, 10);
        scene.idle(7);
        scene.world().stallBeltItem(beltItem, false);
        scene.idle(35);

        ElementLink<EntityElement> planeEntity = launchPlane(scene, util, ejectorPos, packageItem, -30f, 90f);
        scene.world().hideIndependentSection(mainSection, Direction.EAST);
        scene.idle(30);

        scene.world().modifyEntity(planeEntity, Entity::discard);
        scene.overlay().showText(70)
                .text("The plane will skip unloaded chunks and can traverse through other dimensions");
        scene.idle(50);

        scene.addKeyframe();
        scene.idle(20);


        ElementLink<WorldSectionElement> playerSection = scene.world()
                .showIndependentSection(util.select().cuboid(util.grid().at(12,0,0), new Vec3i(4,0,4)), Direction.EAST);
        scene.world().moveSection(playerSection, util.vector().of(-12, 0, 0), 0);
        ElementLink<EntityElement> zombieEntity = scene.world().createEntity(w -> {
            Vec3 centerPos = util.vector().topOf(util.grid().at(2,0,2));
            Zombie zombie = new Zombie(w);
            zombie.setPos(centerPos);
            zombie.xo = centerPos.x();
            zombie.yo = centerPos.y();
            zombie.zo = centerPos.z();
            zombie.lookAt(EntityAnchorArgument.Anchor.FEET, util.vector().of(3.5,1.5,0));
            return zombie;
        });
        scene.idle(20);
        scene.overlay().showText(40)
                .pointAt(util.vector().of(2,2.5,2))
                .colored(PonderPalette.BLUE)
                .placeNearTarget()
                .text("John Create");
        scene.idle(20);

        scene.overlay().showText(60)
                .text("Pretend this is a player");
        scene.idle(60);

        scene.overlay()
                .showControls(util.vector().of(2, 2, 2), Pointing.UP, 90)
                .withItem(new ItemStack(LocationTransmitterReg.LOCATION_TRANSMITTER.get()));
        scene.overlay().showText(100)
                .pointAt(util.vector().of(2,2.5,2))
                .placeNearTarget()
                .text("Players are required to have a location transmitter in their inventory to be targeted by a plane");
        scene.idle(100);

        planeEntity = launchPlane(scene, util.vector().of(10,6.5,2), packageItem, 25f, 90f);
        scene.idle(19);

        ParticleEmitter cardboardParticleEmitter = scene.effects().particleEmitterWithinBlockSpace(
                new ItemParticleOption(ParticleTypes.ITEM, AllItems.CARDBOARD.asStack()),
                util.vector().of(0, 0, 0));
        scene.effects().emitParticles(util.vector().of(3,3,2), cardboardParticleEmitter, 20, 1);
        scene.world().modifyEntity(planeEntity, Entity::discard);
        scene.world().modifyEntity(zombieEntity, entity -> {
            if (entity instanceof Zombie zombie) {
                zombie.setItemInHand(InteractionHand.MAIN_HAND, packageItem);
            }
        });
        scene.idle(40);

        scene.world().hideIndependentSection(playerSection, Direction.WEST);
        scene.idle(10);
        scene.world().modifyEntity(zombieEntity, Entity::discard);
        scene.idle(20);


        mainSection = scene.world()
                .showIndependentSection(util.select().cuboid(util.grid().at(0,0,0), new Vec3i(5,3,4)), Direction.WEST);
        setRotationSpeed(scene, util, 2, reverseSpeedPos);
        scene.idle(10);

        scene.overlay().showText(80)
                .attachKeyFrame()
                .text("Planes can also deliver to depots with signs matching the package address");
        beltItem = scene.world().createItemOnBelt(beltEndPos, Direction.EAST, packageItem);
        scene.idle(10);

        scene.overlay().showText(30)
                .placeNearTarget()
                .pointAt(util.vector().topOf(beltEndPos))
                .colored(PonderPalette.OUTPUT)
                .text("→ Outpost");
        scene.idle(30);

        setRotationSpeed(scene, util, 32, reverseSpeedPos);
        scene.idle(20);
        scene.world().stallBeltItem(beltItem, true);
        scene.world().moveDeployer(deployerPos, 1, 10);
        scene.idle(10);
        scene.world().removeItemsFromBelt(deployerPos.relative(Direction.DOWN, 2));
        beltItem = scene.world().createItemOnBelt(deployerPos.relative(Direction.DOWN, 2), Direction.UP, planeItem);
        scene.world().moveDeployer(deployerPos, -1, 10);
        scene.idle(7);
        scene.world().stallBeltItem(beltItem, false);
        scene.idle(35);

        planeEntity = launchPlane(scene, util, ejectorPos, packageItem, -30f, 90f);
        scene.world().hideIndependentSection(mainSection, Direction.EAST);
        scene.idle(40);
        scene.world().modifyEntity(planeEntity, Entity::discard);


        ElementLink<WorldSectionElement> outpostSection = scene.world()
                .showIndependentSection(util.select().cuboid(util.grid().at(6,0,0), new Vec3i(4,1,4)), Direction.EAST);
        scene.world().moveSection(outpostSection, util.vector().of(-6, 0, 0), 0);
        scene.idle(10);

        scene.overlay().showText(60)
                .attachKeyFrame()
                .text("Destinations have to be chunkloaded for this to work");
        scene.idle(20);

        scene.overlay().showText(40)
                .pointAt(util.vector().centerOf(2,1,2))
                .colored(PonderPalette.BLUE)
                .text("Outpost");
        scene.idle(20);

        planeEntity = launchPlane(scene, util.vector().of(10,6.5,2), packageItem, 25f, 90f);
        scene.idle(19);

        cardboardParticleEmitter = scene.effects().particleEmitterWithinBlockSpace(
                new ItemParticleOption(ParticleTypes.ITEM, AllItems.CARDBOARD.asStack()),
                util.vector().of(0, 0, 0));
        scene.effects().emitParticles(util.vector().of(3,3,2), cardboardParticleEmitter, 20, 1);
        scene.world().modifyEntity(planeEntity, Entity::discard);
        scene.world().modifyBlockEntity(depotPos, DepotBlockEntity.class, depot -> depot.setHeldItem(packageItem));
        scene.idle(40);

        scene.world().hideIndependentSection(outpostSection, Direction.WEST);
        scene.idle(40);


        scene.world().showIndependentSection(util.select().cuboid(util.grid().at(0,0,0), new Vec3i(5,3,4)), Direction.WEST);
        scene.idle(10);

        scene.overlay().showText(100)
                .attachKeyFrame()
                .text("If there are no available targets, the plane will be ejected as an item as usual");
        beltItem = scene.world().createItemOnBelt(beltEndPos, Direction.EAST, packageItem);
        scene.idle(20);

        scene.world().stallBeltItem(beltItem, true);
        scene.world().moveDeployer(deployerPos, 1, 10);
        scene.idle(10);
        scene.world().removeItemsFromBelt(deployerPos.relative(Direction.DOWN, 2));
        beltItem = scene.world().createItemOnBelt(deployerPos.relative(Direction.DOWN, 2), Direction.UP, planeItem);
        scene.world().moveDeployer(deployerPos, -1, 10);
        scene.idle(7);
        scene.world().stallBeltItem(beltItem, false);
        scene.idle(35);

        scene.world().modifyBlockEntity(
                ejectorPos,
                EjectorBlockEntity.class,
                EjectorBlockEntity::activate);
        scene.idle(10);
    }

    public static void specialCraftingInteractions(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("special_crafting_interactions", "Special Crafting Interactions");
        scene.configureBasePlate(0,0, 5);
        scene.removeShadow();

        List<BlockPos> reverseSpeedPos = new ArrayList<>();
        reverseSpeedPos.add(util.grid().at(1,1,2));
        reverseSpeedPos.add(util.grid().at(1,1,3));
        reverseSpeedPos.add(util.grid().at(3,1,3));
        reverseSpeedPos.add(util.grid().at(3,3,2));
        reverseSpeedPos.add(util.grid().at(3,3,3));
        reverseSpeedPos.add(util.grid().at(5,0,2));
        reverseSpeedPos.add(util.grid().at(14,1,2));
        reverseSpeedPos.add(util.grid().at(14,1,3));
        reverseSpeedPos.add(util.grid().at(17,0,2));

        BlockPos sawBeltEndPos = util.grid().at(16,1,2);
        BlockPos deployerPos = util.grid().at(3,3,2);
        BlockPos ejectorPos = util.grid().at(1,1,2);
        BlockPos beltEndPos = util.grid().at(4,1,2);

        ItemStack packageItem = PackageStyles.getDefaultBox();
        ItemStack planeItem = CardboardPlaneItem.withPackage(packageItem.copy());
        ItemStack shearsItem = new ItemStack(Items.SHEARS);


        ElementLink<WorldSectionElement> sawSection = scene.world()
                .showIndependentSection(util.select().cuboid(util.grid().at(12,0,0), new Vec3i(5,3,4)), Direction.DOWN);
        scene.world().moveSection(sawSection, util.vector().of(-12, 0, 0), 0);
        setRotationSpeed(scene, util, 12, reverseSpeedPos);
        scene.idle(10);

        ElementLink<BeltItemElement> otherBeltItem = scene.world().createItemOnBelt(sawBeltEndPos, Direction.UP, planeItem.copy());
        scene.idle(10);

        scene.overlay().showText(50)
                .placeNearTarget()
                .pointAt(util.vector().topOf(util.grid().at(2,1,2)))
                .text("You can use a saw to cut off a plane's wings");
        scene.idle(100);

        scene.world().hideIndependentSection(sawSection, Direction.UP);
        scene.idle(20);


        setRotationSpeed(scene, util, 24, reverseSpeedPos);
        scene.world().modifyBlockEntityNBT(
                util.select().position(deployerPos),
                DeployerBlockEntity.class,
                nbt -> nbt.put("HeldItem", shearsItem.serializeNBT()));
        ElementLink<WorldSectionElement> mainSection = scene.world()
                .showIndependentSection(util.select().cuboid(util.grid().at(0,0,0), new Vec3i(5,3,4)), Direction.UP);
        scene.idle(20);

        scene.overlay().showControls(util.vector().centerOf(deployerPos.below()), Pointing.UP, 30)
                .withItem(shearsItem);
        scene.overlay().showText(50)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(deployerPos))
                .text("You can use shears in a deployer to pre-open a plane");
        scene.idle(50);

        ElementLink<BeltItemElement> beltItem = scene.world().createItemOnBelt(beltEndPos, Direction.EAST, planeItem.copy());
        scene.idle(30);

        scene.world().moveDeployer(deployerPos, 1, 15);
        scene.idle(15);
        scene.world().moveDeployer(deployerPos, -1, 15);
        scene.idle(5);
        scene.world().stallBeltItem(beltItem, false);
        scene.overlay().showText(100)
                .attachKeyFrame()
                .placeNearTarget()
                .text("pre-opened packages will get unpacked upon arrival");
        scene.idle(40);

        ElementLink<EntityElement> planeEntity = launchPlane(scene, util, ejectorPos, packageItem, -30f, 90f);
        scene.world().hideIndependentSection(mainSection, Direction.EAST);
        scene.idle(20);
        scene.world().modifyEntity(planeEntity, Entity::discard);


        ElementLink<WorldSectionElement> playerSection = scene.world()
                .showIndependentSection(util.select().cuboid(util.grid().at(12,0,0), new Vec3i(4,0,4)), Direction.EAST);
        scene.world().moveSection(playerSection, util.vector().of(-12, 0, 0), 0);
        ElementLink<EntityElement> zombieEntity = scene.world().createEntity(w -> {
            Vec3 centerPos = util.vector().topOf(util.grid().at(2,0,2));
            Zombie zombie = new Zombie(w);
            zombie.setPos(centerPos);
            zombie.xo = centerPos.x();
            zombie.yo = centerPos.y();
            zombie.zo = centerPos.z();
            zombie.lookAt(EntityAnchorArgument.Anchor.FEET, util.vector().of(3.5,1.5,0));
            return zombie;
        });
        scene.idle(20);

        planeEntity = launchPlane(scene, util.vector().of(10,6.5,2), packageItem, 25f, 90f);
        scene.idle(19);

        ParticleEmitter cardboardParticleEmitter = scene.effects().particleEmitterWithinBlockSpace(
                new ItemParticleOption(ParticleTypes.ITEM, AllItems.CARDBOARD.asStack()),
                util.vector().of(0, 0, 0));
        scene.effects().emitParticles(util.vector().of(3,3,2), cardboardParticleEmitter, 20, 1);
        scene.world().modifyEntity(planeEntity, Entity::discard);
        scene.world().modifyEntity(zombieEntity, entity -> {
            if (entity instanceof Zombie zombie) {
                zombie.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIRT));
            }
        });
        scene.idle(20);
    }

    public static void addressOverrides(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("address_interactions", "Additional Address Interactions");
        scene.configureBasePlate(0,0, 5);

        List<BlockPos> reverseSpeedPos = new ArrayList<>();
        reverseSpeedPos.add(util.grid().at(1,1,2));
        reverseSpeedPos.add(util.grid().at(1,1,3));
        reverseSpeedPos.add(util.grid().at(5,0,2));

        BlockPos ejectorPos = util.grid().at(1,1,2);
        BlockPos beltEndPos = util.grid().at(4,1,2);

        ItemStack packageItem = PackageStyles.getDefaultBox();
        ItemStack planeItem = CardboardPlaneItem.withPackage(packageItem.copy());

        ElementLink<WorldSectionElement> mainSection = scene.world()
                .showIndependentSection(util.select().cuboid(util.grid().at(0,0,0), new Vec3i(5,3,4)), Direction.DOWN);
        setRotationSpeed(scene, util, 24, reverseSpeedPos);
        scene.idle(10);

        scene.overlay().showText(70)
                .placeNearTarget()
                .text("Names inside \"<>\" will override the target address of the plane when it launches");
        scene.idle(60);

        ElementLink<BeltItemElement> beltItem = scene.world().createItemOnBelt(beltEndPos, Direction.EAST, planeItem);
        scene.idle(30);

        setRotationSpeed(scene, util, 2, reverseSpeedPos);
        scene.overlay().showText(30)
                .placeNearTarget()
                .pointAt(util.vector().topOf(util.grid().at(3,1,2)))
                .colored(PonderPalette.OUTPUT)
                .text("→ <Outpost> Farm");
        scene.idle(30);

        setRotationSpeed(scene, util, 24, reverseSpeedPos);
        scene.idle(35);

        ElementLink<EntityElement> planeEntity = launchPlane(scene, util, ejectorPos, packageItem, -30f, 90f);
        scene.overlay().showText(30)
                .placeNearTarget()
                .pointAt(util.vector().topOf(ejectorPos))
                .text("Going to \"Outpost\"");
        scene.idle(20);

        scene.world().modifyEntity(planeEntity, Entity::discard);
        scene.idle(20);

        scene.overlay().showText(60)
                .placeNearTarget()
                .attachKeyFrame()
                .text("This allows for a bit more flexibility when doing complex routing");
        scene.idle(60);

        scene.world().hideIndependentSection(mainSection, Direction.DOWN);
        scene.idle(20);


        ElementLink<WorldSectionElement> sellSection = scene.world()
                .showIndependentSection(util.select().cuboid(util.grid().at(6,0,0), new Vec3i(5,3,4)), Direction.DOWN);
        scene.world().moveSection(sellSection, util.vector().of(-6, 0, 0), 0);
        scene.idle(20);

        scene.overlay().showText(70)
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().topOf(util.grid().at(1,1,1)))
                .text("Table cloth addresses with \"<>\" would have those filled with the buyer's name");
        scene.idle(85);

        scene.overlay().showText(30)
                .placeNearTarget()
                .pointAt(util.vector().topOf(util.grid().at(1,1,1)))
                .colored(PonderPalette.OUTPUT)
                .text("→ Sold <>");
        scene.idle(45);

        scene.overlay().showControls(util.vector().topOf(util.grid().at(3,1,1)), Pointing.DOWN, 20)
                .rightClick()
                .withItem(AllItems.SHOPPING_LIST.asStack());
        scene.idle(7);
        scene.effects().indicateSuccess(util.grid().at(3, 1, 1));
        PonderHilo.linkEffect(scene, util.grid().at(1, 2, 4));
        PonderHilo.packagerCreate(scene, util.grid().at(7, 2, 3), PackageItem.containing(List.of()));
        scene.idle(20);

        scene.overlay().showText(30)
                .placeNearTarget()
                .pointAt(util.vector().centerOf(util.grid().at(1, 2, 3)))
                .colored(PonderPalette.OUTPUT)
                .text("→ Sold <John_Create>");
        scene.idle(30);
    }


    private static void setRotationSpeed(CreateSceneBuilder scene, SceneBuildingUtil util, int speed, List<BlockPos> reversedDirection) {
        scene.world().setKineticSpeed(util.select().everywhere(), speed);
        for (BlockPos pos : reversedDirection) {
            scene.world().setKineticSpeed(util.select().position(pos), -speed);
        }
    }

    private static ElementLink<EntityElement> launchPlane(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos ejectorPos, ItemStack box, float xRot, float yRot) {
        scene.world().modifyBlockEntity(
                ejectorPos,
                EjectorBlockEntity.class,
                be -> be.getBehaviour(DepotBehaviour.TYPE).setHeldItem(new TransportedItemStack(ItemStack.EMPTY)));
        scene.world().modifyBlockEntity(
                ejectorPos,
                EjectorBlockEntity.class,
                EjectorBlockEntity::activate);
        return launchPlane(scene, util.vector().topOf(ejectorPos), box, xRot, yRot);
    }

    private static ElementLink<EntityElement> launchPlane(CreateSceneBuilder scene, Vec3 spawnPos, ItemStack box, float xRot, float yRot) {
        return scene.world().createEntity(w -> {
            CardboardPlaneEntity plane = new CardboardPlaneEntity(w);
            plane.setPos(spawnPos);
            plane.setPackage(box);
            plane.setSpeedAndRot(0.4, xRot, yRot);
            return plane;
        });
    }
}