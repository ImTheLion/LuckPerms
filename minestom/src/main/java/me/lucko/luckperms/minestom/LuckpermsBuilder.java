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

import me.lucko.luckperms.common.plugin.bootstrap.LuckPermsBootstrap;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minestom.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public sealed interface LuckpermsBuilder permits LuckpermsBuilder.BuilderImpl {

    /**
     * Creates a LuckPerms builder, using which the luckperms plugin can be configured for use with Minestom.
     *
     * @param dataDirectory The LuckPerms working directory.
     * @return The LuckPerms builder.
     */
    static LuckpermsBuilder builder(Path dataDirectory) {
        return new BuilderImpl(dataDirectory.toAbsolutePath());
    }

    /**
     * Set the logger for this luckperms implementation
     *
     * @param logger the logger use.
     * @return this, for chaining
     */
    LuckpermsBuilder logger(Logger logger);

    /**
     * Enables the luckperms plugin.
     *
     * @return The enabled LuckPerms instance.
     */
    LuckPerms enable();

    final class BuilderImpl implements LuckpermsBuilder {

        // Let the user explicitly set the data directory for luckperms to use.
        // This makes sure the user knows where the configs are located.
        private final Path dataDirectory;
        private Logger logger = LoggerFactory.getLogger("LuckPerms");

        private BuilderImpl(Path dataDirectory) {
            this.dataDirectory = dataDirectory;
        }

        @Override
        public LuckpermsBuilder logger(Logger logger) {
            this.logger = logger;
            return this;
        }

        @Override
        public LuckPerms enable() {
            LPMinestomBootstrap bootstrap = new LPMinestomBootstrap(
                    this.logger,
                    this.dataDirectory
            );

            bootstrap.enable();
            MinecraftServer.getSchedulerManager().buildShutdownTask(bootstrap::disable);
            return LuckPermsProvider.get();
        }
    }

}
