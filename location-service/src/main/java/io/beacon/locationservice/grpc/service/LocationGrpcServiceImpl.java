package io.beacon.locationservice.grpc.service;

import io.beacon.locationservice.location.service.LocationService;
import io.grpc.stub.StreamObserver;
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
    String requesterUserId = request.getRequesterUserId();
    locationService.getLKLFromBoundingBox(request.getBbox());
  }
}
