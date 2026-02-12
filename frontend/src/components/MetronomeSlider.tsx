import Slider from "@react-native-community/slider";
import {COLORS} from "../constants/Theme";
import {Text, View} from "react-native";
import React from "react";

export default function MetronomeSlider() {
  return (
      <View className="flex-col justify-center items-center">

          <View className="flex-row justify-between w-80">
              <Text className="text-secondary font-semibold text-lg">Tempo</Text>
              <Text className="text-secondary font-semibold text-lg">BPM 30-300</Text>
          </View>

          <Slider
              style={{ width: 300, height: 30, }}
              minimumValue={30}
              maximumValue={300}
              minimumTrackTintColor= {COLORS.primary}
              maximumTrackTintColor= {COLORS.secondary}
              thumbTintColor= {COLORS.secondary}
          />
      </View>
  );
}