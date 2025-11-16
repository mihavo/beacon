import {Modal, StyleSheet, Text, View} from "react-native";
import {Ionicons} from "@expo/vector-icons";

export function SuccessOverlay({visible}: { visible: boolean }) {
    return (
        <Modal transparent visible={visible} animationType="fade">
            <View style={styles.overlay}>
                <View style={styles.box}>
                    <Ionicons name="checkmark-circle" size={70} color="#4CAF50"/>
                    <Text style={styles.text}>Account Created!</Text>
                </View>
            </View>
        </Modal>
    );
}

const styles = StyleSheet.create({
    overlay: {
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
        backgroundColor: "rgba(0,0,0,0.4)",
    },
    box: {
        backgroundColor: "white",
        padding: 25,
        borderRadius: 16,
        alignItems: "center",
    },
    text: {
        marginTop: 10,
        fontSize: 18,
        fontWeight: "600",
    },
});
