import React, {useState} from "react";
import {
    Alert,
    KeyboardAvoidingView,
    Platform,
    ScrollView,
    StyleSheet,
    Text,
    TextInput,
    TouchableOpacity,
    useColorScheme,
    View,
} from "react-native";
import {Controller, useForm} from "react-hook-form";
import {useRouter} from "expo-router";
import {Ionicons} from "@expo/vector-icons";
import {login} from "@/lib/api";
import {useAuth} from "@/app/context/AuthContext";

type FormData = {
    username: string;
    password: string;
};

const FloatingLabelInput = ({
    label,
    value,
    onChangeText,
    secureTextEntry = false,
    error,
    isDark,
}: {
    label: string;
    value: string;
    onChangeText: (text: string) => void;
    secureTextEntry?: boolean;
    error?: string;
    isDark: boolean;
}) => {
    const [isFocused, setIsFocused] = useState(false);
    const [showPassword, setShowPassword] = useState(false);

    return (
        <View style={styles.inputWrapper}>
            <View style={[
                styles.inputContainer,
                isDark && styles.inputContainerDark,
                isFocused && styles.inputContainerFocused,
                isFocused && isDark && styles.inputContainerFocusedDark,
                error && styles.inputContainerError,
            ]}>
                <TextInput
                    value={value}
                    onChangeText={onChangeText}
                    onFocus={() => setIsFocused(true)}
                    onBlur={() => setIsFocused(false)}
                    secureTextEntry={secureTextEntry && !showPassword}
                    style={[styles.input, isDark && styles.inputDark]}
                    placeholder={label}
                    placeholderTextColor={isDark ? "#666" : "#8e8e93"}
                    autoCapitalize="none"
                />
                {secureTextEntry && (
                    <TouchableOpacity
                        onPress={() => setShowPassword(!showPassword)}
                        style={styles.eyeIcon}
                    >
                        <Ionicons
                            name={showPassword ? "eye-off-outline" : "eye-outline"}
                            size={22}
                            color={isDark ? "#999" : "#8e8e93"}
                        />
                    </TouchableOpacity>
                )}
            </View>
            {error && (
                <Text style={styles.errorText}>{error}</Text>
            )}
        </View>
    );
};

export default function Login() {
    const {control, handleSubmit, formState: {errors}} = useForm<FormData>();
    const router = useRouter();
    const auth = useAuth();
    const colorScheme = useColorScheme();
    const isDark = colorScheme === 'dark';

    const onSubmit = async (data: FormData) => {
        auth.setIsLoading(true);

        try {
            const res = await login(data.username, data.password);

            if (res?.token) {
                await auth.login(res.token);
                router.replace("/private/maps");
            } else {
                console.log(res);
                Alert.alert("Login failed", res.error || "Invalid credentials");
                auth.setIsLoading(false);
            }
        } catch (err: any) {
            auth.setIsLoading(false);
            Alert.alert("Login failed", "Server Error.");
            console.error("Failed to login:", err);
        }
    };

    return (
        <KeyboardAvoidingView
            behavior={Platform.OS === "ios" ? "padding" : "height"}
            style={[styles.container, isDark && styles.containerDark]}
        >
            <ScrollView
                contentContainerStyle={styles.scrollContent}
                keyboardShouldPersistTaps="handled"
                showsVerticalScrollIndicator={false}
            >
                <View style={styles.logoContainer}>
                    <View style={[styles.logoCircle, isDark && styles.logoCircleDark]}>
                        <Ionicons
                            name="radio-outline"
                            size={64}
                            color="#007aff"
                        />
                    </View>
                    <View style={styles.appNameContainer}>
                        <Text style={[styles.appName, isDark && styles.appNameDark]}>Beacon</Text>
                        <View style={[styles.appNameUnderline,
                            isDark && styles.appNameUnderlineDark]}/>
                    </View>
                    <Text style={[styles.title, isDark && styles.titleDark]}>Welcome Back</Text>
                </View>

                <View style={styles.formContainer}>
                    <Controller
                        control={control}
                        name="username"
                        rules={{
                            required: "Username is required",
                            minLength: {
                                value: 3,
                                message: "Username must be at least 3 characters"
                            }
                        }}
                        render={({field: {onChange, value}}) => (
                            <FloatingLabelInput
                                label="Username"
                                value={value}
                                onChangeText={onChange}
                                error={errors.username?.message}
                                isDark={isDark}
                            />
                        )}
                    />

                    <Controller
                        control={control}
                        name="password"
                        rules={{
                            required: "Password is required",
                            minLength: {
                                value: 6,
                                message: "Password must be at least 6 characters"
                            }
                        }}
                        render={({field: {onChange, value}}) => (
                            <FloatingLabelInput
                                label="Password"
                                value={value}
                                onChangeText={onChange}
                                secureTextEntry
                                error={errors.password?.message}
                                isDark={isDark}
                            />
                        )}
                    />

                    <TouchableOpacity
                        style={[
                            styles.button,
                            auth.isLoading && styles.buttonDisabled
                        ]}
                        onPress={auth.isLoading ? undefined : handleSubmit(onSubmit)}
                        disabled={auth.isLoading}
                        activeOpacity={0.8}
                    >
                        {auth.isLoading ? (
                            <Text style={styles.buttonText}>Logging in...</Text>
                        ) : (
                            <Text style={styles.buttonText}>Login</Text>
                        )}
                    </TouchableOpacity>

                    <View style={styles.dividerContainer}>
                        <View style={[styles.divider, isDark && styles.dividerDark]}/>
                        <Text style={[styles.dividerText, isDark && styles.dividerTextDark]}>
                            OR
                        </Text>
                        <View style={[styles.divider, isDark && styles.dividerDark]}/>
                    </View>

                    <TouchableOpacity
                        onPress={() => router.push("/auth/register")}
                        style={[styles.secondaryButton, isDark && styles.secondaryButtonDark]}
                        activeOpacity={0.7}
                    >
                        <Text style={[styles.secondaryButtonText,
                            isDark && styles.secondaryButtonTextDark]}>
                            Create New Account
                        </Text>
                    </TouchableOpacity>
                </View>
            </ScrollView>
        </KeyboardAvoidingView>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: "#f8f9fa",
    },
    containerDark: {
        backgroundColor: "#000000",
    },
    scrollContent: {
        flexGrow: 1,
        justifyContent: "center",
        paddingHorizontal: 24,
        paddingVertical: 40,
    },
    logoContainer: {
        alignItems: "center",
        marginBottom: 48,
    },
    logoCircle: {
        width: 110,
        height: 110,
        borderRadius: 55,
        backgroundColor: "rgba(0, 122, 255, 0.08)",
        justifyContent: "center",
        alignItems: "center",
        marginBottom: 24,
        borderWidth: 2,
        borderColor: "rgba(0, 122, 255, 0.15)",
    },
    logoCircleDark: {
        backgroundColor: "rgba(10, 132, 255, 0.12)",
        borderColor: "rgba(10, 132, 255, 0.2)",
    },
    appNameContainer: {
        alignItems: "center",
        marginBottom: 16,
    },
    appName: {
        fontSize: 42,
        fontWeight: "900",
        color: "#007aff",
        letterSpacing: -1,
        marginBottom: 4,
    },
    appNameDark: {
        color: "#0a84ff",
    },
    appNameUnderline: {
        width: 60,
        height: 4,
        backgroundColor: "#007aff",
        borderRadius: 2,
        marginTop: 2,
    },
    appNameUnderlineDark: {
        backgroundColor: "#0a84ff",
    },
    title: {
        fontSize: 24,
        fontWeight: "600",
        color: "#1c1c1e",
        marginBottom: 6,
    },
    titleDark: {
        color: "#ffffff",
    },
    subtitle: {
        fontSize: 15,
        color: "#8e8e93",
        letterSpacing: 0.2,
    },
    subtitleDark: {
        color: "#999",
    },
    formContainer: {
        width: "100%",
    },
    inputWrapper: {
        marginBottom: 20,
    },
    inputContainer: {
        flexDirection: "row",
        alignItems: "center",
        height: 56,
        borderWidth: 1.5,
        borderColor: "#e5e5ea",
        borderRadius: 14,
        backgroundColor: "#fff",
        paddingHorizontal: 16,
        shadowColor: "#000",
        shadowOffset: {width: 0, height: 2},
        shadowOpacity: 0.05,
        shadowRadius: 4,
        ...Platform.select({android: {elevation: 2}}),
    },
    inputContainerDark: {
        backgroundColor: "#1a1a1a",
        borderColor: "#333",
    },
    inputContainerFocused: {
        borderColor: "#007aff",
        shadowOpacity: 0.1,
    },
    inputContainerFocusedDark: {
        borderColor: "#0a84ff",
        shadowOpacity: 0.15,
    },
    inputContainerError: {
        borderColor: "#ff3b30",
    },
    input: {
        flex: 1,
        fontSize: 16,
        color: "#1c1c1e",
    },
    inputDark: {
        color: "#ffffff",
    },
    eyeIcon: {
        padding: 4,
    },
    errorText: {
        color: "#ff3b30",
        fontSize: 13,
        marginTop: 6,
        marginLeft: 4,
    },
    button: {
        backgroundColor: "#007aff",
        height: 56,
        borderRadius: 14,
        justifyContent: "center",
        alignItems: "center",
        marginTop: 8,
        shadowColor: "#007aff",
        shadowOffset: {width: 0, height: 4},
        shadowOpacity: 0.3,
        shadowRadius: 8,
        ...Platform.select({android: {elevation: 4}}),
    },
    buttonDisabled: {
        opacity: 0.6,
    },
    buttonText: {
        color: "#fff",
        fontWeight: "600",
        fontSize: 17,
    },
    dividerContainer: {
        flexDirection: "row",
        alignItems: "center",
        marginVertical: 30,
    },
    divider: {
        flex: 1,
        height: 1,
        backgroundColor: "#e5e5ea",
    },
    dividerDark: {
        backgroundColor: "#333",
    },
    dividerText: {
        marginHorizontal: 16,
        color: "#8e8e93",
        fontSize: 14,
        fontWeight: "500",
    },
    dividerTextDark: {
        color: "#666",
    },
    secondaryButton: {
        height: 56,
        borderRadius: 14,
        justifyContent: "center",
        alignItems: "center",
        backgroundColor: "#f2f2f7",
        borderWidth: 1.5,
        borderColor: "#e5e5ea",
    },
    secondaryButtonDark: {
        backgroundColor: "#1a1a1a",
        borderColor: "#333",
    },
    secondaryButtonText: {
        color: "#007aff",
        fontWeight: "600",
        fontSize: 17,
    },
    secondaryButtonTextDark: {
        color: "#0a84ff",
    },
});