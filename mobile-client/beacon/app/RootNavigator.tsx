import {Stack} from "expo-router";
import {useAuth} from "@/app/context/AuthContext";

export default function RootNavigator() {
    const {isLoggedIn} = useAuth();

    return (
        <Stack screenOptions={{
            headerShown: false,
            gestureEnabled: true,
            fullScreenGestureEnabled: true,
            gestureDirection: 'horizontal',
        }}>
            <Stack.Protected guard={!isLoggedIn}>
                <Stack.Screen name="auth"/>
            </Stack.Protected>

            <Stack.Protected guard={isLoggedIn}>
                <Stack.Screen name="private"/>
            </Stack.Protected>
        </Stack>
    )
}