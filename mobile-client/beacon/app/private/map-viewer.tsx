import React, {useEffect, useState} from 'react';
import {StyleSheet, View} from 'react-native';
import * as Location from 'expo-location';
import {PermissionStatus} from 'expo-location';
import MapView, {Region} from "react-native-maps";

export default function MapViewer() {
    const [region, setRegion] = useState<Region | null>(null);

    useEffect(() => {
        (async () => {
            const {status} = await Location.requestForegroundPermissionsAsync();
            if (status === PermissionStatus.GRANTED) {
                const location = await Location.getCurrentPositionAsync({});
                setRegion({
                    latitude: location.coords.latitude,
                    longitude: location.coords.longitude,
                    latitudeDelta: 0.01,
                    longitudeDelta: 0.01,
                });
            }
        })();
    }, []);

    if (!region) return null;

    return (
        <View style={styles.container}>
            <MapView
                style={styles.map}
                region={region}
                showsUserLocation={true}
                followsUserLocation={true}
            >
            </MapView>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
    map: {
        flex: 1,
    },
});