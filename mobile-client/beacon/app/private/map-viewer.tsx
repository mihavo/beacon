import React, {useCallback, useEffect, useMemo, useRef, useState} from 'react';
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
    const LOCATION_BATCH_INTERVAL = 10000;
    const ZOOM_THRESHOLD = 1.0;
    const MAX_SNAPSHOTS = 100;

    const [region, setRegion] = useState<Region | null>(null);
    const [snapshots, setSnapshots] = useState<EnrichedSnapshot[]>([]);
    const [authToken, setAuthToken] = useState<string | null>(null);
    const [isLoadingSnapshots, setIsLoadingSnapshots] = useState(false);
    const [isZoomedOut, setIsZoomedOut] = useState(false);

    const mapRef = useRef<MapView>(null);
    const eventSourceRef = useRef<EventSource | null>(null);
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

    const fetchSnapshots = useCallback(async (currentRegion: Region) => {
        try {
            setIsLoadingSnapshots(true);

            if (currentRegion.latitudeDelta > ZOOM_THRESHOLD ||
                currentRegion.longitudeDelta > ZOOM_THRESHOLD) {
                setIsZoomedOut(true);
                setSnapshots([]);
                return;
            }

            setIsZoomedOut(false);
            const boundingBox = regionToBoundingBox(currentRegion);
            const data = await getInitialSnapshot(boundingBox);

            const limitedData = data.slice(0, MAX_SNAPSHOTS);
            setSnapshots(limitedData);
        } catch (error) {
            console.error('Error fetching snapshots:', error);
            setSnapshots([]);
        } finally {
            setIsLoadingSnapshots(false);
        }
    }, [ZOOM_THRESHOLD, MAX_SNAPSHOTS]);

    const debouncedFetchSnapshots = useCallback((newRegion: Region) => {
        if (debounceTimer.current) {
            clearTimeout(debounceTimer.current);
        }
        debounceTimer.current = setTimeout(() => {
            fetchSnapshots(newRegion);
        }, 1200);
    }, [fetchSnapshots]);

    useEffect(() => {
        (async () => {
            const token = await getToken();
            if (!token) throw new Error("Token is missing. User must login.");
            setAuthToken(token);
        })()
    }, []);

    // Location tracking and batching
    useEffect(() => {
        let subscription: Location.LocationSubscription | null = null;
        let intervalId: number;

        (async () => {
            if (!permission?.granted) {
                const res = await requestPermission();
                if (!res.granted) {
                    console.debug('Location permission denied');
                    return;
                }
            }

            console.debug('Starting location tracking...');

            subscription = await Location.watchPositionAsync(
                {
                    accuracy: Location.Accuracy.High,
                    timeInterval: 5000,
                    distanceInterval: 10,
                },
                (loc) => {
                    if (locationBuffer.current.length > 500) {
                        console.error('Buffer overflow protection triggered');
                        locationBuffer.current = [];
                        return;
                    }

                    const newPoint: BatchedLocation = {
                        latitude: loc.coords.latitude,
                        longitude: loc.coords.longitude,
                        timestamp: loc.timestamp,
                    };
                    locationBuffer.current.push(newPoint);
                }
            );

            console.debug('Location tracking started');
        })();

        intervalId = setInterval(() => {
            (async () => {
                if (locationBuffer.current.length > 0) {
                    if (locationBuffer.current.length > 500) {
                        console.warn('Location buffer overflow! Dropping old data.');
                        locationBuffer.current = locationBuffer.current.slice(-100);
                        return;
                    }

                    console.debug(`About to send ${locationBuffer.current.length} batched.`);
                    const locationsToSend = [...locationBuffer.current];

                    const request = locationsToSend.map(loc => ({
                        coords: {
                            latitude: loc.latitude,
                            longitude: loc.longitude,
                        },
                        capturedAt: new Date(loc.timestamp).toISOString(),
                    }));

                    try {
                        await sendBatchedLocations(request);
                        locationBuffer.current = locationBuffer.current.filter(
                            loc => !locationsToSend.includes(loc)
                        );
                        console.debug('Successfully sent locations, buffer now:',
                            locationBuffer.current.length);
                    } catch (error) {
                        console.error('Failed to send locations:', error);
                        if (locationBuffer.current.length > 200) {
                            console.warn('Trimming buffer due to send failure');
                            locationBuffer.current = locationBuffer.current.slice(-100);
                        }
                    }
                }
            })().catch(err => {
                console.error('Interval error:', err);
            });
        }, LOCATION_BATCH_INTERVAL);

        return () => {
            console.debug('Cleaning up location tracking and interval');
            subscription?.remove();
            if (intervalId) {
                clearInterval(intervalId);
            }
        };
    }, [permission, requestPermission]);

    // Initial region setup
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

    // SSE connection for real-time updates
    useEffect(() => {
        if (!authToken || !region) return;

        if (region.latitudeDelta > ZOOM_THRESHOLD ||
            region.longitudeDelta > ZOOM_THRESHOLD) {
            if (eventSourceRef.current) {
                eventSourceRef.current.close();
                eventSourceRef.current = null;
            }
            return;
        }

        if (eventSourceRef.current) {
            eventSourceRef.current.close();
        }

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

        eventSourceRef.current = es;

        es.addEventListener("open", () => {
            console.debug("Maps SSE connection opened");
        });

        const handleMessage = (e: any) => {
            const snapshot: MapSnapshotResponse[0] = JSON.parse(e.data!);
            setSnapshots(prev => {
                const updated = prev.some(sn => sn.userId === snapshot.userId)
                    ? prev.map(sn => sn.userId === snapshot.userId ? snapshot : sn)
                    : [...prev, snapshot];

                return updated.slice(0, MAX_SNAPSHOTS);
            });
        };

        es.addEventListener("message", handleMessage);

        es.addEventListener("error", (e) => {
            console.error("SSE error:", e);
        });

        return () => {
            if (eventSourceRef.current) {
                es.removeEventListener("message", handleMessage);
                eventSourceRef.current.close();
                eventSourceRef.current = null;
            }
        };
    }, [authToken, region]);

    useEffect(() => {
        const cleanupInterval = setInterval(() => {
            setSnapshots(prev => {
                const now = Date.now();
                const fiveMinutesAgo = now - (5 * 60 * 1000);

                const filtered = prev.filter(s => {
                    const snapshotTime = new Date(s.timestamp).getTime();
                    return snapshotTime > fiveMinutesAgo;
                });

                if (filtered.length !== prev.length) {
                    console.debug(`Cleaned up ${prev.length - filtered.length} old snapshots`);
                }

                return filtered;
            });
        }, 60000);

        return () => clearInterval(cleanupInterval);
    }, []);

    const handleRegionChangeComplete = (newRegion: Region) => {
        setRegion(newRegion);
        debouncedFetchSnapshots(newRegion);
    };

    const handleMarkerPress = useCallback(async (snapshot: EnrichedSnapshot) => {
        try {
            const user = await getUser(snapshot.userId);
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
    }, []);

    // Memoize markers to prevent unnecessary re-renders
    const markerComponents = useMemo(() => {
        return snapshots.map((snapshot) => (
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
        ));
    }, [snapshots, handleMarkerPress]);

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
                {markerComponents}
            </MapView>

            {isZoomedOut && (
                <View style={styles.zoomMessage}>
                    <Ionicons name="search" size={24} color="#007bff"/>
                    <Text style={styles.zoomMessageText}>
                        Zoom in to see users
                    </Text>
                </View>
            )}

            {isLoadingSnapshots && !isZoomedOut && (
                <View style={styles.loadingIndicator}>
                    <Text style={styles.loadingText}>Loading...</Text>
                </View>
            )}
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
    },
    zoomMessage: {
        position: 'absolute',
        top: 50,
        left: 0,
        right: 0,
        alignItems: 'center',
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        paddingVertical: 12,
        paddingHorizontal: 20,
        marginHorizontal: 40,
        borderRadius: 20,
        shadowColor: '#000',
        shadowOffset: {width: 0, height: 2},
        shadowOpacity: 0.25,
        shadowRadius: 3.84,
        elevation: 5,
        flexDirection: 'row',
        gap: 8,
    },
    zoomMessageText: {
        fontSize: 16,
        color: '#007bff',
        fontWeight: '600',
    },
    loadingIndicator: {
        position: 'absolute',
        top: 50,
        left: 0,
        right: 0,
        alignItems: 'center',
        backgroundColor: 'rgba(255, 255, 255, 0.9)',
        paddingVertical: 8,
        paddingHorizontal: 16,
        marginHorizontal: 40,
        borderRadius: 20,
        shadowColor: '#000',
        shadowOffset: {width: 0, height: 2},
        shadowOpacity: 0.15,
        shadowRadius: 2,
        elevation: 3,
    },
    loadingText: {
        fontSize: 14,
        color: '#666',
        fontWeight: '500',
    },
});