import {StyleSheet, Text, View} from "react-native";
import React from "react";
import {ProfileMenu} from "@/components/profile-menu";

export default function Maps() {

    return (
        <View style={styles.container}>
            <View style={styles.header}>
                <Text style={styles.title}>Maps</Text>
                <ProfileMenu/>
            </View>

            <View style={styles.content}>
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {flex: 1},
    header: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: 16,
        paddingTop: 60,
        backgroundColor: '#fff',
    },
    title: {fontSize: 24, fontWeight: 'bold'},
    profileButton: {
        width: 40,
        height: 40,
        borderRadius: 20,
        backgroundColor: '#f0f0f0',
        justifyContent: 'center',
        alignItems: 'center',
    },
    content: {flex: 1},
});