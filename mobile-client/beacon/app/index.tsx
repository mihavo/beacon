import React, {useEffect, useRef} from "react";
import {ActivityIndicator, Animated, StyleSheet, View} from "react-native";
import {useRouter} from "expo-router";
import {Ionicons} from "@expo/vector-icons";
import {useAuth} from "@/app/context/AuthContext";

export default function Index() {
    const router = useRouter();
    const {isLoggedIn, isReady} = useAuth();

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
            Animated.delay(800), // show splash a bit longer
        ]).start(() => {
            console.log(`Loading ${isReady}`)
            if (isReady) {
                console.log(`From index: logged in ` + isLoggedIn + " ready: " + isReady);
                router.replace(isLoggedIn ? "/private/maps" : "/auth/login");
            }
        });
    }, [isReady, isLoggedIn]);

    return (
        <View style={styles.container}>
            <Animated.View style={{ transform: [{ scale }] }}>
                <Ionicons name="radio-outline" size={120} color="#007aff" />
            </Animated.View>
            <Animated.Text style={[styles.title, { opacity }]}>
                Beacon
            </Animated.Text>

            {isReady && (
                <View style={{marginTop: 30}}>
                    <ActivityIndicator size="large" color="#007aff"/>
                </View>
            )}
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
