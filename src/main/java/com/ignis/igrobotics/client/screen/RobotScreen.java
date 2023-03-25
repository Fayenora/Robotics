public class RobotScreen extends BaseContainerScreen<RobotMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Robotics.MODID, "textures/gui/robot.png");

	private EntityRobot robot;
	private boolean hasActivePotionEffects;

    public RobotScreen(RobotMenu menu, Inventory inv, Component comp) {
        super(menu, inv, comp);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new EnergyBarElement(leftPos + 155, topPos + 7, 71, () -> menu.data.get(1), () -> menu.data.get(2)));
		updateActivePotionEffects();
    }

    @Override
    protected void renderBg(PoseStack poseStack, float p_97788_, int p_97789_, int p_97790_) {
		if(robot == null) return;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        RenderSystem.setShaderTexture(0, Reference.MISC);
        robot.getCapability(ModCapabilities.PARTS).ifPresent(parts -> {
                if(parts.hasBodyPart(EnumRobotPart.RIGHT_ARM)) {
                    blit(poseStack, leftPos + 76, topPos + 43, 238, 0, 18, 18);
                }
                if(parts.hasBodyPart(EnumRobotPart.LEFT_ARM)) {
                    blit(poseStack, leftPos + 76, topPos + 61, 238, 0, 18, 18);
                }
            });
        //TODO Draw robot in black window

		this.drawHealthBar(poseStack, 7, 81, Math.round(robot.getHealth()), Math.round(robot.getMaxHealth()));
		this.drawArmor(poseStack, 89, 81, robot.getTotalArmorValue());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);

		if(robot == null) return;
		updateActivePotionEffects();
		if(hasActivePotionEffects) {
			drawActivePotionEffects(robot.getActivePotionEffects());
		}

        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack p_97808_, int p_97809_, int p_97810_) {
        //Don't
    }

	protected void updateActivePotionEffects() {
        boolean hasVisibleEffect = false;
        if(robot == null) return;
        for(PotionEffect potioneffect : robot.getActivePotionEffects()) {
            Potion potion = potioneffect.getPotion();
            if(potion.shouldRender(potioneffect)) { hasVisibleEffect = true; break; }
        }
        if (robot.getActivePotionEffects().isEmpty() || !hasVisibleEffect)
        {
            this.guiLeft = (this.width - this.xSize) / 2;
            this.hasActivePotionEffects = false;
        }
        else
        {
            if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.PotionShiftEvent(this))) this.guiLeft = (this.width - this.xSize) / 2; else
            this.guiLeft = 160 + (this.width - this.xSize - 200) / 2;
            this.hasActivePotionEffects = true;
        }
    }

	private void drawActivePotionEffects(Collection<PotionEffect> collection) {
        //TODO Look at Player Screen!
        int i = this.guiLeft - 124;
        int j = this.guiTop;

        if (!collection.isEmpty())
        {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableLighting();
            int l = 33;

            if (collection.size() > 5)
            {
                l = 132 / (collection.size() - 1);
            }

            for (PotionEffect potioneffect : Ordering.natural().sortedCopy(collection))
            {
                Potion potion = potioneffect.getPotion();
                if(!potion.shouldRender(potioneffect)) continue;
                if(potioneffect.getDuration() <= 0) continue;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.mc.getTextureManager().bindTexture(INVENTORY_BACKGROUND);
                this.drawTexturedModalRect(i, j, 0, 166, 140, 32);

                if (potion.hasStatusIcon())
                {
                    int i1 = potion.getStatusIconIndex();
                    this.drawTexturedModalRect(i + 6, j + 7, 0 + i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18);
                }

                potion.renderInventoryEffect(i, j, potioneffect, mc);
                if (!potion.shouldRenderInvText(potioneffect)) { j += l; continue; }
                String s1 = I18n.format(potion.getName());

                if (potioneffect.getAmplifier() == 1)
                {
                    s1 = s1 + " " + I18n.format("enchantment.level.2");
                }
                else if (potioneffect.getAmplifier() == 2)
                {
                    s1 = s1 + " " + I18n.format("enchantment.level.3");
                }
                else if (potioneffect.getAmplifier() == 3)
                {
                    s1 = s1 + " " + I18n.format("enchantment.level.4");
                }

                this.fontRenderer.drawStringWithShadow(s1, i + 10 + 18, j + 6, 16777215);
                String s = Potion.getPotionDurationString(potioneffect, 1.0F);
                this.fontRenderer.drawStringWithShadow(s, i + 10 + 18, j + 6 + 10, 8355711);
                j += l;
            }
        }
    }

	public void drawHealthBar(PoseStack poseStack, int x, int y, int health, int maxhealth) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, Reference.ICONS);

		for(int i = 0; i < maxhealth / 2; i++) {
			blit(poseStack, this.guiLeft + x + i * 8, this.guiTop + y, 16, 0, 9, 9);
		}
		for(int i = 0; i < health / 2; i++) {
			blit(poseStack, this.guiLeft + x + i * 8, this.guiTop + y, 52, 0, 9, 9);
		}

		if(maxhealth % 2 == 1) {
            RenderSystem.setShaderTexture(0, Reference.MISC);
			blit(poseStack, this.guiLeft + x + (maxhealth - 1)/2 * 8, this.guiTop + y, 250, 18, 6, 9);
            RenderSystem.setShaderTexture(0, Reference.ICONS);
		}
		if(health % 2 == 1) {
			blit(poseStack, this.guiLeft + x + (health - 1)/2 * 8, this.guiTop + y, 52, 0, 5, 9);
		}
	}

	public void drawArmor(PoseStack poseStack, int x, int y, int armor) {
		if(armor == 0) return;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, Reference.ICONS);

		for(int i = 0; i < 10; i++) {
			blit(poseStack, this.guiLeft + x + i * 8, this.guiTop + y, 16, 9, 9, 9);
		}
		for(int i = 0; i < armor / 2; i++) {
			blit(poseStack, this.guiLeft + x + i * 8, this.guiTop + y, 34, 9, 9, 9);
		}
		if(armor % 2 == 1) {
			blit(poseStack, this.guiLeft + x + ((armor - 1)/ 2) * 8, this.guiTop + y, 25, 9, 9, 9);
		}
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		NetworkHandler.sendToServer(new PacketSetWatched(robot, false));
	}
}
