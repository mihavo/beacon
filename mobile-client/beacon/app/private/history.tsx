import {
    ActivityIndicator,
    FlatList,
    RefreshControl,
    ScrollView,
    StyleSheet,
    Text,
    TouchableOpacity,
    useColorScheme,
    View,
} from 'react-native';
import React, {useCallback, useEffect, useRef, useState} from "react";
import {ProfileMenu} from "@/components/profile-menu";
import {Ionicons} from "@expo/vector-icons";
import MapView, {Marker, Polyline} from 'react-native-maps';
import {LocationPoint} from "@/types/History";
import {getLocationHistory} from "@/lib/api";
import {reverseGeocodeAsync} from "expo-location";

interface MostVisitedLocation {
    id: string;
    name: string;
    address: string;
    latitude: number;
    longitude: number;
    visitCount: number;
    lastVisited: string;
}

type TimeRange = '24h' | '7d' | '30d';

export default function LocationHistory() {
    const colorScheme = useColorScheme();
    const isDark = colorScheme === 'dark';

    const [selectedRange, setSelectedRange] = useState<TimeRange>('24h');
    const [locationHistory, setLocationHistory] = useState<LocationPoint[]>([]);
    const [mostVisited, setMostVisited] = useState<MostVisitedLocation[]>([]);
    const [loading, setLoading] = useState(false);
    const [refreshing, setRefreshing] = useState(false);
    const [viewMode, setViewMode] = useState<'timeline' | 'dashboard'>('timeline');
    const mapRef = useRef<MapView>(null);

    const getTimeRangeParams = (range: TimeRange) => {
        const end = new Date();
        let start = new Date();
        switch (range) {
            case '24h':
                start = new Date(end.getTime() - 24 * 60 * 60 * 1000);
                break;
            case '7d':
                start = new Date(end.getTime() - 7 * 24 * 60 * 60 * 1000);
                break;
            case '30d':
                start = new Date(end.getTime() - 30 * 24 * 60 * 60 * 1000);
                break;
        }
        return {
            start: start,
            end: end,
            direction: 'DESC',
        };
    };

    const fetchLocationHistory = useCallback(async (range: TimeRange) => {
        try {
            setLoading(true);
            const historyLocations = await getLocationHistory(getTimeRangeParams(range));
            const locationsWithAddresses = await Promise.all(
                historyLocations.map(async (location, index) => {
                    try {
                        // Add a small delay between requests to avoid rate limiting
                        await new Promise(resolve => setTimeout(resolve, index * 800));

                        const geocode = await reverseGeocodeAsync({
                            latitude: location.latitude,
                            longitude: location.longitude,
                        });

                        if (geocode && geocode.length > 0) {
                            const address = geocode[0];
                            const addressString = [
                                address.streetNumber,
                                address.street,
                                address.city,
                                address.region,
                            ].filter(Boolean).join(', ');

                            return {
                                ...location,
                                address: addressString || 'Unknown location',
                            };
                        }
                    } catch (error) {
                        console.error('Geocoding error for location:', location.id, error);
                        // Return location without address on error
                        return {
                            ...location,
                            address: 'Address unavailable',
                        };
                    }
                    return location;
                })
            );

            setLocationHistory(locationsWithAddresses);
        } catch (error) {
            console.error('Error fetching location history:', error);
        } finally {
            setLoading(false);
        }
    }, []);

    const fetchMostVisited = useCallback(async () => {
        try {
            // TODO: Replace with actual API call
            // const response = await getMostVisitedLocations();
            // setMostVisited(response.locations);

            // Mock data
            const mockMostVisited: MostVisitedLocation[] = [
                {
                    id: '1',
                    name: 'Home',
                    address: '123 Market St, San Francisco, CA',
                    latitude: 37.78825,
                    longitude: -122.4324,
                    visitCount: 156,
                    lastVisited: new Date(Date.now() - 1000 * 60 * 30).toISOString(),
                },
                {
                    id: '2',
                    name: 'Office',
                    address: '456 Mission St, San Francisco, CA',
                    latitude: 37.79025,
                    longitude: -122.4344,
                    visitCount: 98,
                    lastVisited: new Date(Date.now() - 1000 * 60 * 60 * 8).toISOString(),
                },
                {
                    id: '3',
                    name: 'Gym',
                    address: '789 Howard St, San Francisco, CA',
                    latitude: 37.78625,
                    longitude: -122.4304,
                    visitCount: 45,
                    lastVisited: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(),
                },
                {
                    id: '4',
                    name: 'Coffee Shop',
                    address: '321 Valencia St, San Francisco, CA',
                    latitude: 37.78425,
                    longitude: -122.4284,
                    visitCount: 32,
                    lastVisited: new Date(Date.now() - 1000 * 60 * 60 * 48).toISOString(),
                },
            ];
            setMostVisited(mockMostVisited);
        } catch (error) {
            console.error('Error fetching most visited:', error);
        }
    }, []);

    useEffect(() => {
        fetchLocationHistory(selectedRange);
        fetchMostVisited();
    }, [selectedRange, fetchLocationHistory, fetchMostVisited]);

    const onRefresh = useCallback(async () => {
        setRefreshing(true);
        await fetchLocationHistory(selectedRange);
        await fetchMostVisited();
        setRefreshing(false);
    }, [selectedRange, fetchLocationHistory, fetchMostVisited]);

    const formatTime = (timestamp: string) => {
        const date = new Date(timestamp);
        const now = new Date();
        const diffMs = now.getTime() - date.getTime();
        const diffMins = Math.floor(diffMs / 60000);
        const diffHours = Math.floor(diffMs / 3600000);
        const diffDays = Math.floor(diffMs / 86400000);

        if (diffMins < 60) {
            return `${diffMins}m ago`;
        } else if (diffHours < 24) {
            return `${diffHours}h ago`;
        } else if (diffDays < 7) {
            return `${diffDays}d ago`;
        } else {
            return date.toLocaleDateString();
        }
    };

    const timeRanges: { value: TimeRange; label: string }[] = [
        {value: '24h', label: '24 Hours'},
        {value: '7d', label: '7 Days'},
        {value: '30d', label: '30 Days'}
    ];

    const handleLocationPress = (location: LocationPoint | MostVisitedLocation) => {
        if (mapRef.current) {
            mapRef.current.animateToRegion({
                latitude: location.latitude,
                longitude: location.longitude,
                latitudeDelta: 0.01,
                longitudeDelta: 0.01,
            }, 500);
        }
    };

    const renderTimelineItem = ({item}: { item: LocationPoint }) => (
        <TouchableOpacity
            activeOpacity={0.7}
            onPress={() => handleLocationPress(item)}
        >
            <View style={[styles.timelineItem, isDark && styles.timelineItemDark]}>
                <View style={styles.timelineMarker}>
                    <View style={[styles.timelineDot, isDark && styles.timelineDotDark]}/>
                    <View style={[styles.timelineLine, isDark && styles.timelineLineDark]}/>
                </View>
                <View style={styles.timelineContent}>
                    <View style={[styles.locationCard, isDark && styles.locationCardDark]}>
                        <View style={styles.locationHeader}>
                            <Ionicons
                                name="location"
                                size={24}
                                color="#007bff"
                            />
                            <Text style={[styles.locationTime, isDark && styles.locationTimeDark]}>
                                {formatTime(item.timestamp)}
                            </Text>
                        </View>
                        {item.address && (
                            <Text style={[styles.locationAddress,
                                isDark && styles.locationAddressDark]}>
                                {item.address}
                            </Text>
                        )}
                        <View style={styles.tapHint}>
                            <Ionicons name="map-outline" size={14}
                                      color={isDark ? '#666' : '#999'}/>
                            <Text style={[styles.tapHintText, isDark && styles.tapHintTextDark]}>
                                Tap to view on map
                            </Text>
                        </View>
                    </View>
                </View>
            </View>
        </TouchableOpacity>
    );

    const renderMostVisitedItem = ({item}: { item: MostVisitedLocation }) => (
        <TouchableOpacity
            activeOpacity={0.7}
            onPress={() => handleLocationPress(item)}
        >
            <View style={[styles.visitedCard, isDark && styles.visitedCardDark]}>
                <View style={styles.visitedLeft}>
                    <View style={[styles.visitedIcon, isDark && styles.visitedIconDark]}>
                        <Ionicons
                            name="location"
                            size={28}
                            color="#007bff"
                        />
                    </View>
                    <View style={styles.visitedInfo}>
                        <Text style={[styles.visitedName, isDark && styles.visitedNameDark]}>
                            {item.name}
                        </Text>
                        <Text style={[styles.visitedAddress, isDark && styles.visitedAddressDark]}>
                            {item.address}
                        </Text>
                        <Text style={[styles.visitedTime, isDark && styles.visitedTimeDark]}>
                            Last visited {formatTime(item.lastVisited)}
                        </Text>
                    </View>
                </View>
                <View style={styles.visitedRight}>
                    <View style={[styles.visitCountBadge, isDark && styles.visitCountBadgeDark]}>
                        <Text style={[styles.visitCountNumber,
                            isDark && styles.visitCountNumberDark]}>
                            {item.visitCount}
                        </Text>
                        <Text
                            style={[styles.visitCountLabel, isDark && styles.visitCountLabelDark]}>
                            visits
                        </Text>
                    </View>
                </View>
            </View>
        </TouchableOpacity>
    ); // <-- Correct closing of the arrow function

    const renderMap = () => {
        if (locationHistory.length === 0) return null;

        const coordinates = locationHistory.map(loc => ({
            latitude: loc.latitude,
            longitude: loc.longitude,
        }));

        const region = {
            latitude: locationHistory[0].latitude,
            longitude: locationHistory[0].longitude,
            latitudeDelta: 0.05,
            longitudeDelta: 0.05,
        };

        return (
            <View style={styles.mapContainer}>
                <MapView
                    ref={mapRef} // Added ref to allow animating to location
                    style={styles.map}
                    initialRegion={region}
                >
                    {locationHistory.map((location, index) => (
                        <Marker
                            key={`marker-${index}`}
                            coordinate={{
                                latitude: location.latitude,
                                longitude: location.longitude,
                            }}
                        >
                            <View style={styles.markerContainer}>
                                <View style={[
                                    styles.customMarker,
                                    index === 0 && styles.customMarkerRecent
                                ]}>
                                    <Text style={styles.markerText}>{index + 1}</Text>
                                </View>
                            </View>
                        </Marker>
                    ))}
                    <Polyline
                        key="polyline"
                        coordinates={coordinates}
                        strokeColor="#007bff"
                        strokeWidth={3}
                        lineDashPattern={[5, 5]}
                    />
                </MapView>
            </View>
        );
    };

    return (
        <View style={[styles.container, isDark && styles.containerDark]}>
            <View style={[styles.header, isDark && styles.headerDark]}>
                <Text style={[styles.title, isDark && styles.titleDark]}>Location History</Text>
                <ProfileMenu/>
            </View>

            {/* View Mode Toggle */}
            <View style={[styles.viewModeContainer, isDark && styles.viewModeContainerDark]}>
                <TouchableOpacity
                    style={[
                        styles.viewModeButton,
                        isDark && styles.viewModeButtonDark,
                        viewMode === 'timeline' && styles.viewModeButtonActive,
                    ]}
                    onPress={() => setViewMode('timeline')}
                >
                    <Ionicons
                        name="time-outline"
                        size={20}
                        color={viewMode === 'timeline' ? '#fff' : (isDark ? '#999' : '#666')}
                    />
                    <Text style={[
                        styles.viewModeText,
                        isDark && styles.viewModeTextDark,
                        viewMode === 'timeline' && styles.viewModeTextActive,
                    ]}>
                        Timeline
                    </Text>
                </TouchableOpacity>
                <TouchableOpacity
                    style={[
                        styles.viewModeButton,
                        isDark && styles.viewModeButtonDark,
                        viewMode === 'dashboard' && styles.viewModeButtonActive,
                    ]}
                    onPress={() => setViewMode('dashboard')}
                >
                    <Ionicons
                        name="bar-chart-outline"
                        size={20}
                        color={viewMode === 'dashboard' ? '#fff' : (isDark ? '#999' : '#666')}
                    />
                    <Text style={[
                        styles.viewModeText,
                        isDark && styles.viewModeTextDark,
                        viewMode === 'dashboard' && styles.viewModeTextActive,
                    ]}>
                        Dashboard
                    </Text>
                </TouchableOpacity>
            </View>

            {viewMode === 'timeline' ? (
                <>
                    {/* Time Range Selector */}
                    <View style={[styles.rangeSelector, isDark && styles.rangeSelectorDark]}>
                        <ScrollView
                            horizontal
                            showsHorizontalScrollIndicator={false}
                            contentContainerStyle={styles.rangeSelectorContent}
                        >
                            {timeRanges.map((range) => (
                                <TouchableOpacity
                                    key={range.value}
                                    style={[
                                        styles.rangeButton,
                                        isDark && styles.rangeButtonDark,
                                        selectedRange === range.value && styles.rangeButtonActive,
                                    ]}
                                    onPress={() => setSelectedRange(range.value)}
                                >
                                    <Text style={[
                                        styles.rangeButtonText,
                                        isDark && styles.rangeButtonTextDark,
                                        selectedRange
                                        === range.value
                                        && styles.rangeButtonTextActive,
                                    ]}>
                                        {range.label}
                                    </Text>
                                </TouchableOpacity>
                            ))}
                        </ScrollView>
                    </View>

                    {/* Map Preview */}
                    {renderMap()}

                    {/* Timeline */}
                    {loading ? (
                        <View style={styles.loadingContainer}>
                            <ActivityIndicator size="large" color="#007bff"/>
                            <Text style={[styles.loadingText, isDark && styles.loadingTextDark]}>
                                Loading history...
                            </Text>
                        </View>
                    ) : (
                        <FlatList
                            data={locationHistory}
                            keyExtractor={(item) => item.id}
                            renderItem={renderTimelineItem}
                            contentContainerStyle={styles.timelineList}
                            refreshControl={
                                <RefreshControl
                                    refreshing={refreshing}
                                    onRefresh={onRefresh}
                                    tintColor={isDark ? '#fff' : '#000'}
                                />
                            }
                            ListEmptyComponent={
                                <View style={styles.emptyContainer}>
                                    <Ionicons
                                        name="location-outline"
                                        size={64}
                                        color={isDark ? '#666' : '#ccc'}
                                    />
                                    <Text
                                        style={[styles.emptyText, isDark && styles.emptyTextDark]}>
                                        No location history
                                    </Text>
                                    <Text style={[styles.emptySubtext,
                                        isDark && styles.emptySubtextDark]}>
                                        Your location history will appear here
                                    </Text>
                                </View>
                            }
                        />
                    )}
                </>
            ) : (
                <ScrollView
                    style={styles.dashboardContainer}
                    refreshControl={
                        <RefreshControl
                            refreshing={refreshing}
                            onRefresh={onRefresh}
                            tintColor={isDark ? '#fff' : '#000'}
                        />
                    }
                >
                    <View style={styles.dashboardContent}>
                        <Text style={[styles.sectionTitle, isDark && styles.sectionTitleDark]}>
                            Most Visited Locations
                        </Text>
                        {mostVisited.length > 0 ? (
                            mostVisited.map((location) => (
                                <View key={location.id}>
                                    {renderMostVisitedItem({item: location})}
                                </View>
                            ))
                        ) : (
                            <View style={styles.emptyContainer}>
                                <Ionicons
                                    name="bar-chart-outline"
                                    size={64}
                                    color={isDark ? '#666' : '#ccc'}
                                />
                                <Text style={[styles.emptyText, isDark && styles.emptyTextDark]}>
                                    No data available
                                </Text>
                                <Text style={[styles.emptySubtext,
                                    isDark && styles.emptySubtextDark]}>
                                    Visit places to see your statistics
                                </Text>
                            </View>
                        )}
                    </View>
                </ScrollView>
            )}
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#f8f9fa',
    },
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
        borderBottomWidth: 1,
        borderBottomColor: '#e0e0e0',
    },
    headerDark: {
        backgroundColor: '#1a1a1a',
        borderBottomColor: '#333',
    },
    title: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#000',
    },
    titleDark: {
        color: '#ffffff',
    },
    viewModeContainer: {
        flexDirection: 'row',
        padding: 16,
        gap: 12,
        backgroundColor: '#fff',
        borderBottomWidth: 1,
        borderBottomColor: '#e0e0e0',
    },
    viewModeContainerDark: {
        backgroundColor: '#1a1a1a',
        borderBottomColor: '#333',
    },
    viewModeButton: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 8,
        backgroundColor: '#f0f0f0',
        padding: 12,
        borderRadius: 10,
        borderWidth: 2,
        borderColor: 'transparent',
    },
    viewModeButtonDark: {
        backgroundColor: '#2a2a2a',
    },
    viewModeButtonActive: {
        backgroundColor: '#007bff',
        borderColor: '#0056b3',
    },
    viewModeText: {
        fontSize: 16,
        fontWeight: '600',
        color: '#666',
    },
    viewModeTextDark: {
        color: '#999',
    },
    viewModeTextActive: {
        color: '#fff',
    },
    rangeSelector: {
        backgroundColor: '#fff',
        borderBottomWidth: 1,
        borderBottomColor: '#e0e0e0',
        paddingVertical: 12,
    },
    rangeSelectorDark: {
        backgroundColor: '#1a1a1a',
        borderBottomColor: '#333',
    },
    rangeSelectorContent: {
        paddingHorizontal: 16,
        gap: 8,
    },
    rangeButton: {
        backgroundColor: '#f0f0f0',
        paddingHorizontal: 20,
        paddingVertical: 10,
        borderRadius: 20,
        borderWidth: 2,
        borderColor: 'transparent',
    },
    rangeButtonDark: {
        backgroundColor: '#2a2a2a',
    },
    rangeButtonActive: {
        backgroundColor: '#007bff',
        borderColor: '#0056b3',
    },
    rangeButtonText: {
        fontSize: 14,
        fontWeight: '600',
        color: '#666',
    },
    rangeButtonTextDark: {
        color: '#999',
    },
    rangeButtonTextActive: {
        color: '#fff',
    },
    mapContainer: {
        height: 200,
        width: '100%',
    },
    map: {
        flex: 1,
    },
    markerContainer: {
        alignItems: 'center',
        justifyContent: 'center',
    },
    customMarker: {
        backgroundColor: '#007bff',
        borderRadius: 15,
        width: 30,
        height: 30,
        alignItems: 'center',
        justifyContent: 'center',
        borderWidth: 2,
        borderColor: '#fff',
    },
    customMarkerRecent: {
        backgroundColor: '#28a745',
    },
    markerText: {
        color: '#fff',
        fontSize: 12,
        fontWeight: 'bold',
    },
    timelineList: {
        padding: 16,
    },
    timelineItem: {
        flexDirection: 'row',
        marginBottom: 16,
    },
    timelineItemDark: {},
    timelineMarker: {
        alignItems: 'center',
        marginRight: 16,
    },
    timelineDot: {
        width: 12,
        height: 12,
        borderRadius: 6,
        backgroundColor: '#007bff',
        borderWidth: 2,
        borderColor: '#fff',
        shadowColor: '#000',
        shadowOffset: {width: 0, height: 1},
        shadowOpacity: 0.2,
        shadowRadius: 2,
        elevation: 2,
    },
    timelineDotDark: {
        borderColor: '#1a1a1a',
    },
    timelineLine: {
        width: 2,
        flex: 1,
        backgroundColor: '#e0e0e0',
        marginTop: 4,
    },
    timelineLineDark: {
        backgroundColor: '#333',
    },
    timelineContent: {
        flex: 1,
    },
    locationCard: {
        backgroundColor: '#fff',
        padding: 16,
        borderRadius: 12,
        shadowColor: '#000',
        shadowOffset: {width: 0, height: 1},
        shadowOpacity: 0.05,
        shadowRadius: 3,
        elevation: 2,
    },
    locationCardDark: {
        backgroundColor: '#1a1a1a',
        shadowColor: '#fff',
        shadowOpacity: 0.1,
    },
    locationHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        marginBottom: 8,
    },
    locationTime: {
        fontSize: 14,
        fontWeight: '600',
        color: '#666',
    },
    locationTimeDark: {
        color: '#999',
    },
    locationAddress: {
        fontSize: 16,
        fontWeight: '500',
        color: '#000',
        marginBottom: 8,
    },
    locationAddressDark: {
        color: '#ffffff',
    },
    locationCoords: {
        backgroundColor: '#f0f0f0',
        padding: 8,
        borderRadius: 6,
    },
    coordsText: {
        fontSize: 12,
        fontFamily: 'monospace',
        color: '#666',
    },
    coordsTextDark: {
        color: '#999',
    },
    tapHint: {
        flexDirection: 'row',
        alignItems: 'center',
        marginTop: 4,
    },
    tapHintText: {
        fontSize: 12,
        color: '#999',
        marginLeft: 4,
    },
    tapHintTextDark: {
        color: '#666',
    },
    loadingContainer: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
        paddingVertical: 40,
    },
    loadingText: {
        marginTop: 12,
        fontSize: 14,
        color: '#666',
    },
    loadingTextDark: {
        color: '#999',
    },
    emptyContainer: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
        paddingVertical: 60,
    },
    emptyText: {
        fontSize: 18,
        fontWeight: '600',
        color: '#999',
        marginTop: 16,
    },
    emptyTextDark: {
        color: '#666',
    },
    emptySubtext: {
        fontSize: 14,
        color: '#ccc',
        marginTop: 8,
    },
    emptySubtextDark: {
        color: '#555',
    },
    dashboardContainer: {
        flex: 1,
    },
    dashboardContent: {
        padding: 16,
    },
    sectionTitle: {
        fontSize: 20,
        fontWeight: '600',
        color: '#333',
        marginBottom: 16,
    },
    sectionTitleDark: {
        color: '#ffffff',
    },
    visitedCard: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        backgroundColor: '#fff',
        padding: 16,
        borderRadius: 12,
        marginBottom: 12,
        shadowColor: '#000',
        shadowOffset: {width: 0, height: 1},
        shadowOpacity: 0.05,
        shadowRadius: 3,
        elevation: 2,
    },
    visitedCardDark: {
        backgroundColor: '#1a1a1a',
        shadowColor: '#fff',
        shadowOpacity: 0.1,
    },
    visitedLeft: {
        flexDirection: 'row',
        alignItems: 'center',
        flex: 1,
    },
    visitedIcon: {
        width: 56,
        height: 56,
        borderRadius: 28,
        backgroundColor: '#e3f2fd',
        alignItems: 'center',
        justifyContent: 'center',
        marginRight: 12,
    },
    visitedIconDark: {
        backgroundColor: '#1a3a52',
    },
    visitedInfo: {
        flex: 1,
    },
    visitedName: {
        fontSize: 18,
        fontWeight: '600',
        color: '#000',
        marginBottom: 4,
    },
    visitedNameDark: {
        color: '#ffffff',
    },
    visitedAddress: {
        fontSize: 14,
        color: '#666',
        marginBottom: 4,
    },
    visitedAddressDark: {
        color: '#999',
    },
    visitedTime: {
        fontSize: 12,
        color: '#999',
    },
    visitedTimeDark: {
        color: '#666',
    },
    visitedRight: {
        marginLeft: 12,
    },
    visitCountBadge: {
        backgroundColor: '#e3f2fd',
        paddingHorizontal: 16,
        paddingVertical: 8,
        borderRadius: 12,
        alignItems: 'center',
        minWidth: 70,
    },
    visitCountBadgeDark: {
        backgroundColor: '#1a3a52',
    },
    visitCountNumber: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#007bff',
    },
    visitCountNumberDark: {
        color: '#4a9eff',
    },
    visitCountLabel: {
        fontSize: 12,
        color: '#007bff',
        marginTop: 2,
    },
    visitCountLabelDark: {
        color: '#4a9eff',
    },
});