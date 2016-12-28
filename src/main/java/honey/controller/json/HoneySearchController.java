package honey.controller.json;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import honey.dao.HoneySearcherDao;
import honey.service.HoneySearchService;
import honey.vo.HoneySearchKeyword;
import honey.vo.JsonResult;

@Controller
@RequestMapping({ "/resultOfSearch/", "/mainpage/", "/writepage/", "/adminpage/", "/membership/" })
public class HoneySearchController {
	@Autowired HoneySearchService searchService;
	@Autowired HoneySearcherDao searcherDao;
	String searchKeyword;
	@RequestMapping("searchInfo")
	public Object searchInfo(String searchValue, HttpServletResponse response)
			throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

		try {
			if (searchValue.equals("") || searchValue != null) {
				searchKeyword = searchValue;
			}
			return JsonResult.success();
		} catch (Exception e) {
			e.printStackTrace();
			return JsonResult.error(e.getMessage());
		}
	}

	@RequestMapping("searcher")
	public Object searchResult(
			@RequestParam(defaultValue = "4") int memberLength, 
			@RequestParam(defaultValue = "6") int boardLength,
			@RequestParam(defaultValue = "20") int fileLength) throws Exception {

		List<HoneySearchKeyword> searchBoardResult = searchService.searchServiceBoardResult(searchKeyword, boardLength);
		List<HoneySearchKeyword> searchMemberResult = searchService.searchServiceMemberResult(searchKeyword, memberLength);

		for(int i =0; i < searchMemberResult.size();i++) {
			String[] userPhoto = (searchMemberResult.get(i).getUserProfilePath()).split("\\.");
			if(userPhoto.length == 2) {
				searchMemberResult.get(i).setUserProfilePath("/TeamProject/upload/" + userPhoto[0] + "." + userPhoto[1]);
			} else {
				searchMemberResult.get(i).setUserProfilePath("http://graph.facebook.com/" + userPhoto[0] + "/picture");
			}

		}


		List<HoneySearchKeyword> searchFileResult = searchService.searchServiceFileResult(searchKeyword, fileLength);

		String extension;
		String[] extensionArr;
		for (int i = 0; i < searchFileResult.size(); i++) {
			extension = searchFileResult.get(i).getOriFileName();
			extensionArr = extension.split("\\.");

			if(extensionArr[extensionArr.length-1].equals("txt")) {
				searchFileResult.get(i).setFileThumbnail("txt.png");
			} else if (extensionArr[extensionArr.length-1].equals("avi")) {
				searchFileResult.get(i).setFileThumbnail("avi.png");
			} else if (extensionArr[extensionArr.length-1].equals("doc") || extensionArr[extensionArr.length-1].equals("docx")) {
				searchFileResult.get(i).setFileThumbnail("doc.png");
			} else if (extensionArr[extensionArr.length-1].equals("mp4")) {
				searchFileResult.get(i).setFileThumbnail("mp4.png");
			} else if (extensionArr[extensionArr.length-1].equals("mpeg")) {
				searchFileResult.get(i).setFileThumbnail("mpeg.png");
			} else if (extensionArr[extensionArr.length-1].equals("pdf")) {
				searchFileResult.get(i).setFileThumbnail("pdf.png");
			} else if (extensionArr[extensionArr.length-1].equals("ppt") || extensionArr[extensionArr.length-1].equals("pptx")) {
				searchFileResult.get(i).setFileThumbnail("ppt.png");
			} else if (extensionArr[extensionArr.length-1].equals("wmv")) {
				searchFileResult.get(i).setFileThumbnail("wmv.png");
			} else if (extensionArr[extensionArr.length-1].equals("xls") || extensionArr[extensionArr.length-1].equals("xlsx")) {
				searchFileResult.get(i).setFileThumbnail("xls.png");
			} else if (extensionArr[extensionArr.length-1].equals("zip") || extensionArr[extensionArr.length-1].equals("7z") || extensionArr[extensionArr.length-1].equals("egg") || extensionArr[extensionArr.length-1].equals("rar") || extensionArr[extensionArr.length-1].equals("alz")) {
				searchFileResult.get(i).setFileThumbnail("zip.png");
			} else if (extensionArr[extensionArr.length-1].equals("jpg") || extensionArr[extensionArr.length-1].equals("jpeg") || extensionArr[extensionArr.length-1].equals("png") || extensionArr[extensionArr.length-1].equals("svg") || extensionArr[extensionArr.length-1].equals("bmp") 
					|| extensionArr[extensionArr.length-1].equals("gif")) {
				searchFileResult.get(i).setFileThumbnail(searchFileResult.get(i).getFilename());
			} else {
				searchFileResult.get(i).setFileThumbnail("java.png");
			}

			double mb = Math.round(((searchFileResult.get(i).getFileSize() / (double)1048576) * 100d));
			double temp = mb / 100d;
			searchFileResult.get(i).setFileSize(temp);
		}



		List<HoneySearchKeyword> searchBoardResultListLength = searchService.boardResultTotalPage(searchKeyword);
		List<HoneySearchKeyword> searchMemberResultListLength = searchService.memberResultTotalPage(searchKeyword);
		List<HoneySearchKeyword> searchFileResultListLength = searchService.FileResultTotalPage(searchKeyword);

		try {
			HashMap<String, Object> searchData = new HashMap<>();
			searchData.put("searchMemberResult", searchMemberResult);
			searchData.put("memberLength", memberLength);
			searchData.put("memberSearchLength", searchMemberResultListLength.size());

			searchData.put("searchBoardResult", searchBoardResult);
			searchData.put("boardLength", boardLength);
			searchData.put("boardSearchLength", searchBoardResultListLength.size());

			searchData.put("searchFileResult",searchFileResult);
			searchData.put("fileLength", fileLength);
			searchData.put("allsearchFileResultListLength", searchFileResultListLength.size());

			searchData.put("searchValue", searchKeyword); //검색어 

			searchKeyword = "";

			return JsonResult.success(searchData);
		} catch (Exception e) {
			e.printStackTrace();
			searchKeyword = "";
			return JsonResult.fail(e.getMessage());
		}
	}

	@RequestMapping("autoSearcher")
	public Object autoSearcher(){
		try {
			List<HoneySearchKeyword> list = searchService.autoSearchValue();
			List<String> values = new ArrayList<>();
			for (int i = 0; i < list.size(); i++) {
				values.add(list.get(i).getTitle());
			}
			HashMap<String, Object> autoSearchData = new HashMap<>();
			autoSearchData.put("searchValues", values);
			return JsonResult.success(autoSearchData);
		} catch (Exception e) {
			e.printStackTrace();
			return JsonResult.fail(e.getMessage());
		}

	}

}
