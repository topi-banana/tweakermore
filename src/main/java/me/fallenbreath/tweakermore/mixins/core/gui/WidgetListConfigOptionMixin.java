package me.fallenbreath.tweakermore.mixins.core.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IHotkeyTogglable;
import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerButton;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ConfigButtonBoolean;
import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.*;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import me.fallenbreath.tweakermore.gui.HotkeyedBooleanResetListener;
import me.fallenbreath.tweakermore.gui.TranslatedOptionLabel;
import me.fallenbreath.tweakermore.gui.TweakermoreConfigGui;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(WidgetConfigOption.class)
public abstract class WidgetListConfigOptionMixin extends WidgetConfigOptionBase<GuiConfigsBase.ConfigOptionWrapper>
{
	@Shadow(remap = false) @Final protected IKeybindConfigGui host;

	public WidgetListConfigOptionMixin(int x, int y, int width, int height, WidgetListConfigOptionsBase<?, ?> parent, GuiConfigsBase.ConfigOptionWrapper entry, int listIndex)
	{
		super(x, y, width, height, parent, entry, listIndex);
	}

	private boolean isTweakerMoreConfigGui()
	{
		return this.parent instanceof WidgetListConfigOptions && ((WidgetListConfigOptionsAccessor)this.parent).getParent() instanceof TweakermoreConfigGui;
	}

	@ModifyArgs(
			method = "addConfigOption",
			at = @At(
					value = "INVOKE",
					target = "Lfi/dy/masa/malilib/gui/widgets/WidgetConfigOption;addLabel(IIIII[Ljava/lang/String;)V",
					remap = false
			),
			remap = false
	)
	private void notThisMethod(Args args)
	{
		if (isTweakerMoreConfigGui())
		{
			int x = args.get(0);
			int y = args.get(1);
			int width = args.get(2);
			int height = args.get(3);
			int textColor = args.get(4);
			String[] lines = args.get(5);

			args.set(5, null);  // cancel original call

			WidgetLabel label = new TranslatedOptionLabel(x, y, width, height, textColor, lines);
			this.addWidget(label);
		}
	}

	@Inject(
			method = "addConfigOption",
			at = @At(
					value = "FIELD",
					target = "Lfi/dy/masa/malilib/config/ConfigType;BOOLEAN:Lfi/dy/masa/malilib/config/ConfigType;",
					remap = false
			),
			remap = false,
			cancellable = true
	)
	private void tweakerMoreCustomConfigGui(int x, int y, float zLevel, int labelWidth, int configWidth, IConfigBase config, CallbackInfo ci)
	{
		if (this.isTweakerMoreConfigGui() && config instanceof IHotkeyTogglable)
		{
			this.addBooleanAndHotkeyWidgets(x, y, configWidth, (IHotkeyTogglable)config);
			ci.cancel();
		}
	}

	/**
	 * Stolen from malilib 1.18 v0.11.4
	 */
	private void addBooleanAndHotkeyWidgets(int x, int y, int configWidth, IHotkeyTogglable config)
	{
		IKeybind keybind = config.getKeybind();

		int booleanBtnWidth = 60;
		ConfigButtonBoolean booleanButton = new ConfigButtonBoolean(x, y, booleanBtnWidth, 20, config);
		x += booleanBtnWidth + 2;
		configWidth -= booleanBtnWidth + 2 + 22;

		ConfigButtonKeybind keybindButton = new ConfigButtonKeybind(x, y, configWidth, 20, keybind, this.host);
		x += configWidth + 2;

		this.addWidget(new WidgetKeybindSettings(x, y, 20, 20, keybind, config.getName(), this.parent, this.host.getDialogHandler()));
		x += 24;

		ButtonGeneric resetButton = this.createResetButton(x, y, config);

		ConfigOptionChangeListenerButton booleanChangeListener = new ConfigOptionChangeListenerButton(config, resetButton, null);
		HotkeyedBooleanResetListener resetListener = new HotkeyedBooleanResetListener(config, booleanButton, keybindButton, resetButton, this.host);

		this.host.addKeybindChangeListener(resetListener);

		this.addButton(booleanButton, booleanChangeListener);
		this.addButton(keybindButton, this.host.getButtonPressListener());
		this.addButton(resetButton, resetListener);
	}

	// some ocd alignment things xd

	@ModifyArg(
			method = "addConfigOption",
			at = @At(
					value = "INVOKE",
					target = "Lfi/dy/masa/malilib/gui/widgets/WidgetKeybindSettings;<init>(IIIILfi/dy/masa/malilib/hotkeys/IKeybind;Ljava/lang/String;Lfi/dy/masa/malilib/gui/widgets/WidgetListBase;Lfi/dy/masa/malilib/gui/interfaces/IDialogHandler;)V",
					remap = false
			),
			index = 0,
			remap = false
	)
	private int whyNotAlignTheHotkeyConfigButtonWidthWithOthers(int x)
	{
		if (isTweakerMoreConfigGui())
		{
			x += 1;
		}
		return x;
	}

	@ModifyArg(
			method = "addConfigOption",
			at = @At(
					value = "INVOKE",
					target = "Lfi/dy/masa/malilib/gui/button/ConfigButtonKeybind;<init>(IIIILfi/dy/masa/malilib/hotkeys/IKeybind;Lfi/dy/masa/malilib/gui/interfaces/IKeybindConfigGui;)V",
					remap = false
			),
			index = 2,
			remap = false
	)
	private int whyNotAlignTheHotkeySetterButtonWidthWithOthers(int width)
	{
		if (isTweakerMoreConfigGui())
		{
			width += 3;
		}
		return width;
	}
}