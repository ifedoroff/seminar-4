package ru.tinkoff.edu.synchronize;

import java.util.*;

public class SimpleSubscriptionService implements SubscriptionService {
    private final Map<UUID, List<UUID>> userSubscriptions = new HashMap<>();
    private final Map<UUID, List<UUID>> subscriptionUsers = new HashMap<>();
    private final List<Subscription> subscriptionList = new ArrayList<>();

    @Override
    public void createSubscription(UUID subscriptionId, long fee) {
        synchronized (subscriptionList) {
            subscriptionList.add(new Subscription(subscriptionId, fee));

            synchronized (subscriptionUsers) {
                subscriptionUsers.put(subscriptionId, new ArrayList<>());
            }
        }
    }
    @Override
    public void subscribeUser(UUID subscriptionId, UUID userId) {
        synchronized (subscriptionUsers) {
            Optional.ofNullable(subscriptionUsers.get(subscriptionId))
                    .ifPresent(it -> it.add(userId));
        }
    }
    @Override
    public void deleteSubscription(UUID subscriptionId) {
        synchronized (subscriptionList) {
            subscriptionList.removeIf(it -> it.uuid.equals(subscriptionId));

            for (UUID userId : Optional.ofNullable(subscriptionUsers.get(subscriptionId)).orElseGet(Collections::emptyList)) {
                synchronized (userSubscriptions) {
                    Optional.ofNullable(userSubscriptions.get(userId))
                            .ifPresent(it -> it.remove(subscriptionId));
                }
            }

            subscriptionUsers.remove(subscriptionId);
        }
    }
    @Override
    public long calculateTotalFee(UUID userId) {
        synchronized (userSubscriptions) {
            synchronized (subscriptionList) {
                List<UUID> subscriptionIds = Optional.ofNullable(userSubscriptions.get(userId)).orElseGet(Collections::emptyList);
                return subscriptionList.stream().filter(sub -> subscriptionIds.contains(sub.uuid))
                        .mapToLong(it -> it.fee)
                        .sum();
            }
        }
    }

    record Subscription(UUID uuid, long fee) {
    }
}
