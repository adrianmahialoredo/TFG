import { View, Image } from "react-native";
import { useState } from "react";
import SlidePanel2 from "./SlidePanel2";

type Instrument = {
  id: string;
  name: string;
  icon: React.ReactNode;
};

type Lesson = {
  id: string;
    title: string;
};

  const exampleLessons: Lesson[] = [
    { id: '1', title: 'Acordes Básicos' },
    { id: '2', title: 'Ritmo y Compás' },
    { id: '3', title: 'Escalas Mayores' },
    { id: '4', title: 'Arpegios' },
    { id: '5', title: 'Acordes Básicos' },
    { id: '6', title: 'Ritmo y Compás' },
    { id: '7', title: 'Escalas Mayores' },
    { id: '8', title: 'Arpegios' },
  ];

export default function InstrumentGenre({ selectedInstrument }: { selectedInstrument: Instrument }) {
  const [selectedLesson, setSelectedLesson] = useState<Lesson>(exampleLessons[0]);

  return (
    <><View>
          <Image
              source={{ uri: 'https://i.ibb.co/wr4BSxk6/36ffe640309721-5790d08d50f7d.png' }}
              className="h-24 rounded-lg mb-9" />
      </View>
              <SlidePanel2 lessons={exampleLessons} onSelectLesson={setSelectedLesson} />
</>

  );
}