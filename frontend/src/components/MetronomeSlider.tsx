import Slider from "@react-native-community/slider";
import {COLORS} from "../constants/Theme";
import {Text, View} from "react-native";
import React, {useState} from "react";
import MetronomeTempoButton from "./buttons/MetronomeTempoButton";

const minTempo = 30;
const maxTempo = 300;
const defaultTempo = 120;

type MetronomeSliderProps = {
    onTempoChange?: (value: number) => void;
};

export default function MetronomeSlider( {onTempoChange} : MetronomeSliderProps) {

    const [tempo, setTempo] = useState(defaultTempo);

    const updateTempo = (value: number) => {
        const nextTempo = Math.min(maxTempo, Math.max(minTempo, value));
        setTempo(nextTempo);
        onTempoChange?.(nextTempo);
    }

    const handleTempoChangeByButton = (value: number) => {
        updateTempo(tempo + value);
    }

    const handleTempoChangeBySlider = (value: number) => {
        updateTempo(value);
    }

  return (
      <View className="flex-col justify-center items-center">

          <View className="flex-row justify-between w-80">
              <Text className="text-secondary font-light text-lg">Tempo</Text>
              <Text className="text-secondary font-light text-lg">BPM {minTempo}-{maxTempo}</Text>
          </View>

          <Slider
              style={{ width: 312, height: 20, }}
              minimumValue={minTempo}
              maximumValue={maxTempo}
              value={defaultTempo}
              step={1}
              minimumTrackTintColor= {COLORS.primary}
              maximumTrackTintColor= {COLORS.secondary}
              thumbTintColor= {COLORS.secondary}
              onValueChange={handleTempoChangeBySlider}
          />

          <View className="">
              <Text className="text-primary font-extrabold text-[70px]">{tempo} BPM</Text>
          </View>

          <View className="mt-1 flex-row justify-between w-80 gap-3">
              <MetronomeTempoButton title={"-10"} onPress={() => handleTempoChangeByButton(-10)} />
              <MetronomeTempoButton title={"-1"} onPress={() => handleTempoChangeByButton(-1)} />
              <MetronomeTempoButton title={"+1"} onPress={() => handleTempoChangeByButton(1)} />
              <MetronomeTempoButton title={"+10"} onPress={() => handleTempoChangeByButton(10)} />
          </View>



      </View>
  );
}