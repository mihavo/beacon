# Beacon Backend Architecture

<div align="center">
<img src="assets/beacon_arch.png" alt="Architecture Diagram" width="1200px">
</div>

## Overview

Beacon is designed using a microservices architecture to ensure separation of concerns, scalability, and maintainability.
It consists of the following services:

- **API Gateway** : The entry point for all client requests, responsible for routing and load balancing.
- **Service Registry**: Maintains a list of available services and their instances for dynamic discovery.
- **Auth Service** : Manages user authentication.
- **User Service** : Handles user accounts.
- **Location Service** : Manages real-time location data. It is responsible for both receiving & streaming location updates to clients.
- **History Service**: Responsible for receiving and storing historical location data.
- **Map Service**: Provides locations of connected users on a map using bounding box queries.
- **Geofencing Service** : Manages geofences and triggers notifications when users enter or approach geofenced areas.
- **Notification Service** : Sends notifications via Firebase Cloud Messaging to subscribed users.

## Infrastructure

- **PostgreSQL with TimescaleDB** : Used by the History service to store historical location data efficiently and process analytics queries.
- **PostGIS**: An extension of PostgreSQL used by the map & history services for geospatial queries.
- **Neo4j** : Used by the User service to manage user relationships and connections.
- **Redis** : Used for caching frequently accessed data (primarily locations & timestamps).
- **Apache Kafka** : Used for event-driven communication between services.
- **WebSockets**: Used between the mobile client and the user service for realtime user search.
- **gRPC** : Used for communication between microservices to ensure low latency and high performance.
- **Firebase Cloud Messaging** : Used by the Notification service to subscribe mobile clients & send geofence push notifications.
- **Prometheus & Grafana** : Used for metrics collection and visualization.
