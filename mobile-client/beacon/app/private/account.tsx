import {
    ActivityIndicator,
    Alert,
    ScrollView,
    StyleSheet,
    Text,
    TouchableOpacity,
    useColorScheme,
    View,
} from 'react-native';
import React, {useCallback, useEffect, useState} from "react";
import {Ionicons} from "@expo/vector-icons";
import {deleteUserAccount, getUserAccount} from "@/lib/api";
import {UserAccount} from "@/types/Account";
import {router} from "expo-router";
import {useAuth} from "@/app/context/AuthContext";

export default function Account() {
    const colorScheme = useColorScheme();
    const auth = useAuth();

    const isDark = colorScheme === 'dark';

    const [account, setAccount] = useState<UserAccount | null>(null);
    const [loading, setLoading] = useState(true);

    const fetchAccountInfo = useCallback(async () => {
        try {
            setLoading(true);
            const account = await getUserAccount();
            setAccount(account);
        } catch (error) {
            console.error('Error fetching account info:', error);
            Alert.alert('Error', 'Failed to load account information');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchAccountInfo();
    }, [fetchAccountInfo]);

    const formatDate = (dateString: string) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
        });
    };

    const getInitials = (name: string) => {
        return name
            .split(' ')
            .map(n => n[0])
            .join('')
            .toUpperCase()
            .slice(0, 2);
    };

    const handleEditProfile = () => {
        Alert.alert('Edit Profile',
            'This feature will allow you to edit your profile information.');
    };

    const handleChangePassword = () => {
        Alert.alert('Change Password', 'This feature will allow you to change your password.');
    };

    const handlePrivacySettings = () => {
        Alert.alert('Privacy Settings',
            'This feature will allow you to manage your privacy settings.');
    };

    const handleDeleteAccount = () => {
        Alert.alert(
            'Delete Account',
            'Are you sure you want to delete your account? This action cannot be undone.',
            [
                {text: 'Cancel', style: 'cancel'},
                {
                    text: 'Delete',
                    style: 'destructive',
                    onPress: async () => {
                        await deleteUserAccount();
                        Alert.alert('Account deleted');
                        await auth.logout();
                        router.push('/auth/login');
                    },
                },
            ]
        );
    };

    if (loading) {
        return (
            <View style={[styles.container, isDark && styles.containerDark]}>
                <View style={[styles.header, isDark && styles.headerDark]}>
                    <TouchableOpacity>
                        <Ionicons name="chevron-back" size={28} color={isDark ? '#fff' : '#000'}/>
                    </TouchableOpacity>
                    <Text style={[styles.title, isDark && styles.titleDark]}>Account</Text>
                    <View style={styles.placeholder}/>
                </View>
                <View style={styles.loadingContainer}>
                    <ActivityIndicator size="large" color="#007bff"/>
                    <Text style={[styles.loadingText, isDark && styles.loadingTextDark]}>
                        Loading account...
                    </Text>
                </View>
            </View>
        );
    }

    if (!account) {
        return (
            <View style={[styles.container, isDark && styles.containerDark]}>
                <View style={[styles.header, isDark && styles.headerDark]}>
                    <TouchableOpacity>
                        <Ionicons name="chevron-back" size={28} color={isDark ? '#fff' : '#000'}/>
                    </TouchableOpacity>
                    <Text style={[styles.title, isDark && styles.titleDark]}>Account</Text>
                    <View style={styles.placeholder}/>
                </View>
                <View style={styles.errorContainer}>
                    <Ionicons name="alert-circle-outline" size={64}
                              color={isDark ? '#666' : '#ccc'}/>
                    <Text style={[styles.errorText, isDark && styles.errorTextDark]}>
                        Failed to load account
                    </Text>
                </View>
            </View>
        );
    }

    return (
        <View style={[styles.container, isDark && styles.containerDark]}>
            <View style={[styles.header, isDark && styles.headerDark]}>
                <TouchableOpacity
                    style={styles.backButton}
                >
                    <Ionicons name="chevron-back" size={28} color={isDark ? '#fff' : '#000'}/>
                </TouchableOpacity>
                <Text style={[styles.title, isDark && styles.titleDark]}>Account</Text>
                <View style={styles.placeholder}/>
            </View>

            <ScrollView style={styles.content} showsVerticalScrollIndicator={false}>
                {/* Profile Section */}
                <View style={[styles.profileSection, isDark && styles.profileSectionDark]}>
                    <View style={[styles.avatarContainer, isDark && styles.avatarContainerDark]}>
                        <Text style={[styles.avatarText, isDark && styles.avatarTextDark]}>
                            {getInitials(account.fullName)}
                        </Text>
                    </View>
                    <Text style={[styles.fullName, isDark && styles.fullNameDark]}>
                        {account.fullName}
                    </Text>
                    <Text style={[styles.username, isDark && styles.usernameDark]}>
                        @{account.username}
                    </Text>
                    <TouchableOpacity
                        style={[styles.editButton, isDark && styles.editButtonDark]}
                        onPress={handleEditProfile}
                    >
                        <Ionicons name="create-outline" size={18} color="#007bff"/>
                        <Text style={styles.editButtonText}>Edit Profile</Text>
                    </TouchableOpacity>
                </View>

                {/* Account Details */}
                <View style={styles.section}>
                    <Text style={[styles.sectionTitle, isDark && styles.sectionTitleDark]}>
                        Account Details
                    </Text>


                    <View style={[styles.infoItem, styles.infoItemBorder,
                        isDark && styles.infoItemBorderDark]}>
                        <View style={styles.infoLeft}>
                            <Ionicons name="key-outline" size={20}
                                      color={isDark ? '#999' : '#666'}/>
                            <Text style={[styles.infoLabel, isDark && styles.infoLabelDark]}>
                                User ID
                            </Text>
                        </View>
                        <Text style={[styles.infoValue, styles.infoValueMono,
                            isDark && styles.infoValueDark]} numberOfLines={1}>
                            {account.id}
                        </Text>
                    </View>
                </View>

                {/* Settings & Actions */}
                <View style={styles.section}>
                    <Text style={[styles.sectionTitle, isDark && styles.sectionTitleDark]}>
                        Settings & Security
                    </Text>

                    <TouchableOpacity
                        style={[styles.actionCard, isDark && styles.actionCardDark]}
                        onPress={handleChangePassword}
                    >
                        <View style={styles.actionLeft}>
                            <View style={[styles.actionIcon, isDark && styles.actionIconDark]}>
                                <Ionicons name="lock-closed-outline" size={22} color="#007bff"/>
                            </View>
                            <View style={styles.actionInfo}>
                                <Text
                                    style={[styles.actionTitle, isDark && styles.actionTitleDark]}>
                                    Change Password
                                </Text>
                                <Text style={[styles.actionSubtitle,
                                    isDark && styles.actionSubtitleDark]}>
                                    Update your password
                                </Text>
                            </View>
                        </View>
                        <Ionicons name="chevron-forward" size={20}
                                  color={isDark ? '#666' : '#999'}/>
                    </TouchableOpacity>

                    <TouchableOpacity
                        style={[styles.actionCard, isDark && styles.actionCardDark]}
                        onPress={handlePrivacySettings}
                    >
                        <View style={styles.actionLeft}>
                            <View style={[styles.actionIcon, isDark && styles.actionIconDark]}>
                                <Ionicons name="shield-outline" size={22} color="#28a745"/>
                            </View>
                            <View style={styles.actionInfo}>
                                <Text
                                    style={[styles.actionTitle, isDark && styles.actionTitleDark]}>
                                    Privacy Settings
                                </Text>
                                <Text style={[styles.actionSubtitle,
                                    isDark && styles.actionSubtitleDark]}>
                                    Control your privacy
                                </Text>
                            </View>
                        </View>
                        <Ionicons name="chevron-forward" size={20}
                                  color={isDark ? '#666' : '#999'}/>
                    </TouchableOpacity>
                </View>

                {/* Danger Zone */}
                <View style={styles.section}>
                    <Text style={[styles.sectionTitle, styles.sectionTitleDanger,
                        isDark && styles.sectionTitleDark]}>
                        Danger Zone
                    </Text>

                    <TouchableOpacity
                        style={[styles.actionCard, styles.actionCardDanger,
                            isDark && styles.actionCardDangerDark]}
                        onPress={handleDeleteAccount}
                    >
                        <View style={styles.actionLeft}>
                            <View style={[styles.actionIcon, styles.actionIconDanger]}>
                                <Ionicons name="trash-outline" size={22} color="#dc3545"/>
                            </View>
                            <View style={styles.actionInfo}>
                                <Text style={[styles.actionTitle, styles.actionTitleDanger]}>
                                    Delete Account
                                </Text>
                                <Text style={[styles.actionSubtitle,
                                    isDark && styles.actionSubtitleDark]}>
                                    Permanently delete your account
                                </Text>
                            </View>
                        </View>
                        <Ionicons name="chevron-forward" size={20} color="#dc3545"/>
                    </TouchableOpacity>
                </View>

                <View style={styles.footer}>
                    <Text style={[styles.footerText, isDark && styles.footerTextDark]}>
                        Beacon Location Tracker v1.0.0
                    </Text>
                </View>
            </ScrollView>
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
    backButton: {
        padding: 4,
        marginLeft: -4,
    },
    title: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#000',
        flex: 1,
        textAlign: 'center',
    },
    titleDark: {
        color: '#ffffff',
    },
    placeholder: {
        width: 36,
    },
    content: {
        flex: 1,
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
    errorContainer: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
        paddingVertical: 40,
    },
    errorText: {
        fontSize: 18,
        fontWeight: '600',
        color: '#999',
        marginTop: 16,
    },
    errorTextDark: {
        color: '#666',
    },
    profileSection: {
        alignItems: 'center',
        padding: 32,
        backgroundColor: '#fff',
        borderBottomWidth: 1,
        borderBottomColor: '#e0e0e0',
    },
    profileSectionDark: {
        backgroundColor: '#1a1a1a',
        borderBottomColor: '#333',
    },
    avatarContainer: {
        width: 100,
        height: 100,
        borderRadius: 50,
        backgroundColor: '#007bff',
        alignItems: 'center',
        justifyContent: 'center',
        marginBottom: 16,
        position: 'relative',
    },
    avatarContainerDark: {
        backgroundColor: '#0056b3',
    },
    avatarText: {
        fontSize: 36,
        fontWeight: 'bold',
        color: '#fff',
    },
    avatarTextDark: {
        color: '#fff',
    },
    verifiedBadge: {
        position: 'absolute',
        bottom: 0,
        right: 0,
        backgroundColor: '#fff',
        borderRadius: 12,
        padding: 2,
    },
    fullName: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#000',
        marginBottom: 4,
    },
    fullNameDark: {
        color: '#ffffff',
    },
    username: {
        fontSize: 16,
        color: '#666',
        marginBottom: 16,
    },
    usernameDark: {
        color: '#999',
    },
    editButton: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 6,
        backgroundColor: '#e3f2fd',
        paddingHorizontal: 20,
        paddingVertical: 10,
        borderRadius: 20,
    },
    editButtonDark: {
        backgroundColor: '#1a3a52',
    },
    editButtonText: {
        fontSize: 14,
        fontWeight: '600',
        color: '#007bff',
    },
    section: {
        padding: 16,
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
    sectionTitleDanger: {
        color: '#dc3545',
    },
    infoCard: {
        backgroundColor: '#fff',
        borderRadius: 12,
        padding: 16,
        shadowColor: '#000',
        shadowOffset: {width: 0, height: 1},
        shadowOpacity: 0.05,
        shadowRadius: 3,
        elevation: 2,
    },
    infoCardDark: {
        backgroundColor: '#1a1a1a',
        shadowColor: '#fff',
        shadowOpacity: 0.1,
    },
    infoItem: {
        paddingVertical: 12,
    },
    infoItemBorder: {
        borderTopWidth: 1,
        borderTopColor: '#f0f0f0',
    },
    infoItemBorderDark: {
        borderTopColor: '#2a2a2a',
    },
    infoLeft: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 12,
        marginBottom: 6,
    },
    infoLabel: {
        fontSize: 14,
        fontWeight: '500',
        color: '#666',
    },
    infoLabelDark: {
        color: '#999',
    },
    infoValue: {
        fontSize: 16,
        color: '#000',
        marginLeft: 32,
    },
    infoValueDark: {
        color: '#ffffff',
    },
    infoValueMono: {
        fontFamily: 'monospace',
        fontSize: 12,
    },
    actionCard: {
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
    actionCardDark: {
        backgroundColor: '#1a1a1a',
        shadowColor: '#fff',
        shadowOpacity: 0.1,
    },
    actionCardDanger: {
        borderWidth: 1,
        borderColor: '#ffe0e0',
    },
    actionCardDangerDark: {
        borderColor: '#3a1a1a',
    },
    actionLeft: {
        flexDirection: 'row',
        alignItems: 'center',
        flex: 1,
    },
    actionIcon: {
        width: 44,
        height: 44,
        borderRadius: 22,
        backgroundColor: '#f0f0f0',
        alignItems: 'center',
        justifyContent: 'center',
        marginRight: 12,
    },
    actionIconDark: {
        backgroundColor: '#2a2a2a',
    },
    actionIconDanger: {
        backgroundColor: '#ffe0e0',
    },
    actionInfo: {
        flex: 1,
    },
    actionTitle: {
        fontSize: 16,
        fontWeight: '600',
        color: '#000',
        marginBottom: 2,
    },
    actionTitleDark: {
        color: '#ffffff',
    },
    actionTitleDanger: {
        color: '#dc3545',
    },
    actionSubtitle: {
        fontSize: 13,
        color: '#666',
    },
    actionSubtitleDark: {
        color: '#999',
    },
    footer: {
        alignItems: 'center',
        paddingVertical: 32,
    },
    footerText: {
        fontSize: 12,
        color: '#999',
    },
    footerTextDark: {
        color: '#666',
    },
});