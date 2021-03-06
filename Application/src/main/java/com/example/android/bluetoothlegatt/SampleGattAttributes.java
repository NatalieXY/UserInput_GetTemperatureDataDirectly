/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.view.View;

import java.util.HashMap;
import java.util.List;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String TEMPERATURE_SERVICE = "F000AA00-0451-4000-B000-000000000000"; // M
    public static String TEMPERATURE_MEASUREMENT = "F000AA01-0451-4000-B000-000000000000"; // M
    public static String CTRL_Service = "F0001110-0451-4000-B000-000000000000"; // M
    public static String CTRL_1 = "F0001111-0451-4000-B000-000000000000"; // M
    public static String CTRL_2 = "F0001112-0451-4000-B000-000000000000"; // M
    public static String Trigger = "F0001113-0451-4000-B000-000000000000"; // M
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"; //
    public static String Device_Info_Service = "0000180a-0000-1000-8000-00805f9b34fb"; //
    public static String Manufacturer_Name_String = "00002a29-0000-1000-8000-00805f9b34fb"; //


    static {
        // Sample Services.
        attributes.put(TEMPERATURE_SERVICE, "Temperature Service"); // M
        attributes.put(Device_Info_Service, "Device Information Service");//-
        // Sample Characteristics.
        attributes.put(TEMPERATURE_MEASUREMENT, "Temperature Measurement");
        attributes.put(Manufacturer_Name_String, "Manufacturer Name String"); //-
        //CTRL Services
        attributes.put(CTRL_Service, "LED Service"); // M
        attributes.put(CTRL_1, "CTRL_1");
        attributes.put(CTRL_2, "CTRL_2");
        attributes.put(Trigger, "Trigger");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
