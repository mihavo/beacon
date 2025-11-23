import React, {useEffect, useRef} from "react";
import {ActivityIndicator, Animated, StyleSheet, useColorScheme, View,} from "react-native";
import {useRouter} from "expo-router";
import {Ionicons} from "@expo/vector-icons";
import {useAuth} from "@/app/context/AuthContext";

export default function Index() {
    const router = useRouter();
    const {isLoggedIn, isReady} = useAuth();

    const colorScheme = useColorScheme();
    const isDark = colorScheme === 'dark';
    const scale = useRef(new Animated.Value(0)).current;
    const opacity = useRef(new Animated.Value(0)).current;

    const primaryColor = isDark ? "#0a84ff" : "#007aff"; // A nice blue for dark/light
    const backgroundColor = isDark ? "#000000" : "#f2f2f7";
    const textColor = isDark ? "#ffffff" : "#1c1c1e";

    useEffect(() => {
        Animated.sequence([
            Animated.parallel([
                Animated.spring(scale, {
                    toValue: 1,
                    friction: 4,
                    tension: 40,
                    useNativeDriver: true,
                }),
                Animated.timing(opacity, {
                    toValue: 1,
                    duration: 800,
                    useNativeDriver: true,
                }),
            ]),
            Animated.delay(800), // show splash a bit longer
        ]).start(() => {
            if (isReady) {
                router.replace(isLoggedIn ? "/private/maps" : "/auth/login");
            }
        });
    }, [isReady, isLoggedIn, scale, opacity, router]); // Added dependencies

    return (
        <View style={[styles.container, {backgroundColor: backgroundColor}]}>
            <Animated.View style={{ transform: [{ scale }] }}>
                <Ionicons
                    name="radio-outline"
                    size={120}
                    color={primaryColor}
                />
            </Animated.View>
            <Animated.Text style={[
                styles.title,
                {opacity, color: textColor} // 4. Apply dynamic text color
            ]}>
                Beacon
            </Animated.Text>

            {isReady && (
                <View style={{marginTop: 30}}>
                    <ActivityIndicator
                        size="large"
                        color={primaryColor}
                    />
                </View>
            )}
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
    },
    title: {
        marginTop: 20,
        fontSize: 32,
        fontWeight: "700",
    },
});