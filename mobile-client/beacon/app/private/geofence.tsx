import {
    ActivityIndicator,
    Alert,
    FlatList,
    Modal,
    ScrollView,
    StyleSheet,
    Text,
    TouchableOpacity,
    useColorScheme,
    View,
} from 'react-native';
import React, {useCallback, useEffect, useRef, useState} from "react";
import {ProfileMenu} from "@/components/profile-menu";
import {Connection} from "@/types/Connections";
import {createGeofence, getFriends} from "@/lib/api";
import {Ionicons} from "@expo/vector-icons";
import MapView, {Circle, Marker} from 'react-native-maps';
import * as Location from 'expo-location';
import {Geofence, GeofenceType} from "@/types/Geofence";

export default function GeofenceSetup() {
    const colorScheme = useColorScheme();
    const isDark = colorScheme === 'dark';

    const [friends, setFriends] = useState<Connection[]>([]);
    const [selectedFriend, setSelectedFriend] = useState<Connection | null>(null);
    const [showFriendModal, setShowFriendModal] = useState(false);
    const [geofenceType, setGeofenceType] = useState<GeofenceType>('NEAR');
    const [radius, setRadius] = useState(500); // meters
    const [markerPosition, setMarkerPosition] = useState({
        latitude: 37.78825,
        longitude: -122.4324,
    });
    const [loading, setLoading] = useState(false);
    const mapRef = useRef<MapView>(null);

    const fetchFriends = useCallback(async () => {
        try {
            setLoading(true);
            const response = await getFriends();
            setFriends(response.connections);
        } catch (error) {
            console.error('Error fetching friends:', error);
            Alert.alert('Error', 'Failed to load friends');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchFriends();
        (async () => {
            try {
                const {status} = await Location.requestForegroundPermissionsAsync();
                if (status !== 'granted') {
                    Alert.alert('Permission Denied',
                        'Location permission is required to set geofence');
                    return;
                }

                const location = await Location.getCurrentPositionAsync({
                    accuracy: Location.Accuracy.Balanced,
                });

                const newPosition = {
                    latitude: location.coords.latitude,
                    longitude: location.coords.longitude,
                };

                setMarkerPosition(newPosition);

                if (mapRef.current) {
                    mapRef.current.animateToRegion({
                        latitude: newPosition.latitude,
                        longitude: newPosition.longitude,
                        latitudeDelta: 0.0922,
                        longitudeDelta: 0.0421,
                    }, 1000);
                }
            } catch (error) {
                console.error('Error getting location:', error);
                Alert.alert('Error', 'Failed to get your current location');
            }
        })();
    }, [fetchFriends]);

    const handleMapPress = (event: any) => {
        setMarkerPosition(event.nativeEvent.coordinate);
    };

    const handleSaveGeofence = async () => {
        if (!selectedFriend) {
            Alert.alert('Error', 'Please select a friend');
            return;
        }

        const geofence: Geofence = {
            userId: selectedFriend.userId,
            type: geofenceType,
            centerLatitude: markerPosition.latitude,
            centerLongitude: markerPosition.longitude,
            radiusMeters: radius,
        };

        try {
            await createGeofence(geofence);
            const notificationType = geofenceType === 'ENTER' ? 'enters' : 'is near';
            Alert.alert(
                'Success',
                `Geofence created for ${selectedFriend.fullName} with radius: ${radius}m.\n\nYou'll now be notified when your friend ${notificationType} the selected area.`
            );
        } catch (error) {
            console.error('Error creating geofence:', error);
            Alert.alert('Error', 'Failed to create geofence');
        }
    };

    const radiusOptions = [100, 250, 500, 1000, 2000, 5000];

    return (
        <View style={[styles.container, isDark && styles.containerDark]}>
            <View style={[styles.header, isDark && styles.headerDark]}>
                <Text style={[styles.title, isDark && styles.titleDark]}>Geofences</Text>
                <ProfileMenu/>
            </View>

            {/* Friend Selection Modal */}
            <Modal
                visible={showFriendModal}
                animationType="slide"
                transparent={true}
                onRequestClose={() => setShowFriendModal(false)}
            >
                <View style={styles.modalOverlay}>
                    <View style={[styles.modalContent, isDark && styles.modalContentDark]}>
                        <View style={[styles.modalHeader, isDark && styles.modalHeaderDark]}>
                            <Text style={[styles.modalTitle, isDark && styles.modalTitleDark]}>
                                Select Friend
                            </Text>
                            <TouchableOpacity
                                onPress={() => setShowFriendModal(false)}
                                style={styles.closeButton}
                            >
                                <Ionicons name="close" size={28} color={isDark ? '#fff' : '#000'}/>
                            </TouchableOpacity>
                        </View>

                        {loading ? (
                            <View style={styles.loadingContainer}>
                                <ActivityIndicator size="large" color="#007bff"/>
                                <Text
                                    style={[styles.loadingText, isDark && styles.loadingTextDark]}>
                                    Loading friends...
                                </Text>
                            </View>
                        ) : friends.length === 0 ? (
                            <View>
                                <Text style={[styles.emptyText, isDark && styles.emptyTextDark]}>
                                    No friends available
                                </Text>
                            </View>
                        ) : (
                            <FlatList
                                data={friends}
                                keyExtractor={(item, index) => `${item.userId}-${index}`}
                                renderItem={({item}) => (
                                    <TouchableOpacity
                                        style={[
                                            styles.modalFriendItem,
                                            isDark && styles.modalFriendItemDark,
                                            selectedFriend?.userId === item.userId && (isDark
                                                ? styles.modalFriendItemSelectedDark
                                                : styles.modalFriendItemSelected),
                                        ]}
                                        onPress={() => {
                                            setSelectedFriend(item);
                                            setShowFriendModal(false);
                                        }}
                                    >
                                        <Ionicons
                                            name="person-circle"
                                            size={48}
                                            color={isDark ? '#4a4a4a' : '#e0e0e0'}
                                        />
                                        <View style={styles.modalFriendInfo}>
                                            <Text style={[styles.modalFriendName,
                                                isDark && styles.modalFriendNameDark]}>
                                                {item.fullName}
                                            </Text>
                                            <Text style={[styles.modalFriendUsername,
                                                isDark && styles.modalFriendUsernameDark]}>
                                                @{item.username}
                                            </Text>
                                        </View>
                                        {selectedFriend?.userId === item.userId && (
                                            <Ionicons name="checkmark-circle" size={24}
                                                      color="#007bff"/>
                                        )}
                                    </TouchableOpacity>
                                )}
                                contentContainerStyle={styles.modalListContent}
                            />
                        )}
                    </View>
                </View>
            </Modal>

            <View style={styles.content}>
                {/* Map */}
                <View style={styles.mapContainer}>
                    <MapView
                        ref={mapRef}
                        style={styles.map}
                        initialRegion={{
                            latitude: markerPosition.latitude,
                            longitude: markerPosition.longitude,
                            latitudeDelta: 0.0922,
                            longitudeDelta: 0.0421,
                        }}
                        onPress={handleMapPress}
                    >
                        <Marker
                            coordinate={markerPosition}
                            draggable
                            onDragEnd={handleMapPress}
                        >
                            <View style={styles.customMarker}>
                                <Ionicons name="location" size={40} color="#007bff"/>
                            </View>
                        </Marker>
                        <Circle
                            center={markerPosition}
                            radius={radius}
                            fillColor="rgba(0, 123, 255, 0.2)"
                            strokeColor="rgba(0, 123, 255, 0.8)"
                            strokeWidth={2}
                        />
                    </MapView>
                </View>

                {/* Controls */}
                <ScrollView style={styles.controls} showsVerticalScrollIndicator={false}>
                    {/* Friend Selection */}
                    <View style={styles.section}>
                        <Text style={[styles.label, isDark && styles.labelDark]}>
                            Select Friend
                        </Text>
                        <TouchableOpacity
                            style={[styles.dropdown, isDark && styles.dropdownDark]}
                            onPress={() => setShowFriendModal(true)}
                        >
                            <View style={styles.dropdownContent}>
                                {selectedFriend ? (
                                    <>
                                        <Ionicons
                                            name="person-circle"
                                            size={24}
                                            color={isDark ? '#4a4a4a' : '#e0e0e0'}
                                        />
                                        <Text style={[styles.dropdownText,
                                            isDark && styles.dropdownTextDark]}>
                                            {selectedFriend.fullName}
                                        </Text>
                                    </>
                                ) : (
                                    <Text style={[styles.dropdownPlaceholder,
                                        isDark && styles.dropdownPlaceholderDark]}>
                                        Choose a friend...
                                    </Text>
                                )}
                            </View>
                            <Ionicons
                                name="chevron-down"
                                size={20}
                                color={isDark ? '#999' : '#666'}
                            />
                        </TouchableOpacity>
                    </View>

                    {/* Geofence Type */}
                    <View style={styles.section}>
                        <Text style={[styles.label, isDark && styles.labelDark]}>
                            Geofence Type
                        </Text>
                        <View style={styles.typeButtons}>
                            <TouchableOpacity
                                style={[
                                    styles.typeButton,
                                    isDark && styles.typeButtonDark,
                                    geofenceType === 'NEAR' && styles.typeButtonActive,
                                ]}
                                onPress={() => setGeofenceType('NEAR')}
                            >
                                <Ionicons
                                    name="radio-button-on"
                                    size={20}
                                    color={geofenceType === 'NEAR' ? '#fff' : (isDark ? '#999'
                                        : '#666')}
                                />
                                <Text style={[
                                    styles.typeButtonText,
                                    isDark && styles.typeButtonTextDark,
                                    geofenceType === 'NEAR' && styles.typeButtonTextActive,
                                ]}>
                                    Near
                                </Text>
                            </TouchableOpacity>
                            <TouchableOpacity
                                style={[
                                    styles.typeButton,
                                    isDark && styles.typeButtonDark,
                                    geofenceType === 'ENTER' && styles.typeButtonActive,
                                ]}
                                onPress={() => setGeofenceType('ENTER')}
                            >
                                <Ionicons
                                    name="enter"
                                    size={20}
                                    color={geofenceType === 'ENTER' ? '#fff' : (isDark ? '#999'
                                        : '#666')}
                                />
                                <Text style={[
                                    styles.typeButtonText,
                                    isDark && styles.typeButtonTextDark,
                                    geofenceType === 'ENTER' && styles.typeButtonTextActive,
                                ]}>
                                    Enter
                                </Text>
                            </TouchableOpacity>
                        </View>
                        <Text style={[styles.helpText, isDark && styles.helpTextDark]}>
                            {geofenceType === 'NEAR'
                                ? 'Notify when friend is within the radius'
                                : 'Notify when friend enters the area'}
                        </Text>
                    </View>

                    {/* Radius Selection */}
                    <View style={styles.section}>
                        <Text style={[styles.label, isDark && styles.labelDark]}>
                            Radius: {radius}m
                        </Text>
                        <View style={styles.radiusButtons}>
                            {radiusOptions.map((option) => (
                                <TouchableOpacity
                                    key={option}
                                    style={[
                                        styles.radiusButton,
                                        isDark && styles.radiusButtonDark,
                                        radius === option && styles.radiusButtonActive,
                                    ]}
                                    onPress={() => setRadius(option)}
                                >
                                    <Text style={[
                                        styles.radiusButtonText,
                                        isDark && styles.radiusButtonTextDark,
                                        radius === option && styles.radiusButtonTextActive,
                                    ]}>
                                        {option >= 1000 ? `${option / 1000}km` : `${option}m`}
                                    </Text>
                                </TouchableOpacity>
                            ))}
                        </View>
                    </View>

                    {/* Save Button */}
                    <TouchableOpacity
                        style={[styles.saveButton, !selectedFriend && styles.saveButtonDisabled]}
                        onPress={handleSaveGeofence}
                        disabled={!selectedFriend}
                    >
                        <Ionicons name="checkmark-circle" size={20} color="#fff"/>
                        <Text style={styles.saveButtonText}>Create Geofence</Text>
                    </TouchableOpacity>
                </ScrollView>
            </View>
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
    content: {
        flex: 1,
    },
    mapContainer: {
        height: 300,
        width: '100%',
    },
    map: {
        flex: 1,
    },
    customMarker: {
        alignItems: 'center',
        justifyContent: 'center',
    },
    controls: {
        flex: 1,
        padding: 16,
    },
    section: {
        marginBottom: 24,
    },
    label: {
        fontSize: 16,
        fontWeight: '600',
        color: '#333',
        marginBottom: 8,
    },
    labelDark: {
        color: '#ffffff',
    },
    dropdown: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        backgroundColor: '#fff',
        padding: 12,
        borderRadius: 10,
        borderWidth: 1,
        borderColor: '#e0e0e0',
    },
    dropdownDark: {
        backgroundColor: '#1a1a1a',
        borderColor: '#333',
    },
    dropdownContent: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 8,
    },
    dropdownText: {
        fontSize: 16,
        color: '#000',
    },
    dropdownTextDark: {
        color: '#ffffff',
    },
    dropdownPlaceholder: {
        fontSize: 16,
        color: '#999',
    },
    dropdownPlaceholderDark: {
        color: '#666',
    },
    modalOverlay: {
        flex: 1,
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        justifyContent: 'flex-end',
    },
    modalContent: {
        backgroundColor: '#fff',
        borderTopLeftRadius: 20,
        borderTopRightRadius: 20,
        height: '80%',
        paddingBottom: 20,
    },
    modalContentDark: {
        backgroundColor: '#1a1a1a',
    },
    modalHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: 20,
        borderBottomWidth: 1,
        borderBottomColor: '#e0e0e0',
    },
    modalHeaderDark: {
        borderBottomColor: '#333',
    },
    modalTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        color: '#000',
    },
    modalTitleDark: {
        color: '#ffffff',
    },
    closeButton: {
        padding: 4,
    },
    modalListContent: {
        flexGrow: 1,
    },
    modalFriendItem: {
        flexDirection: 'row',
        alignItems: 'center',
        padding: 16,
        borderBottomWidth: 1,
        borderBottomColor: '#e0e0e0',
    },
    modalFriendItemDark: {
        borderBottomColor: '#333',
    },
    modalFriendItemSelected: {
        backgroundColor: '#f0f8ff',
    },
    modalFriendItemSelectedDark: {
        backgroundColor: '#1a3a52',
    },
    modalFriendInfo: {
        marginLeft: 12,
        flex: 1,
    },
    modalFriendName: {
        fontSize: 18,
        fontWeight: '600',
        color: '#000',
        marginBottom: 4,
    },
    modalFriendNameDark: {
        color: '#ffffff',
    },
    modalFriendUsername: {
        fontSize: 14,
        color: '#666',
    },
    modalFriendUsernameDark: {
        color: '#999',
    },
    loadingContainer: {
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
    friendUsernameDark: {
        color: '#999',
    },
    typeButtons: {
        flexDirection: 'row',
        gap: 12,
    },
    typeButton: {
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
    typeButtonDark: {
        backgroundColor: '#2a2a2a',
    },
    typeButtonActive: {
        backgroundColor: '#007bff',
        borderColor: '#0056b3',
    },
    typeButtonText: {
        fontSize: 16,
        fontWeight: '600',
        color: '#666',
    },
    typeButtonTextDark: {
        color: '#999',
    },
    typeButtonTextActive: {
        color: '#fff',
    },
    helpText: {
        fontSize: 14,
        color: '#666',
        marginTop: 8,
        fontStyle: 'italic',
    },
    helpTextDark: {
        color: '#999',
    },
    radiusButtons: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        gap: 8,
    },
    radiusButton: {
        backgroundColor: '#f0f0f0',
        paddingHorizontal: 16,
        paddingVertical: 10,
        borderRadius: 8,
        borderWidth: 2,
        borderColor: 'transparent',
    },
    radiusButtonDark: {
        backgroundColor: '#2a2a2a',
    },
    radiusButtonActive: {
        backgroundColor: '#007bff',
        borderColor: '#0056b3',
    },
    radiusButtonText: {
        fontSize: 14,
        fontWeight: '600',
        color: '#666',
    },
    radiusButtonTextDark: {
        color: '#999',
    },
    radiusButtonTextActive: {
        color: '#fff',
    },
    saveButton: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 8,
        backgroundColor: '#007bff',
        padding: 16,
        borderRadius: 10,
        marginTop: 8,
    },
    saveButtonDisabled: {
        backgroundColor: '#ccc',
    },
    saveButtonText: {
        color: '#fff',
        fontSize: 16,
        fontWeight: '600',
    },
    emptyText: {
        textAlign: 'center',
        color: '#999',
        fontSize: 14,
        padding: 20,
    },
    emptyTextDark: {
        color: '#666',
    },
});