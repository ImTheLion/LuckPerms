package me.lucko.luckperms.minestom.util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.platform.PlayerAdapter;
import net.luckperms.api.util.Tristate;
import net.minestom.server.entity.Player;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

@NullMarked
public class LuckPermsPlayer extends Player {

    private final LuckPerms luckPerms;
    private final PlayerAdapter<Player> playerAdapter;

    public LuckPermsPlayer(
            PlayerConnection playerConnection,
            GameProfile gameProfile,
            LuckPerms luckPerms
    ) {
        super(playerConnection, gameProfile);
        this.luckPerms = luckPerms;
        this.playerAdapter = luckPerms.getPlayerAdapter(Player.class);
    }

    /**
     * Get the LuckPerms User for this player
     *
     * @return the LuckPerms {@link User}
     */
    private User getLuckPermsUser() {
        return this.playerAdapter.getUser(this);
    }

    /**
     * Gets the LuckPerms {@link CachedMetaData} for this player
     *
     * @return The Metadata
     */
    private CachedMetaData getLuckPermsMetaData() {
        return getLuckPermsUser().getCachedData().getMetaData();
    }

    /**
     * Adds a permission to the player.
     *
     * @param permission the permission to add
     * @return the result of the operation
     */
    public CompletableFuture<DataMutateResult> addPermission(String permission) {
        final User user = getLuckPermsUser();
        DataMutateResult result = user.data().add(Node.builder(permission).build());
        return this.luckPerms.getUserManager().saveUser(user).thenApply(ignored -> result);
    }

    /**
     * Sets a permission for the player. This method uses a {@link Node} rather than a permission name, this allows for
     * permissions that rely on context.
     *
     * @param permission the permission to set
     * @param value      the value of the permission
     * @return the result of the operation
     */
    public CompletableFuture<DataMutateResult> setPermission(@NotNull Node permission, boolean value) {
        User user = getLuckPermsUser();
        DataMutateResult result = value
                                  ? user.data().add(permission)
                                  : user.data().remove(permission);
        return this.luckPerms.getUserManager().saveUser(user).thenApply(ignored -> result);
    }

    /**
     * Removes a permission from the player.
     *
     * @param permissionName the name of the permission to remove
     */
    public CompletableFuture<DataMutateResult> removePermission(@NotNull String permissionName) {
        User user = getLuckPermsUser();
        DataMutateResult result = user.data().remove(Node.builder(permissionName).build());
        return this.luckPerms.getUserManager().saveUser(user).thenApply(ignored -> result);
    }

    /**
     * Checks if the player has a permission.
     *
     * @param permissionName the name of the permission to check
     * @return true if the player has the permission
     */
    public boolean hasPermission(String permissionName) {
        return getPermission(permissionName).asBoolean();
    }

    /**
     * Gets the value of a permission. This passes a {@link Tristate} value straight from LuckPerms, which may be a
     * better option than using boolean values in some cases.
     *
     * @param permissionName the name of the permission to check
     * @return the value of the permission
     */
    public Tristate getPermission(String permissionName) {
        User user = getLuckPermsUser();
        return user.getCachedData().getPermissionData().checkPermission(permissionName);
    }
}
