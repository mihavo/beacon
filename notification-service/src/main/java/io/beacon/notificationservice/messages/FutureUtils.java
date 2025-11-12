package io.beacon.notificationservice.messages;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.CompletableFuture;
import lombok.experimental.UtilityClass;
import reactor.core.publisher.Mono;

@UtilityClass

public class FutureUtils {
  public static <T> CompletableFuture<T> toCompletableFuture(ApiFuture<T> apiFuture) {
    final CompletableFuture<T> cf = new CompletableFuture<>();
    ApiFutures.addCallback(apiFuture,
        new ApiFutureCallback<T>() {
          @Override
          public void onFailure(Throwable t) {
            cf.completeExceptionally(t);
          }

          @Override
          public void onSuccess(T result) {
            cf.complete(result);
          }
        },
        MoreExecutors.directExecutor());
    return cf;
  }

  public static <T> Mono<T> toMono(ApiFuture<T> apiFuture) {
    return Mono.create(sink -> ApiFutures.addCallback(apiFuture,
        new ApiFutureCallback<T>() {
          @Override
          public void onFailure(Throwable t) {
            sink.error(t);
          }

          @Override
          public void onSuccess(T result) {
            sink.success(result);
          }
        },
        MoreExecutors.directExecutor()));
  }
}
