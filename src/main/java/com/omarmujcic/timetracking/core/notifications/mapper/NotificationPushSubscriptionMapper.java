package com.omarmujcic.timetracking.core.notifications.mapper;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.omarmujcic.timetracking.core.auth.entity.User;
import com.omarmujcic.timetracking.core.notifications.dto.PushSubscriptionRequestDTO;
import com.omarmujcic.timetracking.core.notifications.entity.NotificationPushSubscription;

@Mapper(componentModel = "spring", imports = UUID.class)
public interface NotificationPushSubscriptionMapper {

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "endpoint", expression = "java(request.getEndpoint().trim())")
    @Mapping(target = "p256dhKey", expression = "java(request.getKeys().getP256dh().trim())")
    @Mapping(target = "authKey", expression = "java(request.getKeys().getAuth().trim())")
    @Mapping(target = "userAgent", expression = "java(trimOptional(request.getUserAgent()))")
    @Mapping(target = "createdAt", source = "now")
    @Mapping(target = "updatedAt", source = "now")
    @Mapping(target = "lastFailedAt", ignore = true)
    NotificationPushSubscription toEntity(PushSubscriptionRequestDTO request, User user, OffsetDateTime now);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "p256dhKey", expression = "java(request.getKeys().getP256dh().trim())")
    @Mapping(target = "authKey", expression = "java(request.getKeys().getAuth().trim())")
    @Mapping(target = "userAgent", expression = "java(trimOptional(request.getUserAgent()))")
    @Mapping(target = "updatedAt", source = "now")
    @Mapping(target = "lastFailedAt", expression = "java(null)")
    void updateEntity(PushSubscriptionRequestDTO request, User user, OffsetDateTime now,
            @MappingTarget NotificationPushSubscription subscription);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "lastFailedAt", source = "failedAt")
    @Mapping(target = "updatedAt", source = "failedAt")
    void markFailed(OffsetDateTime failedAt, @MappingTarget NotificationPushSubscription subscription);

    default String trimOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
