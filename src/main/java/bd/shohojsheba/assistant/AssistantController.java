package bd.shohojsheba.assistant;

import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {
    private final AssistantService assistant;

    public AssistantController(AssistantService assistant) {
        this.assistant = assistant;
    }

    @PostMapping
    public ResponseEntity<AssistantReply> reply(@Valid @RequestBody AssistantInput input) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(assistant.reply(input.text()));
    }
}
