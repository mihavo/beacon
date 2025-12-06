<div align="center">
  <a href="https://github.com/mihavo/beacon">
    <img src="assets/beacon_logo.png" alt="Beacon logo" width="120" height="120"/>
  </a>

# Beacon

</div>

<div align="center" >

### Real-Time Location Sharing & Geofencing Platform
  
  <a href="https://github.com/mihavo/beacon/issues/new?template=bug_report.md">Report bug</a>
  |
  <a href="https://github.com/mihavo/beacon/issues/new?labels=feature&template=feature_request.md">Request feature</a>
</div>

<div align="center" >
<img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=fff" alt="Spring Boot">
<img src="https://img.shields.io/badge/Spring%20Cloud-6DB33F?logo=spring&logoColor=fff"
alt="Spring Boot">
<img src="https://img.shields.io/badge/Redis-%23DD0031.svg?logo=redis&logoColor=white" alt="Redis">
<img src="https://img.shields.io/badge/Apache%20Kafka-%23000000?logo=apachekafka&logoColor=fff"
alt="Apache Kafka">
<img src="https://img.shields.io/badge/neo4j-%23008CC1.svg?logo=neo4j&logoColor=white" alt="Neo4j">
<br/>
<img src="https://img.shields.io/badge/Postgres-%23316192.svg?logo=postgresql&logoColor=white" alt="Postgres">
<img src="https://img.shields.io/badge/TimescaleDB-%23F5AF19.svg?logo=timescale&logoColor=white"
alt="TimescaleDB">
<img src="https://img.shields.io/badge/Grafana-F46800.svg?logo=grafana&logoColor=white" alt="Grafana">
<img src="https://img.shields.io/badge/Prometheus-E6522C.svg?logo=prometheus&logoColor=white" alt="Prometheus">
<img src="https://img.shields.io/badge/Expo-000020?logo=expo&logoColor=fff" alt="Expo">
<img src="https://img.shields.io/badge/Firebase-039BE5?logo=Firebase&logoColor=white" alt="Firebase">
</div>

<br/>
<div align="center">
<p>
  <i>Beacon is a platform for real-time location sharing and geofencing, built using a microservices architecture with scalability in mind.</i>
  <br/>  
  <i>It is primarily intended for use between closed groups of users with mobile devices.</i>
  <br/>  
  <i>Partially inspired by Apple's Find My & Google's Find Hub.</i>
</p>
</div>

## Features

- [Real-time location sharing](https://github.com/mihavo/beacon/wiki/Location-Processing) between
  users
- [Live map streaming](https://github.com/mihavo/beacon/wiki/Map-Streaming)
- [Geofencing notifications](https://github.com/mihavo/beacon/wiki/Geofencing)
- Scalable microservices architecture
- Mobile client with iOS (and partial Android) support.
- Metrics collection via Prometheus & Grafana
- Log collection via Loki
- Event-driven messaging with Kafka

## Architecture

To achieve scalability and modularity, Beacon is built using a microservices architecture. Visit the [Architecture Docs](https://github.com/mihavo/beacon/wiki/Architecture) for more details.

## Mobile Client

Check the [Mobile Client Docs](https://github.com/mihavo/beacon/wiki/Mobile-Client) for instructions on how to set up and run the mobile client.

## Quick Start

### Backend Services

To quickly start all backend services using Docker Compose, use docker-compose in combination with profiles:

```bash
docker-compose --profile services --profile deps up -d
```

This will start all backend services along with their dependencies (databases, caches & the message broker).

To enable orchestration and log collection, add the `orchestration` profile:

```bash
docker-compose --profile services --profile deps --profile orchestration up -d
```

This will start a Prometheus instance for metrics collection, a Grafana instance for metrics visualization, and Loki for log collection.

## Contributing

Contributions are welcome! Please read the [Contributing Guide](CONTRIBUTING.md) for details on how
to get started.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
