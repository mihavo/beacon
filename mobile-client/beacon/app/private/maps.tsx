import {StyleSheet, Text, useColorScheme, View} from "react-native";
import React, {useEffect} from "react";
import {ProfileMenu} from "@/components/profile-menu";
import MapViewer from "@/app/private/map-viewer";
import {subscribeToNotifications} from "@/lib/api";

export default function Maps() {

    useEffect(() => {
        (async () => {
            await subscribeToNotifications();
        })();
    }, []);
    
    const isDark = useColorScheme() === 'dark';
    return (
        <View style={[styles.container, isDark && styles.containerDark]}>
            <View style={[styles.header, isDark && styles.headerDark]}>
                <Text style={[styles.title, isDark && styles.titleDark]}>Maps</Text>
                <ProfileMenu/>
            </View>
            <MapViewer/>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {flex: 1},
    containerDark: {
        backgroundColor: '#000000',
    },
    header: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: 16,
        paddingTop: 60,
        backgroundColor: '#fff',
    },
    headerDark: {
        backgroundColor: '#1a1a1a',
    },
    title: {fontSize: 24, fontWeight: 'bold'},
    profileButton: {
        width: 40,
        height: 40,
        borderRadius: 20,
        backgroundColor: '#f0f0f0',
        justifyContent: 'center',
        alignItems: 'center',
    }, titleDark: {
        color: '#ffffff',
    },
    content: {flex: 1, alignItems: 'center'},
});