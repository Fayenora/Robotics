@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    @SubscribeEvent
    public static class onEntitySpawn(EntityJoinWorldEvent event) {
        if(event.getLevel().isClientSide()) return;
		if(!(event.getEntity() instanceof Mob mob)) return;
        if(mob.isUndead()) {
            mob.goalSelector.addGoal(3, new AvoidEntityGoal(mob, Entity.class, entity ->
                        entity.getCapability(ModCapabilities.PERK_MAP).orElse(ModCapabilities.DEFAULT_PERK_MAP).contains(RoboticsConfig.current().perks.PERK_LUMINOUS)
            ))
        }
    }
}
