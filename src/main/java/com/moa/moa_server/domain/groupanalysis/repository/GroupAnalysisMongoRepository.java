package com.moa.moa_server.domain.groupanalysis.repository;

import com.moa.moa_server.domain.groupanalysis.mongo.GroupAnalysisContent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupAnalysisMongoRepository extends MongoRepository<GroupAnalysisContent, Long> {}
