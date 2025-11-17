import React, {useCallback, useEffect, useRef, useState} from 'react';
import {StyleSheet, Text, View} from 'react-native';
import * as Location from 'expo-location';
import {PermissionStatus} from 'expo-location';
import MapView, {Callout, Marker, Region} from "react-native-maps";
import {Ionicons} from '@expo/vector-icons';
import {BASE, getInitialSnapshot, getUser, sendBatchedLocations} from '@/lib/api';
import EventSource from "react-native-sse";
import {BoundingBox, MapSnapshotResponse} from "@/types/Map";
import {getToken} from "@/app/context/AuthContext";

type EnrichedSnapshot = MapSnapshotResponse[0] & {
    userData?: any;
};

interface BatchedLocation {
    latitude: number;
    longitude: number;
    timestamp: number;
}

export default function MapViewer() {
    const LOCATION_BATCH_INTERVAL = 5000;
    const [region, setRegion] = useState<Region | null>(null);
    const [snapshots, setSnapshots] = useState<EnrichedSnapshot[]>([]);
    const [snapshotLoaded, setSnapshotLoaded] = useState(false);
    const [authToken, setAuthToken] = useState<string | null>(null);
    const mapRef = useRef<MapView>(null);
    const [batch, setBatch] = useState<BatchedLocation[]>([]);
    const locationBuffer = useRef<BatchedLocation[]>([]);
    const [permission, requestPermission] = Location.useForegroundPermissions();
    const debounceTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

    const regionToBoundingBox = (region: Region): BoundingBox => {
        const latDelta = region.latitudeDelta;
        const lonDelta = region.longitudeDelta;

        return {
            minLat: region.latitude - latDelta / 2,
            maxLat: region.latitude + latDelta / 2,
            minLon: region.longitude - lonDelta / 2,
            maxLon: region.longitude + lonDelta / 2,
        };
    };

    const fetchSnapshots = async (currentRegion: Region) => {
        try {
            const boundingBox = regionToBoundingBox(currentRegion);
            const data = await getInitialSnapshot(boundingBox);
            setSnapshots(data);
            setSnapshotLoaded(true);
        } catch (error) {
            console.error('Error fetching snapshots:', error);
        }
    };

    const debouncedFetchSnapshots = useCallback((newRegion: Region) => {
        if (debounceTimer.current) {
            clearTimeout(debounceTimer.current);
        }

        debounceTimer.current = setTimeout(() => {
            fetchSnapshots(newRegion);
        }, 1200);
    }, []);

    useEffect(() => {
        (async () => {
            const token = await getToken();
            if (!token) throw new Error("Token is missing. User must login.");
            setAuthToken(token);
        })()
    }, []);

    useEffect(() => {
        let subscription: Location.LocationSubscription | null = null;

        (async () => {
            if (permission?.granted) {
                const res = await requestPermission();
                if (!res.granted) return;
            }

            subscription = await Location.watchPositionAsync(
                {
                    accuracy: Location.Accuracy.High,
                    timeInterval: 1000,
                    distanceInterval: 1,
                },
                (loc) => {
                    const newPoint: BatchedLocation = {
                        latitude: loc.coords.latitude,
                        longitude: loc.coords.longitude,
                        timestamp: loc.timestamp,
                    };
                    locationBuffer.current.push(newPoint);
                }
            );
        })();

        const intervalId = setInterval(() => {
            (async () => {
                if (locationBuffer.current.length > 0) {
                    setBatch((prev) => [...prev, ...locationBuffer.current]);
                    console.log(`About to send ${batch.length} batched.`);
                    const request = batch.map(loc => ({
                        coords: {
                            latitude: loc.latitude,
                            longitude: loc.longitude,
                        },
                        capturedAt: new Date(loc.timestamp).toISOString(),
                    }));
                    await sendBatchedLocations(request);
                    locationBuffer.current = [];
                }
            })();
        }, LOCATION_BATCH_INTERVAL);

        return () => {
            subscription?.remove();
        };
    }, [permission, requestPermission, batch]);

    useEffect(() => {
        (async () => {
            const {status} = await Location.requestForegroundPermissionsAsync();
            if (status === PermissionStatus.GRANTED) {
                const location = await Location.getCurrentPositionAsync({});
                const initialRegion = {
                    latitude: location.coords.latitude,
                    longitude: location.coords.longitude,
                    latitudeDelta: 0.01,
                    longitudeDelta: 0.01,
                };
                setRegion(initialRegion);
                await fetchSnapshots(initialRegion);
            }
        })();

        return () => {
            if (debounceTimer.current) {
                clearTimeout(debounceTimer.current);
            }
        };
    }, [fetchSnapshots]);

    useEffect(() => {
        if (authToken && region) {
            const bbox = regionToBoundingBox(region);
            const es = new EventSource(
                `${BASE}/maps/subscribe?minLon=${bbox.minLon}&maxLon=${bbox.maxLon}&minLat=${bbox.minLat}&maxLat=${bbox.maxLat}`,
                {
                    headers: {
                        Authorization: {
                            toString: function () {
                                return `Bearer ${authToken}`;
                            },
                        },
                    },
                });
            es.addEventListener("open", () => {
                console.log("Maps SSE connection opened");
            });
            if (snapshotLoaded) {
                es.addEventListener("message", (e) => {
                    const snapshot: MapSnapshotResponse[0] = JSON.parse(e.data!);
                    setSnapshots(prev =>
                        prev.some(sn => sn.userId === snapshot.userId)
                            ? prev.map(sn => sn.userId === snapshot.userId ? snapshot : sn)
                            : [...prev, snapshot]
                    );
                });

                es.addEventListener("error", (e) => {
                    console.log("SSE error:", e);
                });
            }
            return () => {
                es.close();
            };
        }
    }, [authToken, region, snapshotLoaded]);

    const handleRegionChangeComplete = (newRegion: Region) => {
        setRegion(newRegion);
        debouncedFetchSnapshots(newRegion);
    };

    const handleMarkerPress = async (snapshot: EnrichedSnapshot) => {
        try {
            const user = await getUser(snapshot.userId);
            console.log('Fetched user data:', user);

            setSnapshots(prevSnapshots =>
                prevSnapshots.map(s =>
                    s.userId === snapshot.userId
                        ? {...s, userData: user}
                        : s
                )
            );
        } catch (error) {
            console.error('Error fetching user:', error);
        }
    };

    if (!region) return null;

    return (
        <View style={styles.container}>
            <MapView
                ref={mapRef}
                style={styles.map}
                region={region}
                showsUserLocation={true}
                onRegionChangeComplete={handleRegionChangeComplete}
            >
                {snapshots.map((snapshot) => (
                    <Marker
                        key={snapshot.userId}
                        coordinate={{
                            latitude: snapshot.coords.latitude,
                            longitude: snapshot.coords.longitude,
                        }}
                        onPress={() => handleMarkerPress(snapshot)}
                    >
                        <View style={styles.markerContainer}>
                            <View style={[
                                styles.markerCircle,
                                snapshot.userData && styles.markerCircleEnriched
                            ]}>
                                <Ionicons
                                    name="person"
                                    size={20}
                                    color="#fff"
                                />
                            </View>
                        </View>

                        <Callout tooltip>
                            <View style={styles.calloutContainer}>
                                {snapshot.userData ? (
                                    <>
                                        <View style={styles.calloutHeader}>
                                            <Ionicons name="person-circle" size={32}
                                                      color="#007bff"/>
                                            <View>
                                                <Text style={styles.calloutTitle}>
                                                    {snapshot.userData.fullName
                                                        || snapshot.userData.username
                                                        || 'User'}
                                                </Text>
                                                {snapshot.userData.username && (
                                                    <Text style={styles.calloutUsername}>
                                                        @{snapshot.userData.username}
                                                    </Text>
                                                )}
                                            </View>
                                        </View>
                                        <Text style={styles.calloutText}>
                                            Last seen: {new Date(
                                            snapshot.timestamp).toLocaleString()}
                                        </Text>
                                        {snapshot.userData.email && (
                                            <Text style={styles.calloutSubtext}>
                                                {snapshot.userData.email}
                                            </Text>
                                        )}
                                    </>
                                ) : (
                                    <View style={styles.calloutLoading}>
                                        <Text style={styles.calloutLoadingText}>Loading...</Text>
                                    </View>
                                )}
                            </View>
                        </Callout>
                    </Marker>
                ))}
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
        markerContainer: {
            alignItems: 'center',
        },
        markerCircle: {
            width: 36,
            height: 36,
            borderRadius: 18,
            backgroundColor: '#007bff',
            justifyContent: 'center',
            alignItems: 'center',
            borderWidth: 3,
            borderColor: '#fff',
            shadowColor: '#000',
            shadowOffset: {width: 0, height: 2},
            shadowOpacity: 0.3,
            shadowRadius: 3,
            elevation: 5,
        },
        markerCircleEnriched: {
            backgroundColor: '#28a745',
        },
        calloutContainer: {
            backgroundColor: 'white',
            borderRadius: 8,
            padding: 12,
            minWidth: 180,
            shadowColor: '#000',
            shadowOffset: {width: 0, height: 2},
            shadowOpacity: 0.25,
            shadowRadius: 3.84,
            elevation: 5,
        },
        calloutHeader: {
            flexDirection: 'row',
            alignItems: 'center',
            marginBottom: 8,
            gap: 8,
        },
        calloutTitle: {
            fontWeight: 'bold',
            fontSize: 16,
            color: '#000',
        },
        calloutUsername: {
            fontSize: 12,
            color: '#666',
        },
        calloutText: {
            fontSize: 12,
            color: '#666',
            marginBottom: 4,
        },
        calloutSubtext: {
            fontSize: 10,
            color: '#999',
        },
        calloutLoading: {
            padding: 10,
            alignItems: 'center',
        },
        calloutLoadingText: {
            fontSize: 14,
            color: '#666',
        }
    }
);