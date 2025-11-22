import {
    Alert,
    RefreshControl,
    ScrollView,
    StyleSheet,
    Text,
    TextInput,
    TouchableOpacity,
    useColorScheme,
    View,
} from 'react-native';
import React, {useCallback, useEffect, useRef, useState} from "react";
import {ProfileMenu} from "@/components/profile-menu";
import {Connection, SearchResponse} from "@/types/Connections";
import {
    acceptFriendRequest,
    BASE,
    connect,
    declineFriendRequest,
    getConnections,
    getFriends,
    removeFriend
} from "@/lib/api";
import {Ionicons} from "@expo/vector-icons";
import {getToken} from "@/app/context/AuthContext";

export default function Connections() {
    const colorScheme = useColorScheme();
    const isDark = colorScheme === 'dark';

    const [searchQuery, setSearchQuery] = useState('');
    const [pendingRequests, setPendingRequests] = useState<Connection[]>([]);
    const [friends, setFriends] = useState<Connection[]>([]);
    const [searchResults, setSearchResults] = useState<SearchResponse[]>([]);
    const [sentRequests, setSentRequests] = useState(new Set());
    const [refreshing, setRefreshing] = useState(false);
    const ws = useRef<WebSocket | null>(null);

    const fetchPendingConnections = useCallback(async () => {
        try {
            const response = await getConnections();
            const pending = response.connections.filter(conn => conn.status === 'PENDING');
            setPendingRequests(pending);
        } catch (error) {
            console.error('Error fetching connections:', error);
        }
    }, []);

    const fetchFriends = useCallback(async () => {
        try {
            const response = await getFriends();
            setFriends(response.connections);
        } catch (error) {
            console.error('Error fetching connections:', error);
        }
    }, []);

    useEffect(() => {
        fetchPendingConnections();
        fetchFriends();
    }, [fetchPendingConnections, fetchFriends]);

    useEffect(() => {
        (async () => {
            if (ws.current) return;
            const headers: { [headerName: string]: string } = {};
            headers["Authorization"] = `Bearer ${await getToken()}`;

            ws.current = new WebSocket(`${BASE}/users/ws/search`, null, headers);

            ws.current.onmessage = (message) => {
                setSearchResults(prevResponses => ({
                    ...prevResponses,
                    ...JSON.parse(message.data)
                }));
            };

            ws.current.onerror = (e) => {
                console.debug('WebSocket error:',);
            };

            ws.current.onclose = (e) => {
                console.log('WebSocket closed:', e.code, e.reason);
            };
        })();

        return () => {
            if (ws.current) {
                ws.current.close();
            }
        };
    }, []);

    useEffect(() => {
        if (searchQuery.length > 3 && ws.current && ws.current.readyState === WebSocket.OPEN) {
            ws.current.send(searchQuery);
        }
    }, [searchQuery]);

    const onRefresh = useCallback(async () => {
        setRefreshing(true);
        await fetchPendingConnections();
        setRefreshing(false);
    }, [fetchPendingConnections]);

    const handleSearch = async (text: string) => {
        if (text.trim().length > 0) {
            setSearchResults([]);
            setSearchQuery(text);
        }
    };

    const handleAcceptRequest = async (id: string) => {
        try {
            const response = await acceptFriendRequest({targetUserId: id});
            console.log(response);
            Alert.alert('Success', 'Friend request accepted');
            await fetchPendingConnections();
        } catch (error) {
            console.error('Error accepting request:', error);
        }
    };

    const handleDeclineRequest = async (id: string) => {
        try {
            const response = await declineFriendRequest({targetUserId: id});
            console.log(response);
            Alert.alert('Success', 'Friend request declined');
            await fetchPendingConnections();
        } catch (error) {
            console.error('Error declining request:', error);
        }
    };

    const handleSendRequest = async (id: string) => {
        try {
            const response = await connect(id);
            console.log(response);
            Alert.alert('Success', 'Friend request sent');
            setSentRequests(new Set([...sentRequests, id]));
            await fetchPendingConnections();
        } catch (error) {
            console.error('Error sending request:', error);
        }
    };

    const handleRemoveFriend = async (id: string) => {
        try {
            const response = await removeFriend(id);
            console.log(response);
            Alert.alert('Success', 'Connection removed');
            await fetchPendingConnections();
        } catch (error) {
            console.error('Error removing friend:', error);
        }
    };

    return (
        <View style={[styles.container, isDark && styles.containerDark]}>
            <View style={[styles.header, isDark && styles.headerDark]}>
                <Text style={[styles.title, isDark && styles.titleDark]}>Connections</Text>
                <ProfileMenu/>
            </View>
            <View style={[styles.searchContainer, isDark && styles.searchContainerDark]}>
                <TextInput
                    style={[styles.searchInput, isDark && styles.searchInputDark]}
                    placeholder="Search users..."
                    value={searchQuery}
                    onChangeText={handleSearch}
                    placeholderTextColor={isDark ? '#666' : '#999'}
                />
            </View>

            <ScrollView
                style={styles.scrollView}
                showsVerticalScrollIndicator={false}
                refreshControl={
                    <RefreshControl
                        refreshing={refreshing}
                        onRefresh={onRefresh}
                        tintColor={isDark ? '#fff' : '#000'}
                    />
                }
            >

            {searchQuery.trim().length > 0 && (
                    <View style={styles.section}>
                        <Text style={[styles.sectionTitle, isDark && styles.sectionTitleDark]}>Search
                            Results</Text>
                        {searchResults.length > 0 ? (
                            searchResults.map(user => (
                                <View key={user.userId}
                                      style={[styles.userCard, isDark && styles.userCardDark]}>
                                    <Ionicons
                                        name={'person-circle'}
                                        size={42}
                                        color={isDark ? '#4a4a4a' : '#e0e0e0'}
                                    />
                                    <View style={styles.userInfo}>
                                        <Text style={[styles.userName,
                                            isDark && styles.userNameDark]}>{user.name}</Text>
                                        <Text style={[styles.userUsername, isDark
                                        && styles.userUsernameDark]}>{user.username}</Text>
                                    </View>
                                    {user.connected ? (
                                        <Text style={styles.connectedBadge}>Connected</Text>
                                    ) : sentRequests.has(user.id) ? (
                                        <Text style={[styles.sentBadge,
                                            isDark && styles.sentBadgeDark]}>Sent</Text>
                                    ) : (
                                        <TouchableOpacity
                                            style={styles.addButton}
                                            onPress={() => handleSendRequest(user.id)}
                                        >
                                            <Text style={styles.addButtonText}>Add</Text>
                                        </TouchableOpacity>
                                    )}
                                </View>
                            ))
                        ) : (
                            <Text style={[styles.emptyText, isDark && styles.emptyTextDark]}>No
                                users found</Text>
                        )}
                    </View>
                )}

                {/* Pending Requests */}
                {pendingRequests.length > 0 && (
                    <View style={styles.section}>
                        <Text style={[styles.sectionTitle, isDark && styles.sectionTitleDark]}>
                            Pending Requests ({pendingRequests.length})
                        </Text>
                        {pendingRequests.map(request => (
                            <View key={request.userId}
                                  style={[styles.userCard, isDark && styles.userCardDark]}>
                                <Ionicons
                                    name={'person-circle'}
                                    size={42}
                                    color={isDark ? '#4a4a4a' : '#e0e0e0'}
                                />
                                <View style={styles.userInfo}>
                                    <Text style={[styles.userName,
                                        isDark && styles.userNameDark]}>{request.fullName}</Text>
                                    <Text style={[styles.userUsername, isDark
                                    && styles.userUsernameDark]}>{request.username}</Text>
                                </View>
                                <View style={styles.requestActions}>
                                    <TouchableOpacity
                                        style={styles.acceptButton}
                                        onPress={() => handleAcceptRequest(request.userId)}
                                    >
                                        <Text style={styles.acceptButtonText}>Accept</Text>
                                    </TouchableOpacity>
                                    <TouchableOpacity
                                        style={[styles.declineButton,
                                            isDark && styles.declineButtonDark]}
                                        onPress={() => handleDeclineRequest(request.userId)}
                                    >
                                        <Text style={[styles.declineButtonText,
                                            isDark && styles.declineButtonTextDark]}>Decline</Text>
                                    </TouchableOpacity>
                                </View>
                            </View>
                        ))}
                    </View>
                )}

                {/* Friends List */}
                <View style={styles.section}>
                    <Text style={[styles.sectionTitle, isDark && styles.sectionTitleDark]}>
                        Friends ({friends.length})
                    </Text>
                    {friends.length > 0 ? (
                        friends.map(friend => (
                            <View key={friend.userId}
                                  style={[styles.userCard, isDark && styles.userCardDark]}>
                                <View style={styles.avatarContainer}>
                                    <Ionicons
                                        name={'person-circle'}
                                        size={42}
                                        color={isDark ? '#4a4a4a' : '#e0e0e0'}
                                    />
                                </View>
                                <View style={styles.userInfo}>
                                    <Text style={[styles.userName,
                                        isDark && styles.userNameDark]}>{friend.fullName}</Text>
                                    <Text style={[styles.userUsername,
                                        isDark && styles.userUsernameDark]}>{friend.username}</Text>
                                </View>
                                <TouchableOpacity
                                    style={[styles.removeButton, isDark && styles.removeButtonDark]}
                                    onPress={() => handleRemoveFriend(friend.userId)}
                                >
                                    <Text style={styles.removeButtonText}>Remove</Text>
                                </TouchableOpacity>
                            </View>
                        ))
                    ) : (
                        <Text style={[styles.emptyText, isDark && styles.emptyTextDark]}>No friends
                            yet</Text>
                    )}
                </View>
            </ScrollView>
        </View>
    );
};

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
    searchContainer: {
        padding: 16,
        backgroundColor: '#fff',
        borderBottomWidth: 1,
        borderBottomColor: '#e0e0e0',
    },
    searchContainerDark: {
        backgroundColor: '#1a1a1a',
        borderBottomColor: '#333',
    },
    searchInput: {
        backgroundColor: '#f0f0f0',
        borderRadius: 10,
        padding: 12,
        fontSize: 16,
        color: '#000',
    },
    searchInputDark: {
        backgroundColor: '#2a2a2a',
        color: '#ffffff',
    },
    scrollView: {
        flex: 1,
    },
    section: {
        marginTop: 20,
        paddingHorizontal: 16,
        marginBottom: 20,
    },
    sectionTitle: {
        fontSize: 18,
        fontWeight: '600',
        color: '#333',
        marginBottom: 12,
    },
    sectionTitleDark: {
        color: '#ffffff',
    },
    userCard: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: '#fff',
        padding: 12,
        borderRadius: 12,
        marginBottom: 10,
        shadowColor: '#000',
        shadowOffset: {width: 0, height: 1},
        shadowOpacity: 0.05,
        shadowRadius: 3,
        elevation: 2,
    },
    userCardDark: {
        backgroundColor: '#1a1a1a',
        shadowColor: '#fff',
        shadowOpacity: 0.1,
    },
    avatarContainer: {
        position: 'relative',
    },
    userInfo: {
        flex: 1,
        marginLeft: 12,
    },
    userName: {
        fontSize: 16,
        fontWeight: '600',
        color: '#000',
        marginBottom: 2,
    },
    userNameDark: {
        color: '#ffffff',
    },
    userUsername: {
        fontSize: 14,
        color: '#666',
    },
    userUsernameDark: {
        color: '#999',
    },
    requestActions: {
        flexDirection: 'row',
        gap: 8,
    },
    acceptButton: {
        backgroundColor: '#007bff',
        paddingHorizontal: 16,
        paddingVertical: 8,
        borderRadius: 6,
    },
    acceptButtonText: {
        color: '#fff',
        fontWeight: '600',
        fontSize: 14,
    },
    declineButton: {
        backgroundColor: '#f0f0f0',
        paddingHorizontal: 16,
        paddingVertical: 8,
        borderRadius: 6,
    },
    declineButtonDark: {
        backgroundColor: '#2a2a2a',
    },
    declineButtonText: {
        color: '#666',
        fontWeight: '600',
        fontSize: 14,
    },
    declineButtonTextDark: {
        color: '#999',
    },
    addButton: {
        backgroundColor: '#007bff',
        paddingHorizontal: 20,
        paddingVertical: 8,
        borderRadius: 6,
    },
    addButtonText: {
        color: '#fff',
        fontWeight: '600',
        fontSize: 14,
    },
    removeButton: {
        backgroundColor: '#f0f0f0',
        paddingHorizontal: 16,
        paddingVertical: 8,
        borderRadius: 6,
    },
    removeButtonDark: {
        backgroundColor: '#2a2a2a',
    },
    removeButtonText: {
        color: '#dc3545',
        fontWeight: '600',
        fontSize: 14,
    },
    connectedBadge: {
        color: '#4caf50',
        fontWeight: '600',
        fontSize: 14,
    },
    sentBadge: {
        color: '#666',
        fontWeight: '600',
        fontSize: 14,
    },
    sentBadgeDark: {
        color: '#999',
    },
    emptyText: {
        textAlign: 'center',
        color: '#999',
        fontSize: 14,
        paddingVertical: 20,
    },
    emptyTextDark: {
        color: '#666',
    },
});