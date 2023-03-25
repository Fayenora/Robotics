@EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RobotBehavior {

    @SubscribeEvent
    public static void onRobotRightClick(EntityInteractSpecific event) {
        if(event.getPlayer().level.isClientSide()) return;
        //TODO Open robot screen
    }
}
