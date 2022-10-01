/*
 * Copyright (c) 2022 HDB
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package com.baehyeonwoo.shareadvancement.plugin.commands

import com.baehyeonwoo.shareadvancement.plugin.objects.ShareAdvancementObject.isRunning
import com.baehyeonwoo.shareadvancement.plugin.objects.ShareAdvancementObject.plugin
import com.baehyeonwoo.shareadvancement.plugin.objects.ShareAdvancementObject.start
import com.baehyeonwoo.shareadvancement.plugin.objects.ShareAdvancementObject.stop
import io.github.monun.kommand.node.LiteralNode

/**
 * @author Komworld Dev Team
 */

object ShareAdvancementKommand {
    fun register(builder: LiteralNode) {
        builder.apply {
            executes {
                if (!isRunning) {
                    plugin.logger.info("Let's destruct! Let's destruct the whole thing...")
                    start()
                }
                else {
                    plugin.logger.info("Game stopped!")
                    stop()
                }
            }
        }
    }
}