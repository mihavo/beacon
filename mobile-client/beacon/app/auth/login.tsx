import React, {useRef, useState} from "react";
import {
    Alert,
    Animated,
    Platform,
    StyleSheet,
    Text,
    TextInput,
    TouchableOpacity,
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
}: {
    label: string;
    value: string;
    onChangeText: (text: string) => void;
    secureTextEntry?: boolean;
}) => {
    const [isFocused, setIsFocused] = useState(false);
    const animatedIsFocused = useRef(new Animated.Value(value ? 1 : 0)).current;
    React.useEffect(() => {
        Animated.timing(animatedIsFocused, {
            toValue: isFocused || value ? 1 : 0,
            duration: 200,
            useNativeDriver: false,
        }).start();
    }, [isFocused, value]);


    return (
        <View style={styles.inputContainer}>
            <TextInput
                value={value}
                onChangeText={onChangeText}
                onFocus={() => setIsFocused(true)}
                onBlur={() => setIsFocused(false)}
                secureTextEntry={secureTextEntry}
                style={styles.input}
                placeholder={label}
                placeholderTextColor="#8e8e93"
            />
        </View>
    );
};

export default function Login() {
    const {control, handleSubmit, formState: {errors}} = useForm<FormData>();
    const router = useRouter();
    const auth = useAuth();


    const onSubmit = async (data: FormData) => {
        auth.setIsLoading(true);

        try {
            console.log("Logging in...");
            const res = await login(data.username, data.password);

            if (res?.token) {
                await auth.login(res.token);
                router.replace("/private/maps");
            } else {
                console.log(res)
                Alert.alert("Login failed", res.error);
                auth.setIsLoading(false)
            }
        } catch (err: any) {
            auth.setIsLoading(false);
            Alert.alert("Login failed", "Server Error.");
            console.error("Failed to login:", err);
        }
    };

    return (
        <View style={styles.container}>
            <Ionicons
                name="radio-outline"
                size={120}
                color="#007aff"
                style={{ alignSelf: "center" }}
            />
            <Text style={styles.title}>Welcome Back</Text>

            <Controller
                control={control}
                name="username"
                rules={{
                    required: "Username is required",
                }}
                render={({ field: { onChange, value } }) => (
                    <>
                        <FloatingLabelInput label="Username" value={value} onChangeText={onChange}/>
                        {errors.username && (
                            <Text style={{color: "red"}}>
                                {errors.username.message}
                            </Text>
                        )}
                    </>
                )}
            />

            <Controller
                control={control}
                name="password"
                rules={{
                    required: "Password is required",
                }}
                render={({ field: { onChange, value } }) => (
                    <>
                    <FloatingLabelInput
                        label="Password"
                        value={value}
                        onChangeText={onChange}
                        secureTextEntry
                    />
                        {errors.password && (
                            <Text style={{color: "red"}}>
                                {errors.password.message}
                            </Text>
                        )}
                    </>
                )}
            />

            <TouchableOpacity
                style={[
                    styles.button,
                    auth.isLoading && {opacity: 0.5}  // visual disabled state
                ]}
                onPress={auth.isLoading ? undefined : handleSubmit(onSubmit)}
                disabled={auth.isLoading}
            >
                <Text style={styles.buttonText}>Login</Text>
            </TouchableOpacity>

            <TouchableOpacity onPress={() => router.push("/auth/register")}>
                <Text style={styles.link}>Create Account</Text>
            </TouchableOpacity>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: "center",
        paddingHorizontal: 30,
        backgroundColor: "#f2f2f7",
    },
    title: {
        fontSize: 28,
        fontWeight: "600",
        marginBottom: 40,
        textAlign: "center",
        color: "#1c1c1e",
    },
    inputContainer: {
        marginBottom: 20,
        position: "relative",
    },
    input: {
        height: 50,
        borderWidth: 1,
        borderColor: "#d1d1d6",
        borderRadius: 12,
        paddingHorizontal: 15,
        fontSize: 16,
        color: "#1c1c1e",
        backgroundColor: "#fff",
        shadowColor: "#000",
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.05,
        shadowRadius: 1,
        ...Platform.select({ android: { elevation: 1 } }),
    },
    button: {
        backgroundColor: "#007aff", // iOS system blue
        height: 50,
        borderRadius: 12,
        justifyContent: "center",
        alignItems: "center",
        marginTop: 10,
    },
    buttonText: {
        color: "#fff",
        fontWeight: "600",
        fontSize: 16,
    },
    link: {
        marginTop: 20,
        color: "#007aff",
        fontSize: 16,
        textAlign: "center",
    },
});
