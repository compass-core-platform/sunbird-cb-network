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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.sunbird.cb.hubservices.exception.BadRequestException;
import org.sunbird.cb.hubservices.util.ConnectionProperties;

@Service
public class AutoCompleteService {

	@Autowired
	private ConnectionProperties connectionProperties;

	@Autowired
	private RestHighLevelClient esClient;
	final String[] includeFields = {"profileDetails.employmentDetails.departmentName", "firstName", "lastName", "id", "profileDetails.professionalDetails.designation"};

	public List<Map<String, Object>> getUserSearchData(String searchTerm) throws IOException {
		if (StringUtils.isEmpty(searchTerm))
			throw new BadRequestException("Search term should not be empty!");
		List<Map<String, Object>> resultArray = new ArrayList<>();
//		Map<String, Object> result;
		String depName;
		final BoolQueryBuilder query = QueryBuilders.boolQuery();
		query.should(QueryBuilders.matchPhrasePrefixQuery("email", searchTerm))
				.should(QueryBuilders.matchPhrasePrefixQuery("firstName", searchTerm));
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().query(query);
		sourceBuilder.fetchSource(includeFields, new String[] {});
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices(connectionProperties.getEsProfileIndex());
		searchRequest.types(connectionProperties.getEsProfileIndexType());
		searchRequest.source(sourceBuilder);
		SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
		for (SearchHit hit : searchResponse.getHits()) {
			Map<String, Object> searObjectMap = hit.getSourceAsMap();

			// Extracting data from the Elasticsearch response
			String firstName = (String) searObjectMap.get("firstName");
			String lastName = (String) searObjectMap.get("lastName");
			String id = (String) searObjectMap.get("id");
			String departmentName = "";
			String designation = "";
			if (searObjectMap.get("profileDetails") != null){
				Map<String, Object> profileDetails = (Map<String, Object>) searObjectMap.getOrDefault("profileDetails","");
				if (profileDetails.get("employmentDetails") != null){
					Map<String, Object> employmentDetails = (Map<String, Object>) profileDetails.getOrDefault("employmentDetails","");
					departmentName = (String) employmentDetails.get("departmentName");
				}
				if (profileDetails.get("professionalDetails") != null){
					List<Map<String, Object>> professionalDetailsList = (List<Map<String, Object>>) profileDetails.get("professionalDetails");
					Map<String,Object> firstIndex = professionalDetailsList.get(0);
					designation = (String) firstIndex.get("designation");
				}
			}
			Float rank = hit.getScore();
			Map<String, Object> result = new HashMap<>();
			result.put("first_name", firstName);
			result.put("last_name", lastName);
			result.put("wid", id);
			result.put("department_name", departmentName);
			result.put("designation", designation);
			result.put("rank", rank);

			resultArray.add(result);
		}
		return resultArray;
	}
}
