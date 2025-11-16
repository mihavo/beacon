import {
    Alert,
    ScrollView,
    StyleSheet,
    Text,
    TextInput,
    TouchableOpacity,
    useColorScheme,
    View,
} from 'react-native';
import React, {useEffect, useState} from "react";
import {ProfileMenu} from "@/components/profile-menu";
import {Connection} from "@/types/Connections";
import {
    acceptFriendRequest,
    connect,
    declineFriendRequest,
    getConnections,
    removeFriend
} from "@/lib/api";
import {Ionicons} from "@expo/vector-icons";

export default function Connections() {
    const isDark = useColorScheme() == 'dark';

    // Mock data
    const mockPendingRequests = [
        {
            id: '1',
            name: 'Sarah Johnson',
            username: '@sarahj',
            avatar: 'https://i.pravatar.cc/150?img=1'
        },
        {
            id: '2',
            name: 'Mike Chen',
            username: '@mikechen',
            avatar: 'https://i.pravatar.cc/150?img=2'
        },
        {
            id: '3',
            name: 'Emily Davis',
            username: '@emilyd',
            avatar: 'https://i.pravatar.cc/150?img=3'
        },
    ];

    const mockFriends = [
        {
            id: '4',
            name: 'Alex Thompson',
            username: '@alexthom',
            avatar: 'https://i.pravatar.cc/150?img=4',
            status: 'online'
        },
        {
            id: '5',
            name: 'Jessica Lee',
            username: '@jesslee',
            avatar: 'https://i.pravatar.cc/150?img=5',
            status: 'offline'
        },
        {
            id: '6',
            name: 'David Wilson',
            username: '@davidw',
            avatar: 'https://i.pravatar.cc/150?img=6',
            status: 'online'
        },
        {
            id: '7',
            name: 'Rachel Green',
            username: '@rachelg',
            avatar: 'https://i.pravatar.cc/150?img=7',
            status: 'offline'
        },
        {
            id: '8',
            name: 'Tom Anderson',
            username: '@tomand',
            avatar: 'https://i.pravatar.cc/150?img=8',
            status: 'online'
        },
    ];

    const mockSearchResults = [
        {
            id: '9',
            name: 'Chris Martin',
            username: '@chrism',
            avatar: 'https://i.pravatar.cc/150?img=9',
            connected: false
        },
        {
            id: '10',
            name: 'Linda Park',
            username: '@lindap',
            avatar: 'https://i.pravatar.cc/150?img=10',
            connected: false
        },
        {
            id: '11',
            name: 'James Brown',
            username: '@jamesb',
            avatar: 'https://i.pravatar.cc/150?img=11',
            connected: true
        },
    ];

    const [searchQuery, setSearchQuery] = useState('');
    const [pendingRequests, setPendingRequests] = useState<Connection[]>([]);
    const [friends, setFriends] = useState<Connection[]>([]);
    const [searchResults, setSearchResults] = useState([]);
    const [sentRequests, setSentRequests] = useState(new Set());

    const handleSearch = (text) => {
        setSearchQuery(text);
        if (text.trim().length > 0) {
            setSearchResults(mockSearchResults.filter(user =>
                user.name.toLowerCase().includes(text.toLowerCase()) ||
                user.username.toLowerCase().includes(text.toLowerCase())
            ));
        } else {
            setSearchResults([]);
        }
    };

    useEffect(() => {
        (async () => {
            const response = await getConnections();
            const friends = response.connections.filter(conn => conn.status === 'FRIENDS_WITH');
            const pending = response.connections.filter(conn => conn.status === 'PENDING');
            setFriends(friends);
            setPendingRequests(pending);
        })()
    }, []);

    const handleAcceptRequest = async (id: string) => {
        const response = await acceptFriendRequest({targetUserId: id})
        console.log(response);
        Alert.alert('Accepted Friend Request', JSON.stringify(response));
    }

    const handleDeclineRequest = async (id: string) => {
        const response = await declineFriendRequest({targetUserId: id})
        console.log(response);
        Alert.alert('Declined Friend Request', JSON.stringify(response));
    };

    const handleSendRequest = async (id: string) => {
        const response = await connect(id);
        console.log(response);
        Alert.alert('Friend Request Sent', JSON.stringify(response.message));
    };

    const handleRemoveFriend = async (id: string) => {
        const response = await removeFriend(id);
        console.log(response);
        Alert.alert('Removed connection with friend');
    };

    return (
        <View style={[styles.container, isDark && styles.containerDark]}>
            <View style={[styles.header, isDark && styles.headerDark]}>
                <Text style={[styles.title, isDark && styles.titleDark]}>Connections</Text>
                <ProfileMenu/>
            </View>
            <View style={styles.searchContainer}>
                <TextInput
                    style={styles.searchInput}
                    placeholder="Search users..."
                    value={searchQuery}
                    onChangeText={handleSearch}
                    placeholderTextColor="#999"
                />
            </View>

            <ScrollView style={styles.scrollView} showsVerticalScrollIndicator={false}>
                {/* Search Results */}
                {searchQuery.trim().length > 0 && (
                    <View style={styles.section}>
                        <Text style={styles.sectionTitle}>Search Results</Text>
                        {searchResults.length > 0 ? (
                            searchResults.map(user => (
                                <View key={user.id} style={styles.userCard}>
                                    <View style={styles.userInfo}>
                                        <Text style={styles.userName}>{user.name}</Text>
                                        <Text style={styles.userUsername}>{user.username}</Text>
                                    </View>
                                    {user.connected ? (
                                        <Text style={styles.connectedBadge}>Connected</Text>
                                    ) : sentRequests.has(user.id) ? (
                                        <Text style={styles.sentBadge}>Sent</Text>
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
                            <Text style={styles.emptyText}>No users found</Text>
                        )}
                    </View>
                )}

                {/* Pending Requests */}
                {pendingRequests.length > 0 && (
                    <View style={styles.section}>
                        <Text style={styles.sectionTitle}>
                            Pending Requests ({pendingRequests.length})
                        </Text>
                        {pendingRequests.map(request => (
                            <View key={request.userId} style={styles.userCard}>
                                <Ionicons name={'person.circle'} style={styles.avatar}/>
                                <View style={styles.userInfo}>
                                    <Text style={styles.userName}>{request.fullName}</Text>
                                    <Text style={styles.userUsername}>{request.username}</Text>
                                </View>
                                <View style={styles.requestActions}>
                                    <TouchableOpacity
                                        style={styles.acceptButton}
                                        onPress={() => handleAcceptRequest(request.userId)}
                                    >
                                        <Text style={styles.acceptButtonText}>Accept</Text>
                                    </TouchableOpacity>
                                    <TouchableOpacity
                                        style={styles.declineButton}
                                        onPress={() => handleDeclineRequest(request.userId)}
                                    >
                                        <Text style={styles.declineButtonText}>Decline</Text>
                                    </TouchableOpacity>
                                </View>
                            </View>
                        ))}
                    </View>
                )}

                {/* Friends List */}
                <View style={styles.section}>
                    <Text style={styles.sectionTitle}>
                        Friends ({friends.length})
                    </Text>
                    {friends.map(friend => (
                        <View key={friend.userId} style={styles.userCard}>
                            <View style={styles.avatarContainer}>
                                <Ionicons name={'person.circle'} style={styles.avatar}/>
                            </View>
                            <View style={styles.userInfo}>
                                <Text style={styles.userName}>{friend.fullName}</Text>
                                <Text style={styles.userUsername}>{friend.username}</Text>
                            </View>
                            <TouchableOpacity
                                style={styles.removeButton}
                                onPress={() => handleRemoveFriend(friend.userId)}
                            >
                                <Text style={styles.removeButtonText}>Remove</Text>
                            </TouchableOpacity>
                        </View>
                    ))}
                </View>
            </ScrollView>
        </View>
    );
};

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
    searchContainer: {
        padding: 16,
        backgroundColor: '#fff',
        borderBottomWidth: 1,
        borderBottomColor: '#e0e0e0',
    },
    searchInput: {
        backgroundColor: '#f0f0f0',
        borderRadius: 10,
        padding: 12,
        fontSize: 16,
        color: '#000',
    },
    scrollView: {
        flex: 1,
    },
    section: {
        marginTop: 20,
        paddingHorizontal: 16,
    },
    sectionTitle: {
        fontSize: 18,
        fontWeight: '600',
        color: '#333',
        marginBottom: 12,
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
    avatarContainer: {
        position: 'relative',
    },
    avatar: {
        width: 50,
        height: 50,
        borderRadius: 25,
        backgroundColor: '#e0e0e0',
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
    userUsername: {
        fontSize: 14,
        color: '#666',
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
    declineButtonText: {
        color: '#666',
        fontWeight: '600',
        fontSize: 14,
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
    emptyText: {
        textAlign: 'center',
        color: '#999',
        fontSize: 14,
        paddingVertical: 20,
    },
});