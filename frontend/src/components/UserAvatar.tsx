import { Image, View, Text } from "react-native";

type UserAvatarProps = {
  user?: { name?: string; username?: string; profileImageUrl?: string } | null;
  size: number;
  className?: string;
};

/**
 * Foto de perfil del usuario. Si no tiene, fondo claro con la inicial de su nombre
 * (igual que las listas sin portada).
 */
export default function UserAvatar({ user, size, className = '' }: UserAvatarProps) {
  if (user?.profileImageUrl) {
    return (
      <Image
        source={{ uri: user.profileImageUrl }}
        className={`rounded-full ${className}`}
        style={{ width: size, height: size }}
      />
    );
  }

  const initial = (user?.name || user?.username)?.charAt(0)?.toUpperCase() || '?';

  return (
    <View
      className={`rounded-full bg-secondary items-center justify-center ${className}`}
      style={{ width: size, height: size }}
    >
      <Text className="text-primary font-bold" style={{ fontSize: Math.round(size * 0.45) }}>
        {initial}
      </Text>
    </View>
  );
}
