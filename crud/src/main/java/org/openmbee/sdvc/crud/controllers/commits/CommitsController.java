package org.openmbee.sdvc.crud.controllers.commits;

import java.util.Map;

import org.openmbee.sdvc.core.objects.CommitsRequest;
import org.openmbee.sdvc.core.objects.CommitsResponse;
import org.openmbee.sdvc.crud.controllers.BaseController;
import org.openmbee.sdvc.core.objects.BaseResponse;
import org.openmbee.sdvc.crud.exceptions.ForbiddenException;
import org.openmbee.sdvc.crud.services.CommitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}")
public class CommitsController extends BaseController {

    private CommitService commitService;

    @Autowired
    public CommitsController(CommitService commitService) {
        this.commitService = commitService;
    }

    @GetMapping(value = "/refs/{refId}/commits")
    public ResponseEntity<? extends BaseResponse> handleGet(
        @PathVariable String projectId,
        @PathVariable String refId,
        @RequestParam(required = false) Map<String, String> params,
        Authentication auth) {

        checkPerm(auth, projectId);
        CommitsResponse res = commitService.getRefCommits(projectId, refId, params);
        return ResponseEntity.ok(res);
    }

    @GetMapping(value = "/commits/{commitId}")
    public ResponseEntity<? extends BaseResponse> handleCommitGet(
        @PathVariable String projectId,
        @PathVariable String commitId,
        Authentication auth) {

        checkPerm(auth, projectId);
        CommitsResponse res = commitService.getCommit(projectId, commitId);
        return ResponseEntity.ok(res);
    }

    @GetMapping(value = "/refs/{refId}/elements/{elementId}/commits")
    public ResponseEntity<? extends BaseResponse> handleElementCommitsGet(
        @PathVariable String projectId,
        @PathVariable String refId,
        @PathVariable String elementId,
        @RequestParam(required = false) Map<String, String> params,
        Authentication auth) {

        checkPerm(auth, projectId);
        CommitsResponse res = commitService.getElementCommits(projectId, refId, elementId, params);
        return ResponseEntity.ok(res);
    }

    @PutMapping(value = "/commits")
    public ResponseEntity<? extends BaseResponse> handleBulkGet(
        @PathVariable String projectId,
        @RequestBody CommitsRequest req,
        Authentication auth) {

        checkPerm(auth, projectId);
        CommitsResponse res = commitService.getCommits(projectId, req);
        return ResponseEntity.ok(res);
    }

    private void checkPerm(Authentication auth, String projectId) {
        if (!permissionService.isProjectPublic(projectId)) {
            rejectAnonymous(auth);
            if (!permissionService.hasProjectPrivilege("PROJECT_READ_COMMITS", auth.getName(), projectId)) {
                throw new ForbiddenException(new CommitsResponse().addMessage("No permission to read commits"));
            }
        }
    }
}
