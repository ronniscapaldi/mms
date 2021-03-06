package org.openmbee.sdvc.rdb.repositories;

import org.openmbee.sdvc.data.domains.global.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface MetadataRepository extends JpaRepository<Metadata, Long> {

    Collection<Metadata> findByProject_ProjectId(String projectId);
    Collection<Metadata> findByProject_ProjectIdAndKey(String projectId, String key);

}
