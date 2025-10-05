package io.locationservice.publish;

import io.locationservice.request.PublishLocationRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@RestController
@AllArgsConstructor
@Validated
public class PublishController {

    private final PublishService publishService;

    @PostMapping("/")
    public Mono<ResponseEntity<Void>> publish(@RequestBody @Valid Set<PublishLocationRequest> requests) {
        Flux<RecordId> publish = publishService.publish(requests);
        return publish.then(Mono.just(ResponseEntity.ok().build())); //TODO: evaluate async return of record ids
    }

}
