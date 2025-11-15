import React, {useEffect, useRef} from "react";
import {Animated, StyleSheet, View} from "react-native";
import {useRouter} from "expo-router";
import {Ionicons} from "@expo/vector-icons";

export default function Index() {
    const router = useRouter();

    // Replace with real login check
    const isLoggedIn = false;

    // Animation values
    const scale = useRef(new Animated.Value(0)).current;
    const opacity = useRef(new Animated.Value(0)).current;

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
            Animated.delay(1000), // show splash a bit longer
        ]).start(() => {
            // Navigate after animation
            router.replace(isLoggedIn ? "/maps" : "/auth/login");
        });
    }, []);

    return (
        <View style={styles.container}>
            <Animated.View style={{ transform: [{ scale }] }}>
                <Ionicons name="radio-outline" size={120} color="#007aff" />
            </Animated.View>
            <Animated.Text style={[styles.title, { opacity }]}>
                Beacon
            </Animated.Text>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: "#f2f2f7",
        justifyContent: "center",
        alignItems: "center",
    },
    title: {
        marginTop: 20,
        fontSize: 32,
        fontWeight: "700",
        color: "#1c1c1e",
    },
});
