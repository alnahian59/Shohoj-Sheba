package bd.shohojsheba.api;

import bd.shohojsheba.account.AccountPrincipal;
import bd.shohojsheba.catalog.CatalogService;
import bd.shohojsheba.dto.CreateRequestInput;
import bd.shohojsheba.dto.CreatedRequest;
import bd.shohojsheba.dto.HelplineView;
import bd.shohojsheba.dto.LookupRequestInput;
import bd.shohojsheba.dto.RequestView;
import bd.shohojsheba.dto.ServiceGuide;
import bd.shohojsheba.dto.ServiceSuggestion;
import bd.shohojsheba.dto.ServiceSummary;
import bd.shohojsheba.dto.SuggestionInput;
import bd.shohojsheba.dto.UpdateRequestInput;
import bd.shohojsheba.dto.UpdateOwnedRequestInput;
import bd.shohojsheba.intent.IntentResolver;
import bd.shohojsheba.request.AssistanceRequestService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final CatalogService catalog;
    private final IntentResolver intentResolver;
    private final AssistanceRequestService requests;

    public ApiController(CatalogService catalog, IntentResolver intentResolver,
            AssistanceRequestService requests) {
        this.catalog = catalog;
        this.intentResolver = intentResolver;
        this.requests = requests;
    }

    @GetMapping("/services")
    public List<ServiceSummary> services() {
        return catalog.listEnabled();
    }

    @GetMapping("/services/{code}/guide")
    public ServiceGuide guide(@PathVariable String code) {
        return catalog.getEnabled(code);
    }

    @PostMapping("/suggestions")
    public ResponseEntity<List<ServiceSuggestion>> suggestions(@Valid @RequestBody SuggestionInput input) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(intentResolver.suggest(input.text()));
    }

    @PostMapping("/requests")
    public ResponseEntity<CreatedRequest> create(@Valid @RequestBody CreateRequestInput input,
            @AuthenticationPrincipal AccountPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CacheControl.noStore())
                .body(requests.create(input.serviceCode(), input.question(), principal.id()));
    }

    @PostMapping("/requests/lookup")
    public ResponseEntity<RequestView> lookup(@Valid @RequestBody LookupRequestInput input,
            @AuthenticationPrincipal AccountPrincipal principal) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(requests.lookup(input.reference(), input.trackingCode(), principal.id()));
    }

    @PostMapping("/requests/update")
    public ResponseEntity<RequestView> update(@Valid @RequestBody UpdateRequestInput input,
            @AuthenticationPrincipal AccountPrincipal principal) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(requests.update(input.reference(), input.trackingCode(), input.question(),
                        input.expectedVersion(), principal.id()));
    }

    @PostMapping("/requests/claim")
    public ResponseEntity<RequestView> claim(@Valid @RequestBody LookupRequestInput input,
            @AuthenticationPrincipal AccountPrincipal principal) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(requests.claimLegacy(input.reference(), input.trackingCode(), principal.id()));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<RequestView>> ownRequests(@AuthenticationPrincipal AccountPrincipal principal) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(requests.listOwned(principal.id()));
    }

    @GetMapping("/requests/{reference}")
    public ResponseEntity<RequestView> ownRequest(@PathVariable String reference,
            @AuthenticationPrincipal AccountPrincipal principal) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(requests.getOwned(reference, principal.id()));
    }

    @PostMapping("/requests/{reference}/update")
    public ResponseEntity<RequestView> updateOwnRequest(@PathVariable String reference,
            @Valid @RequestBody UpdateOwnedRequestInput input,
            @AuthenticationPrincipal AccountPrincipal principal) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(requests.updateOwned(reference, input.question(), input.expectedVersion(),
                        principal.id()));
    }

    @GetMapping("/helplines")
    public List<HelplineView> helplines() {
        return catalog.listHelplines();
    }
}
