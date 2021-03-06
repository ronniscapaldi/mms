package org.openmbee.sdvc.crud.services;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openmbee.sdvc.core.config.Constants;
import org.openmbee.sdvc.core.config.ContextHolder;
import org.openmbee.sdvc.core.config.Formats;
import org.openmbee.sdvc.core.dao.BranchDAO;
import org.openmbee.sdvc.core.dao.BranchIndexDAO;
import org.openmbee.sdvc.core.dao.OrgDAO;
import org.openmbee.sdvc.core.dao.ProjectDAO;
import org.openmbee.sdvc.core.objects.EventObject;
import org.openmbee.sdvc.core.services.EventService;
import org.openmbee.sdvc.core.services.ProjectService;
import org.openmbee.sdvc.core.exceptions.InternalErrorException;
import org.openmbee.sdvc.data.domains.global.Organization;
import org.openmbee.sdvc.data.domains.global.Project;
import org.openmbee.sdvc.core.exceptions.BadRequestException;
import org.openmbee.sdvc.core.dao.ProjectIndex;
import org.openmbee.sdvc.data.domains.scoped.Branch;
import org.openmbee.sdvc.json.ProjectJson;
import org.openmbee.sdvc.core.objects.ProjectsResponse;
import org.openmbee.sdvc.json.RefJson;
import org.openmbee.sdvc.json.RefType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("defaultProjectService")
public class DefaultProjectService implements ProjectService {

    protected final Logger logger = LogManager.getLogger(getClass());
    protected ProjectDAO projectRepository;
    protected OrgDAO orgRepository;
    protected ProjectIndex projectIndex;
    protected BranchDAO branchRepository;
    protected BranchIndexDAO branchIndex;
    protected Optional<EventService> eventPublisher;

    @Autowired
    public void setProjectRepository(ProjectDAO projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Autowired
    public void setOrganizationRepository(OrgDAO orgRepository) {
        this.orgRepository = orgRepository;
    }

    @Autowired
    public void setProjectIndex(ProjectIndex projectIndex) {
        this.projectIndex = projectIndex;
    }

    @Autowired
    public void setBranchRepository(BranchDAO branchRepository) {
        this.branchRepository = branchRepository;
    }

    @Autowired
    public void setBranchIndex(BranchIndexDAO branchIndex) {
        this.branchIndex = branchIndex;
    }

    @Autowired
    public void setEventPublisher(Optional<EventService> eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public ProjectJson create(ProjectJson project) {
        if (project.getOrgId() == null || project.getOrgId().isEmpty()) {
            throw new BadRequestException(new ProjectsResponse().addMessage("Organization ID not provided"));
        }

        Optional<Organization> org = orgRepository.findByOrganizationId(project.getOrgId());
        if (!org.isPresent() || org.get().getOrganizationId().isEmpty()) {
            throw new BadRequestException(new ProjectsResponse().addMessage("Organization not found"));
        }

        Project proj = new Project();
        proj.setProjectId(project.getId());
        proj.setProjectName(project.getName());
        proj.setOrganization(org.get());
        proj.setProjectType(project.getProjectType());

        String uuid = UUID.randomUUID().toString();
        proj.setDocId(uuid);
        project.setDocId(uuid);
        project.setCreated(Formats.FORMATTER.format(Instant.now()));
        project.setType("Project");

        try {
            projectRepository.save(proj);
            projectIndex.create(proj.getProjectId(), project.getProjectType());
            projectIndex.update(project);

            //Index master branch
            Optional<Branch> masterBranch = branchRepository.findByBranchId(Constants.MASTER_BRANCH);
            if (masterBranch.isPresent()) {
                Branch master = masterBranch.get();
                RefJson branchJson = new RefJson();
                String docId = UUID.randomUUID().toString();
                branchJson.setId(Constants.MASTER_BRANCH);
                branchJson.setName(Constants.MASTER_BRANCH);
                branchJson.setParentRefId(null);
                branchJson.setDocId(docId);
                branchJson.setRefType(RefType.Branch);
                branchJson.setCreated(project.getCreated());
                branchJson.setProjectId(project.getId());
                branchJson.setCreator(project.getCreator());
                branchJson.setDeleted(false);

                master.setDocId(docId);
                master.setParentCommit(0L);

                branchRepository.save(master);
                branchIndex.index(branchJson);
            }

            eventPublisher.ifPresent((pub) -> pub.publish(
                EventObject.create(project.getId(), "master", "project_created", project)));
            return project;
        } catch (Exception e) {
            logger.error("Couldn't create project: {}", project.getProjectId());
            logger.error(e);
        }
        throw new InternalErrorException("Could not create project");
    }

    public ProjectJson update(ProjectJson project) {
        Optional<Project> projOption = projectRepository.findByProjectId(project.getProjectId());
        if (projOption.isPresent()) {
            ContextHolder.setContext(project.getProjectId());
            Project proj = projOption.get();
            if (project.getName() != null && !project.getName().isEmpty()) {
                proj.setProjectName(project.getName());
            }
            if (project.getOrgId() != null && !project.getOrgId().isEmpty()) {
                Optional<Organization> org = orgRepository.findByOrganizationId(project.getOrgId());
                if (org.isPresent() && !org.get().getOrganizationId().isEmpty()) {
                    proj.setOrganization(org.get());
                    //TODO check permissions and fix inherited permissions
                } else {
                    throw new BadRequestException("Invalid organization");
                }
            }
            project.setDocId(proj.getDocId());
            projectRepository.save(proj);
            return projectIndex.update(project);
        }
        throw new InternalErrorException("Could not update project");
    }

    public ProjectsResponse read(String projectId) {
        return null;
    }

    public boolean exists(String projectId) {
        Optional<Project> project = this.projectRepository.findByProjectId(projectId);
        return project.isPresent() && project.get().getProjectId().equals(projectId);
    }
}
