package io.beacon.notificationservice.subscriptions.mappers;

import io.beacon.notificationservice.subscriptions.dto.SubscriptionResponse;
import io.beacon.notificationservice.subscriptions.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SubscriptionMapper {

  SubscriptionResponse toSubscriptionResponse(Subscription subscription);
}
