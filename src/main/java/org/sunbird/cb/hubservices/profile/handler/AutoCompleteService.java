package org.sunbird.cb.hubservices.profile.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.sunbird.cb.hubservices.exception.BadRequestException;
import org.sunbird.cb.hubservices.util.ConnectionProperties;
import org.sunbird.cb.hubservices.util.Constants;

@Service
public class AutoCompleteService {

	private Logger logger = LoggerFactory.getLogger(AutoCompleteService.class);
	@Autowired
	private ConnectionProperties connectionProperties;

	@Autowired
	private RestHighLevelClient esClient;
	final String[] includeFields = {"profileDetails.employmentDetails.departmentName", ProfileUtils.Profile.FIRSTNAME, ProfileUtils.Profile.LASTNAME, ProfileUtils.Profile.ID, "profileDetails.professionalDetails.designation"};

	public List<Map<String, Object>> getUserSearchData(String searchTerm) throws IOException {
		if (StringUtils.isEmpty(searchTerm))
			throw new BadRequestException("Search term should not be empty!");
		List<Map<String, Object>> resultArray = new ArrayList<>();

		final BoolQueryBuilder query = QueryBuilders.boolQuery();
		query.should(QueryBuilders.matchPhrasePrefixQuery(ProfileUtils.Profile.EMAIL, searchTerm))
				.should(QueryBuilders.matchPhrasePrefixQuery(ProfileUtils.Profile.FIRSTNAME, searchTerm));
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().query(query);
		logger.info("Auto complete search fields", includeFields);
		sourceBuilder.fetchSource(includeFields, new String[] {});
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices(connectionProperties.getEsProfileIndex());
		searchRequest.types(connectionProperties.getEsProfileIndexType());
		searchRequest.source(sourceBuilder);
		logger.info("Auto complete searchRequest", searchRequest);
		SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
		logger.info("Auto complete es response", searchResponse);
		for (SearchHit hit : searchResponse.getHits()) {
			Map<String, Object> searObjectMap = hit.getSourceAsMap();

			// Extracting data from the Elasticsearch response
			String firstName = (String) searObjectMap.get(ProfileUtils.Profile.FIRSTNAME);
			logger.info("Auto complete response firstname", firstName);
			String lastName = (String) searObjectMap.get(ProfileUtils.Profile.LASTNAME);
			String id = (String) searObjectMap.get(ProfileUtils.Profile.ID);
			String departmentName = "";
			String designation = "";
			if (searObjectMap.get(ProfileUtils.Profile.PROFILE_DETAILS) != null){
				Map<String, Object> profileDetails = (Map<String, Object>) searObjectMap.getOrDefault(ProfileUtils.Profile.PROFILE_DETAILS,"");
				if (profileDetails.get(Constants.EMPLOYMENT_DETAILS) != null){
					Map<String, Object> employmentDetails = (Map<String, Object>) profileDetails.getOrDefault(Constants.EMPLOYMENT_DETAILS,"");
					departmentName = (String) employmentDetails.get(Constants.DEPARTMENT_NAME);
				}
				if (profileDetails.get(ProfileUtils.Profile.PROFESSIONAL_DETAILS) != null){
					List<Map<String, Object>> professionalDetailsList = (List<Map<String, Object>>) profileDetails.get(ProfileUtils.Profile.PROFESSIONAL_DETAILS);
					Map<String,Object> firstIndex = professionalDetailsList.get(0);
					designation = (String) firstIndex.get(Constants.DESIGNATION);
				}
			}
			Float rank = hit.getScore();
			Map<String, Object> result = new HashMap<>();
			result.put(ProfileUtils.Profile.FIRSTNAME, firstName);
			result.put(ProfileUtils.Profile.LASTNAME, lastName);
			result.put(Constants.WID, id);
			result.put(Constants.DEPARTMENT_NAME, departmentName);
			result.put(Constants.DESIGNATION, designation);
			result.put("rank", rank);

			resultArray.add(result);
		}
		logger.info("Auto complete result ",resultArray);
		return resultArray;
	}
}
