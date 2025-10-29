package io.beacon.locationservice.grpc.service;

import io.beacon.locationservice.location.service.LocationService;
import io.beacon.locationservice.mappers.LocationMapper;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.UUID;
import locationservice.LocationServiceGrpc;
import locationservice.LocationServiceOuterClass;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.stereotype.Component;

@GrpcService
@Component
@RequiredArgsConstructor
public class LocationGrpcServiceImpl extends LocationServiceGrpc.LocationServiceImplBase {

  private final LocationService locationService;

  @Override public void getLatestLocations(LocationServiceOuterClass.LatestLocationsRequest request,
      StreamObserver<LocationServiceOuterClass.LatestLocationsResponse> responseObserver) {
    UUID requesterId;
    try {
      requesterId = UUID.fromString(request.getRequesterUserId());
    } catch (IllegalArgumentException e) {
      responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid userId format").asRuntimeException());
      return;
    }

    locationService.fetchLKL(request.getBbox(), requesterId).collectList().map(locations -> {
      List<LocationServiceOuterClass.UserLocation> grpcLocations =
          locations.stream().map(LocationMapper::toGrpcUserLocation).toList();
      return LocationServiceOuterClass.LatestLocationsResponse.newBuilder().addAllLocations(grpcLocations).build();
    }).subscribe(responseObserver::onNext, error -> {
      if (error instanceof StatusRuntimeException) {
        responseObserver.onError(error);
      } else {
        responseObserver.onError(Status.INTERNAL.withDescription(error.getMessage()).asRuntimeException());
      }
    }, responseObserver::onCompleted);
  }
}
