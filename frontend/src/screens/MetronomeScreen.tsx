import ProfileHeader from "../components/ProfileHeader";
import AppLayout from "../components/AppLayout";
import {View} from "react-native";
import Slider from "@react-native-community/slider";
import {COLORS} from "../constants/Theme";
import React from "react";
import MetronomeSlider from "../components/MetronomeSlider";

export default function MetronomeScreen() {

  return (
      <AppLayout>
          <View className = "bg-background flex-1 pt-2">
              <ProfileHeader title={"Metrónomo"} />
              <MetronomeSlider />

          </View>
      </AppLayout>
  );
}