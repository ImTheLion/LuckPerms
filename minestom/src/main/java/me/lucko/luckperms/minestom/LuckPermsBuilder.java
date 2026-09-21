/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.luckperms.minestom;

import me.lucko.luckperms.minestom.util.LuckPermsPlayer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minestom.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public sealed interface LuckPermsBuilder permits LuckPermsBuilder.BuilderImpl {

    /**
     * Creates a LuckPerms builder, using which the luckperms plugin can be configured for use with Minestom.
     *
     * @param dataDirectory The LuckPerms working directory.
     * @return The LuckPerms builder.
     */
    static LuckPermsBuilder builder(Path dataDirectory) {
        return new BuilderImpl(dataDirectory.toAbsolutePath());
    }

    /**
     * Set the logger for this luckperms implementation
     *
     * @param logger the logger use.
     * @return this, for chaining
     */
    LuckPermsBuilder logger(Logger logger);

    /**
     * Sets whether to use a built-in {@link net.minestom.server.network.PlayerProvider}, to replace the standard
     * {@link net.minestom.server.entity.Player} with {@link LuckPermsPlayer}. This is recommended for ease of use
     * when you are not registering a custom player class yourself. If you are implementing one, you may choose to
     * copy the methods to your own implementation, or extend the class.
     */
    LuckPermsBuilder usePlayerProvider(boolean use);

    /**
     * Enables the luckperms plugin.
     *
     * @return The enabled LuckPerms instance.
     */
    LuckPerms enable();

    final class BuilderImpl implements LuckPermsBuilder {

        // Let the user explicitly set the data directory for luckperms to use.
        // This makes sure the user knows where the configs are located.
        private final Path dataDirectory;
        private Logger logger = LoggerFactory.getLogger("LuckPerms");
        private boolean usePlayerProvider = false;

        private BuilderImpl(Path dataDirectory) {
            this.dataDirectory = dataDirectory;
        }

        @Override
        public LuckPermsBuilder logger(Logger logger) {
            this.logger = logger;
            return this;
        }

        @Override
        public LuckPermsBuilder usePlayerProvider(boolean use) {
            this.usePlayerProvider = use;
            return this;
        }

        @Override
        public LuckPerms enable() {
            LPMinestomBootstrap bootstrap = new LPMinestomBootstrap(
                    this.logger,
                    this.usePlayerProvider,
                    this.dataDirectory
            );

            bootstrap.enable();
            MinecraftServer.getSchedulerManager().buildShutdownTask(bootstrap::disable);
            return LuckPermsProvider.get();
        }
    }

}
