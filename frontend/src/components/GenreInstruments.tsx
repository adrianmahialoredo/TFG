import { Scroll } from "lucide-react-native";
import React, { useState } from "react";
import { View, ScrollView, Text } from "react-native";
import LessonsCarousel from "./carousels/LessonsCarousel";
import { ToolButton } from "./buttons/ToolButton";
import { Guitar, Keyboard, Drum} from 'lucide-react-native';
import SlidePanel from "./SlidePanel";
import InstrumentGenre from "./InstrumentGenre";

const recommendations = [
  { id: '1', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '2', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '3', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '4', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '5', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
  { id: '6', title: 'Canción recomendada', imgUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/e/e8/Taylor_Swift_-_Red.png/250px-Taylor_Swift_-_Red.png' },
];

const instruments = [
  { id: '1', name: 'Guitarra', icon: <Guitar size={85} color="white" strokeWidth={1} /> },
  { id: '2', name: 'Piano', icon: <Keyboard size={85} color="white" strokeWidth={1}/> },
  { id: '3', name: 'Batería', icon: <Drum size={85} color="white" strokeWidth={1}/> },
  { id: '4', name: 'Bajo', icon: <Guitar size={85} color="white" strokeWidth={1}/> },
  // más instrumentos
];

type Instrument = {
  id: string;
  name: string;
  icon: React.ReactNode;
};

type Lesson = {
  id: string;
  title: string;
};

function chunkArray<T>(arr: T[], size: number): T[][] {
  const chunks: T[][] = [];
  for (let i = 0; i < arr.length; i += size) {
    chunks.push(arr.slice(i, i + size));
  }
  return chunks;
}

export default function GenreInstruments({ description }: { description: string }) {

  const [selectedInstrument, setSelectedInstrument] = useState<Instrument | null>(null);

  const exampleLessons: Lesson[] = [
    { id: '1', title: 'Acordes Básicos' },
    { id: '2', title: 'Ritmo y Compás' },
    { id: '3', title: 'Escalas Mayores' },
    { id: '4', title: 'Arpegios' },
  ];
  
  const exampleInstrument: Instrument = {
    id: '1',
    name: 'Guitarra',
    icon: <Guitar size={85} color="white" strokeWidth={1} />,
  };

  if (selectedInstrument) {

    return (
      <InstrumentGenre selectedInstrument={exampleInstrument}/>
    );
  }

  return (
    <ScrollView className="bg-background py-4" contentContainerStyle={{ paddingBottom: 120 }}>
      <View className="px-4 mt-10 mb-6">
        {chunkArray(instruments, 2).map((pair, rowIndex) => (
          <View key={rowIndex} className="flex-row justify-center gap-4 mb-4">
            {pair.map((instrument: Instrument) => (
              <ToolButton
                key={instrument.id}
                label={instrument.name}
                icon={instrument.icon}
                onPress={() => setSelectedInstrument(instrument)}
              />
            ))}
          </View>
        ))}
      </View>
      <LessonsCarousel title="Recomendado" data={recommendations} />
    </ScrollView>
  );
}