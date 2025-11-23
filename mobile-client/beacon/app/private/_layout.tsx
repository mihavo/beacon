import {Icon, Label, NativeTabs} from "expo-router/unstable-native-tabs";
import React from "react";

export default function PrivateTabsLayout() {
    return (
        <NativeTabs minimizeBehavior={"onScrollUp"}>
            <NativeTabs.Trigger name="maps">
                <Label>Maps</Label>
                <Icon sf={"map"}></Icon>
            </NativeTabs.Trigger>

            <NativeTabs.Trigger name="geofence">
                <Label>Geofences</Label>
                <Icon sf={"network.badge.shield.half.filled"}></Icon>
            </NativeTabs.Trigger>


            <NativeTabs.Trigger name="connections">
                <Label>Connections</Label>
                <Icon sf={"person.2.circle"}></Icon>
            </NativeTabs.Trigger>

            <NativeTabs.Trigger name="history">
                <Label>History</Label>
                <Icon sf={"clock.arrow.trianglehead.counterclockwise.rotate.90"}></Icon>
            </NativeTabs.Trigger>

            <NativeTabs.Trigger name="account">
                <Label>Account</Label>
                <Icon sf={"person.crop.circle"}/>
            </NativeTabs.Trigger>
        </NativeTabs>
    );
}