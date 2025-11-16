import {DarkTheme, DefaultTheme, ThemeProvider,} from "@react-navigation/native";
import {StatusBar} from "expo-status-bar";
import "react-native-reanimated";

import {useColorScheme} from "@/hooks/use-color-scheme";
import AuthProvider from "@/app/context/AuthContext";
import RootNavigator from "@/app/RootNavigator";
import SplashController from "@/app/splash";

export default function RootLayout() {
  const colorScheme = useColorScheme();

    return (
    <ThemeProvider value={colorScheme === "dark" ? DarkTheme : DefaultTheme}>
        <AuthProvider>
            <SplashController/>
            <RootNavigator/>
        </AuthProvider>
      <StatusBar style="auto" />
    </ThemeProvider>
  );
}
