import ProfileHeader from "../components/ProfileHeader";
import AppLayout from "../components/AppLayout";
import {Text, TouchableOpacity, View} from "react-native";
import Slider from "@react-native-community/slider";
import {COLORS} from "../constants/Theme";
import React, {useState} from "react";
import MetronomeSlider from "../components/MetronomeSlider";

export default function MetronomeScreen() {
    const [tempo, setTempo] = useState(120);
    return (
        <AppLayout>
            <View className = "bg-background flex-1 pt-2">
                <ProfileHeader title={"Metrónomo"} />
                <MetronomeSlider onTempoChange={setTempo} />
                <View className="flex-column mt-10 gap-1">
                    <TouchableOpacity className="bg-secondary w-80 py-4 rounded-md self-center">
                        <Text className="text-primary text-3xl self-center">TAP TEMPO</Text>
                    </TouchableOpacity>
                    <TouchableOpacity className="bg-secondary w-80 py-5 rounded-md self-center">
                        <Text className="text-primary text-lg text-bold self-center">Hi-Hat</Text>
                    </TouchableOpacity>
                </View>
            </View>
        </AppLayout>
    );
}