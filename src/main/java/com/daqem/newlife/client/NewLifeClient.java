package com.daqem.newlife.client;

import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NewLifeClient implements ClientModInitializer {

    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitializeClient() {
        LOGGER.info("NewLife Client Initialized!");
    }
}
