@Service
public class DownloadBO {
	private static final Log LOG = LogFactory.getLog(DownloadBO.class);
	
	@Value("${movieDownloadContentExistApi}")
	private String movieDownloadContentExistApi;
	
	@Autowired
	private RemoteDataLoader remoteDataLoader;
	
	/**
	 * 다운로드 컨텐츠 존재 유무
	 * @param movieCode 영화코드
	 * @return
	 */
	@Cacheable(value = "downloadCache", key = "'com.naver.movie.end.movie.bo.DownloadBO.getDownloadContentExistYn-'.concat(#movieCode)")
	public boolean getDownloadContentExistYn(int movieCode) {
		String downloadContentExistApiUrl = movieDownloadContentExistApi + "&movieCode=" + movieCode;
		
		String response = "";
		boolean existYn = false;
		
		try {
			response = remoteDataLoader.loadDataViaGET(downloadContentExistApiUrl, new StringResponseHandler(), 3000, 3000);
			JSONObject jsonObject = JSONObject.fromObject(response);
			JSONObject result = jsonObject.getJSONObject("result");
			existYn = result.getBoolean("existYn");
		} catch (Exception e) {
			LOG.error(e.getMessage(), new DownloadApiException());
		}
		
		return existYn;
	}
}
