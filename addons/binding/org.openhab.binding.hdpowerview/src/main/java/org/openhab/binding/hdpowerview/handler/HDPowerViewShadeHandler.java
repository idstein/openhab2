package org.openhab.binding.hdpowerview.handler;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.hdpowerview.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.config.HDPowerViewShadeConfiguration;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades.Shade;

/**
 * Handles commands for an HD Power View shade
 *
 * @author Andy Lintner
 */
public class HDPowerViewShadeHandler extends AbstractHubbedThingHandler {

    private static final int MAX_POSITION = 65535;
    private static final int MAX_VANE = 32767;

    public HDPowerViewShadeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        getBridgeHandler().pollNow();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case HDPowerViewBindingConstants.CHANNEL_SHADE_POSITION:
                if (command instanceof PercentType) {
                    setPosition(((PercentType) command).intValue());
                } else if (command instanceof OnOffType) {
                    setPosition(
                            ShadePosition.forPosition(((OnOffType) command).equals(OnOffType.ON) ? MAX_POSITION : 0));
                }
                break;

            case HDPowerViewBindingConstants.CHANNEL_SHADE_VANE:
                if (command instanceof PercentType) {
                    setVane(((PercentType) command).intValue());
                } else if (command instanceof OnOffType) {
                    setPosition(ShadePosition.forVane(((OnOffType) command).equals(OnOffType.ON) ? MAX_VANE : 0));
                }
                break;
        }
    }

    void onReceiveUpdate(Shade shade) {
        updatePosition(shade.positions);
        updateState(HDPowerViewBindingConstants.CHANNEL_SHADE_BATTERY_LOW,
                shade.batteryIsLow ? OnOffType.ON : OnOffType.OFF);
        updateStatus(ThingStatus.ONLINE);
    }

    private void updatePosition(ShadePosition pos) {
        if (pos != null) {
            updateState(HDPowerViewBindingConstants.CHANNEL_SHADE_POSITION,
                    new PercentType((int) Math.round(((double) pos.getPosition()) / MAX_POSITION * 100)));
            updateState(HDPowerViewBindingConstants.CHANNEL_SHADE_VANE,
                    new PercentType((int) Math.round(((double) pos.getVane()) / MAX_VANE * 100)));
        } else {
            updateState(HDPowerViewBindingConstants.CHANNEL_SHADE_POSITION, UnDefType.UNDEF);
            updateState(HDPowerViewBindingConstants.CHANNEL_SHADE_VANE, UnDefType.UNDEF);
        }
    }

    private void setPosition(int percent) {
        ShadePosition position = ShadePosition.forPosition((int) Math.round(percent / 100d * MAX_POSITION));
        setPosition(position);
    }

    private void setVane(int value) {
        ShadePosition position = ShadePosition.forVane((int) Math.round(value / 100d * MAX_VANE));
        setPosition(position);
    }

    private void setPosition(ShadePosition position) {
        HDPowerViewHubHandler bridge;
        if ((bridge = getBridgeHandler()) == null) {
            return;
        }
        int shadeId = getShadeId();
        Response response;
        try {
            response = bridge.getWebTargets().moveShade(shadeId, position);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return;
        }
        if (response != null) {
            updatePosition(position);
        }
    }

    private int getShadeId() {
        return getConfigAs(HDPowerViewShadeConfiguration.class).id;
    }

}