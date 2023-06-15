package com.eazy.daiku.utility.other;

import com.eazy.daiku.data.network.CustomHttpLogging;

import com.eazy.daiku.utility.Constants;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class LogDemoJson {
    public static CustomHttpLogging KessLogDataGson() {
        CustomHttpLogging customHttpLogging = new CustomHttpLogging(message -> {

            if (!message.startsWith("{")) {
                AppLOGG.INSTANCE.d(Constants.pretty_gson, message);
                return;
            }
            try {
                /* new JsonParser().parse(message) deprecated */
                JsonElement jsonElement = new JsonParser().parse(message);// JsonParser.parseString(message);
                String prettyPrintJson = new GsonBuilder().setPrettyPrinting().create().toJson(jsonElement);
                AppLOGG.INSTANCE.d(Constants.pretty_gson, prettyPrintJson);
            } catch (JsonSyntaxException m) {
                AppLOGG.INSTANCE.d(Constants.pretty_gson, message);
            }
        });
        customHttpLogging.setLevel(CustomHttpLogging.Level.BODY);
        return customHttpLogging;
    }
}
