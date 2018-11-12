package org.openmbee.sdvc.crud.domains;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "nodes")
public class Node {

    @Id
    @GeneratedValue(
        strategy = GenerationType.IDENTITY
    )
    @Column(name = "id", updatable = false, nullable = false)
    Long id;

    private String sysmlId;
    private String elasticId;
    private String lastCommit;
    private String initialCommit;
    private boolean deleted;

    @Column(columnDefinition = "smallint")
    private NodeType nodeType;

    public Node() {
    }

    public Node(long id, String sysmlId, String elasticId, String lastCommit, String initialCommit,
        boolean deleted) {
        setId(id);
        setSysmlId(sysmlId);
        setElasticId(elasticId);
        setLastCommit(lastCommit);
        setInitialCommit(initialCommit);
        setDeleted(deleted);
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSysmlId() {
        return sysmlId;
    }

    public void setSysmlId(String sysmlId) {
        this.sysmlId = sysmlId;
    }

    public String getElasticId() {
        return elasticId;
    }

    public void setElasticId(String elasticId) {
        this.elasticId = elasticId;
    }

    public String getLastCommit() {
        return lastCommit;
    }

    public void setLastCommit(String lastCommit) {
        this.lastCommit = lastCommit;
    }

    public String getInitialCommit() {
        return initialCommit;
    }

    public void setInitialCommit(String initialCommit) {
        this.initialCommit = initialCommit;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Enumerated(EnumType.ORDINAL)
    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

}
