@EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RobotBehavior {

    @SubscribeEvent
    public static void onRobotRightClick(EntityInteractSpecific event) {
        if(event.getPlayer().level.isClientSide()) return;
        if(!event.getTarget.getCapability(ModCapabilities.ROBOT).isPresent()) return;
        NetworkHooks.openScreen(event.getPlayer(), new SimpleMenuProvider((id, playerInv, player) -> new RobotMenu(id, playerInv, Lang.localise("container.robot"))));
    }
}
