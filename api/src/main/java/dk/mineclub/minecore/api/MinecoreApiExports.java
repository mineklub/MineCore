package dk.mineclub.minecore.api;

/**
 * Public API exports for the Minecore API module.
 *
 * <p>Services:
 *
 * <ul>
 *   <li>{@link dk.mineclub.minecore.api.service.MinecoreServerService}
 *   <li>{@link dk.mineclub.minecore.api.service.SocketGatewayService}
 * </ul>
 *
 * <p>DTOs (from common module):
 *
 * <ul>
 *   <li>{@link dk.mineclub.minecore.common.dto.CreateMinecoreRequestDto}
 *   <li>{@link dk.mineclub.minecore.common.dto.MinecoreRequestProductDto}
 *   <li>{@link dk.mineclub.minecore.common.dto.MinecoreRequestQuantityDto}
 * </ul>
 *
 * <p>Managers (from common module):
 *
 * <ul>
 *   <li>{@link dk.mineclub.minecore.common.manager.MinecoreRequestManager}
 *   <li>{@link dk.mineclub.minecore.common.socket.SocketIoClientManager}
 * </ul>
 */
public final class MinecoreApiExports {

    private MinecoreApiExports() {
        throw new AssertionError("Utility class");
    }
}
