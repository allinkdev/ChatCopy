package me.allink.chatcopy.mixin;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

	@Shadow protected abstract boolean isChatFocused();

	@Shadow public abstract double getChatScale();

	@Shadow public abstract int getVisibleLineCount();

	@Shadow @Final private List<ChatHudLine<OrderedText>> visibleMessages;

	@Shadow private int scrolledLines;

	@Shadow public abstract int getWidth();

	@Inject(method = "mouseClicked", at = @At("RETURN"), cancellable = true)
	public void mouseClicked(double x, double y, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue()) {
			final MinecraftClient client = MinecraftClient.getInstance();
			if (this.isChatFocused() && client.player != null) {

				if (InputUtil.isKeyPressed(client.getWindow().getHandle(),
					InputUtil.GLFW_KEY_LEFT_CONTROL)) {
					// skidded from mojang 😎
					double d = x - 2.0D;
					double e = (double) client.getWindow().getScaledHeight() - y - 40.0D;
					d = MathHelper.floor(d / this.getChatScale());
					e = MathHelper.floor(
						e / (this.getChatScale() * (client.options.chatLineSpacing + 1.0D)));
					if (!(d < 0.0D) && !(e < 0.0D)) {
						int i = Math.min(this.getVisibleLineCount(), this.visibleMessages.size());
						if (d <= (double) MathHelper.floor(
							(double) this.getWidth() / this.getChatScale())) {
							Objects.requireNonNull(client.textRenderer);
							if (e < (double) (9 * i + i)) {
								Objects.requireNonNull(client.textRenderer);
								int j = (int) (e / 9.0D + (double) this.scrolledLines);
								if (j >= 0 && j < this.visibleMessages.size()) {
									ChatHudLine<OrderedText> chatHudLine = this.visibleMessages.get(
										j);

									StringBuilder builder = new StringBuilder();
									chatHudLine.getText().accept((index, style, codePoint) -> {
										builder.append(Character.toChars(codePoint));
										return true;
									});

									String string = builder.toString();
									client.keyboard.setClipboard(string);
									client.player.sendSystemMessage(new LiteralText(String.format("Copied \"%s\" to your clipboard.", string.replaceAll("§", "&"))),
										Util.NIL_UUID);
								}
							}
						}
					}
				}
			}
		}
	}
}
