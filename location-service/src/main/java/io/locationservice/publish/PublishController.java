package io.locationservice.publish;

import io.locationservice.request.PublishLocationRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class PublishController {

    private final PublishService publishService;

    @PostMapping("/")
    public Mono<ResponseEntity<Void>> publish(@Valid @RequestBody PublishLocationRequest request) {
        publishService.publish(request);
        return Mono.just(ResponseEntity.ok().build());
    }

}
