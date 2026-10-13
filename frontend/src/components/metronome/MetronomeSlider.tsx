import Slider from "@react-native-community/slider";
import {COLORS} from "../../constants/Theme";
import {Text, View} from "react-native";
import React, {useEffect, useRef, useState} from "react";
import MetronomeTempoButton from "./MetronomeTempoButton";

export const MIN_TEMPO = 30;
export const MAX_TEMPO = 300;
export const DEFAULT_TEMPO = 120;

export const clampTempo = (value: number) => Math.min(MAX_TEMPO, Math.max(MIN_TEMPO, Math.round(value)));

type MetronomeSliderProps = {
    tempo: number;
    onTempoChange: (value: number) => void;
};

/**
 * Control del tempo: slider + botones de ajuste fino.
 * Es controlado (el tempo vive en la pantalla) para que el tap tempo también lo mueva.
 */
export default function MetronomeSlider({tempo, onTempoChange}: MetronomeSliderProps) {

    const updateTempo = (value: number) => onTempoChange(clampTempo(value));

    // Mientras se arrastra no se le vuelve a pasar `value` al slider nativo: si no, cada
    // onValueChange -> setTempo -> nuevo `value` pelea con el dedo y el slider da saltos.
    // Con el dedo fuera, sigue al tempo (botones, tap tempo).
    const [sliderValue, setSliderValue] = useState(tempo);
    const slidingRef = useRef(false);
    useEffect(() => {
        if (!slidingRef.current) setSliderValue(tempo);
    }, [tempo]);

  return (
      <View className="flex-col justify-center items-center">

          <View className="flex-row justify-between w-80">
              <Text className="text-secondary font-light text-lg">Tempo</Text>
              <Text className="text-secondary font-light text-lg">BPM {MIN_TEMPO}-{MAX_TEMPO}</Text>
          </View>

          <Slider
              style={{ width: 312, height: 20, }}
              minimumValue={MIN_TEMPO}
              maximumValue={MAX_TEMPO}
              value={sliderValue}
              step={1}
              minimumTrackTintColor= {COLORS.primary}
              maximumTrackTintColor= {COLORS.secondary}
              thumbTintColor= {COLORS.secondary}
              onSlidingStart={() => { slidingRef.current = true; }}
              onValueChange={updateTempo}
              onSlidingComplete={(value) => {
                  slidingRef.current = false;
                  updateTempo(value);
                  setSliderValue(clampTempo(value));
              }}
          />

          <View className="">
              <Text className="text-primary font-extrabold text-[70px]">{tempo} BPM</Text>
          </View>

          <View className="mt-1 flex-row justify-between w-80 gap-3">
              <MetronomeTempoButton title={"-10"} onPress={() => updateTempo(tempo - 10)} />
              <MetronomeTempoButton title={"-1"} onPress={() => updateTempo(tempo - 1)} />
              <MetronomeTempoButton title={"+1"} onPress={() => updateTempo(tempo + 1)} />
              <MetronomeTempoButton title={"+10"} onPress={() => updateTempo(tempo + 10)} />
          </View>

      </View>
  );
}
